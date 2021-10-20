package byx.regex;

import java.util.*;

public class OneOrMore implements Regex {
    private final Regex parser;

    public OneOrMore(Regex parser) {
        this.parser = parser;
    }

    @Override
    public Set<Cursor> parse(Cursor input) {
        Set<Cursor> result = new HashSet<>();
        Set<Cursor> start = parser.parse(input);
        Queue<Cursor> queue = new LinkedList<>(start);
        Set<Cursor> set = new HashSet<>(start);

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
        return String.format("OneOrMore{parser: %s}", parser);
    }
}
