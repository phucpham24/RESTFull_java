package vn.hoidanit.jobhunter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final MailSender mailSender;

    public EmailService(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendSimpleEmail() {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo("phucsaiyan249@gmail.com");
        msg.setSubject("Testing from springboot");
        msg.setText("helloworld");
        this.mailSender.send(msg);
    }
}
