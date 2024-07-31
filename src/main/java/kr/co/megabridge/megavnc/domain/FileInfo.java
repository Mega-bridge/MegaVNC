package kr.co.megabridge.megavnc.domain;



import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer seq;

    private Date createdAt; //파일 업로드 날짜

    @Column(unique = true)
    private String fileName;

    private String filePath;

    private Long fileSize;

    private String reconnectId; //앱의 고유한 아이디


    @PrePersist
    private void createdAt() {
        this.createdAt = new Date();
    }


    public static FileInfo createFileInfo(String fileName,String filePath ,Long fileSize ,String reconnectId) {
       FileInfo fileInfo = new FileInfo();
        fileInfo.fileName = fileName;
        fileInfo.filePath = filePath;
        fileInfo.fileSize = fileSize;
        fileInfo.reconnectId = reconnectId;
        return fileInfo;
    }
}
