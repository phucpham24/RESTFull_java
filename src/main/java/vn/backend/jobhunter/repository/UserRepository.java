package vn.backend.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.backend.jobhunter.domain.Company;
import vn.backend.jobhunter.domain.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    User findUserByEmail(String email);

    boolean existsByEmail(String email);

    User findUserById(long id);

    User findByRefreshTokenAndEmail(String token, String email);

    List<User> findByCompany(Company company);
}
