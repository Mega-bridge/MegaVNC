package kr.co.megabridge.megavnc.web.admin;

import jakarta.validation.Valid;
import kr.co.megabridge.megavnc.domain.Group;
import kr.co.megabridge.megavnc.dto.*;
import kr.co.megabridge.megavnc.service.AdminGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/groups")
public class AdminGroupController {

    private final AdminGroupService adminGroupService;

    @GetMapping
    public String showGroups(Model model) {
        List<Group> groups = adminGroupService.listAllGroups();
        model.addAttribute("groups", groups);
        return "admin/groupManagement/groups";
    }


    @PostMapping
    public String createGroup(@ModelAttribute("group") @Valid GroupRegisterDto group ){

        adminGroupService.register(group.getGroupName());

        return "redirect:/admin/groups";
    }


    @GetMapping("/delete/{groupId}")
    public String deleteGroup(@PathVariable Long groupId){
        adminGroupService.deleteGroup(groupId);
        return "redirect:/admin/groups";
    }

}
