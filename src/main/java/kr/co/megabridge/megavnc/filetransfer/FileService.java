package kr.co.megabridge.megavnc.filetransfer;

import kr.co.megabridge.megavnc.domain.FileInfo;
import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.exception.ErrorCode;
import kr.co.megabridge.megavnc.exception.exceptions.ApiException;
import kr.co.megabridge.megavnc.repository.FileInfoRepository;
import kr.co.megabridge.megavnc.repository.RemotePcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private final FileInfoRepository fileInfoRepository;
    private final RemotePcRepository remotePcRepository;
    @Value("uploads/")
    private String uploadDir;
    @Value("${fileKey}")
    private  String FILE_KEY;
    private static final String ADMIN_REQUEST = "ADMIN_REQUEST";

    @Transactional
    public String uploadFile(MultipartFile file, Long repeaterId)  {
        try {
            String reconnectId = ADMIN_REQUEST;
            if(repeaterId != null) {
                RemotePc remotePc = remotePcRepository.findByRepeaterId(repeaterId).orElseThrow(() -> new ApiException(ErrorCode.PC_NOT_FOUND));
                reconnectId =  Optional.ofNullable(remotePc.getReconnectId()).orElseThrow(() -> new ApiException(ErrorCode.DISASSIGNED_PC));
            }
            Path directory = Paths.get(uploadDir);

            if (!Files.exists(directory)) {
                Files.createDirectories(directory); // 디렉토리가 존재하지 않으면 생성
            }

            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String uniqueFileName = timestamp + "_" + fileName;
            String encodedFileName =UriUtils.encode(uniqueFileName, StandardCharsets.UTF_8);

            //파일명 암호화
            String encryptedFileName = encodeAES256(encodedFileName);

            //파일 경로 지정
            Path filePath = directory.resolve(encryptedFileName);

            //파일 올리기
            Files.copy(file.getInputStream(), filePath);


            //파일 정보 db에 저장
            FileInfo fileInfo = FileInfo.createFileInfo(uniqueFileName, filePath.toString(), file.getSize(), reconnectId);
            fileInfoRepository.save(fileInfo);
            return encryptedFileName;


        } catch (IOException e) {
            throw new ApiException(ErrorCode.FILE_CANNOT_UPLOAD,": "+e.getMessage());
        }
    }


    public ResponseEntity<Resource> downloadFile(String encryptedFilename){
        String uploadDir = "uploads/";
        Path fileLocation = Paths.get(uploadDir).toAbsolutePath().resolve(encryptedFilename);

        Resource file;
        try {
            file = new UrlResource(fileLocation.toUri()); // 파일을 리소스로 읽어옴
        } catch (MalformedURLException e) {
            throw new ApiException(ErrorCode.FILE_NOT_FOUND,": " + e.getMessage());
        }

        String contentDisposition = "attachment; filename=\"" +
                UriUtils.encode(encryptedFilename, StandardCharsets.UTF_8) + "\"";

        ResponseEntity<Resource> responseEntity = ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(file);



        return responseEntity;
    }


    //todo: 파일정보 전체 조회 서비스
    public List<FileInfo> findAll(){
        return fileInfoRepository.findAllByOrderByCreatedAtDesc();
    }


    //todo: 관리자 조회 및, reconnectId로 필터링 조회
    //todo : 파일 삭제 서비스


    ////////////////////////////////////////////////////////////
    /*private 매서드*/

    private String encodeAES256(String data){
        try {
            byte[] key = (FILE_KEY).getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 32); // AES 256에 필요한 32바이트 키

            byte[] ivBytes = new byte[16]; // 16 바이트 IV
            new SecureRandom().nextBytes(ivBytes);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), iv);
            byte[] encrypted = cipher.doFinal(data.getBytes());

            // IV를 암호화된 데이터 앞에 추가
            byte[] encryptedWithIv = new byte[ivBytes.length + encrypted.length];
            System.arraycopy(ivBytes, 0, encryptedWithIv, 0, ivBytes.length);
            System.arraycopy(encrypted, 0, encryptedWithIv, ivBytes.length, encrypted.length);

            return Base64.getUrlEncoder().encodeToString(encryptedWithIv);

        } catch (Exception e) {
            throw new ApiException(ErrorCode.ENCODING_FAIL,": "+e.getMessage());
        }
    }


}
