this project for study purpose, using DDD (Data Driven Domain) for architechture the sorce code

this jobhunter website simulate based on the real world project using java Spring version 2.3.4 and mySQL for storing data spring security 2.5.7

with functions:
1. login, create account
2. manage all user with there role (recruiter, job seeker), permission (create, edit, update, delete)
3. filter job by some indem(salary, company, )
4. 


all about this code:

1. format global resopnse entity
2. handle local exception (within the scope of 1 file by @exceptionHandler) and global exception (by @controllerAdvice) and format RESTResponse before sending to client in util.error
3. authorisation, authentication by java security; generate JWT key in each session (Header, Payload, Signature), format response payload (exclude some sensitive data eg: passwords,...), especially encrypt user password by hashing
  a. encrypt password by Hasing password with saw function
  b. authentication by spring security filter using JWT token, and save to security context with exclude some router (login, create account) by OAuth
5. 
6. valid with cookie and sesion

