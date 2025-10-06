package com.fairshare.fairshare.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fairshare.fairshare.dto.UserDTO;
import com.fairshare.fairshare.entity.User;
import com.fairshare.fairshare.repository.UserRepository; // ✅

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * creates a user
     * 
     * @param email unique email of user
     * @param name  name of user (duplicates allowed)
     * @return the user DTO
     */
    @Transactional
    public UserDTO createUser(String email, String name) {

        if (userRepository.findByUserEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        /* TODO: Enforce stricter name conventions */

        // create and save the user
        User u = new User();
        u.setUserEmail(email);
        u.setUserName(name);

        User saved = userRepository.save(u);

        return new UserDTO(saved.getUserId(), saved.getUserName(), saved.getUserEmail());
    }

    /**
     * gets a user by their unique email
     * 
     * @param email to fetch user
     * @return the user
     * @throws NotFoundException if user not found
     */
    @Transactional
    public User getUserByEmail(String email) throws NotFoundException {
        Optional<User> found = userRepository.findByUserEmail(email.toLowerCase().trim());

        if (found.isEmpty()) {
            throw new NotFoundException();
        }

        return found.get();
    }

    /**
     * Gets a user by their ID
     * 
     * @param userId user to fetch
     * @return the user
     * @throws NotFoundException user not found
     */
    @Transactional
    public User getUserById(Long userId) throws NotFoundException {
        Optional<User> found = userRepository.findById(userId);

        if (found.isEmpty()) {
            throw new NotFoundException();
        }

        return found.get();
    }

    /**
     * Lists all users
     * 
     * @return list of users
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Deletes a user (soft deletes, retains info)
     * 
     * @param userId user to delete
     * @throws NotFoundException user not found
     */
    @Transactional
    public void softDeleteUser(Long userId) throws NotFoundException {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            throw new NotFoundException();
        }

        User user = optionalUser.get();

        // if user is already in the deleted at table
        if (user.getDeletedAt() != null) {
            throw new IllegalStateException("User already deleted");
        }

        // set deleted at table
        user.setDeletedAt(Instant.now());
        userRepository.save(user);
    }
}
