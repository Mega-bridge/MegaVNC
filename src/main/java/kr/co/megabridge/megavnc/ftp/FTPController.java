package kr.co.megabridge.megavnc.ftp;


import kr.co.megabridge.megavnc.dto.RepeaterIdDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ftp")
public class FTPController {

    private final FtpUtils ftpUtils;

    @PostMapping
    @ResponseBody
    public ResponseEntity<String> uploadFile(@RequestPart("file") MultipartFile file, @ModelAttribute RepeaterIdDto repeaterIdDto) throws IOException {
        ftpUtils.upload(file,repeaterIdDto);
        return ResponseEntity
                .ok().body("파일전송 완료");
    }


}
