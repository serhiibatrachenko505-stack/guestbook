package sumdu.edu.ua.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Об'єкт форми додавання книги (form binding: th:object="${bookForm}", th:field="*{title}").
 * Spring заповнює поля з параметрів POST-запиту через сеттери, тому клас змінюваний.
 * Повідомлення валідації — ключі з messages*.properties, тобто теж локалізовані.
 */
public class BookForm {

    @NotBlank(message = "{validation.required}")
    @Size(max = 255, message = "{validation.tooLong}")
    private String title;

    @NotBlank(message = "{validation.required}")
    @Size(max = 255, message = "{validation.tooLong}")
    private String author;

    @NotNull(message = "{validation.required}")
    @Min(value = 1, message = "{validation.year}")
    @Max(value = 2100, message = "{validation.year}")
    private Integer pubYear;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public Integer getPubYear() { return pubYear; }
    public void setPubYear(Integer pubYear) { this.pubYear = pubYear; }
}
