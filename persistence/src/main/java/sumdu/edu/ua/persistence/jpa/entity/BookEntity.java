package sumdu.edu.ua.persistence.jpa.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/** Таблиця books. Одна книга — багато відгуків. */
@Entity
@Table(name = "books")
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String author;

    @Column(name = "pub_year", nullable = false)
    private Integer pubYear;

    /** Двосторонній зв'язок: власник зв'язку — CommentEntity.book. Відгуки видаляються разом із книгою. */
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CommentEntity> comments = new ArrayList<>();

    protected BookEntity() {
        // потрібен JPA
    }

    public BookEntity(String title, String author, Integer pubYear) {
        this.title = title;
        this.author = author;
        this.pubYear = pubYear;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public Integer getPubYear() { return pubYear; }
    public List<CommentEntity> getComments() { return comments; }
}
