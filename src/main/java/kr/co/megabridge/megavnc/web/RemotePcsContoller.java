package kr.co.megabridge.megavnc.web;

import jakarta.servlet.http.HttpServletResponse;
import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.service.RemotePcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
