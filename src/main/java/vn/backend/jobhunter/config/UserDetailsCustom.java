package vn.backend.jobhunter.config;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import vn.backend.jobhunter.service.UserService;

@Component("userDetailsService")
public class UserDetailsCustom implements UserDetailsService {

    private UserService userService;

    public UserDetailsCustom(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO Auto-generated method stub

        vn.backend.jobhunter.domain.User user = this.userService.handleFindUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("username/password invalid");
        }

        return new User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

    }

}
