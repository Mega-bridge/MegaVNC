package kr.co.megabridge.megavnc.web;


import kr.co.megabridge.megavnc.domain.Group;
import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.dto.RegisterRemotePcDto;
import kr.co.megabridge.megavnc.dto.ResponseRemotePcDto;
import kr.co.megabridge.megavnc.dto.ResponseDeleteDto;
import kr.co.megabridge.megavnc.service.RemotePcService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/remote-pcs")
public class RemotePcsContoller {

    private final RemotePcService remotePcService;


    @GetMapping
    public String showRemotePcs(@AuthenticationPrincipal User user, @RequestParam(value = "selectedGroup", required = false) String selectedGroupName, Model model) {
        List<Group> groups = remotePcService.findGroupByMember(user);

        List<ResponseRemotePcDto> remotePcs = new ArrayList<>();
        if (selectedGroupName == null || selectedGroupName.equals("All Group")) {
            remotePcs.addAll(remotePcService.findByGroups(groups));

        } else {
            remotePcs.addAll(remotePcService.findByGroupName(selectedGroupName, user));
        }

        model.addAttribute("error", false);
        model.addAttribute("remotePcs", remotePcs);
        model.addAttribute("user", user);
        model.addAttribute("groups", groups);
        model.addAttribute("selectedGroup", selectedGroupName);
        model.addAttribute("RegisterRemotePcDto",new RegisterRemotePcDto());



        return "remote-pcs";
    }



    @GetMapping("/{id}")
    public String showViewer(@AuthenticationPrincipal User user, @PathVariable Long id, Model model) {
        RemotePc remotePc = remotePcService.findById(user, id);

        model.addAttribute("status",remotePc.getStatus());

        Long repeaterId = remotePc.getRepeaterId();
        model.addAttribute("repeaterId", repeaterId);

        String pcName = remotePc.getName();
        model.addAttribute("pcName", pcName);


        String accessPassword = remotePc.getAccessPassword();
        model.addAttribute("accessPassword", accessPassword);

        model.addAttribute("user", user);


        return "viewer";
    }

    @GetMapping("/download-server")
    public ResponseEntity<Resource> downloadServer() {
        Resource file = new ClassPathResource("static/bin/MegaVNC-v0.2.0.zip");
        String contentDisposition = "attachment; filename=\"" +
                UriUtils.encode(file.getFilename(), StandardCharsets.UTF_8) + "\"";
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(file);
    }
    @PostMapping("/register-pc")
    public String registerRemotePc(RegisterRemotePcDto registerRemotePcDto) {

        String encodedGroupName = UriUtils.encode(registerRemotePcDto.getGroupName(), StandardCharsets.UTF_8);
        remotePcService.registerRemotePc(registerRemotePcDto);

        return "redirect:/remote-pcs?selectedGroup=" + encodedGroupName;
    }

    //원래 삭제요청을 get방식으로 하는 것은 바람직 하지 않다. 그러나 html에서 폼을 폼안에 넣는게 안된다고 해서 일단 이렇게 했음//다른 방법을 찾아 봐야 함
    @GetMapping("/delete/{id}")
    public String deleteRemotePc(@AuthenticationPrincipal User user, @PathVariable Long id) {
        //user로 그룹 조회해서 권한검사
        ResponseDeleteDto responseDto = remotePcService.deletePc(user, id);
        String encodedGroupName = UriUtils.encode(responseDto.getGroupName(), StandardCharsets.UTF_8);

        return "redirect:/remote-pcs?selectedGroup=" + encodedGroupName;
    }

}
