package sumdu.edu.ua.persistence.jpa.entity;

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

/** Таблиця users. Один користувач — багато відгуків. */
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 32)
    private String role;

    /**
     * Без каскаду: відгуки не видаляються разом із користувачем
     * (у БД — ON DELETE SET NULL). LAZY — список завантажується лише за потреби.
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<CommentEntity> comments = new ArrayList<>();

    protected UserEntity() {
        // потрібен JPA
    }

    public UserEntity(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public List<CommentEntity> getComments() { return comments; }
}
