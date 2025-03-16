this project for study purpose, using DDD (Data Driven Domain) for architechture the sorce code

this jobhunter website simulate based on the real world project using java Spring version 2.3.4 and mySQL for storing data spring security 2.5.7

with functions:
1. login, create account
2. manage all user with there role (recruiter, job seeker), permission (create, edit, update, delete)
3. filter job by some indem(salary, company)
4. 


all about this code:

1. format global resopnse entity
2. handle local exception (within the scope of 1 file by @exceptionHandler) and global exception (by @controllerAdvice) and format RESTResponse before sending to client in util.error
3. authentication by java security OAuth 2.0; generate JWT key in each session (Header, Payload, Signature), format response payload (exclude some sensitive data eg: passwords,...), 
authentication by spring security filter using JWT token, and save into security context for reusing purpose with exclude some router (login, create account) by OAuth following some steps:

    1. Validate User Input
    Ensure that username and password are present in the request.
    If missing, throw a custom exception before Spring Security processes it.
    [ResLoginDTO.java](https://github.com/phucpham24/RESTFull_java/blob/master/src/main/java/vn/backend/jobhunter/domain/response/ResLoginDTO.java)
    
    3. Authenticate User
    Search for the user in the database using username.
    Retrieve the stored hashed password from the database.
    Compare the hashed password with the user's input (use BCryptPasswordEncoder).
    If authentication fails, throw an AuthenticationException.

    5. Generate JWT Token
    Use a shared secret key (HS256 or another secure algorithm).
    Store the secret key securely (e.g., environment variables, properties file).
    Use Base64 encoding for secret key storage.
    Set an expiration period (e.g., 1 day).
    Add user roles and necessary claims to the JWT payload.

    6. Respond to User
    Create a UserDTO (Data Transfer Object).
    Exclude the password from the response.
    Return a structured response containing:
    JWT Token
    User details (username, roles, etc.)
    Expiry time

    7. Handle Exceptions Properly
    Use Global Exception Handling (@ControllerAdvice + @ExceptionHandler).
    Return meaningful HTTP status codes (e.g., 401 Unauthorized, 403 Forbidden).

    8. Protect Endpoints (Excluding Login & Signup)
    Exclude routes (/login, /register) from JWT filtering.
    Use BearerTokenAuthenticationFilter (from Nimbus OAuth2).
    Extract the Authorization: Bearer <JWT> token from request headers.
    Validate JWT (verify signature, check expiration).
    If valid, set SecurityContext with authenticated user details.
    Restrict access to endpoints based on roles and permissions.
    
    7.Use Refresh Tokens
    JWT expires in 1 day, so consider implementing refresh tokens for better user experience.
    Store refresh tokens securely in the database or HTTP-only cookies.
Data interaction with DB:
    1. using lib Hibernate and @prePersist for formating data before persisting (adding CreateAt UpdateAt with local format Date&time)
    2. query with pagination (offset & limit receive current page and page size from front end to querry in DB) and Specification (filter by spec): [formatDTO](https://github.com/phucpham24/RESTFull_java/blob/master/src/main/java/vn/backend/jobhunter/domain/response/ResultPaginationDTO.java) and [query](https://github.com/phucpham24/RESTFull_java/blob/22fe52ba95b727c137b06ca30e3f0f4a2347e7d2/src/main/java/vn/backend/jobhunter/service/UserService.java#L49) 
  
5. authorisation by adding permission
6. valid with cookie and sesion

