package byx.regex;

public class RegexParseException extends RuntimeException {
    public RegexParseException(String msg) {
        super(msg);
    }

    public RegexParseException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
