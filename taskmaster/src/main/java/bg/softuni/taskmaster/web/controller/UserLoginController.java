package bg.softuni.taskmaster.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserLoginController {

    @GetMapping("/login")
    public String loginView() {
        return "login";

    }

    @PostMapping("/login-error")
    public String loginError(@ModelAttribute("username") String username, Model model) {
        model.addAttribute("hasError", true);
        model.addAttribute("username", username);
        return "login";
    }
}
