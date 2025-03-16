this project for study purpose, using DDD (Data Driven Domain) for architechture the sorce code

this jobhunter website simulate based on the real world project using java Spring version 2.3.4 and mySQL for storing data spring security 2.5.7

with functions:
1. login, create account
2. manage all user with there role (recruiter, job seeker), permission (create, edit, update, delete)
3. filter job by some indem(salary, company)
4. 


all about this code:

## 1. format global resopnse entity
## 2. handle local exception (within the scope of 1 file by @exceptionHandler) and global exception (by @controllerAdvice) and format RESTResponse before sending to client in util.error
## 3. authentication by java security OAuth 2.0; generate JWT key in each session (Header, Payload, Signature), format response payload (exclude some sensitive data eg: passwords,...), 
authentication by spring security filter using JWT token, and save into security context for reusing purpose with exclude some router (login, create account) by OAuth following some steps:

###1. Validate User Input
    - Ensure that username and password are present in the request.
    If missing, throw a custom exception before Spring Security processes it.
    [ResLoginDTO.java](https://github.com/phucpham24/RESTFull_java/blob/master/src/main/java/vn/backend/jobhunter/domain/response/ResLoginDTO.java)
    
###3. Authenticate User
    Search for the user in the database using username.
    Retrieve the stored hashed password from the database.
    Compare the hashed password with the user's input (use BCryptPasswordEncoder).
    If authentication fails, throw an AuthenticationException.
    
###5. Generate JWT Token
    Use a shared secret key (HS256 or another secure algorithm).
    Store the secret key securely (e.g., environment variables, properties file).
    Use Base64 encoding for secret key storage.
    Set an expiration period (e.g., 1 day).
    Add user roles and necessary claims to the JWT payload.

###6. Respond to User
    Create a UserDTO (Data Transfer Object).
    Exclude the password from the response.
    Return a structured response containing:
    JWT Token
    User details (username, roles, etc.)
    Expiry time

###7. Handle Exceptions Properly
    Use Global Exception Handling (@ControllerAdvice + @ExceptionHandler).
    Return meaningful HTTP status codes (e.g., 401 Unauthorized, 403 Forbidden).

###8. Protect Endpoints (Excluding Login & Signup)
    Exclude routes (/login, /register) from JWT filtering.
    Use BearerTokenAuthenticationFilter (from Nimbus OAuth2).
    Extract the Authorization: Bearer <JWT> token from request headers.
    Validate JWT (verify signature, check expiration).
    If valid, set SecurityContext with authenticated user details.
    Restrict access to endpoints based on roles and permissions.
    
###7.Use Refresh Tokens
    JWT expires in 1 day, so consider implementing refresh tokens for better user experience.
    Store refresh tokens securely in the database or HTTP-only cookies.
## Data interaction with DB:
### 1. Data Persistence & Formatting
- **Hibernate** is used for ORM, ensuring seamless database interactions.
- `@PrePersist` annotation is implemented to automatically format data before persistence.
- Fields such as `createdAt` and `updatedAt` are automatically generated with a localized date and time format.

### 2. Query with Pagination & Filtering
- Pagination is implemented using **offset** and **limit**, dynamically receiving the **current page** and **page size** from the frontend.[DTO Pagination Format](https://github.com/phucpham24/RESTFull_java/blob/master/src/main/java/vn/backend/jobhunter/domain/response/ResultPaginationDTO.java)
- Uses **Spring Data JPA** to query data efficiently.[Pagination Query Implementation](https://github.com/phucpham24/RESTFull_java/blob/22fe52ba95b727c137b06ca30e3f0f4a2347e7d2/src/main/java/vn/backend/jobhunter/service/UserService.java#L49)
- Filtering and sorting are implemented using **Spring Data Specifications**:
  - [Spring Filter JPA 3.1.7](https://www.baeldung.com/rest-api-search-language-spring-data-specifications) allows dynamic filtering.
  - **Criteria API & Predicate** enable flexible query criteria.
  
#### References:
- [CriteriaBuilder Documentation](https://docs.oracle.com/javaee/7/api/javax/persistence/criteria/CriteriaBuilder.html)

### 3. Custom Annotations & Messaging
- Custom annotation `@ApiMessage` is created to manage response messages dynamically.
- Annotations are set with specific retention policies and execution scopes.[Annotation Function](https://github.com/phucpham24/RESTFull_java/blob/master/src/main/java/vn/backend/jobhunter/util/annotation/ApiMessage.java)
- The system ensures meaningful API responses by testing annotations via controller methods.[Testing Custom Annotations](https://github.com/phucpham24/RESTFull_java/blob/0978ede89499aea3907eb3cbb41adc6797b05f9a/src/main/java/vn/backend/jobhunter/controller/AuthController.java#L102)

#### References:
- [Creating Custom Annotations](https://www.geeksforgeeks.org/java-retention-annotations/)

### 4. API Versioning
- Supports **API versioning** to maintain backward compatibility.
- Implemented via **Spring API Versioning** to manage multiple API versions effectively.[Versioning via Controller](https://github.com/phucpham24/RESTFull_java/blob/0978ede89499aea3907eb3cbb41adc6797b05f9a/src/main/java/vn/backend/jobhunter/controller/AuthController.java#L34)
- Allows smooth transition between API updates while maintaining legacy support.

#### References:
- [Spring API Versioning Library](https://github.com/lkqm/spring-api-versioning)

## Authentication Flow
### 1. Login and Token Generation
After a successful login:
- The server **creates an access token** ([AuthController.java#L77](https://github.com/phucpham24/RESTFull_java/blob/581754a011de9e7cac2b4f2f2357f5c2ebae5851/src/main/java/vn/backend/jobhunter/controller/AuthController.java#L77)).
- This token contains:
  - **Subject (sub)**: User email (ensures uniqueness) ([SecurityUtil.java#L67](https://github.com/phucpham24/RESTFull_java/blob/581754a011de9e7cac2b4f2f2357f5c2ebae5851/src/main/java/vn/backend/jobhunter/util/SecurityUtil.java#L67)).
  - **Claims**: Description of token ([SecurityUtil.java#L68](https://github.com/phucpham24/RESTFull_java/blob/581754a011de9e7cac2b4f2f2357f5c2ebae5851/src/main/java/vn/backend/jobhunter/util/SecurityUtil.java#L68)).
- The token is included in the response as part of the **user information** ([ResLoginDTO.java](https://github.com/phucpham24/RESTFull_java/blob/master/src/main/java/vn/backend/jobhunter/domain/response/ResLoginDTO.java)).

### 2. Refresh Token Management
- Since the **access token has a short lifespan**, a **refresh token** is also created ([AuthController.java#L82](https://github.com/phucpham24/RESTFull_java/blob/581754a011de9e7cac2b4f2f2357f5c2ebae5851/src/main/java/vn/backend/jobhunter/controller/AuthController.java#L82)).
- The refresh token:
  - Stores the **user's email** and is saved in the database with a **100-day expiration**.
  - Persists across browser sessions (unlike regular session settings).
  - Is **set as an HTTP-only cookie** to enhance security ([AuthController.java#L87](https://github.com/phucpham24/RESTFull_java/blob/581754a011de9e7cac2b4f2f2357f5c2ebae5851/src/main/java/vn/backend/jobhunter/controller/AuthController.java#L87)).

🔗 **References**:
- [Refactor Cookie Handling](https://reflectoring.io/spring-boot-cookies/)
- [Spring ResponseCookie Documentation](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/ResponseCookie.html)

### 3. Token Usage & Authorization
- When accessing protected endpoints, clients **must send the access token** in the `Authorization` header (`Bearer token`).
- The **decoder** is triggered to validate the token before processing the request ([SecurityConfiguration.java#L42](https://github.com/phucpham24/RESTFull_java/blob/581754a011de9e7cac2b4f2f2357f5c2ebae5851/src/main/java/vn/backend/jobhunter/config/SecurityConfiguration.java#L42)).
- User **roles and authorities** are assigned accordingly ([SecurityConfiguration.java#L66](https://github.com/phucpham24/RESTFull_java/blob/581754a011de9e7cac2b4f2f2357f5c2ebae5851/src/main/java/vn/backend/jobhunter/config/SecurityConfiguration.java#L66)).

### 4. Handling Expired Access Tokens
If a user refreshes the page (e.g., **presses F5**):
- The client calls an API with the **access token** in the header.
- If the access token **is expired**, the response is **401 Unauthorized**.
- The client then sends the **refresh token** to **request a new access token** ([AuthController.java#L119](https://github.com/phucpham24/RESTFull_java/blob/581754a011de9e7cac2b4f2f2357f5c2ebae5851/src/main/java/vn/backend/jobhunter/controller/AuthController.java#L119)).
- The server **decodes the refresh token** and verifies the **user’s email** in the database ([SecurityUtil.java#L102](https://github.com/phucpham24/RESTFull_java/blob/581754a011de9e7cac2b4f2f2357f5c2ebae5851/src/main/java/vn/backend/jobhunter/util/SecurityUtil.java#L102)).
- A new **access token and refresh token** are issued, and the refresh token is stored as a new **cookie**.

### 5. Logout Mechanism
When a client logs out:
1. The client [**calls the logout API**](https://github.com/phucpham24/RESTFull_java/blob/ff6045c2cc53ab2cba20497d00a990a3cc6dff03/src/main/java/vn/backend/jobhunter/controller/AuthController.java#L175), sending the refresh token.
2. The server:
   - Retrieves the **user email** from the **Spring Security context**.
   - Updates the **refresh token in the database to `null`**.
   - Sets the refresh token **cookie to `null`**.
3. The server responds with:
   ```json
   {
       "statusCode": 200,
       "message": "Logout User",
       "data": null
   }
   ```
DATA model ass a picture

## sending email to subscriber
setup for spring email (https://github.com/phucpham24/RESTFull_java/blob/ff6045c2cc53ab2cba20497d00a990a3cc6dff03/src/main/resources/application.properties#L46)

ref: https://docs.spring.io/spring-framework/reference/integration/email.html
https://www.baeldung.com/spring-email






