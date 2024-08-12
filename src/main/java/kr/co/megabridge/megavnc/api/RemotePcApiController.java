package kr.co.megabridge.megavnc.api;

import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.dto.requests.RegisterRemotePcDto;
import kr.co.megabridge.megavnc.dto.requests.RequestRemotePcDto;
import kr.co.megabridge.megavnc.dto.responses.ResponseRemotePcApiDto;
import kr.co.megabridge.megavnc.dto.responses.ResponseRepeaterStatusDto;
import kr.co.megabridge.megavnc.enums.Status;
import kr.co.megabridge.megavnc.service.RemotePcApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/remote-pcs")
public class RemotePcApiController {

    private final RemotePcApiService remotePcApiService;
    //앱
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
        remotePcApiService.disAssignRemotePcByRepeaterId(reconnectId);
        return ResponseEntity.ok("ok");
    }

    //웹
    @PostMapping("/register-pc")
    public ResponseEntity<String> registerRemotePc(@RequestBody RegisterRemotePcDto registerRemotePcDto) {

        remotePcApiService.registerRemotePc(registerRemotePcDto);

        return ResponseEntity.ok("ok");
    }


}
