package kr.co.megabridge.megavnc.service;

import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.repository.RemotePcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RemotePcRepositoryService {

    private final RemotePcRepository repository;

    @Autowired
    public RemotePcRepositoryService(RemotePcRepository repository) {
        this.repository = repository;
    }

    public Iterable<RemotePc> findAll() {
        return repository.findAll();
    }

    public Iterable<RemotePc> findByOwner(User user) {
        return repository.findByOwner(user);
    }
}
