package sumdu.edu.ua.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Об'єкт форми додавання відгуку. Автор — поточний користувач (з Spring Security), не поле форми. */
public class CommentForm {

    @NotBlank(message = "{validation.required}")
    @Size(max = 1000, message = "{validation.tooLong}")
    private String text;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
