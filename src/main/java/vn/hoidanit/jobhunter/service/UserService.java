package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.config.SecurityConfiguration;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.repository.UserRepository;

@Service
public class UserService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User handleSaveUser(User user) {
        return this.userRepository.save(user);
    }

    public void deleteUserById(long id) {
        this.userRepository.deleteById(id);
    }

    public User findUser(long id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        if (userOptional.isPresent()) {
            return userOptional.get();
        }
        return null;
    }

    public List<User> findAllUser() {
        return this.userRepository.findAll();
    }

    public User updateUser(User requestUser) {
        User currentUser = this.findUser(requestUser.getId());
        if (currentUser != null) {
            currentUser.setName(requestUser.getName());
            currentUser.setPassword(requestUser.getPassword());
            currentUser.setEmail(requestUser.getEmail());
            currentUser = this.handleSaveUser(currentUser);
        }
        return currentUser;
    }

    public User handleFindUserByUsername(String email) {
        User user = this.userRepository.findUserByEmail(email);
        return user;
    }
}
