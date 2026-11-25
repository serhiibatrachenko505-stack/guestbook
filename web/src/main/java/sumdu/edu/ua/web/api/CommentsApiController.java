package sumdu.edu.ua.web.api;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.exception.ValidationException;
import sumdu.edu.ua.core.service.CommentService;
import sumdu.edu.ua.web.dto.CommentRequest;

/**
 * REST API відгуків (JSON).
 * GET    /api/comments?bookId=1&author=&since=&page=&size=
 * POST   /api/comments                     {"bookId":1,"author":"..","text":".."} -> 201
 * DELETE /api/comments/{commentId}?bookId=1 -> 204 (409, якщо старший за 24 год)
 */
@RestController
@RequestMapping("/api/comments")
public class CommentsApiController {
    private static final Logger log = LoggerFactory.getLogger(CommentsApiController.class);

    private final CommentService comments;

    public CommentsApiController(CommentService comments) {
        this.comments = comments;
    }

    @GetMapping
    public ResponseEntity<Page<Comment>> list(
            @RequestParam("bookId") long bookId,
            @RequestParam(name = "author", required = false) String author,
            @RequestParam(name = "since", required = false) Instant since,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(comments.list(bookId, author, since, new PageRequest(page, size)));
    }

    @PostMapping
    public ResponseEntity<Comment> add(@RequestBody CommentRequest req) {
        if (req.bookId() == null) {
            throw new ValidationException("bookId is required");
        }
        Comment saved = comments.add(req.bookId(), req.author(), req.text());
        log.info("Comment created: id={}, bookId={}, author='{}', length={}",
                saved.getId(), saved.getBookId(), saved.getAuthor(), saved.getText().length());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable("commentId") long commentId,
                                       @RequestParam("bookId") long bookId) {
        Comment deleted = comments.delete(bookId, commentId);   // правило 24 годин — у core
        log.info("Comment deleted: id={}, bookId={}, author='{}'",
                deleted.getId(), deleted.getBookId(), deleted.getAuthor());
        return ResponseEntity.noContent().build();
    }
}
