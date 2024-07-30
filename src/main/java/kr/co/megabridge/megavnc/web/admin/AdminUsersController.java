package kr.co.megabridge.megavnc.web.admin;

import jakarta.validation.Valid;
import kr.co.megabridge.megavnc.domain.Member;

import kr.co.megabridge.megavnc.dto.AssignGroupDto;
import kr.co.megabridge.megavnc.dto.UserRegisterDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUsersController {

    private final AdminUserService adminUserService;



    @GetMapping
    public String showUsers(Model model) {
        List<Member> users = adminUserService.listAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("UserRegisterDto", new UserRegisterDto());


        return "admin/userManagement/users";
    }

    @GetMapping("/delete/{memberId}")
    public String deleteUser(@PathVariable Long memberId){
        adminUserService.deleteUser(memberId);
        return "redirect:/admin/users";
    }
}
