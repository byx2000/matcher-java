package byx.regex;

import java.util.Collections;
import java.util.Set;

public class Range implements Regex {
    private final char c1, c2;

    public Range(char c1, char c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    @Override
    public Set<Cursor> parse(Cursor input) {
        if (input.end()) {
            return Collections.emptySet();
        }
        char c = input.current();
        if ((c - c1) * (c - c2) > 0) {
            return Collections.emptySet();
        }
        return Set.of(input.next());
    }
}
