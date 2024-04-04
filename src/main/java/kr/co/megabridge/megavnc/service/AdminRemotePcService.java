package kr.co.megabridge.megavnc.service;

import jakarta.transaction.Transactional;
import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.enums.ErrorCode;
import kr.co.megabridge.megavnc.enums.Status;
import kr.co.megabridge.megavnc.exception.RemotePcException;
import kr.co.megabridge.megavnc.repository.RemotePcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminRemotePcService {

    private final RemotePcRepository remotePcRepository;


    public Iterable<RemotePc> findAllPcs(){
        return remotePcRepository.findAll();
    }
    @Transactional
    public void deletePc(Long remotePcId){

        Optional<RemotePc> optionalRemotePc =  remotePcRepository.findById(remotePcId);
        RemotePc remotePc = optionalRemotePc.orElseThrow(() -> new RemotePcException(ErrorCode.PC_NOT_FOUND," 페이지를 새로고침 해주세요."));
        if(remotePc.getStatus() == Status.ACTIVE){
            throw new RemotePcException(ErrorCode.DELETE_NOT_ONLY_WHEN_ACTIVE,"페이지를 새로고침 해주세요.");
        }

        remotePcRepository.deleteById(remotePcId);

    }

}
