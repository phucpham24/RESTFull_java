package vn.backend.jobhunter.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.backend.jobhunter.domain.Permission;
import vn.backend.jobhunter.domain.Role;
import vn.backend.jobhunter.domain.User;
import vn.backend.jobhunter.service.UserService;
import vn.backend.jobhunter.util.SecurityUtil;
import vn.backend.jobhunter.util.error.IdInvalidException;
import vn.backend.jobhunter.util.error.PermissionException;

public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response, Object handler)
            throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();
        System.out.println(">>> RUN preHandle");
        System.out.println(">>> path= " + path);
        System.out.println(">>> httpMethod= " + httpMethod);
        System.out.println(">>> requestURI= " + requestURI);

        // check permission
        String email = SecurityUtil.getCurrentUserLogin().isPresent() == true ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        if (email != null && !email.isEmpty()) {
            User user = this.userService.handleFindUserByUsername(email);
            if (user != null) {
                Role role = user.getRole();
                if (role != null) {
                    List<Permission> permissions = role.getPermissions();
                    boolean isAllow = permissions.stream()
                            .anyMatch(item -> item.getApiPath().equals(path) && item.getMethod().equals(httpMethod));

                    if (isAllow == false) {
                        throw new PermissionException(">>>> do not permit to access this endpoint!");
                    }
                } else {
                    throw new PermissionException(">>>> do not permit to access this endpoint!");
                }
            }
        }
        return true;
    }
}
