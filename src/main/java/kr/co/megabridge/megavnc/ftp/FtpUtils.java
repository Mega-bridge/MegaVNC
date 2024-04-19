package kr.co.megabridge.megavnc.ftp;

import jakarta.servlet.http.HttpServletResponse;
import kr.co.megabridge.megavnc.dto.RepeaterIdDto;
import kr.co.megabridge.megavnc.repository.RemotePcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;

@Slf4j
@Component
@RequiredArgsConstructor
@PropertySource("classpath:ftp-config.properties")
public class FtpUtils {
    private final RemotePcRepository remotePcRepository;



    @Value("${ftp.port}")
    private int port;

    @Value("${ftp.username}")
    private String username;

    @Value("${ftp.password}")
    private String password;

    private FTPClient ftp;

    //FTPClient 객체를 통한 ftp 서버 연결
    public void open(Long repeaterId) throws  IOException {

         String  server = remotePcRepository.findByRepeaterId(repeaterId).get().getFtpHost();


        ftp = new FTPClient();
        ftp.setControlEncoding("UTF-8");

        //log에 주고받은 명령 출력해주는 설정
        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));

        ftp.connect(server, port);
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            log.error("연결 실패.");
        }

        ftp.setSoTimeout(1000);
        ftp.login(username, password);
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
    }

    public void close() throws IOException {
        ftp.logout();
        ftp.disconnect();
    }

    public void upload(MultipartFile file, RepeaterIdDto repeaterIdDto) throws IOException {
        open(repeaterIdDto.getRepeaterId());
        InputStream inputStream = null;
        inputStream = file.getInputStream();

        //뽀인토 put 명령에 해당
        ftp.storeFile(file.getOriginalFilename(), inputStream);
        inputStream.close();
        close();
    }


    public void downlod(String fName, HttpServletResponse resp, RepeaterIdDto repeaterIdDto) throws IOException {

        String fileName = URLEncoder.encode(fName, "UTF-8");

        //간단히 하기위해 크롬 브라우져 기준 설정!
        resp.setContentType("application/octet-stream");
        resp.setHeader("Content-Disposition", "attachment; filename=\""+ fileName + "\"");


        open(repeaterIdDto.getRepeaterId());
        OutputStream outputStream = new BufferedOutputStream(resp.getOutputStream());
        InputStream inputStream = null;

        //뽀인토  get 명령에 해당
        inputStream =ftp.retrieveFileStream("/"+fName);

        byte[] bytesArray = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(bytesArray)) != -1) {
            outputStream.write(bytesArray, 0, bytesRead);
        }

        boolean isOK  = ftp.completePendingCommand();
        log.debug("check: " + isOK);
        outputStream.close();
        inputStream.close();
        close();
    }

}