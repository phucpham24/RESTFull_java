package vn.backend.jobhunter.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.backend.jobhunter.domain.User;
import vn.backend.jobhunter.domain.request.ReqLoginDTO;
import vn.backend.jobhunter.domain.response.ResCreateUserDTO;
import vn.backend.jobhunter.domain.response.ResLoginDTO;
import vn.backend.jobhunter.service.UserService;
import vn.backend.jobhunter.util.SecurityUtil;
import vn.backend.jobhunter.util.annotation.ApiMessage;
import vn.backend.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Value("${ backend.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil,
            UserService userService, PasswordEncoder passwordEncoder) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO loginDTO) {

        // Nạp input gồm username/password vào Security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword());

        // xác thực người dùng => cần viết hàm loadUserByUsername
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // set userDetail into context for further use
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResLoginDTO res = new ResLoginDTO();
        User currentUserDB = this.userService.handleFindUserByUsername(loginDTO.getUsername());
        if (currentUserDB != null) {
            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                    currentUserDB.getId(),
                    currentUserDB.getEmail(),
                    currentUserDB.getName(),
                    currentUserDB.getRole());
            res.setUserLogin(userLogin);
        }
        // create a token
        String access_token = this.securityUtil.createAccessToken(authentication.getName(), res);

        res.setAccessToken(access_token);

        // create refresh token
        String refreshToken = this.securityUtil.createRefreshToken(loginDTO.getUsername(), res);

        // update user
        this.userService.updateUserToken(refreshToken, loginDTO.getUsername());

        // set cokkies
        ResponseCookie responseCookie = ResponseCookie
                .from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                // .domain("example.com")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(res);
    }

    @GetMapping("/auth/account")
    @ApiMessage("fetch account")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        User currentUserDB = this.userService.handleFindUserByUsername(email);
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();

        if (currentUserDB != null) {
            userLogin.setId(currentUserDB.getId());
            userLogin.setEmail(currentUserDB.getEmail());
            userLogin.setName(currentUserDB.getName());
            userLogin.setRole(currentUserDB.getRole());
            userGetAccount.setUser(userLogin);
        }
        return ResponseEntity.ok().body(userGetAccount);
    }

    @GetMapping("/auth/refresh")
    @ApiMessage("Get User by refresh token")
    public ResponseEntity<ResLoginDTO> getRefreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "abc") String refresh_token)
            throws IdInvalidException {

        if (refresh_token.equals("abc")) {
            throw new IdInvalidException("not having refresh token in cookies");
        }
        // check valid
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refresh_token);
        String email = decodedToken.getSubject();

        // check user by token + email
        User currentUser = this.userService.getUserByRefreshUserAndEmail(refresh_token, email);

        if (currentUser == null) {
            throw new IdInvalidException("refresh token is not valid");
        }
        ResLoginDTO res = new ResLoginDTO();
        User currentUserDB = this.userService.handleFindUserByUsername(email);
        if (currentUserDB != null) {
            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                    currentUserDB.getId(),
                    currentUserDB.getEmail(),
                    currentUserDB.getName(),
                    currentUserDB.getRole());
            res.setUserLogin(userLogin);
        }
        // create a token
        String access_token = this.securityUtil.createAccessToken(email, res);

        res.setAccessToken(access_token);

        // create refresh token
        String new_refreshToken = this.securityUtil.createRefreshToken(email, res);

        // update user
        this.userService.updateUserToken(new_refreshToken, email);

        // set cokkies
        ResponseCookie responseCookie = ResponseCookie
                .from("refresh_token",
                        new_refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                // .domain("example.com")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(res);

    }

    @PostMapping("/auth/logout")
    @ApiMessage("Logout User")
    public ResponseEntity<Void> logoutUser() throws IdInvalidException {
        // TODO: process POST request
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";

        if (email.equals(null)) {
            throw new IdInvalidException("Access Token is in valid");
        }
        // update user refresh_token
        this.userService.updateUserToken(null, email);

        // set cookies
        ResponseCookie deletedCookie = ResponseCookie
                .from("refresh_token",
                        null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                // .domain("example.com")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deletedCookie.toString())
                .body(null);
    }

    @PostMapping("/auth/register")
    @ApiMessage("Register a new user")
    public ResponseEntity<ResCreateUserDTO> register(@Valid @RequestBody User postManUser) throws IdInvalidException {
        boolean isEmailExist = this.userService.checkEmailExisted(postManUser.getEmail());
        if (isEmailExist) {
            throw new IdInvalidException(
                    "Email " + postManUser.getEmail() + "đã tồn tại, vui lòng sử dụng email khác.");
        }

        String hashPassword = this.passwordEncoder.encode(postManUser.getPassword());
        postManUser.setPassword(hashPassword);
        User ericUser = this.userService.handleCreateUser(postManUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertUserToUserDTO(ericUser));
    }

}
