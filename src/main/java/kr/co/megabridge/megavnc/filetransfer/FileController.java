package kr.co.megabridge.megavnc.filetransfer;

import kr.co.megabridge.megavnc.domain.Group;
import kr.co.megabridge.megavnc.security.User;
import kr.co.megabridge.megavnc.service.RemotePcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;

import java.util.List;


@Controller
@RequiredArgsConstructor
@RequestMapping("/files")
@Slf4j
public class FileController {

    private final RemotePcService remotePcService;

    @GetMapping
    @RequestMapping("/download")
    public String showFiles(@AuthenticationPrincipal User user, Model model) {
//        List<Group> groups = remotePcService.findGroupByMember(user);
//        log.info("username = {}", user.getUsername());
//        model.addAttribute("user", user);
//        model.addAttribute("groups", groups);
        return "downloadFiles";
    }

    @GetMapping
    @RequestMapping("/management")
    public String management(@AuthenticationPrincipal User user, Model model) {
        return "/admin/fileManagement/fileManagement";
    }

    @GetMapping
    @RequestMapping("/upload")
    public String upload(@AuthenticationPrincipal User user, Model model) {
        return "/admin/fileManagement/uploadFile";
    }

    @GetMapping
    @RequestMapping("/list")
    public String list(@AuthenticationPrincipal User user, Model model) {
        return "/admin/fileManagement/fileList";
    }

}
