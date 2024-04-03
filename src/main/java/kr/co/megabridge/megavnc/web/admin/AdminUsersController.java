package kr.co.megabridge.megavnc.web.admin;

import kr.co.megabridge.megavnc.domain.Member;
import kr.co.megabridge.megavnc.service.AdminUserService;
import kr.co.megabridge.megavnc.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUsersController {

    AdminUserService adminUserService;



    @GetMapping
    public String showUsers(Model model) {
        List<Member> users = adminUserService.listAllUsers();

        model.addAttribute("users", users);

        return "admin/users";
    }
}
