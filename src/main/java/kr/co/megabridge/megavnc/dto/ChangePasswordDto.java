package kr.co.megabridge.megavnc.dto;

import lombok.Data;

@Data
public class ChangePasswordDto {

    private String password;
    private String newPassword;
    private String newPasswordConfirm;
}
