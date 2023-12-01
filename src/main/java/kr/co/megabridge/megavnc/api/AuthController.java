package kr.co.megabridge.megavnc.api;

import kr.co.megabridge.megavnc.domain.JwtToken;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

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

    @PostMapping("/check")
    public ResponseEntity<String> check(@RequestBody Map<String, String> loginForm) {
        Optional<User> user = userService.authUser(loginForm.get("username"), loginForm.get("password"));

        if (user.isEmpty())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Wrong username or password");

        return ResponseEntity.ok("Success");
    }
}
