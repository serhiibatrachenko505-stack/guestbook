package sumdu.edu.ua.web.error;

/** Помилка рівня HTTP (невідомий маршрут, неправильний метод, некоректний параметр). */
public class HttpError extends RuntimeException {
    private final int status;

    public HttpError(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public static HttpError badRequest(String message) {
        return new HttpError(400, message);
    }

    public static HttpError notFound(String message) {
        return new HttpError(404, message);
    }

    public static HttpError methodNotAllowed(String method) {
        return new HttpError(405, "Method " + method + " is not allowed here");
    }
}
