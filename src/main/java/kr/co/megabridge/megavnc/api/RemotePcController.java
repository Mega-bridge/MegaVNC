package kr.co.megabridge.megavnc.api;

import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.dto.RequestRemotePcDto;
import kr.co.megabridge.megavnc.dto.ResponseRemotePcApiDto;
import kr.co.megabridge.megavnc.service.RemotePcService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/remote-pcs")
public class RemotePcController {

    private final RemotePcService remotePcService;

    @PostMapping
    public ResponseEntity<ResponseRemotePcApiDto>  getRepeaterIdByPcName(@RequestBody RequestRemotePcDto requestRemotePcDto){

        ResponseRemotePcApiDto remotePc = remotePcService.findRemotePcByPcName(requestRemotePcDto);
        //배정 처리
        return  ResponseEntity.ok(remotePc);
    }


    @PostMapping("/cancel-assignment")
    public ResponseEntity<String> cancelAssignment(@RequestBody RequestRemotePcDto requestRemotePcDto){

        remotePcService.cancelAssignment(requestRemotePcDto);


        return ResponseEntity.ok("배정 취소됨");
    }



    @GetMapping("/{repeaterId}")
    public ResponseEntity<RemotePc> getRemotePcByRepeaterId(@PathVariable Long repeaterId) {
        Optional<RemotePc> remotePc = remotePcService.findRemotePcByRepeaterId(repeaterId);

        if (remotePc.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(remotePc.get());
    }
}
