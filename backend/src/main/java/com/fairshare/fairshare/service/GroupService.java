package com.fairshare.fairshare.service;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fairshare.fairshare.dto.GroupDTO;
import com.fairshare.fairshare.dto.MembershipDTO;
import com.fairshare.fairshare.entity.Group;
import com.fairshare.fairshare.entity.Membership;
import com.fairshare.fairshare.repository.GroupRepository;
import com.fairshare.fairshare.repository.MembershipRepository;
import com.fairshare.fairshare.repository.UserRepository;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository,
            MembershipRepository membershipRepository,
            UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a group and makes the creating user an admin
     * 
     * @param name
     * @param creatorUserId
     * @return GroupDTO
     */
    @Transactional
    public GroupDTO createGroup(String name, Long creatorUserId) {

        /* TODO: Enforce stricter group name conventions */

        /* create the group and save it */
        Group g = new Group();
        g.setGroupName(name);
        Group saved = groupRepository.save(g);

        /* defining the membership and making group creator an admin */
        Membership member = new Membership();
        var creator = userRepository.getReferenceById(creatorUserId);
        member.setUser(creator);
        member.setGroup(saved);
        member.setRole(Membership.Role.ADMIN);

        membershipRepository.save(member);

        return new GroupDTO(saved.getGroupId(), saved.getGroupName(), saved.getGroupCreatedAt());

    }

    /**
     * Adds a user to a group and defaults them to member role
     * 
     * @param groupId group to add user to
     * @param userId  user to be added
     * @param role    role of the user (defaults to member)
     * @return newly creating membership of user
     * @throws NotFoundException        if user or group not found
     * @throws IllegalArgumentException if user already exists within group
     */
    @Transactional
    public MembershipDTO addMember(Long groupId, Long userId, Membership.Role role)
            throws NotFoundException, IllegalArgumentException {

        // validate existence
        var group = groupRepository.findById(groupId)
                .orElseThrow(NotFoundException::new);
        var user = userRepository.findById(userId)
                .orElseThrow(NotFoundException::new);

        // prevent duplicates
        if (membershipRepository.findByUser_UserIdAndGroup_GroupId(userId, groupId).isPresent()) {
            throw new IllegalArgumentException("User is already a member");
        }

        // create & default role to MEMBER if null
        Membership newMember = new Membership();
        newMember.setUser(user);
        newMember.setGroup(group);
        newMember.setRole(role != null ? role : Membership.Role.MEMBER);

        Membership saved = membershipRepository.save(newMember);

        return new MembershipDTO(
                saved.getMembershipId(),
                saved.getUser().getUserId(),
                saved.getGroup().getGroupId(),
                saved.getRole());
    }

    /**
     * Removes a member from a group (only actionable by admins)
     * 
     * @param requesterUserId user making the remove request
     * @param targetUserId    user to be removed
     * @param groupId         group to remove target user from
     * @throws NotFoundException     if user/group not found
     * @throws AccessDeniedException if requesting user is a member and not an admin
     */
    @Transactional
    public void removeMember(Long requesterUserId, Long targetUserId, Long groupId)
            throws NotFoundException, AccessDeniedException {

        // verify group exists
        if (groupRepository.findById(groupId).isEmpty()) {
            throw new NotFoundException();
        }

        // ensure requester is admin
        var requesterMembership = membershipRepository.findByUser_UserIdAndGroup_GroupId(requesterUserId, groupId);
        if (requesterMembership.isEmpty() || requesterMembership.get().getRole() != Membership.Role.ADMIN) {
            throw new AccessDeniedException("Only admins can remove members");
        }

        // ensure target membership exists
        var targetMembership = membershipRepository.findByUser_UserIdAndGroup_GroupId(targetUserId, groupId);
        if (targetMembership.isEmpty()) {
            throw new NotFoundException();
        }

        var target = targetMembership.get();

        // prevent removing last admin
        if (target.getRole() == Membership.Role.ADMIN &&
                membershipRepository.countByGroup_GroupIdAndRole(groupId, Membership.Role.ADMIN) == 1) {
            throw new IllegalStateException("Cannot remove last admin from the group");
        }

        membershipRepository.delete(target);
    }

    /* TODO: method for an admin to change a user's role within a group */

    /**
     * List all members within a specified group
     * 
     * @param groupId group to list members of
     * @return list of membership DTOs
     * @throws NotFoundException if group not found
     */
    @Transactional(readOnly = true)
    public List<MembershipDTO> listAllMembers(Long groupId) throws NotFoundException {

        if (!(groupRepository.existsById(groupId))) {
            throw new NotFoundException();
        }

        // using map to map each member in the group to their respective DTO
        // converting those into a list
        return membershipRepository.findByGroup_GroupId(groupId).stream()
                .map(m -> new MembershipDTO(
                        m.getMembershipId(),
                        m.getUser().getUserId(),
                        m.getGroup().getGroupId(),
                        (m.getRole())))
                .toList();

    }

    /**
     * Gets a group from a coresponding ID
     * 
     * @param groupId group to fetch
     * @return the gorup DTO
     * @throws NotFoundException if group not found
     */
    @Transactional(readOnly = true)
    public GroupDTO getGroupById(Long groupId) throws NotFoundException {
        var foundGroup = groupRepository.findById(groupId);

        if (foundGroup.isEmpty()) {
            throw new NotFoundException();
        }

        var group = foundGroup.get();

        return new GroupDTO(
                group.getGroupId(),
                group.getGroupName(),
                group.getGroupCreatedAt());
    }

    /**
     * Deletes a group (only actionable if requesting user is an admin)
     * 
     * @param groupId
     * @param requestingUserId
     * @throws NotFoundException
     * @throws AccessDeniedException
     */
    @Transactional
    public void deleteGroup(Long groupId, Long requestingUserId) throws NotFoundException, AccessDeniedException {

        // throw not found if group does not exist
        if (!(groupRepository.findById(groupId).isPresent())) {
            throw new NotFoundException();
        }

        // find the corresponding membership between the user and the group
        var membershipOpt = membershipRepository.findByUser_UserIdAndGroup_GroupId(requestingUserId, groupId);

        // if no membership, deny deletion
        if (membershipOpt.isEmpty()) {
            throw new AccessDeniedException("User is not a member of the group");
        }

        var membership = membershipOpt.get();

        // if user is not an admin, deny deletion
        if (membership.getRole() != Membership.Role.ADMIN) {
            throw new AccessDeniedException("Only admins can delete groups");
        }

        var group = groupRepository.findById(groupId).orElseThrow(NotFoundException::new);
        groupRepository.delete(group);
    }

    /**
     * Lists all groups in the system.
     * 
     * @return list of all groups as DTOs
     */
    @Transactional(readOnly = true)
    public List<GroupDTO> listAllGroups() {
        return groupRepository.findAll().stream()
                .map(g -> new GroupDTO(
                        g.getGroupId(),
                        g.getGroupName(),
                        g.getGroupCreatedAt()))
                .toList();
    }
}
