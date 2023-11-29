package kr.co.megabridge.megavnc.web.admin;

import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUsersController {

    UserService userService;

    @Autowired
    public AdminUsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showUsers(Model model) {
        List<User> users = userService.listAllUsers();

        model.addAttribute("users", users);

        return "admin/users";
    }
}