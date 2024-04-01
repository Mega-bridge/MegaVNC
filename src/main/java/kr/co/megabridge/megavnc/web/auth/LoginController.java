package kr.co.megabridge.megavnc.web.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/login")
public class LoginController {

    @GetMapping
    public String showLogin(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String registered,
            Model model
    ) {
        if (error != null)
            model.addAttribute("loginError", true);

        if (registered != null)
            model.addAttribute("registered", true);


        return "auth/login";
    }
}
