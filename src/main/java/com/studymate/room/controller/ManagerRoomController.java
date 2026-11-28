package com.studymate.room.controller;

import com.studymate.room.dto.RoomDto;
import com.studymate.room.dto.RoomFormRequest;
import com.studymate.room.entity.RoomStatus;
import com.studymate.room.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/manager/rooms")
@PreAuthorize("hasRole('MANAGER')")
public class ManagerRoomController {

    private static final int PAGE_SIZE = 10;

    private final RoomService roomService;

    public ManagerRoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public String listRooms(@RequestParam(name = "q", required = false) String keyword,
                            @RequestParam(name = "building", required = false) String building,
                            @RequestParam(name = "status", required = false) RoomStatus status,
                            @RequestParam(name = "page", defaultValue = "0") int page,
                            Model model) {
        int safePage = Math.max(0, page);
        Pageable pageable = PageRequest.of(
                safePage,
                PAGE_SIZE,
                Sort.by(Sort.Order.asc("building"), Sort.Order.asc("code"))
        );

        Page<RoomDto> roomsPage = roomService.searchRooms(keyword, building, status, pageable);

        model.addAttribute("roomsPage", roomsPage);
        model.addAttribute("buildings", roomService.listDistinctBuildings());
        model.addAttribute("statuses", RoomStatus.values());
        model.addAttribute("keyword", keyword != null ? keyword.trim() : "");
        model.addAttribute("selectedBuilding", building != null ? building.trim() : "");
        model.addAttribute("selectedStatus", status);
        return "manager/rooms";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("roomForm")) {
            model.addAttribute("roomForm", new RoomFormRequest());
        }
        prepareFormModel(model, false, null);
        return "manager/room-form";
    }

    @PostMapping
    public String createRoom(@Valid @ModelAttribute("roomForm") RoomFormRequest form,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                RoomDto created = roomService.createRoom(form);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã thêm phòng " + created.code() + " thành công.");
                return "redirect:/manager/rooms";
            } catch (RoomService.DuplicateRoomCodeException ex) {
                bindingResult.rejectValue("code", "room.code.duplicate", ex.getMessage());
            }
        }
        prepareFormModel(model, false, null);
        return "manager/room-form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            RoomDto room = roomService.getRoom(id);
            RoomFormRequest form = new RoomFormRequest();
            form.setCode(room.code());
            form.setBuilding(room.building());
            form.setCapacity(room.capacity());
            form.setEquipment(room.equipment());
            form.setStatus(room.status());
            model.addAttribute("roomForm", form);
            prepareFormModel(model, true, id);
            return "manager/room-form";
        } catch (RoomService.RoomNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/manager/rooms";
        }
    }

    @PostMapping("/{id}")
    public String updateRoom(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("roomForm") RoomFormRequest form,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                RoomDto updated = roomService.updateRoom(id, form);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã cập nhật phòng " + updated.code() + " thành công.");
                return "redirect:/manager/rooms";
            } catch (RoomService.DuplicateRoomCodeException ex) {
                bindingResult.rejectValue("code", "room.code.duplicate", ex.getMessage());
            } catch (RoomService.RoomNotFoundException ex) {
                redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
                return "redirect:/manager/rooms";
            }
        }
        prepareFormModel(model, true, id);
        return "manager/room-form";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable("id") Long id,
                               @RequestParam("newStatus") RoomStatus newStatus,
                               @RequestParam(name = "q", required = false) String keyword,
                               @RequestParam(name = "building", required = false) String building,
                               @RequestParam(name = "status", required = false) RoomStatus status,
                               @RequestParam(name = "page", defaultValue = "0") int page,
                               RedirectAttributes redirectAttributes) {
        try {
            roomService.updateRoomStatus(id, newStatus);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái phòng.");
        } catch (RoomService.RoomNotFoundException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        if (keyword != null && !keyword.isBlank()) {
            redirectAttributes.addAttribute("q", keyword.trim());
        }
        if (building != null && !building.isBlank()) {
            redirectAttributes.addAttribute("building", building.trim());
        }
        if (status != null) {
            redirectAttributes.addAttribute("status", status.name());
        }
        if (page > 0) {
            redirectAttributes.addAttribute("page", page);
        }
        return "redirect:/manager/rooms";
    }

    private void prepareFormModel(Model model, boolean isEdit, Long roomId) {
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("pageTitle", isEdit ? "Cập nhật phòng học" : "Thêm phòng học");
        model.addAttribute("formAction", isEdit ? "/manager/rooms/" + roomId : "/manager/rooms");
        model.addAttribute("statuses", RoomStatus.values());
    }
}
