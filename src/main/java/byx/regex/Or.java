package byx.regex;

import java.util.HashSet;
import java.util.Set;

public class Or implements Regex {
    private final Regex lhs, rhs;

    public Or(Regex lhs, Regex rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public Set<Cursor> parse(Cursor input) {
        Set<Cursor> result = new HashSet<>(lhs.parse(input));
        result.addAll(rhs.parse(input));
        return result;
    }

    @Override
    public String toString() {
        return String.format("Or{lhs: %s, rhs: %s}", lhs, rhs);
    }
}
