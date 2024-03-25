package kr.co.megabridge.megavnc.web;

import jakarta.servlet.http.HttpServletResponse;
import kr.co.megabridge.megavnc.domain.Member;
import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.service.RemotePcService;
import kr.co.megabridge.megavnc.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/remote-pcs")
public class RemotePcsContoller {

    private final RemotePcService remotePcService;
    private final UserService userService;


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


        String accessPassword = remotePc.getAccessPassword();
        model.addAttribute("accessPassword",accessPassword);
        return "viewer";
    }

    @GetMapping("/download-server")
    public ResponseEntity<Resource> downloadServer() {
        Resource file = new ClassPathResource("static/bin/MegaVNC-v0.1.7.zip");
        String contentDisposition = "attachment; filename=\"" +
                UriUtils.encode(file.getFilename(), StandardCharsets.UTF_8) + "\"";
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(file);
    }

    @PostMapping("/register-pc")
    public String registerRemotePc(@AuthenticationPrincipal User user, @RequestParam String accessPassword,
                                   @RequestParam String remotePcName) {
        Optional<Member> member = userService.authUser(user.getUsername(), user.getPassword());
        if (member.isEmpty()) {
            throw new UsernameNotFoundException("유저 " + user.getUsername() + "이 존재하지 않습니다");
        }
         remotePcService.registerRemotePc(remotePcName, accessPassword, member.get());


        return "redirect:/";
    }



}
