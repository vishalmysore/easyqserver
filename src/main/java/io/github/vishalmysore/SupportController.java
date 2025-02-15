package io.github.vishalmysore;

import io.github.vishalmysore.data.ContactUs;
import io.github.vishalmysore.service.base.ContactUsDBService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log
@RestController
@RequestMapping("/api")
public class SupportController {
    @Autowired
    @Qualifier("contactUsDBService")
    private ContactUsDBService contactUsDBService;

    @PostMapping("/contactUs")
    public String updateResults(@RequestBody ContactUs score, HttpServletRequest request) {
        log.info("Getting results "+score);
        contactUsDBService.insertSupportTicket(score);
        return "success";
    }
}
