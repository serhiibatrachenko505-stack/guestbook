package sumdu.edu.ua.core.domain;

import java.time.Instant;

/** Відгук до книги. userId — автор-користувач (може бути null для старих записів). */
public class Comment {
    private final long id;
    private final long bookId;
    private final Long userId;
    private final String author;
    private final String text;
    private final Instant createdAt;

    public Comment(long id, long bookId, Long userId, String author, String text, Instant createdAt) {
        this.id = id;
        this.bookId = bookId;
        this.userId = userId;
        this.author = author;
        this.text = text;
        this.createdAt = createdAt;
    }

    public Comment(long id, long bookId, String author, String text, Instant createdAt) {
        this(id, bookId, null, author, text, createdAt);
    }

    public long getId() { return id; }
    public long getBookId() { return bookId; }
    public Long getUserId() { return userId; }
    public String getAuthor() { return author; }
    public String getText() { return text; }
    public Instant getCreatedAt() { return createdAt; }
}
