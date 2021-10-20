package byx.regex;

import java.util.Collections;
import java.util.Set;

public class Ch implements Regex {
    private final char c;

    public Ch(char c) {
        this.c = c;
    }

    @Override
    public Set<Cursor> parse(Cursor input) {
        if (input.end() || input.current() != c) {
            return Collections.emptySet();
        }
        return Set.of(input.next());
    }

    @Override
    public String toString() {
        return String.format("Ch{c: '%c'}", c);
    }
}
