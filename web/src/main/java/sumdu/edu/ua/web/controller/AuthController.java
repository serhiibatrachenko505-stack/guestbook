package sumdu.edu.ua.web.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sumdu.edu.ua.core.exception.ConflictException;
import sumdu.edu.ua.core.exception.ValidationException;
import sumdu.edu.ua.core.service.UserService;
import sumdu.edu.ua.web.dto.RegisterForm;

/**
 * Вхід, реєстрація і підтвердження акаунта.
 *   GET  /login               -> templates/auth/login.html (POST /login обробляє Spring Security)
 *   GET  /register            -> templates/auth/register.html
 *   POST /register            -> створення акаунта, лист з посиланням, redirect:/login?registered
 *   GET  /confirm?code=...    -> активація акаунта
 */
@Controller
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService users;

    public AuthController(UserService users) {
        this.users = users;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form, BindingResult binding) {
        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmPassword())) {
            binding.rejectValue("confirmPassword", "validation.password.mismatch");
        }
        if (binding.hasErrors()) {
            return "auth/register";
        }
        try {
            users.register(form.getUsername().trim(), form.getEmail().trim(), form.getPassword());
            log.info("User registered: username='{}', email='{}'", form.getUsername(), form.getEmail());
            return "redirect:/login?registered";
        } catch (ConflictException e) {
            String field = e.getMessage().startsWith("Email") ? "email" : "username";
            binding.rejectValue(field, field.equals("email") ? "error.emailTaken" : "error.usernameTaken");
            return "auth/register";
        } catch (ValidationException e) {
            binding.reject("error.validation", new Object[]{e.getMessage()}, e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/confirm")
    public String confirm(@RequestParam(name = "code", required = false) String code) {
        return users.confirm(code)
                .map(u -> {
                    log.info("Account confirmed: username='{}'", u.getUsername());
                    return "redirect:/login?confirmed";
                })
                .orElse("redirect:/login?confirmError");
    }
}
