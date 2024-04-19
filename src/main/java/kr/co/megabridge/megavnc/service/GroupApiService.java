package kr.co.megabridge.megavnc.service;


import kr.co.megabridge.megavnc.domain.Group;
import kr.co.megabridge.megavnc.dto.ResponseGroupApiDto;
import kr.co.megabridge.megavnc.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupApiService {

    private final GroupRepository groupRepository;

    public List<ResponseGroupApiDto> findAllGroups(){
        List<Group> groups = groupRepository.findAllStartWith(2L);
        List<ResponseGroupApiDto> responses = new ArrayList<>();
        for( Group group : groups){
            responses.add(new ResponseGroupApiDto(group.getId(), group.getGroupName()));
        }
        return responses;
    }

}