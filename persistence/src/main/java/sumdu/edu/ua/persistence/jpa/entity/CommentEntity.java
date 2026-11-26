package sumdu.edu.ua.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

/** Таблиця comments. Багато відгуків належать одній книзі й одному користувачу. */
@Entity
@Table(name = "comments")
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Зовнішній ключ comments.book_id -> books.id. LAZY: книга не завантажується без потреби. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private BookEntity book;

    /** Зовнішній ключ comments.user_id -> users.id (може бути NULL). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(nullable = false, length = 64)
    private String author;

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CommentEntity() {
        // потрібен JPA
    }

    public CommentEntity(BookEntity book, UserEntity user, String author, String text) {
        this.book = book;
        this.user = user;
        this.author = author;
        this.text = text;
    }

    /** Колбек життєвого циклу JPA: час створення встановлюється перед INSERT. */
    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public BookEntity getBook() { return book; }
    public UserEntity getUser() { return user; }
    public String getAuthor() { return author; }
    public String getText() { return text; }
    public Instant getCreatedAt() { return createdAt; }
}
