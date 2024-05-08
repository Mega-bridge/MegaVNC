package kr.co.megabridge.megavnc.filetransfer;

import kr.co.megabridge.megavnc.exception.ErrorCode;
import kr.co.megabridge.megavnc.exception.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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


@Service
@Slf4j
@RequiredArgsConstructor
public class FileTransferService {

    @Value("uploads/")
    private String uploadDir;
    @Value("${fileKey}")
    private  String FILE_KEY;

    public String uploadFile(MultipartFile file)  {
        try {
            Path directory = Paths.get(uploadDir);

            if (!Files.exists(directory)) {
                Files.createDirectories(directory); // 디렉토리가 존재하지 않으면 생성
            }

            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String uniqueFileName =UriUtils.encode(timestamp+"_"+fileName, StandardCharsets.UTF_8);

           String encodedFileName = encodeAES256(uniqueFileName);

            //파일 경로 지정
            Path filePath = directory.resolve(encodedFileName);

            //파일 올리기
            Files.copy(file.getInputStream(), filePath);

            //파일명 암호화
            return encodedFileName;


        } catch (IOException e) {
            throw new ApiException(ErrorCode.FILE_CANNOT_UPLOAD,": "+e.getMessage());
        }
    }

    public ResponseEntity<Resource> downloadServer(String encodedFilename){
        String uploadDir = "uploads/";
        Path fileLocation = Paths.get(uploadDir).toAbsolutePath().resolve(encodedFilename);

        Resource file;
        try {
            file = new UrlResource(fileLocation.toUri()); // 파일을 리소스로 읽어옴
        } catch (MalformedURLException e) {
            throw new ApiException(ErrorCode.FILE_NOT_FOUND,": " + e.getMessage());
        }

        String contentDisposition = "attachment; filename=\"" +
                UriUtils.encode(encodedFilename, StandardCharsets.UTF_8) + "\"";

        ResponseEntity<Resource> responseEntity = ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(file);



        return responseEntity;
    }

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
