package kr.co.megabridge.megavnc.filetransfer;

import kr.co.megabridge.megavnc.domain.FileInfo;
import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.exception.ErrorCode;
import kr.co.megabridge.megavnc.exception.exceptions.ApiException;
import kr.co.megabridge.megavnc.exception.exceptions.FileDeleteException;
import kr.co.megabridge.megavnc.exception.exceptions.FileDownloadException;
import kr.co.megabridge.megavnc.repository.FileInfoRepository;
import kr.co.megabridge.megavnc.repository.RemotePcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import kr.co.megabridge.megavnc.security.User;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.util.UriUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private final FileInfoRepository fileInfoRepository;
    private final RemotePcRepository remotePcRepository;
    @Value("${file.directory}")
    private String uploadDir;
    @Value("${file.fileKey}")
    private String FILE_KEY;
    @Value("${file.cleanup.cron}")
    private String CRON;
    @Value("${file.cleanup.days}")
    private Integer EXPIRED_DATE;


    private static final String ADMIN_REQUEST = "ADMIN_REQUEST";

    //fixme: 파일 확장자 검사 추가
    @Transactional
    public Integer uploadFile(MultipartFile file, Long repeaterId, User user) {
        try {
            String reconnectId ;
            if (repeaterId != null) {
                RemotePc remotePc = remotePcRepository.findByRepeaterId(repeaterId).orElseThrow(() -> new ApiException(ErrorCode.PC_NOT_FOUND));
                reconnectId = Optional.ofNullable(remotePc.getReconnectId()).orElseThrow(() -> new ApiException(ErrorCode.DISASSIGNED_PC));
            }else if(user.isAdmin()){
                reconnectId = ADMIN_REQUEST;
            } else {
                throw new ApiException(ErrorCode.PC_NOT_FOUND);
            }
            Path directory = Paths.get(uploadDir);

            if (!Files.exists(directory)) {
                Files.createDirectories(directory); // 디렉토리가 존재하지 않으면 생성
            }

            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String uniqueFileName = timestamp + "_" + fileName;
            String encodedFileName = UriUtils.encode(uniqueFileName, StandardCharsets.UTF_8);

            //파일명 암호화
            String encryptedFileName = encodeAES256(encodedFileName);

            //파일 경로 지정
            Path filePath = directory.resolve(encryptedFileName);


            //파일 정보 db에 저장
            FileInfo fileInfo = FileInfo.createFileInfo(uniqueFileName, filePath.toString(), file.getSize(), reconnectId);
            fileInfoRepository.save(fileInfo);

            //파일 올리기
            Files.copy(file.getInputStream(), filePath);

            return fileInfo.getSeq();


        } catch (IOException e) {
            throw new ApiException(ErrorCode.FILE_CANNOT_UPLOAD, ": " + e.getMessage());
        }
    }


    @Transactional
    public ResponseEntity<StreamingResponseBody> downloadFile(Integer fileSeq) {
        FileInfo fileInfo = fileInfoRepository.findBySeq(fileSeq).orElseThrow(() -> new FileDownloadException(ErrorCode.FILE_NOT_FOUND));
        String encryptedFilename = encodeAES256(UriUtils.encode(fileInfo.getFileName(), StandardCharsets.UTF_8));


        Path fileLocation = Paths.get(uploadDir).toAbsolutePath().resolve(encryptedFilename);



        if (!fileInfo.getReconnectId().equals(ADMIN_REQUEST)) {
            fileInfoRepository.deleteBySeq(fileSeq);
        }

        StreamingResponseBody responseBody = outputStream -> {
            try {
                Files.copy(fileLocation, outputStream);
                outputStream.flush();
                // 파일 전송 완료 후 파일 삭제
                if (!fileInfo.getReconnectId().equals(ADMIN_REQUEST)) {
                    try {
                        Files.delete(fileLocation);

                    } catch (IOException e) {
                        throw new FileDownloadException(ErrorCode.FILE_CANNOT_DELETE, e.getMessage());
                    }
                }
            } catch (IOException e) {
                throw new FileDownloadException(ErrorCode.FILE_NOT_FOUND, ": " + e.getMessage());
            }
        };
        String contentDisposition = "attachment; filename=\"" +
                UriUtils.encode(fileInfo.getFileName().replaceFirst("^\\d{8}_\\d{6}_", ""), StandardCharsets.UTF_8) + "\"";

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(responseBody);
    }



    @Transactional
    public void deleteDistributionFile(Integer fileSeq) {
        FileInfo fileInfo = fileInfoRepository.findBySeq(fileSeq).orElseThrow(() -> new FileDeleteException(ErrorCode.FILE_NOT_FOUND));
        if (!fileInfo.getReconnectId().equals(ADMIN_REQUEST)) {
            throw new FileDeleteException(ErrorCode.FILE_CANNOT_DELETE,"관리자가 배포한 파일이 아닙니다.");
        }
        fileInfoRepository.delete(fileInfo);
        String encryptedFilename = encodeAES256(UriUtils.encode(fileInfo.getFileName(), StandardCharsets.UTF_8));


        Path fileLocation = Paths.get(uploadDir).toAbsolutePath().resolve(encryptedFilename);
        try {
            Files.delete(fileLocation);

        } catch (IOException e) {
            throw new FileDeleteException(ErrorCode.FILE_CANNOT_DELETE, e.getMessage());
        }

    }

    public List<FileInfo> findAllByReconnectId(String reconnectId) {
        List<FileInfo> response =  fileInfoRepository.findAllByReconnectIdOrderByCreatedAtDesc(ADMIN_REQUEST);
        if (reconnectId != null) {
            response.addAll(fileInfoRepository.findAllByReconnectIdOrderByCreatedAtDesc(reconnectId));
        }
        return response;
    }


    /**
     * 특정 시간이 지나면 특정 기간이 지난 전송 파일 자동으로 삭제
     * **/
    @Scheduled(cron = "#{fileCleanupProperties.cron}")
    @Transactional
    public void cleanupOldFiles() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,  - EXPIRED_DATE);
        Date DaysAgo = calendar.getTime();
        List<FileInfo> oldFiles = fileInfoRepository.findAllByCreatedAtBeforeAndReconnectIdIsNot(DaysAgo,ADMIN_REQUEST);
        for (FileInfo fileInfo : oldFiles) {
            try {
                Path filePath = Paths.get(fileInfo.getFilePath());
                Files.deleteIfExists(filePath);
                fileInfoRepository.delete(fileInfo);
            } catch (IOException e) {
                // 예외 처리
                throw new RuntimeException("파일 삭제 실패: " + fileInfo.getFilePath(), e);
            }
        }
    }



    ////////////////////////////////////////////////////////////
    /*private 매서드*/

    private String encodeAES256(String data) {
        try {
            byte[] key = (FILE_KEY).getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 32); // AES 256에 필요한 32바이트 키

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
            byte[] encrypted = cipher.doFinal(data.getBytes("UTF-8"));

            return Base64.getUrlEncoder().encodeToString(encrypted);

        } catch (Exception e) {
            throw new ApiException(ErrorCode.ENCODING_FAIL, ": " + e.getMessage());
        }
    }


}
