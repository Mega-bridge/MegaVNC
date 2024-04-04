package kr.co.megabridge.megavnc.api;

import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.dto.RequestRemotePcDto;
import kr.co.megabridge.megavnc.dto.ResponseRemotePcApiDto;
import kr.co.megabridge.megavnc.dto.ResponseRepeaterStatusDto;
import kr.co.megabridge.megavnc.enums.Status;
import kr.co.megabridge.megavnc.service.RemotePcApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/remote-pcs")
public class RemotePcController {

    private final RemotePcApiService remotePcApiService;

    @PostMapping
    public ResponseEntity<ResponseRemotePcApiDto>  getRepeaterIdByPcName(@RequestBody RequestRemotePcDto requestRemotePcDto){
        ResponseRemotePcApiDto remotePc = remotePcApiService.findRemotePcByPcName(requestRemotePcDto);
        return  ResponseEntity.ok(remotePc);
    }

    @GetMapping("/{repeaterId}")
    public ResponseEntity<ResponseRepeaterStatusDto> getRemotePcStatusByRepeaterId(@PathVariable Long repeaterId) {
        RemotePc remotePc = remotePcApiService.findRemotePcByRepeaterId(repeaterId);
        ResponseRepeaterStatusDto responseRepeaterStatusDto = new ResponseRepeaterStatusDto(Status.toValue(remotePc.getStatus()));
        return ResponseEntity.ok(responseRepeaterStatusDto);
    }
}
