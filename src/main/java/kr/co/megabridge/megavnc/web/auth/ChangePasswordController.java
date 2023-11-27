package kr.co.megabridge.megavnc.web.auth;

import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.dto.ChangePasswordDto;
import kr.co.megabridge.megavnc.service.UserRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/change-password")
public class ChangePasswordController {

    private final PasswordEncoder encoder;
    private final UserRepositoryService userRepositoryService;

    @Autowired
    public ChangePasswordController(PasswordEncoder encoder, UserRepositoryService userRepositoryService) {
        this.encoder = encoder;
        this.userRepositoryService = userRepositoryService;
    }

    @GetMapping
    public String showChangePassword() {
        return "auth/change-password";
    }

    @PostMapping
    public String processChangePassword(
            ChangePasswordDto changePassword,
            Model model,
            @AuthenticationPrincipal User user
    ) {
        if (!encoder.matches(changePassword.getPassword(), user.getPassword())) {
            model.addAttribute("passwordError", true);
        }

        if (!changePassword.getNewPassword().equals(changePassword.getNewPasswordConfirm())) {
            model.addAttribute("passwordConfirmError", true);
        }

        if (
                model.containsAttribute("passwordError") ||
                model.containsAttribute("passwordConfirmError")
        ) {
            return "auth/change-password";
        }

        userRepositoryService.changePassword(user, changePassword.getNewPassword());

        return "redirect:/login";
    }
}
