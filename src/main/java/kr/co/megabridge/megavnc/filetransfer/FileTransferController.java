package kr.co.megabridge.megavnc.filetransfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
@Slf4j
public class FileTransferController {

    private final FileTransferService fileTransferService;



    @PostMapping
    @ResponseBody
    public ResponseEntity<String> uploadFile(@RequestPart("file") MultipartFile file,
                                             @RequestParam(required = false) Long repeaterId)  {

        String encodedFilename = fileTransferService.uploadFile(file, repeaterId);
        return ResponseEntity
                .ok().body(encodedFilename);
    }


    @GetMapping("/download-files/{encodedFilename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String encodedFilename) {


        return fileTransferService.downloadFile(encodedFilename);



    }

}
