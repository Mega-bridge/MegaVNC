package kr.co.megabridge.megavnc.web;

import jakarta.validation.Valid;
import kr.co.megabridge.megavnc.domain.HostPC;
import kr.co.megabridge.megavnc.dto.HostPCRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/hosts")
    public String showHostList(Model model) {
        loadData(model);

        model.addAttribute("newHostPC", new HostPC());

        return "admin-hosts";
    }

    @PostMapping("/hosts")
    public String createHostPC(
            @ModelAttribute("newHostPC") @Valid HostPCRequestDTO newHostPC, Errors errors, Model model
    ) {
        if (errors.hasErrors()) {
            loadData(model);
            return "admin-hosts";
        }

        log.info("Create HostPC: " + newHostPC);
        return "redirect:/admin/hosts";
    }

    private void loadData(Model model) {
        List<HostPCRequestDTO> hostPCs = Arrays.asList(
                new HostPCRequestDTO("PC1", "11.11.11.11", "5700"),
                new HostPCRequestDTO("PC2", "22.22.22.22", "5700"),
                new HostPCRequestDTO("PC3", "33.33.33.33", "5700"),
                new HostPCRequestDTO("PC4", "44.44.44.44", "5700"),
                new HostPCRequestDTO("PC5", "55.55.55.55", "5700")
        );

        model.addAttribute("hostPCs", hostPCs);
    }
}
