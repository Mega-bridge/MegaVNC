package kr.co.megabridge.megavnc.web.admin;

import jakarta.validation.Valid;
import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.dto.RemotePcRegisterDto;
import kr.co.megabridge.megavnc.repository.RemotePcRepository;
import kr.co.megabridge.megavnc.service.RemotePcRepositoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/remote-pcs")
public class AdminRemotePcsController {

    private final RemotePcRepositoryService remotePcRepositoryService;

    @Autowired
    public AdminRemotePcsController(RemotePcRepositoryService remotePcRepositoryService) {
        this.remotePcRepositoryService = remotePcRepositoryService;
    }

    @GetMapping
    public String showHostList(Model model) {
        loadRemotePcs(model);

        model.addAttribute("newRemotePc", new RemotePcRegisterDto());

        return "admin/remote-pcs";
    }

    @PostMapping
    public String createRemotePc(
            @ModelAttribute("newRemotePc") @Valid RemotePcRegisterDto newRemotePc,
            Errors errors,
            Model model,
            @AuthenticationPrincipal User user
    ) {
        if (errors.hasErrors()) {
            loadRemotePcs(model);
            return "admin/remote-pcs";
        }

        // remotePCRepository.save(RemotePc.createRemotePc(newRemotePc.getName(), user));

        return "redirect:/admin/remote-pcs";
    }

    private void loadRemotePcs(Model model) {
        List<RemotePc> remotePcs = new ArrayList<>();
        remotePcRepositoryService.findAll().forEach(remotePcs::add);

        model.addAttribute("remotePcs", remotePcs);
    }
}
