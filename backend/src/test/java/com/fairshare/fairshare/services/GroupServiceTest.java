package com.fairshare.fairshare.services;

import java.nio.file.AccessDeniedException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fairshare.fairshare.dto.GroupDTO;
import com.fairshare.fairshare.entity.Group;
import com.fairshare.fairshare.entity.Membership;
import com.fairshare.fairshare.entity.User;
import com.fairshare.fairshare.repository.GroupRepository;
import com.fairshare.fairshare.repository.MembershipRepository;
import com.fairshare.fairshare.repository.UserRepository;
import com.fairshare.fairshare.service.GroupService;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {

    @Mock
    GroupRepository groupRepository;
    @Mock
    MembershipRepository membershipRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    GroupService groupService;

    /**
     * Test for the group creation flow and ensuring user who created group is auto
     * assigned admin
     * 
     * @throws Exception if error occurs
     */
    @Test
    void createGroup_MakesCreatorAdmin() throws Exception {
        Long creatorId = 10L;
        String name = "Lunch";

        Group savedGroup = new Group();
        savedGroup.setGroupId(1L);
        savedGroup.setGroupName(name);
        when(groupRepository.save(any(Group.class))).thenReturn(savedGroup);

        User creatorRef = new User();
        creatorRef.setUserId(creatorId);
        when(userRepository.getReferenceById(creatorId)).thenReturn(creatorRef);

        when(membershipRepository.save(any(Membership.class))).thenReturn(new Membership());

        GroupDTO dto = groupService.createGroup(name, creatorId);

        assertEquals(1L, dto.id());
        assertEquals("Lunch", dto.name());

        verify(groupRepository).save(any(Group.class));
        verify(membershipRepository).save(argThat(m -> m.getUser() != null &&
                m.getUser().getUserId().equals(creatorId) &&
                m.getGroup() != null &&
                m.getGroup().getGroupId().equals(1L) &&
                (m.getRole() == Membership.Role.ADMIN)));
    }

    /**
     * Test that last admin within a group cannot be removed
     * 
     * @throws Exception if error occurs
     */
    @Test
    void removeMember_whenUserIsLastAdmin() throws Exception {
        Long groupId = 1L;
        Long adminId = 10L;

        Group g = new Group();
        g.setGroupId(groupId);

        Membership admin = new Membership();
        admin.setUser(new User());
        admin.setGroup(g);
        admin.setRole(Membership.Role.ADMIN);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(g));

        when(membershipRepository.findByUser_UserIdAndGroup_GroupId(adminId, groupId))
                .thenReturn(Optional.of(admin));

        when(membershipRepository.countByGroup_GroupIdAndRole(groupId, Membership.Role.ADMIN))
                .thenReturn(1L);

        assertThrows(IllegalStateException.class,
                () -> groupService.removeMember(adminId, adminId, groupId));

        verify(membershipRepository, never()).delete(any());
    }

    /**
     * Test that a member cannot delete a group
     * 
     * @throws Exception if error occurs
     */
    @Test
    void deleteGroup_whenRequesterNotAdmin() throws Exception {
        Long groupId = 1L;
        Long memberId = 20L;

        Group g = new Group();
        g.setGroupId(groupId);

        Membership member = new Membership();
        member.setUser(new User());
        member.setGroup(g);
        // user not an admin
        member.setRole(Membership.Role.MEMBER);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(g));
        when(membershipRepository.findByUser_UserIdAndGroup_GroupId(memberId, groupId))
                .thenReturn(Optional.of(member));

        assertThrows(AccessDeniedException.class,
                () -> groupService.deleteGroup(groupId, memberId));

        verify(groupRepository, never()).deleteById(groupId);
    }

    /**
     * Test adding a duplicate member to a group is prevented
     * 
     * @throws Exception if error occurs
     */
    @Test
    void addMember_whenUserAlreadyInGroup() throws Exception {
        Long groupId = 1L;
        Long userId = 42L;

        Group g = new Group();
        g.setGroupId(groupId);

        User u = new User();
        u.setUserId(userId);

        Membership existing = new Membership();
        existing.setUser(u);
        existing.setGroup(g);
        existing.setRole(Membership.Role.MEMBER);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(g));
        when(userRepository.findById(userId)).thenReturn(Optional.of(u));
        when(membershipRepository.findByUser_UserIdAndGroup_GroupId(userId, groupId))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class,
                () -> groupService.addMember(groupId, userId, Membership.Role.MEMBER));

        verify(membershipRepository, never()).save(any());
    }
}
