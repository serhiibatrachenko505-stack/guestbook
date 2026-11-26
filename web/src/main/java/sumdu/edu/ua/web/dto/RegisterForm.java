package sumdu.edu.ua.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Форма реєстрації. */
public class RegisterForm {

    @NotBlank(message = "{validation.required}")
    @Size(min = 3, max = 64, message = "{validation.username.size}")
    @Pattern(regexp = "[\\p{L}0-9_.-]+", message = "{validation.username.chars}")
    private String username;

    @NotBlank(message = "{validation.required}")
    @Email(message = "{validation.email}")
    @Size(max = 128, message = "{validation.tooLong}")
    private String email;

    @NotBlank(message = "{validation.required}")
    @Size(min = 6, max = 100, message = "{validation.password.size}")
    private String password;

    @NotBlank(message = "{validation.required}")
    private String confirmPassword;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
