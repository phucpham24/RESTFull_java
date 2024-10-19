package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.dto.ResCreateUserDTO;
import vn.hoidanit.jobhunter.domain.dto.ResUpdateUserDTO;
import vn.hoidanit.jobhunter.domain.dto.ResUserDTO;
import vn.hoidanit.jobhunter.domain.dto.ResultPaginationDTO;
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

    public ResultPaginationDTO findAllUser(
            Specification<User> specification,
            Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(specification, pageable);
        ResultPaginationDTO res = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pageUser.getTotalPages());
        mt.setTotal(pageUser.getTotalElements());

        res.setMeta(mt);
        // remove sensitive data
        List<ResUserDTO> listUser = pageUser.getContent()
                .stream().map(item -> new ResUserDTO(
                        item.getId(),
                        item.getEmail(),
                        item.getName(),
                        item.getGender(),
                        item.getAddress(),
                        item.getAge(),
                        item.getUpdatedAt(),
                        item.getCreatedAt()))
                .collect(Collectors.toList());

        res.setResult(listUser);

        return res;
    }

    public User updateUser(User requestUser) {
        User currentUser = this.findUser(requestUser.getId());
        if (currentUser != null) {
            currentUser.setId(requestUser.getId());
            currentUser.setName(requestUser.getName());
            currentUser.setAddress(requestUser.getAddress());
            currentUser.setAge(requestUser.getAge());
            currentUser.setGender(requestUser.getGender());
            currentUser.setUpdatedAt(requestUser.getUpdatedAt());
            currentUser = this.handleSaveUser(currentUser);
        }
        return currentUser;
    }

    public User handleFindUserByUsername(String email) {
        User user = this.userRepository.findUserByEmail(email);
        return user;
    }

    public Boolean checkEmailExisted(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public ResCreateUserDTO convertUserToUserDTO(User user) {
        ResCreateUserDTO userdto = new ResCreateUserDTO();
        userdto.setName(user.getName());
        userdto.setId(user.getId());
        userdto.setAge(user.getAge());
        userdto.setCreatedAt(user.getCreatedAt());
        userdto.setGender(user.getGender());
        userdto.setAddress(user.getAddress());
        userdto.setEmail(user.getEmail());

        return userdto;
    }

    public ResUpdateUserDTO convertUserToUserUpDTO(User user) {
        ResUpdateUserDTO resUpdateUserDTO = new ResUpdateUserDTO();
        resUpdateUserDTO.setName(user.getName());
        resUpdateUserDTO.setId(user.getId());
        resUpdateUserDTO.setAge(user.getAge());
        resUpdateUserDTO.setUpdateAt(user.getUpdatedAt());
        resUpdateUserDTO.setGender(user.getGender());
        resUpdateUserDTO.setAddress(user.getAddress());
        return resUpdateUserDTO;
    }

    public ResUserDTO convertToUserDTO(User user) {
        ResUserDTO resUserDTO = new ResUserDTO();
        resUserDTO.setName(user.getName());
        resUserDTO.setId(user.getId());
        resUserDTO.setAge(user.getAge());
        resUserDTO.setEmail(user.getEmail());
        resUserDTO.setCreatedAt(user.getCreatedAt());
        resUserDTO.setUpdatedAt(user.getUpdatedAt());
        resUserDTO.setGender(user.getGender());
        resUserDTO.setAddress(user.getAddress());
        return resUserDTO;
    }

    public void updateUserToken(String token, String email) {
        User currentUser = this.handleFindUserByUsername(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }

    public User getUserByRefreshUserAndEmail(String token, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(token, email);
    }
}
