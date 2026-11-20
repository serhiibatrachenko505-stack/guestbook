package sumdu.edu.ua.web.http;

/** Тіло запиту POST /books/{id}/comments. */
public record CommentForm(String author, String text) {
}
