package kr.co.megabridge.megavnc.service;

import jakarta.transaction.Transactional;
import kr.co.megabridge.megavnc.domain.Group;
import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.dto.RequestRemotePcDto;
import kr.co.megabridge.megavnc.dto.ResponseRemotePcApiDto;
import kr.co.megabridge.megavnc.exception.ErrorCode;
import kr.co.megabridge.megavnc.exception.exceptions.ApiException;
import kr.co.megabridge.megavnc.repository.GroupRepository;
import kr.co.megabridge.megavnc.repository.RemotePcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RemotePcApiService {

    private final RemotePcRepository remotePcRepository;
    private final GroupRepository groupRepository;



    @Transactional
    public ResponseRemotePcApiDto connectSettingRepeater(RequestRemotePcDto remotePcDto){



        Optional<Group> optionalGroup = groupRepository.findByGroupName(remotePcDto.getGroupName());
        Group group = optionalGroup.orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));

        List<RemotePc> remotePcs = remotePcRepository.findByName(remotePcDto.getPcName());
        if(remotePcs.isEmpty()){
            //진짜 Pc 자체가 없을 때
            throw new ApiException(ErrorCode.PC_NOT_FOUND);
        }
        RemotePc response = null;
        for (RemotePc remotePc : remotePcs){
            if(remotePc.getGroup().equals(group)){
                response = remotePc;
                break;
            }
        }
        if (response == null){
            //remotePcs 에는 존재 하지만 선택한 그룹에 속하지는 않을 때
            throw new ApiException(ErrorCode.PC_NOT_FOUND,"그룹을 확인해 주세요");
        }

        if (response.getAssignedAt() != null && !response.getReconnectId().equals(remotePcDto.getReconnectId())){
            throw new ApiException(ErrorCode.ALREADY_ASSIGNED_PC);
        }

        if (!remotePcDto.getAccessPassword().equals(response.getAccessPassword()))
            throw new ApiException(ErrorCode.PASSWORD_NOT_MATCH);

        if(!remotePcDto.getReconnectId().equals("default")) {
            response.updateReconnectId(remotePcDto.getReconnectId());
        }

        return new ResponseRemotePcApiDto(response.getRepeaterId());
    }


    @Transactional
    public void disAssignRemotePcByRepeaterId(String reconnectId){
        Optional<RemotePc> optionalRemotePc = remotePcRepository.findByReconnectId(reconnectId);
        RemotePc remotePc = optionalRemotePc.orElseThrow(() -> new ApiException(ErrorCode.CANNOT_DISASSIGNED_PC));
        remotePc.disAssign();

    }



    public RemotePc findRemotePcByRepeaterId(Long repeaterId) {
        Optional<RemotePc> optionalRemotePc = remotePcRepository.findByRepeaterId(repeaterId);

        return optionalRemotePc.orElseThrow(() -> new ApiException(ErrorCode.PC_NOT_FOUND));
    }
}
