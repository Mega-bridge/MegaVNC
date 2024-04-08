package kr.co.megabridge.megavnc.exception;



import jakarta.servlet.http.HttpServletRequest;
import kr.co.megabridge.megavnc.domain.Group;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.dto.AssignGroupDto;
import kr.co.megabridge.megavnc.dto.RegisterRemotePcDto;
import kr.co.megabridge.megavnc.dto.ResponseRemotePcDto;
import kr.co.megabridge.megavnc.dto.UserRegisterDto;
import kr.co.megabridge.megavnc.exception.exceptions.*;
import kr.co.megabridge.megavnc.service.AdminAssignService;
import kr.co.megabridge.megavnc.service.AdminGroupService;
import kr.co.megabridge.megavnc.service.AdminUserService;
import kr.co.megabridge.megavnc.service.RemotePcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.HandlerMapping;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final RemotePcService remotePcService;
    private final AdminUserService adminUserService;
    private final AdminGroupService adminGroupService;
    private final AdminAssignService adminAssignService;



    @ExceptionHandler(RemotePcException.class)
    protected String handleRemotePcViewException(@AuthenticationPrincipal User user, RemotePcException e , Model model) {
        List<Group> groups = remotePcService.findGroupByMember(user);

        List<ResponseRemotePcDto> remotePcs = remotePcService.findByGroups(groups);
        model.addAttribute("errorMessage",  e.getMessage());
        model.addAttribute("error", true);
        model.addAttribute("remotePcs", remotePcs);
        model.addAttribute("user", user);
        model.addAttribute("groups", groups);
        model.addAttribute("RegisterRemotePcDto",new RegisterRemotePcDto());
        return "remote-pcs";
    }
    @ExceptionHandler(RemotePcApiException.class)
    protected ResponseEntity<ErrorResponse> RemotePcApiException(RemotePcApiException e) {
        log.error("RemotePcApiException", e);
        final ErrorCode errorCode = e.getErrorCode();
        final ErrorResponse response = ErrorResponse.of(errorCode, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.valueOf(errorCode.getStatus()));
    }

    @ExceptionHandler(AdminUserException.class)
    protected String handleAdminUserException( AdminUserException e , Model model) {
        model.addAttribute("error", true);
        model.addAttribute("errorMessage",  e.getMessage());
        model.addAttribute("users", adminUserService.listAllUsers());
        model.addAttribute("UserRegisterDto", new UserRegisterDto());
        model.addAttribute("AssignUserDto", new AssignGroupDto());
        model.addAttribute("groups", adminUserService.findAllGroups());
        return "admin/userManagement/users";
    }
    @ExceptionHandler(AdminGroupException.class)
    protected String handleAdminGroupException( AdminGroupException e , Model model) {
        model.addAttribute("error", true);
        model.addAttribute("errorMessage",  e.getMessage());
        model.addAttribute("groups", adminGroupService.listAllGroups());
        return "admin/groupManagement/groups";

    }








}
