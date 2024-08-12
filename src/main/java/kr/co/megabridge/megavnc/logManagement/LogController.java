package kr.co.megabridge.megavnc.logManagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@Slf4j
public class LogController {

    private final ErrorLogsRepository errorLogsRepository;
    private final AccessLogsRepository accessLogsRepository;
    @GetMapping
    @RequestMapping("/logs")
    public String logs() {

        return "admin/logManagement/logManagement";
    }

    @GetMapping
    @RequestMapping("/connectLogs")
    public String connectLog(Model model) {
        model.addAttribute("accessLogs", accessLogsRepository.findAll());
        return "admin/logManagement/connectLog";
    }

    @GetMapping
    @RequestMapping("/errorLogs")
    public String upload(Model model) {
        model.addAttribute("errorLogs",errorLogsRepository.findAll() );
        return "admin/logManagement/errorLog";
    }

}
