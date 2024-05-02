package kr.co.megabridge.megavnc.service;

import kr.co.megabridge.megavnc.exception.ErrorCode;
import kr.co.megabridge.megavnc.exception.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileTransferService {

    @Value("uploads/")
    private String uploadDir;

    public String uploadFile(MultipartFile file)  {
        try {
            Path directory = Paths.get(uploadDir);

            if (!Files.exists(directory)) {
                Files.createDirectories(directory); // 디렉토리가 존재하지 않으면 생성
            }

            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            String uniqueFileName = System.currentTimeMillis() + "_" + fileName;

            //파일 경로 지정
            Path filePath = directory.resolve(uniqueFileName);

            //파일 올리기
            Files.copy(file.getInputStream(), filePath);

            //경로 반환
            return UriUtils.encode(uniqueFileName, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ApiException(ErrorCode.FILE_CANNOT_UPLOAD);
        }
    }
}
