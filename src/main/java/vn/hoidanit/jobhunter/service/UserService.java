package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.Role;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.response.ResCreateUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUpdateUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.UserRepository;

@Service
public class UserService {
    private UserRepository userRepository;
    private CompanyService companyService;
    private RoleService roleService;

    public UserService(UserRepository userRepository, CompanyService companyService, RoleService roleService) {
        this.userRepository = userRepository;
        this.companyService = companyService;
        this.roleService = roleService;
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
                .stream().map(item -> this.convertToUserDTO(item))
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

            // check company
            if (requestUser.getCompany() != null) {
                Optional<Company> companyOp = this.companyService.findCompanyById(requestUser.getCompany().getId());
                currentUser.setCompany(companyOp.isPresent() ? companyOp.get() : null);

            }

            // check role
            if (requestUser.getRole() != null) {
                Role role = this.roleService.fetchById(requestUser.getRole().getId());
                requestUser.setRole(role != null ? role : null);
            }

            // update user
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

    // convert createUser
    public ResCreateUserDTO convertUserToUserDTO(User user) {
        ResCreateUserDTO userdto = new ResCreateUserDTO();

        ResCreateUserDTO.CompanyUser companyUser = new ResCreateUserDTO.CompanyUser();

        userdto.setName(user.getName());
        userdto.setId(user.getId());
        userdto.setAge(user.getAge());
        userdto.setCreatedAt(user.getCreatedAt());
        userdto.setGender(user.getGender());
        userdto.setAddress(user.getAddress());
        userdto.setEmail(user.getEmail());
        if (user.getCompany() != null) {
            companyUser.setId(user.getCompany().getId());
            companyUser.setName(user.getCompany().getName());
            userdto.setCompany(companyUser);
        }
        return userdto;
    }

    public ResUpdateUserDTO convertUserToUserUpDTO(User user) {
        ResUpdateUserDTO resUpdateUserDTO = new ResUpdateUserDTO();

        // set company info
        ResUpdateUserDTO.CompanyUser companyUser = new ResUpdateUserDTO.CompanyUser();
        if (user.getCompany() != null) {
            companyUser.setId(user.getCompany().getId());
            companyUser.setName(user.getCompany().getName());
            resUpdateUserDTO.setCompany(companyUser);
        }

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

        ResUserDTO.CompanyUser companyUser = new ResUserDTO.CompanyUser();
        if (user.getCompany() != null) {
            companyUser.setId(user.getCompany().getId());
            companyUser.setName(user.getCompany().getName());
            resUserDTO.setCompany(companyUser);
        }

        ResUserDTO.RoleUser roleUser = new ResUserDTO.RoleUser();
        if (user.getRole() != null) {
            roleUser.setId(user.getRole().getId());
            roleUser.setName(user.getRole().getName());
            resUserDTO.setRole(roleUser);
        }

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

    public User handleCreateUser(User user) {
        // check company
        if (user.getCompany() != null) {
            Optional<Company> companyOp = this.companyService.findCompanyById(user.getCompany().getId());
            user.setCompany(companyOp.isPresent() ? companyOp.get() : null);

        }

        // check role
        if (user.getRole() != null) {
            Role role = this.roleService.fetchById(user.getRole().getId());
            user.setRole(role != null ? role : null);
        }
        return this.userRepository.save(user);
    }
}
