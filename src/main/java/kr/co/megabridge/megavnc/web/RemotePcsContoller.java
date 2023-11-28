package kr.co.megabridge.megavnc.web;

import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.service.RemotePcRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/remote-pcs")
public class RemotePcsContoller {

    RemotePcRepositoryService remotePcRepositoryService;

    @Autowired
    public RemotePcsContoller(RemotePcRepositoryService remotePcRepositoryService) {
        this.remotePcRepositoryService = remotePcRepositoryService;
    }

    @GetMapping
    public String showRemotePcs(@AuthenticationPrincipal User user, Model model) {
        Iterable<RemotePc> remotePcs = remotePcRepositoryService.findByOwner(user);

        model.addAttribute("remotePcs", remotePcs);
        model.addAttribute("user", user);

        return "remote-pcs";
    }
}
