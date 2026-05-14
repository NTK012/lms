package com.example.lms;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class LeaveController {

    // Simulated In-Memory Database
    private Map<String, Integer> balances = new HashMap<>(Map.of(
            "Annual", 15,
            "Sick", 28,
            "Family", 3
    ));

    private List<Map<String, Object>> history = new ArrayList<>(List.of(
            new HashMap<>(Map.of("id", 1, "submitDate", "10 May 2026", "start", "2026-05-20", "end", "2026-05-25", "type", "Annual", "days", 5, "status", "Pending")),
            new HashMap<>(Map.of("id", 2, "submitDate", "15 Feb 2026", "start", "2026-02-20", "end", "2026-02-21", "type", "Sick", "days", 2, "status", "Approved"))
    ));

    // Feature 1: Load Dashboard
    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("balances", balances);
        model.addAttribute("history", history);
        return "index"; // Looks for index.html in src/main/resources/templates
    }

    // Feature 2: Apply for Leave Logic
    @PostMapping("/apply")
    public String applyLeave(
            @RequestParam String leaveType,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam int leaveDays,
            RedirectAttributes redirectAttributes) {

        if (leaveDays <= 0) {
            redirectAttributes.addFlashAttribute("error", "Error: Number of days must be greater than 0.");
        } else if (leaveDays > balances.get(leaveType)) {
            redirectAttributes.addFlashAttribute("error", "Declined: Insufficient " + leaveType + " leave balance.");
        } else {
            // Deduct balance
            balances.put(leaveType, balances.get(leaveType) - leaveDays);

            // Save to history
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
            Map<String, Object> newRequest = new HashMap<>();
            newRequest.put("id", history.size() + 1);
            newRequest.put("submitDate", today);
            newRequest.put("start", startDate);
            newRequest.put("end", endDate);
            newRequest.put("type", leaveType);
            newRequest.put("days", leaveDays);
            newRequest.put("status", "Pending");

            history.add(0, newRequest); // Add to top of list
            redirectAttributes.addFlashAttribute("success", "Success! " + leaveDays + " days of " + leaveType + " leave submitted.");
        }

        return "redirect:/";
    }

    // Manager Action: Approve Leave
    @GetMapping("/approve/{id}")
    public String approveLeave(@PathVariable int id, RedirectAttributes redirectAttributes) {
        for (Map<String, Object> req : history) {
            if ((int) req.get("id") == id) {
                req.put("status", "Approved");
                redirectAttributes.addFlashAttribute("success", "Request #" + id + " Approved.");
                break;
            }
        }
        return "redirect:/";
    }
}