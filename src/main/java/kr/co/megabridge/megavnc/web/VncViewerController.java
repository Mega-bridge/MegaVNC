package kr.co.megabridge.megavnc.web;

import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.repository.RemotePcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/viewer")
public class VncViewerController {

    private final RemotePcRepository remotePCRepository;

    @Autowired
    public VncViewerController(RemotePcRepository remotePCRepository) {
        this.remotePCRepository = remotePCRepository;
    }

    @GetMapping
    public String showViewer(@RequestParam Long id) {
        // Check permission

        Optional<RemotePc> hostPC = remotePCRepository.findById(id);
        return "viewer";
    }
}
