package sumdu.edu.ua.persistence.jpa.adapter;

import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.User;
import sumdu.edu.ua.persistence.jpa.entity.BookEntity;
import sumdu.edu.ua.persistence.jpa.entity.CommentEntity;
import sumdu.edu.ua.persistence.jpa.entity.UserEntity;

/** Перетворення JPA-сутностей на доменні моделі core (сутності не виходять за межі persistence). */
final class Mappers {
    private Mappers() {}

    static Book toDomain(BookEntity e) {
        return new Book(e.getId(), e.getTitle(), e.getAuthor(), e.getPubYear());
    }

    static User toDomain(UserEntity e) {
        return new User(e.getId(), e.getUsername(), e.getEmail(), e.getRole(), e.isEnabled());
    }

    static Comment toDomain(CommentEntity e) {
        // getBook().getId() не завантажує книгу: id відомий проксі-об'єкту LAZY-зв'язку
        Long userId = e.getUser() == null ? null : e.getUser().getId();
        return new Comment(e.getId(), e.getBook().getId(), userId, e.getAuthor(), e.getText(), e.getCreatedAt());
    }
}
