package bg.softuni.planner.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    @GetMapping("/add-task")
    public String addTaskView() {
        return "add-task";
    }

    @GetMapping("edit-task")
    public String editTask() {
        return "edit-task";
    }
}