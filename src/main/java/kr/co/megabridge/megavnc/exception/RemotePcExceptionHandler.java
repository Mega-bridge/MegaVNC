package kr.co.megabridge.megavnc.exception;


import kr.co.megabridge.megavnc.domain.Group;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.dto.RegisterRemotePcDto;
import kr.co.megabridge.megavnc.dto.ResponseRemotePcDto;
import kr.co.megabridge.megavnc.service.RemotePcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class RemotePcExceptionHandler {
    private final RemotePcService remotePcService;



    @ExceptionHandler(RemotePcException.class)
    protected String handleRemotePcViewException(@AuthenticationPrincipal User user, RemotePcException e , Model model) {
        List<Group> groups = remotePcService.findGroupByMember(user);

        List<ResponseRemotePcDto> remotePcs = new ArrayList<>();
        model.addAttribute("errorMessage",  e.getMessage());
        model.addAttribute("error", true);
        model.addAttribute("remotePcs", remotePcs);
        model.addAttribute("user", user);
        model.addAttribute("groups", groups);
        model.addAttribute("RegisterRemotePcDto",new RegisterRemotePcDto());
        return "remote-pcs";
    }


}
