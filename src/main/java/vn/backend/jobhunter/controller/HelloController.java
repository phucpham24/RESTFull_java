package vn.backend.jobhunter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.backend.jobhunter.util.error.IdInvalidException;

@RestController
public class HelloController {

    @GetMapping("/")
    public String getHelloWorld() throws IdInvalidException {
        // if (true)
        // throw new IdInvalidException("check");

        return "Hello World (Hỏi Dân IT & Eric)";
    }
}
