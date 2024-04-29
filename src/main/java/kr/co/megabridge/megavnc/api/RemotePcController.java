package kr.co.megabridge.megavnc.api;

import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.dto.RequestRemotePcDto;
import kr.co.megabridge.megavnc.dto.ResponseRemotePcApiDto;
import kr.co.megabridge.megavnc.dto.ResponseRepeaterStatusDto;
import kr.co.megabridge.megavnc.enums.Status;
import kr.co.megabridge.megavnc.service.RemotePcApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/remote-pcs")
@Slf4j
public class RemotePcController {

    private final RemotePcApiService remotePcApiService;

    @PostMapping
    public ResponseEntity<ResponseRemotePcApiDto> connectSettingRepeater(@RequestBody RequestRemotePcDto requestRemotePcDto){
        ResponseRemotePcApiDto remotePc = remotePcApiService.connectSettingRepeater(requestRemotePcDto);
        return  ResponseEntity.ok(remotePc);
    }

    @GetMapping("/{repeaterId}")
    public ResponseEntity<ResponseRepeaterStatusDto> getRemotePcStatusByRepeaterId(@PathVariable Long repeaterId) {
        RemotePc remotePc = remotePcApiService.findRemotePcByRepeaterId(repeaterId);
        ResponseRepeaterStatusDto responseRepeaterStatusDto = new ResponseRepeaterStatusDto(Status.toValue(remotePc.getStatus()));
        return ResponseEntity.ok(responseRepeaterStatusDto);
    }
    @DeleteMapping("/{reconnectId}")
    public ResponseEntity<String> disAssignRemotePc(@PathVariable String reconnectId){

        log.info("@PathVariable  reconnectId = {} " , reconnectId);
        remotePcApiService.disAssignRemotePcByRepeaterId(reconnectId);
        return ResponseEntity.ok("ok");
    }
}
