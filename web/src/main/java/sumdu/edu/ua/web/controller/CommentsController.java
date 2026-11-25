package sumdu.edu.ua.web.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.exception.ConflictException;
import sumdu.edu.ua.core.service.CommentService;
import sumdu.edu.ua.web.dto.CommentForm;

/**
 * Форми відгуків на сторінці книги.
 *   POST /comments          -> додати, redirect:/books/{bookId}
 *   POST /comments/delete   -> видалити, redirect:/books/{bookId}
 */
@Controller
@RequestMapping("/comments")
public class CommentsController {
    private static final Logger log = LoggerFactory.getLogger(CommentsController.class);

    private final CommentService comments;
    private final BookPageModel bookPage;

    public CommentsController(CommentService comments, BookPageModel bookPage) {
        this.comments = comments;
        this.bookPage = bookPage;
    }

    @PostMapping
    public String add(@RequestParam("bookId") long bookId,
                      @Valid @ModelAttribute("commentForm") CommentForm form,
                      BindingResult binding,
                      Model model,
                      RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            bookPage.fill(model, bookId, form);   // показати сторінку книги з помилками у формі
            return "book";
        }
        Comment saved = comments.add(bookId, form.getAuthor(), form.getText());
        log.info("Comment created: id={}, bookId={}, author='{}', length={}",
                saved.getId(), bookId, saved.getAuthor(), saved.getText().length());
        redirect.addFlashAttribute("message", "msg.commentAdded");
        return "redirect:/books/" + bookId;
    }

    @PostMapping("/delete")
    public String delete(@RequestParam("bookId") long bookId,
                         @RequestParam("commentId") long commentId,
                         RedirectAttributes redirect) {
        try {
            Comment deleted = comments.delete(bookId, commentId);
            log.info("Comment deleted: id={}, bookId={}", deleted.getId(), bookId);
            redirect.addFlashAttribute("message", "msg.commentDeleted");
        } catch (ConflictException e) {
            log.warn("Delete comment {} rejected: {}", commentId, e.getMessage());
            redirect.addFlashAttribute("error", "error.commentTooOld");
        }
        return "redirect:/books/" + bookId;
    }
}
