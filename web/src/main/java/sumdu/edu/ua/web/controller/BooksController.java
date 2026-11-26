package sumdu.edu.ua.web.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sumdu.edu.ua.config.LibraryProperties;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.exception.ValidationException;
import sumdu.edu.ua.core.service.BookService;
import sumdu.edu.ua.web.SortParam;
import sumdu.edu.ua.web.dto.BookForm;
import sumdu.edu.ua.web.dto.CommentForm;
import sumdu.edu.ua.web.service.MailService;

/**
 * MVC-контролер сторінок каталогу. Методи повертають ІМ'Я шаблону (View),
 * а дані передають у шаблон через Model.
 *   GET  /books           -> templates/books.html
 *   GET  /books/add       -> templates/book-form.html
 *   POST /books/add       -> збереження, redirect:/books
 *   GET  /books/{id}      -> templates/book.html (книга + відгуки)
 */
@Controller
@RequestMapping("/books")
public class BooksController {
    private static final Logger log = LoggerFactory.getLogger(BooksController.class);

    private final BookService books;
    private final BookPageModel bookPage;
    private final MailService mailService;
    private final int defaultPageSize;

    public BooksController(BookService books, BookPageModel bookPage, MailService mailService,
                           LibraryProperties props) {
        this.books = books;
        this.bookPage = bookPage;
        this.mailService = mailService;
        this.defaultPageSize = props.books().defaultPageSize();
    }

    @GetMapping
    public String list(@RequestParam(name = "q", required = false) String q,
                       @RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "sort", defaultValue = "id,desc") String sort,
                       Model model) {
        var result = books.search(q, SortParam.toPageRequest(page, defaultPageSize, sort));
        model.addAttribute("books", result.getItems());
        model.addAttribute("page", result);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("sort", sort);
        return "books";
    }

    @GetMapping({"/add", "/new"})
    public String showAddForm(Model model) {
        model.addAttribute("bookForm", new BookForm());
        return "book-form";
    }

    @PostMapping({"/add", "/new"})
    public String addBook(@Valid @ModelAttribute("bookForm") BookForm form,
                          BindingResult binding,
                          RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return "book-form";                 // та сама форма з помилками під полями
        }
        try {
            Book saved = books.add(form.getTitle(), form.getAuthor(), form.getPubYear());
            log.info("Book created: id={}, title='{}'", saved.getId(), saved.getTitle());
            try {
                mailService.sendNewBookEmail(saved);       // ЛР7: HTML-лист адміністратору
            } catch (Exception e) {
                log.warn("New book saved, but email was not sent: {}", e.getMessage());
            }
            redirect.addFlashAttribute("message", "msg.bookAdded");
            return "redirect:/books";
        } catch (ValidationException e) {      // правила з core, яких не покриває валідація форми
            binding.reject("error.validation", new Object[]{e.getMessage()}, e.getMessage());
            return "book-form";
        }
    }

    /** Видалення книги: правило в SecurityConfig + @PreAuthorize тут + @RolesAllowed у BookService. */
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteBook(@PathVariable("id") long id, RedirectAttributes redirect) {
        books.delete(id);
        log.info("Book deleted: id={}", id);
        redirect.addFlashAttribute("message", "msg.bookDeleted");
        return "redirect:/books";
    }

    @GetMapping("/{id}")
    public String bookPage(@PathVariable("id") long id, Model model) {
        bookPage.fill(model, id, new CommentForm());
        return "book";
    }
}
