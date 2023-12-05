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

    public Long registerRemotePc(String remotePcName, User owner) {
        Optional<RemotePc> currTop = repository.findTopByOrderByRepeaterIdDesc();

        // Make sure the repeater id starts from 100.
        // Base remote pc in data loader also ensures it.
        long nextRepeaterId = 100;
        if (currTop.isPresent())
            nextRepeaterId = currTop.get().getRepeaterId() + 1;

        RemotePc remotePc = RemotePc.createRemotePc(nextRepeaterId, remotePcName, owner);
        repository.save(remotePc);

        return nextRepeaterId;
    }

    public Optional<RemotePc> findRemotePcByRepeaterId(Long repeaterId) {
        return repository.findByRepeaterId(repeaterId);
    }

    public void setRemotePcStatus(Long repeaterId, RemotePc.Status status) {
        Optional<RemotePc> remotePc = repository.findByRepeaterId(repeaterId);
        RemotePc update = remotePc.orElseThrow();
        update.setStatus(status);
        repository.save(update);
    }

    public RemotePc findById(Long id) {
        return repository.findById(id).orElseThrow();
    }
}
