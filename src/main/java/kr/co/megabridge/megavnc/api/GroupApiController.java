package kr.co.megabridge.megavnc.api;

import kr.co.megabridge.megavnc.dto.ResponseGroupApiDto;
import kr.co.megabridge.megavnc.service.GroupApiService;
import kr.co.megabridge.megavnc.web.admin.AdminGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups")
public class GroupApiController {

    private final GroupApiService groupApiService;
    private final AdminGroupService adminGroupService;

    @PostMapping("/create")
    public ResponseEntity<String> createUser(@RequestBody ResponseGroupApiDto responseGroupApiDto, Model model ){
        adminGroupService.register(responseGroupApiDto);
        return ResponseEntity.ok("ok");
    }

    @GetMapping
    public ResponseEntity<List<ResponseGroupApiDto>> getRepeaterIdByPcName(){
        List<ResponseGroupApiDto> responses = groupApiService.findAllGroups();
        return  ResponseEntity.ok(responses);
    }


}
