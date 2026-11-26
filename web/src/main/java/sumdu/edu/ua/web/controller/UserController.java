package sumdu.edu.ua.web.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.User;
import sumdu.edu.ua.core.service.BookService;
import sumdu.edu.ua.core.service.CommentService;
import sumdu.edu.ua.core.service.UserService;

/**
 * Сторінки користувачів.
 *   GET /users                 -> users.html (список користувачів)
 *   GET /users/{id}/comments   -> user-comments.html (усі відгуки користувача)
 */
@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final CommentService commentService;
    private final BookService bookService;

    public UserController(UserService userService, CommentService commentService, BookService bookService) {
        this.userService = userService;
        this.commentService = commentService;
        this.bookService = bookService;
    }

    @GetMapping
    public String users(Model model) {
        Map<User, Integer> users = new LinkedHashMap<>();
        for (User u : userService.findAll()) {
            users.put(u, commentService.getCommentsByUser(u.getId()).size());
        }
        model.addAttribute("users", users);
        return "users";
    }

    @GetMapping("/{id}/comments")
    public String userComments(@PathVariable("id") long id, Model model) {
        User user = userService.findById(id);                      // 404, якщо немає
        List<Comment> comments = commentService.getCommentsByUser(id);

        // назви книг для посилань (по одному запиту на кожну різну книгу)
        Map<Long, Book> books = new LinkedHashMap<>();
        for (Comment c : comments) {
            books.computeIfAbsent(c.getBookId(), bookService::get);
        }

        model.addAttribute("user", user);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("comments", comments);
        model.addAttribute("books", books);
        return "user-comments";
    }
}
