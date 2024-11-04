package kr.co.megabridge.megavnc.api;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 서버의 기본 설정값을 js 에서 호출해서 사용하게 하는 api
 **/
@RestController
public class ConfigController {

    @Value("${vnc.websocket.url}")
    private String websocketUrl;

    @GetMapping("/vnc/websocket-url")
    public String getWebsocketUrl() {
        return websocketUrl;
    }
}
