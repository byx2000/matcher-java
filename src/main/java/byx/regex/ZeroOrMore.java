package byx.regex;

import java.util.*;

public class ZeroOrMore implements Regex {
    private final Regex parser;

    public ZeroOrMore(Regex parser) {
        this.parser = parser;
    }

    @Override
    public Set<Cursor> parse(Cursor input) {
        Set<Cursor> result = new HashSet<>();
        Queue<Cursor> queue = new LinkedList<>(List.of(input));
        Set<Cursor> set = new HashSet<>(Set.of(input));

        while (!queue.isEmpty()) {
            int cnt = queue.size();
            while (cnt-- > 0) {
                Cursor cursor = queue.remove();
                result.add(cursor);
                for (Cursor c : parser.parse(cursor)) {
                    if (!set.contains(c)) {
                        queue.add(c);
                        set.add(c);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return String.format("ZeroOrMore{parser: %s}", parser);
    }
}
