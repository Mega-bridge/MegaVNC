package kr.co.megabridge.megavnc.web;

import kr.co.megabridge.megavnc.exception.ErrorCode;
import kr.co.megabridge.megavnc.exception.exceptions.ApiException;
import kr.co.megabridge.megavnc.service.FileTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.net.MalformedURLException;

import java.nio.file.Path;
import java.nio.file.Paths;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileTransferController {

    private final FileTransferService fileTransferService;



    @PostMapping
    @ResponseBody
    public ResponseEntity<String> uploadFile(@RequestPart("file") MultipartFile file)  {

        String uniqueFilename = fileTransferService.uploadFile(file);
        return ResponseEntity
                .ok().body(uniqueFilename);

    }


    @GetMapping("/download-files/{filename}")
    public ResponseEntity<Resource> downloadServer(@PathVariable String filename) {
        String uploadDir = "uploads/";
        Path fileLocation = Paths.get(uploadDir).toAbsolutePath().resolve(filename);

        Resource file;
        try {
            file = new UrlResource(fileLocation.toUri()); // 파일을 리소스로 읽어옴
        } catch (MalformedURLException e) {

            throw new ApiException(ErrorCode.FILE_NOT_FOUND);
        }

        String contentDisposition = "attachment; filename=\"" +
                filename + "\"";




        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(file);
    }

}
