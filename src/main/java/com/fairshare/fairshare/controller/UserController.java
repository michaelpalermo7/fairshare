package com.fairshare.fairshare.controller;

import java.util.List;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fairshare.fairshare.dto.CreateUserRequest;
import com.fairshare.fairshare.dto.UserDTO;
import com.fairshare.fairshare.entity.User;
import com.fairshare.fairshare.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDTO createUser(@RequestBody @Valid CreateUserRequest request) {
        return userService.createUser(request.name(), request.email());
    }

    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) throws NotFoundException {
        User user = userService.getUserById(id);

        return new UserDTO(
            user.getUserId(),
            user.getUserName(),
            user.getUserEmail()
        );
    }

    @GetMapping
    public List<UserDTO> getAllUsers() {
        List<User> users = userService.getAllUsers();

        //Map each user in users to a DTO
        return users.stream()
            .map(u -> new UserDTO(
                u.getUserId(),
                u.getUserName(),
                u.getUserEmail()
            ))
            .toList();
    }

    // TODO: GET /users/by-email?email=...
    // - call userService.getUserByEmail(email)
    // - return UserDTO

    // TODO: DELETE /users/{id}
    // - call userService.softDeleteUser(id)
    // - return 204 No Content
    
}
