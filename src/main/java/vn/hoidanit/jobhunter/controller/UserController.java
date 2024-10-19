package vn.hoidanit.jobhunter.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.response.ResCreateUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUpdateUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/users")
    @ApiMessage("created a user successfully")
    public ResponseEntity<ResCreateUserDTO> createNewUser(@RequestBody User postmanUser) throws IdInvalidException {
        if (this.userService.checkEmailExisted(postmanUser.getEmail())) {
            throw new IdInvalidException(
                    "Email " + postmanUser.getEmail() + "is existed, please using another email.");
        }

        String hashPassword = this.passwordEncoder.encode(postmanUser.getPassword());
        postmanUser.setPassword(hashPassword);
        User user = this.userService.handleSaveUser(postmanUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertUserToUserDTO(user));

    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("deleted one user")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") long id)
            throws IdInvalidException {
        User user = this.userService.findUser(id);
        if (user == null) {
            throw new IdInvalidException("user with id: " + id + " does not exist");
        }

        this.userService.deleteUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @GetMapping("/users/{id}")
    @ApiMessage("found one user")
    public ResponseEntity<ResUserDTO> fetchOneUser(@PathVariable("id") long id) {
        User user = this.userService.findUser(id);
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.convertToUserDTO(user));

    }

    @GetMapping("/users")
    @ApiMessage("fetch all users")
    public ResponseEntity<ResultPaginationDTO> fetchAllUser(
            @Filter Specification<User> spec, Pageable pageable) {

        ResultPaginationDTO res = this.userService.findAllUser(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PutMapping("/users")
    @ApiMessage("updated the user")
    public ResponseEntity<ResUpdateUserDTO> updateUser(@RequestBody User updateUser) throws IdInvalidException {
        User updatedUser = this.userService.updateUser(updateUser);
        if (updatedUser == null) {
            throw new IdInvalidException(
                    "user " + updateUser.getId() + "is not existed, please chosing another user.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.convertUserToUserUpDTO(updatedUser));
    }

}
