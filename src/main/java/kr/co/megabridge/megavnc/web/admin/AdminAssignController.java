package kr.co.megabridge.megavnc.web.admin;

import kr.co.megabridge.megavnc.dto.requests.AssignGroupDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/assign")
public class AdminAssignController {
    private final AdminAssignService adminAssignService;

    @GetMapping("/{memberId}")
    public String showAssignGroups(Model model, @PathVariable Long memberId) {

        model.addAttribute("user",adminAssignService.findByUserId(memberId));
        model.addAttribute("first", true);
        model.addAttribute("assignedGroups", adminAssignService.listAssignedGroups(memberId));
        model.addAttribute("unassignedGroups", adminAssignService.listUnassignedGroups(memberId));
        model.addAttribute("AssignGroupDto", new AssignGroupDto());
        return "admin/userManagement/assign";
    }

    @PostMapping
    public String assignGroup(AssignGroupDto assignGroupDto){

        adminAssignService.assignGroup(assignGroupDto);
        return "redirect:/admin/assign/" + assignGroupDto.getSelectedUserId();
    }


    @GetMapping("/{memberId}/{groupId}")
    public String disassignGroup(@PathVariable Long memberId, @PathVariable Long groupId){

        adminAssignService.disassignGroup(memberId,groupId);
        return "redirect:/admin/assign/" + memberId ;
    }
}
