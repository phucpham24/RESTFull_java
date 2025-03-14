package vn.backend.jobhunter.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.backend.jobhunter.service.EmailService;
import vn.backend.jobhunter.service.SubscriberService;
import vn.backend.jobhunter.util.annotation.ApiMessage;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1")
public class EmailController {

    private final SubscriberService subscriberService;

    public EmailController(SubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }

    @GetMapping("/email")
    @ApiMessage("send simple email")
    public String sendSimpleEmail() {
        // this.emailService.sendEmailSync("phucsaiyan249@gmail.com", "Testing from
        // springboot javasend",
        // "<h1><b> hello </b></h1>", false, true);
        this.subscriberService.sendSubscribersEmailJobs();
        return "ok";
    }
}
