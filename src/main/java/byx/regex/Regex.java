package byx.regex;

import java.util.Set;

public interface Regex {
    Set<Cursor> parse(Cursor input);

    default boolean match(String input) {
        return parse(new Cursor(input, 0)).stream().anyMatch(Cursor::end);
    }

    static Regex of(String expr) throws RegexParseException {
        return new RegexParser(expr).parse();
    }

    static Regex any() {
        return new Any();
    }

    static Regex ch(char c) {
        return new Ch(c);
    }

    static Regex range(char c1, char c2) {
        return new Range(c1, c2);
    }

    default Regex repeat(int minTimes, int maxTimes) {
        return new Repeat(this, minTimes, maxTimes);
    }

    default Regex repeat(int times) {
        return repeat(times, times);
    }

    static Regex str(String prefix) {
        return new Str(prefix);
    }

    default Regex concat(Regex rhs) {
        return new Concat(this, rhs);
    }

    default Regex or(Regex rhs) {
        return new Or(this, rhs);
    }

    default Regex zeroOrMore() {
        return repeat(0, -1);
    }

    default Regex oneOrMore() {
        return repeat(1, -1);
    }
}
