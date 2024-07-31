package kr.co.megabridge.megavnc.filetransfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;


@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
@Slf4j
public class FileApiController {

    private final FileService fileService;



    @PostMapping
    @ResponseBody
    public ResponseEntity<Integer> uploadFile(@RequestPart("file") MultipartFile file,
                                             @RequestParam(required = false) Long repeaterId)  {

        Integer encodedFilename = fileService.uploadFile(file, repeaterId);
        return ResponseEntity
                .ok().body(encodedFilename);
    }


    @GetMapping("/download-files/{fileSeq}")
    public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable Integer fileSeq) {
        return fileService.downloadFile(fileSeq);
    }



}
