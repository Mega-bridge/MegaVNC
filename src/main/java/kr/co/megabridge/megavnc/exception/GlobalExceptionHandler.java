package kr.co.megabridge.megavnc.exception;


import kr.co.megabridge.megavnc.domain.Group;
import kr.co.megabridge.megavnc.dto.AssignGroupDto;
import kr.co.megabridge.megavnc.filetransfer.FileService;
import kr.co.megabridge.megavnc.logManagement.LogService;
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


    //예외가 호출된 클래스, 매서드, 라인 정보 반환
    private final LogService logService;

    private void saveErrorLogToDB(Throwable e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        if (stackTrace.length > 0) {
            StackTraceElement element = stackTrace[0];
            logService.saveErrorLog(e.getMessage(), element.getClassName(), element.getLineNumber());
        } else {
            logService.saveErrorLog(e.getMessage(), "N/A", -1);
        }
    }


    //RemotePc 페이지에 전달 할 Exception
    @ExceptionHandler(RemotePcException.class)
    public String handleRemotePcViewException(@AuthenticationPrincipal User user, RemotePcException e, Model model) {
        List<Group> groups = remotePcService.findGroupByMember(user);

        List<ResponseRemotePcDto> remotePcs = remotePcService.findByGroups(groups);
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("error", true);
        model.addAttribute("remotePcs", remotePcs);
        model.addAttribute("user", user);
        model.addAttribute("groups", groups);
        model.addAttribute("RegisterRemotePcDto", new RegisterRemotePcDto());


        if (e.getErrorCode().getStatus().is5xxServerError()) {
            log.error(e.getMessage());
            saveErrorLogToDB(e);
        }
        return "remote-pcs";
    }

    //RemotePc Api에 전달 할 Exception
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> ApiException(ApiException e) {

        final ErrorCode errorCode = e.getErrorCode();
        final ErrorResponse response = ErrorResponse.of(errorCode, e.getMessage());

        if (e.getErrorCode().getStatus().is5xxServerError()) {
            log.error(e.getMessage());
            saveErrorLogToDB(e);
        }
        return new ResponseEntity<>(response, errorCode.getStatus());

    }

    //어드민 유저 페이지에 전달 할 Exception
    @ExceptionHandler(AdminUserException.class)
    public String handleAdminUserException(AdminUserException e, Model model) {
        model.addAttribute("error", true);
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("users", adminUserService.listAllUsers());
        model.addAttribute("UserRegisterDto", new UserRegisterDto());

        if (e.getErrorCode().getStatus().is5xxServerError()) {
            log.error(e.getMessage());
            saveErrorLogToDB(e);
        }
        return "admin/userManagement/users";
    }

    //어드민 그룹 페이지에 전달 할 Exception
    @ExceptionHandler(AdminGroupException.class)
    public String handleAdminGroupException(AdminGroupException e, Model model) {
        model.addAttribute("error", true);
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("groups", adminGroupService.listAllGroups());
        if (e.getErrorCode().getStatus().is5xxServerError()) {
            log.error(e.getMessage());
            saveErrorLogToDB(e);
        }
        return "admin/groupManagement/groups";

    }

    //어드민 assign 페이지에 전달 할 Exception
    @ExceptionHandler(AdminAssignException.class)
    public String handleAdminAssignException(AdminAssignException e, Model model) {
        model.addAttribute("error", true);
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("user", adminAssignService.findByUserId(e.getMemberId()));
        model.addAttribute("assignedGroups", adminAssignService.listAssignedGroups(e.getMemberId()));
        model.addAttribute("unassignedGroups", adminAssignService.listUnassignedGroups(e.getMemberId()));
        model.addAttribute("AssignGroupDto", new AssignGroupDto());

        if (e.getErrorCode().getStatus().is5xxServerError()) {
            log.error(e.getMessage());
            saveErrorLogToDB(e);
        }
        return "admin/userManagement/assign";
    }

    //어드민 assign 페이지에 전달 할 Exception
    @ExceptionHandler(FileDownloadException.class)
    public String handleFileDownloadException(FileDownloadException e) {

        if (e.getErrorCode().getStatus().is5xxServerError()) {
            log.error(e.getMessage());
            saveErrorLogToDB(e);
        }
        return "404";
    }

    //어드민 assign 페이지에 전달 할 Exception
    @ExceptionHandler(FileDeleteException.class)
    public String handleFileDeleteException(FileDeleteException e, Model model) {
        model.addAttribute("error", true);
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("files", fileService.findAllByReconnectId(null));

        if (e.getErrorCode().getStatus().is5xxServerError()) {
            log.error(e.getMessage());
            saveErrorLogToDB(e);
        }
        return "admin/fileManagement/fileList";
    }


    /**
     * 예외처리 안된 에러
     **/
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e) {
        log.error(e.getMessage());
        saveErrorLogToDB(e);
        return "500";
    }


}
