package kr.co.megabridge.megavnc.web.auth;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;



@Slf4j
@Controller
@RequestMapping("/login")
public class LoginController {

    @GetMapping
    public String showLogin(
            @RequestParam(required = false) String error,
            Model model
    ) throws IOException {
        if (error != null)
            model.addAttribute("loginError", true);

        return "auth/login";
    }
}
