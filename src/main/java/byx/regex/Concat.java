package byx.regex;

import java.util.HashSet;
import java.util.Set;

public class Concat implements Regex {
    private final Regex lhs, rhs;

    public Concat(Regex lhs, Regex rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public Set<Cursor> parse(Cursor input) {
        Set<Cursor> r = new HashSet<>();
        for (Cursor c : lhs.parse(input)) {
            r.addAll(rhs.parse(c));
        }
        return r;
    }

    @Override
    public String toString() {
        return String.format("Concat{lhs: %s, rhs: %s}", lhs, rhs);
    }
}
