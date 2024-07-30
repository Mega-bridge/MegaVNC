package kr.co.megabridge.megavnc.web.admin;

import kr.co.megabridge.megavnc.domain.Group;
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

    @GetMapping("/delete/{groupId}")
    public String deleteGroup(@PathVariable Long groupId){
        adminGroupService.deleteGroup(groupId);
        return "redirect:/admin/groups";
    }

}
