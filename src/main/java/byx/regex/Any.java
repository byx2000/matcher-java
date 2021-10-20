package byx.regex;

import java.util.Collections;
import java.util.Set;

public class Any implements Regex {
    @Override
    public Set<Cursor> parse(Cursor input) {
        if (input.end()) {
            return Collections.emptySet();
        }
        return Set.of(input.next());
    }

    @Override
    public String toString() {
        return "Any{}";
    }
}
