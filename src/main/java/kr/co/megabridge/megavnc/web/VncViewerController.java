package kr.co.megabridge.megavnc.web;

import jakarta.servlet.http.HttpServletResponse;
import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.repository.RemotePcRepository;
import kr.co.megabridge.megavnc.service.RemotePcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/viewer")
public class VncViewerController {

    private final RemotePcService remotePcService;

    @Autowired
    public VncViewerController(RemotePcService remotePcService) {
        this.remotePcService = remotePcService;
    }

    @GetMapping("/{id}")
    public String showViewer(@PathVariable Long id, @AuthenticationPrincipal User user, Model model, HttpServletResponse response) {
        RemotePc remotePc = remotePcService.findById(id);

        if (!remotePc.getOwner().getUsername().equals(user.getUsername())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "403";
        }

        Long repeaterId = remotePc.getRepeaterId();
        model.addAttribute("repeaterId", repeaterId);

        return "viewer";
    }
}
