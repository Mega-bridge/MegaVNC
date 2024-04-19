package kr.co.megabridge.megavnc.dto;

import kr.co.megabridge.megavnc.domain.Group;
import kr.co.megabridge.megavnc.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseRemotePcDto {

   private Long id;
   private Group group;
   private String name;
   private Date assignedAt;
   private Status status;




}
