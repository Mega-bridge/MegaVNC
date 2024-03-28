package kr.co.megabridge.megavnc.web.auth;

import jakarta.validation.Valid;
import kr.co.megabridge.megavnc.dto.UserRegisterDto;
import kr.co.megabridge.megavnc.service.UserService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegisterController {

    private final UserService userService;

    @GetMapping
    public String showRegister(Model model) {
        model.addAttribute("user", new UserRegisterDto());
        return "auth/register";
    }

    @PostMapping
    public String processRegister(@ModelAttribute("user") @Valid UserRegisterDto user, BindingResult bindingResult, Model model) {
        model.addAttribute("username", user.getUsername());
        model.addAttribute("password", user.getPassword());
        model.addAttribute("passwordConfirm", user.getPasswordConfirm());
        if (!userService.isUsernameUnique(user.getUsername())) {
            bindingResult.rejectValue(
                    "username",
                    "error.user",
                    "이미 존재하는 사용자명입니다");
            model.addAttribute("username",null);
        }

        if (!user.getPassword().equals(user.getPasswordConfirm())) {
            bindingResult.rejectValue(
                    "passwordConfirm",
                    "error.user",
                    "비밀번호 확인이 일치하지 않습니다");
             model.addAttribute("passwordConfirm",null);
        }


        if (bindingResult.hasErrors()) {
            model.addAttribute("groups", userService.findAllGroup());
            return "auth/register";
        }

        userService.register(user.getUsername(), user.getPassword());

        return "redirect:/login?registered";
    }
}
