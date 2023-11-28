package kr.co.megabridge.megavnc.api;

import kr.co.megabridge.megavnc.domain.JwtToken;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtToken> login(@RequestBody Map<String, String> loginForm) {
        JwtToken token = userService.getJwtToken(loginForm.get("username"), loginForm.get("password"));
        return ResponseEntity.ok(token);
    }

    @GetMapping("/check")
    public ResponseEntity<String> check(Principal principal) {
        String username = principal.getName();
        return ResponseEntity.ok("Hello, " + username + "!");
    }
}
