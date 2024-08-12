package kr.co.megabridge.megavnc.dto.requests;

import lombok.Data;

@Data
public class ChangePasswordDto {

    private String password;
    private String newPassword;
    private String newPasswordConfirm;
}
