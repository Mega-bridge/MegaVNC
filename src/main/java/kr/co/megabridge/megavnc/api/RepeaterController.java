package kr.co.megabridge.megavnc.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequestMapping("/api/repeater")
public class RepeaterController {

    @GetMapping("/events")
    public void eventListener(String msg) {
        log.info("event listener");
        log.info(msg);
    }
}
