package sumdu.edu.ua.web.http;

/** Тіло запиту POST /books. */
public record BookForm(String title, String author, Integer pubYear) {
}
