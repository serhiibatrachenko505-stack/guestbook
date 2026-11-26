package sumdu.edu.ua.web.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.exception.InvalidCommentTextException;
import sumdu.edu.ua.core.service.CommentService;
import sumdu.edu.ua.web.dto.CommentForm;

/**
 * Форми відгуків на сторінці книги.
 *   POST /comments          -> додати, redirect:/books/{bookId}
 *   POST /comments/delete   -> видалити, redirect:/books/{bookId}
 * Уся валідація — у CommentService; винятки видалення обробляє GlobalApiExceptionHandler.
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
                      BindingResult binding,            // має йти одразу після об'єкта форми
                      Principal principal,
                      Model model,
                      RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            bookPage.fill(model, bookId, form);   // показати сторінку книги з помилками у формі
            return "book";
        }
        Comment saved;
        try {
            // автор відгуку — користувач, що увійшов у систему
            saved = comments.add(bookId, principal.getName(), form.getText());
        } catch (InvalidCommentTextException e) {
            binding.rejectValue("text", "error.forbiddenWords");   // заборонені слова -> помилка під полем
            bookPage.fill(model, bookId, form);
            return "book";
        }
        log.info("Comment created: id={}, bookId={}, author='{}', length={}",
                saved.getId(), bookId, saved.getAuthor(), saved.getText().length());
        redirect.addFlashAttribute("message", "msg.commentAdded");
        return "redirect:/books/" + bookId;
    }

    @PostMapping("/delete")
    public String delete(@RequestParam("bookId") long bookId,
                         @RequestParam("commentId") long commentId,
                         @RequestParam(name = "createdAt", required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAt,
                         RedirectAttributes redirect) {
        // перевірки (id, createdAt, правило 24 годин) виконує сервіс і кидає власні винятки
        comments.delete(bookId, commentId, createdAt);
        log.info("Comment deleted: id={}, bookId={}", commentId, bookId);
        redirect.addFlashAttribute("message", "msg.commentDeleted");
        return "redirect:/books/" + bookId;
    }
}
