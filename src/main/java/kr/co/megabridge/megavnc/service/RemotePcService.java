package kr.co.megabridge.megavnc.service;

import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.repository.RemotePcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RemotePcService {

    private final RemotePcRepository repository;

    @Autowired
    public RemotePcService(RemotePcRepository repository) {
        this.repository = repository;
    }

    public Iterable<RemotePc> findAll() {
        return repository.findAll();
    }

    public Iterable<RemotePc> findByOwner(User user) {
        return repository.findByOwner(user);
    }

    public String registerRemotePc(String remotePcName, User owner) {
        Optional<RemotePc> currTop = repository.findTopByOrderByRepeaterIdDesc();

        long nextRepeaterId = 0L;
        if (currTop.isPresent())
            nextRepeaterId = Long.parseLong(currTop.get().getRepeaterId()) + 1;

        String nextRepeaterIdStr = String.format("%09d", nextRepeaterId);

        RemotePc remotePc = RemotePc.createRemotePc(nextRepeaterIdStr, remotePcName, owner);
        repository.save(remotePc);

        return nextRepeaterIdStr;
    }

    public Optional<RemotePc> findRemotePcByRepeaterId(String repeaterId) {
        return repository.findByRepeaterId(repeaterId);
    }

    public void setRemotePcStatus(String repeaterId, RemotePc.Status status) {
        Optional<RemotePc> remotePc = repository.findByRepeaterId(repeaterId);
        RemotePc update = remotePc.orElseThrow();
        update.setStatus(status);
        repository.save(update);
    }
}
