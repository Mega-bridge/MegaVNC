package kr.co.megabridge.megavnc.filetransfer;

import kr.co.megabridge.megavnc.security.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;


@Controller
@RequiredArgsConstructor
@RequestMapping("/files")
@Slf4j
public class FileController {

    private final FileService fileService;

    @GetMapping
    @RequestMapping("/download")
    public String showFiles(@RequestParam String reconnectId, Model model) {
        model.addAttribute("files", fileService.findAllByReconnectId(reconnectId));
        return "downloadFiles";
    }

    @GetMapping
    @RequestMapping("/management")
    public String management(@AuthenticationPrincipal User user, Model model) {
        return "admin/fileManagement/fileManagement";
    }

    @GetMapping
    @RequestMapping("/upload")
    public String upload(@AuthenticationPrincipal User user, Model model) {

        return "admin/fileManagement/uploadFile";
    }

    @GetMapping
    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("files", fileService.findAllByReconnectId(null));
        return "admin/fileManagement/fileList";
    }

    @GetMapping("/delete-files/{fileSeq}")
    public String deleteDistributionFile(@PathVariable Integer fileSeq) {
        fileService.deleteDistributionFile(fileSeq);
        return "admin/fileManagement/fileList";

    }

}
