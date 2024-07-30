package kr.co.megabridge.megavnc.api;

import kr.co.megabridge.megavnc.dto.*;
import kr.co.megabridge.megavnc.web.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UsersApiController {

    private final AdminUserService adminUserService;

    @PostMapping("/regist")
    public ResponseEntity<String> createUser(@RequestBody UserRegisterDto user, Model model ){
        adminUserService.register(user);
        return ResponseEntity.ok("ok");
    }


}
