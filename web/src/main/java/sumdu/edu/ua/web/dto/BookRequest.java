package sumdu.edu.ua.web.dto;

/** Тіло POST /books (Jackson сам перетворює JSON у цей record через @RequestBody). */
public record BookRequest(String title, String author, Integer pubYear) {
}
