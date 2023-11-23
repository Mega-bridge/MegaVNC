package kr.co.megabridge.megavnc.web;

import jakarta.validation.Valid;
import kr.co.megabridge.megavnc.domain.HostPC;
import kr.co.megabridge.megavnc.dto.HostPcRequestDto;
import kr.co.megabridge.megavnc.repository.HostPcRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final HostPcRepository hostPCRepository;

    @Autowired
    public AdminController(HostPcRepository hostPCRepository) {
        this.hostPCRepository = hostPCRepository;
    }

    @GetMapping
    public String adminHome() {
        return "redirect:/admin/hosts";
    }

    @GetMapping("/hosts")
    public String showHostList(Model model, Principal principal) {
        loadHostPCs(model);

        model.addAttribute("newHostPC", new HostPcRequestDto());

        return "admin-hosts";
    }

    @PostMapping("/hosts")
    public String createHostPC(
            @ModelAttribute("newHostPC") @Valid HostPcRequestDto newHostPC,
            Errors errors,
            Model model
    ) {
        if (errors.hasErrors()) {
            loadHostPCs(model);
            return "admin-hosts";
        }

        hostPCRepository.save(newHostPC.toEntity());
        log.info("Create HostPC: " + newHostPC);

        return "redirect:/admin/hosts";
    }

    private void loadHostPCs(Model model) {
        List<HostPC> hostPCs = new ArrayList<>();
        hostPCRepository.findAll().forEach(hostPCs::add);

        model.addAttribute("hostPCs", hostPCs);
    }
}
