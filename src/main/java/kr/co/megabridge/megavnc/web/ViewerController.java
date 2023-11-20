package kr.co.megabridge.megavnc.web;

import kr.co.megabridge.megavnc.domain.HostPC;
import kr.co.megabridge.megavnc.repository.HostPcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/viewer")
public class ViewerController {

    private final HostPcRepository hostPCRepository;

    @Autowired
    public ViewerController(HostPcRepository hostPCRepository) {
        this.hostPCRepository = hostPCRepository;
    }

    @GetMapping
    public String showViewer(@RequestParam Long id) {
        // Check permission

        Optional<HostPC> hostPC = hostPCRepository.findById(id);
        return "viewer";
    }
}
