package kr.co.megabridge.megavnc.web;

import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.service.UserRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUsersController {

    UserRepositoryService userRepositoryService;

    @Autowired
    public AdminUsersController(UserRepositoryService userRepositoryService) {
        this.userRepositoryService = userRepositoryService;
    }

    @GetMapping
    public String showUsers(Model model) {
        List<User> users = userRepositoryService.listAllUsers();

        model.addAttribute("users", users);

        return "admin/users";
    }
}
