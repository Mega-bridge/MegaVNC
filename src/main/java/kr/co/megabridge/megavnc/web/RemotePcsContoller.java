package kr.co.megabridge.megavnc.web;

import jakarta.servlet.http.HttpServletResponse;
import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.security.User;
import kr.co.megabridge.megavnc.service.RemotePcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/remote-pcs")
public class RemotePcsContoller {

    RemotePcService remotePcService;

    @Autowired
    public RemotePcsContoller(RemotePcService remotePcService) {
        this.remotePcService = remotePcService;
    }

    @GetMapping
    public String showRemotePcs(@AuthenticationPrincipal User user, Model model) {

        Iterable<RemotePc> remotePcs = remotePcService.findByOwner(user);

        model.addAttribute("remotePcs", remotePcs);
        model.addAttribute("user", user);

        return "remote-pcs";
    }

    @GetMapping("/{id}")
    public String showViewer(@PathVariable Long id, @AuthenticationPrincipal User user, Model model, HttpServletResponse response) {
        RemotePc remotePc = remotePcService.findById(id);

        if (!remotePc.getOwner().getUsername().equals(user.getUsername())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "403";
        }
        model.addAttribute("user", user);

        Long repeaterId = remotePc.getRepeaterId();
        model.addAttribute("repeaterId", repeaterId);

        String pcName = remotePc.getName();
        model.addAttribute("pcName", pcName);

        return "viewer";
    }

    @GetMapping("/download-server")
    public ResponseEntity<Resource> downloadServer() {
        Resource file = new ClassPathResource("static/bin/MegaVNC-Server-v0.1.6.zip");
        String contentDisposition = "attachment; filename=\"" +
                UriUtils.encode(file.getFilename(), StandardCharsets.UTF_8) + "\"";
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(file);
    }

    // @GetMapping("/test")
    public String testViewer(@AuthenticationPrincipal User user, Model model, HttpServletResponse response) {
        model.addAttribute("user", user);

        model.addAttribute("repeaterId", "repeaterId");

        model.addAttribute("pcName", "pcName");

        return "viewer";
    }
}
