package sumdu.edu.ua.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Об'єкт форми додавання відгуку на сторінці книги. */
public class CommentForm {

    @NotBlank(message = "{validation.required}")
    @Size(max = 64, message = "{validation.tooLong}")
    private String author;

    @NotBlank(message = "{validation.required}")
    @Size(max = 1000, message = "{validation.tooLong}")
    private String text;

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
