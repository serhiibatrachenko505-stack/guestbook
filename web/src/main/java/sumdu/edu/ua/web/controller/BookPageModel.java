package sumdu.edu.ua.web.controller;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.service.BookService;
import sumdu.edu.ua.core.service.CommentService;
import sumdu.edu.ua.web.dto.CommentForm;

/** Заповнює Model для сторінки книги (використовують обидва контролери). */
@Component
public class BookPageModel {
    private final BookService books;
    private final CommentService comments;

    public BookPageModel(BookService books, CommentService comments) {
        this.books = books;
        this.comments = comments;
    }

    public void fill(Model model, long bookId, CommentForm form) {
        Book book = books.get(bookId);                       // NotFoundException -> сторінка помилки
        model.addAttribute("book", book);
        model.addAttribute("comments",
                comments.list(bookId, null, null, new PageRequest(0, 50)).getItems());
        model.addAttribute("commentForm", form);
        model.addAttribute("deleteWindowHours", comments.getDeleteWindow().toHours());
    }
}
