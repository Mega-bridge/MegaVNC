package kr.co.megabridge.megavnc.tcp;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

@MessageEndpoint
public class TcpServerEndpoint {
    private final MessageService messageService;

    public TcpServerEndpoint(MessageService messageService) {
        this.messageService = messageService;
    }

    @ServiceActivator(inputChannel = "inboundChannel", async = "true")
    public void process(byte[] message) {
        messageService.processMessage(message);
    }
}

