package kr.co.megabridge.megavnc.filetransfer;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "file.cleanup")
@Getter
@Setter
public class FileCleanupProperties {
    private String cron;
}

