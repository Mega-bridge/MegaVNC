package kr.co.megabridge.megavnc.logManagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@Slf4j
public class LogController {

    @GetMapping
    @RequestMapping("/logs")
    public String upload() {
        return "admin/logManagement/logManagement";
    }

}
