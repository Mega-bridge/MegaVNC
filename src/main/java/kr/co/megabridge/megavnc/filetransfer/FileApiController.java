package kr.co.megabridge.megavnc.filetransfer;

import kr.co.megabridge.megavnc.security.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileApiController {

    private final FileService fileService;


    @PostMapping
    public ResponseEntity<Integer> uploadFile(@RequestPart("file") MultipartFile file,
                                              @RequestParam(required = false) Long repeaterId, @AuthenticationPrincipal User user)  {

        Integer encodedFilename = fileService.uploadFile(file, repeaterId,user);
        return ResponseEntity
                .ok().body(encodedFilename);
    }


}
