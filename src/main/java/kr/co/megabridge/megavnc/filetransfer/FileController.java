package kr.co.megabridge.megavnc.filetransfer;

import kr.co.megabridge.megavnc.security.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;


@Controller
@RequiredArgsConstructor
@RequestMapping("/files")
@Slf4j
public class FileController {

    private final FileService fileService;

    @GetMapping
    @RequestMapping("/download")
    public String showFiles(@RequestParam(required = false) String reconnectId, Model model) {
        if (reconnectId == null || reconnectId.trim().isEmpty()) {
            return "403";
        }else{
            model.addAttribute("files", fileService.findAllByReconnectId(reconnectId));
            return "downloadFiles";
        }
    }

    @GetMapping
    @RequestMapping("/management")
    public String management() {
        return "admin/fileManagement/fileManagement";
    }

    @GetMapping
    @RequestMapping("/upload")
    public String upload() {
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
        return "redirect:/files/list";

    }

    @GetMapping("/download-files/{fileSeq}")
    public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable Integer fileSeq) {
        return fileService.downloadFile(fileSeq);
    }


}
