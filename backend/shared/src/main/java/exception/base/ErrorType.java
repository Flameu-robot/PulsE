package exception.base;

public enum ErrorType {

    BAD_REQUEST(400),
    AUTHENTICATION(401),
    PAYMENT_REQUIRED(402),
    AUTHORIZATION(403),
    NOT_FOUND(404),
    CONFLICT(409),
    GONE(410),
    VALIDATION(422),
    TOO_MANY_REQUESTS(429),
    INTERNAL(500),
    SERVICE_UNAVAILABLE(503);

    private final int defaultStatus;

    ErrorType(int defaultStatus) {
        this.defaultStatus = defaultStatus;
    }

    public int getDefaultStatus() {
        return defaultStatus;
    }
}