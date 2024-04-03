package kr.co.megabridge.megavnc.web.admin;

import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.service.AdminRemotePcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/remote-pcs")
public class AdminRemotePcsController {

    private final AdminRemotePcService adminServiceRemotePcService;



    @GetMapping
    public String showRemotePcs(Model model) {

        Iterable<RemotePc> remotePcs = adminServiceRemotePcService.findAllPcs();
        model.addAttribute("remotePcs", remotePcs);


        return "admin/remote-pcs";
    }

    //원래 삭제요청을 get방식으로 하는 것은 바람직 하지 않다. 그러나 html에서 폼을 폼안에 넣는게 안된다고 해서 일단 이렇게 했음//다른 방법을 찾아 봐야 함
    @GetMapping("/delete/{id}")
    public String deleteRemotePc( @PathVariable Long id) {
        //user로 그룹 조회해서 권한검사
       adminServiceRemotePcService.deletePc(id);

        return "redirect:/admin/remote-pcs";
    }


}
