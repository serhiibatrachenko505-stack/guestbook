package sumdu.edu.ua.web.dto;

/** Тіло POST /comments. */
public record CommentRequest(Long bookId, String author, String text) {
}
