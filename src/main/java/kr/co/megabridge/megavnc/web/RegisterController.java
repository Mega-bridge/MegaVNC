package kr.co.megabridge.megavnc.web;

import jakarta.validation.Valid;
import kr.co.megabridge.megavnc.dto.UserRegisterDto;
import kr.co.megabridge.megavnc.service.UserRegisterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/register")
public class RegisterController {

    private final UserRegisterService userRegisterService;

    @Autowired
    public RegisterController(UserRegisterService userRegisterService) {
        this.userRegisterService = userRegisterService;
    }

    @GetMapping
    public String showRegister(Model model) {
        model.addAttribute("user", new UserRegisterDto());
        return "register";
    }

    @PostMapping
    public String processRegister(@ModelAttribute("user") @Valid UserRegisterDto user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (!user.getPassword().equals(user.getPasswordConfirm())) {
            bindingResult.rejectValue(
                    "passwordConfirm",
                    "error.user",
                    "비밀번호 확인이 일치하지 않습니다.");
            return "register";
        }

        if (!userRegisterService.isUsernameUnique(user.getUsername())) {
            bindingResult.rejectValue(
                    "username",
                    "error.user",
                    "이미 존재하는 사용자명입니다.");
            return "register";
        }

        userRegisterService.register(user.getUsername(), user.getPassword());

        return "redirect:/login";
    }
}
