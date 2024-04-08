package kr.co.megabridge.megavnc.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String home(HttpServletRequest request) {
     /*   if (request.isUserInRole("ADMIN"))
            return "redirect:/admin";
        else if (request.isUserInRole("USER"))
            return "redirect:/remote-pcs";
        else
            return "redirect:/login";*/
        if (request.isUserInRole("ADMIN")||request.isUserInRole("USER"))
            return "redirect:/remote-pcs";
        else
            return "redirect:/login";
    }
}
