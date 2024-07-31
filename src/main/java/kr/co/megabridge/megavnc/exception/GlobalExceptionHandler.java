package kr.co.megabridge.megavnc.exception;



import kr.co.megabridge.megavnc.domain.Group;
import kr.co.megabridge.megavnc.dto.AssignGroupDto;
import kr.co.megabridge.megavnc.filetransfer.FileService;
import kr.co.megabridge.megavnc.security.User;
import kr.co.megabridge.megavnc.dto.RegisterRemotePcDto;
import kr.co.megabridge.megavnc.dto.ResponseRemotePcDto;
import kr.co.megabridge.megavnc.dto.UserRegisterDto;
import kr.co.megabridge.megavnc.exception.exceptions.*;
import kr.co.megabridge.megavnc.web.admin.AdminAssignService;
import kr.co.megabridge.megavnc.web.admin.AdminGroupService;
import kr.co.megabridge.megavnc.web.admin.AdminUserService;
import kr.co.megabridge.megavnc.service.RemotePcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final RemotePcService remotePcService;
    private final AdminUserService adminUserService;
    private final AdminGroupService adminGroupService;
    private final AdminAssignService adminAssignService;
    private final FileService fileService;



//RemotePc 페이지에 전달 할 Exception
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
    //RemotePc Api에 전달 할 Exception
    @ExceptionHandler(ApiException.class)
    protected ResponseEntity<ErrorResponse> ApiException(ApiException e) {

        final ErrorCode errorCode = e.getErrorCode();
        final ErrorResponse response = ErrorResponse.of(errorCode, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.valueOf(errorCode.getStatus()));
    }

    //어드민 유저 페이지에 전달 할 Exception
    @ExceptionHandler(AdminUserException.class)
    protected String handleAdminUserException( AdminUserException e , Model model) {
        model.addAttribute("error", true);
        model.addAttribute("errorMessage",  e.getMessage());
        model.addAttribute("users", adminUserService.listAllUsers());
        model.addAttribute("UserRegisterDto", new UserRegisterDto());

        return "admin/userManagement/users";
    }

    //어드민 그룹 페이지에 전달 할 Exception
    @ExceptionHandler(AdminGroupException.class)
    protected String handleAdminGroupException( AdminGroupException e , Model model) {
        model.addAttribute("error", true);
        model.addAttribute("errorMessage",  e.getMessage());
        model.addAttribute("groups", adminGroupService.listAllGroups());
        return "admin/groupManagement/groups";

    }


    //어드민 assign 페이지에 전달 할 Exception
    @ExceptionHandler(AdminAssignException.class)
    protected String handleAdminAssignException( AdminAssignException e , Model model){
        model.addAttribute("error", true);
        model.addAttribute("errorMessage",  e.getMessage());
        model.addAttribute("user",adminAssignService.findByUserId(e.getMemberId()));
        model.addAttribute("assignedGroups", adminAssignService.listAssignedGroups(e.getMemberId()));
        model.addAttribute("unassignedGroups", adminAssignService.listUnassignedGroups(e.getMemberId()));
        model.addAttribute("AssignGroupDto", new AssignGroupDto());
        return "admin/userManagement/assign";
    }
    //어드민 assign 페이지에 전달 할 Exception
    @ExceptionHandler(FileDownloadException.class)
    protected String handleFileDownloadException(){
        return "404";
    }
    //어드민 assign 페이지에 전달 할 Exception
    @ExceptionHandler(FileDeleteException.class)
    protected String handleFileDeleteException( FileDeleteException e , Model model){
        model.addAttribute("error", true);
        model.addAttribute("errorMessage",  e.getMessage());
        model.addAttribute("files", fileService.findAllByReconnectId(null));

        return "admin/fileManagement/fileList";
    }





}
