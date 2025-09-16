package com.fairshare.fairshare.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fairshare.fairshare.dto.UserDTO;
import com.fairshare.fairshare.entity.User;
import com.fairshare.fairshare.repository.UserRepository;  // ✅

@Service
public class UserService {
    
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserDTO createUser(String email, String name) {

        if(userRepository.findByUserEmail(email).isPresent()){
            throw new IllegalArgumentException("Email already in use");
        }

        User u = new User();
        u.setUserEmail(email);
        u.setUserName(name);

        User saved = userRepository.save(u);

        return new UserDTO(saved.getUserId(), saved.getUserName(), saved.getUserEmail());
    }

    @Transactional
    public User getUserByEmail(String email) throws NotFoundException {
        Optional<User> found = userRepository.findByUserEmail(email.toLowerCase().trim());

        if (found.isEmpty()) {
            throw new NotFoundException();
        }

        return found.get();
    }

    @Transactional
    public User getUserById(Long userId) throws NotFoundException{
        Optional<User> found = userRepository.findById(userId);

        if (found.isEmpty()) {
            throw new NotFoundException();
        }

        return found.get();
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

     /* TODO: Soft delete user
     */
    @Transactional
public void softDeleteUser(Long userId) throws NotFoundException {
    Optional<User> optionalUser = userRepository.findById(userId);

    if (optionalUser.isEmpty()) {
        throw new NotFoundException();
    }

    User user = optionalUser.get();

    if (user.getDeletedAt() != null) {
        throw new IllegalStateException("User already deleted");
    }

    user.setDeletedAt(Instant.now());
    userRepository.save(user);
}
}
