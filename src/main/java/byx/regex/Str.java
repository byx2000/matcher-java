package byx.regex;

import java.util.Collections;
import java.util.Set;

public class Str implements Regex {
    private final String prefix;

    public Str(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Set<Cursor> parse(Cursor input) {
        for (int i = 0; i < prefix.length(); ++i) {
            if (input.end() || input.current() != prefix.charAt(i)) {
                return Collections.emptySet();
            }
            input = input.next();
        }
        return Set.of(input);
    }
}
