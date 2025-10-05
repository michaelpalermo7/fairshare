package com.fairshare.fairshare.controller;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fairshare.fairshare.dto.AddMemberRequest;
import com.fairshare.fairshare.dto.CreateGroupRequest;
import com.fairshare.fairshare.dto.GroupDTO;
import com.fairshare.fairshare.dto.MembershipDTO;
import com.fairshare.fairshare.service.GroupService;

@RestController
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public GroupDTO createGroup(@RequestBody CreateGroupRequest request) {
        return groupService.createGroup(request.name(), request.creatorUserId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(@PathVariable Long groupId, @RequestParam Long requestingUserId)
            throws NotFoundException, AccessDeniedException {
        groupService.deleteGroup(groupId, requestingUserId);
    }

    @PostMapping("/{id}/members")
    public MembershipDTO addMember(
            @PathVariable("id") Long groupId, @RequestBody AddMemberRequest request) throws NotFoundException {
        return groupService.addMember(groupId, request.userId(), request.role());
    }

    @DeleteMapping("/{id}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable("id") Long groupId, @PathVariable("userId") Long targetUserId,
            @RequestParam("requesterId") Long requesterUserId) throws NotFoundException, AccessDeniedException {

        groupService.removeMember(requesterUserId, targetUserId, groupId);
    }

    @GetMapping("/{id}/members")
    public List<MembershipDTO> listAllMembers(@PathVariable Long id) throws NotFoundException {
        return groupService.listAllMembers(id);
    }
}
