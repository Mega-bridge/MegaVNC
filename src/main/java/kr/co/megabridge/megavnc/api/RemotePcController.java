package kr.co.megabridge.megavnc.api;

import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.dto.RemotePcRegisterApiDto;
import kr.co.megabridge.megavnc.service.RemotePcService;
import kr.co.megabridge.megavnc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/api/remote-pcs")
public class RemotePcController {

    private final RemotePcService remotePcService;
    private final UserService userService;

    @Autowired
    public RemotePcController(RemotePcService remotePcService, UserService userService) {
        this.remotePcService = remotePcService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> registerRemotePc(@RequestBody RemotePcRegisterApiDto register) {
        Optional<User> user = userService.authUser(register.getUsername(), register.getPassword());

        if (user.isEmpty())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);

        Long repeaterId = remotePcService.registerRemotePc(register.getRemotePcName(), user.get());

        Map<String, Object> response = new HashMap<>();
        response.put("repeaterId", repeaterId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{repeaterId}")
    public ResponseEntity<RemotePc> getRemotePcByRepeaterId(@PathVariable Long repeaterId) {
        // TODO: DTO 사용하도록 변경
        Optional<RemotePc> remotePc = remotePcService.findRemotePcByRepeaterId(repeaterId);

        if (remotePc.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(remotePc.get());
    }
}
