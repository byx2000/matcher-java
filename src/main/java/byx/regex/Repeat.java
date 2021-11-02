package byx.regex;

import java.util.*;

public class Repeat implements Regex {
    private final Regex regex;
    private final int minTimes, maxTimes;

    public Repeat(Regex regex, int minTimes, int maxTimes) {
        this.regex = regex;
        this.minTimes = minTimes;
        this.maxTimes = maxTimes;
    }

    @Override
    public Set<Cursor> parse(Cursor input) {
        Set<Cursor> results = new HashSet<>();
        Queue<Cursor> queue = new LinkedList<>(List.of(input));

        // 消耗minTimes次
        int times = 0;
        while (!queue.isEmpty() && times < minTimes) {
            int cnt = queue.size();
            while (cnt-- > 0) {
                Cursor c = queue.remove();
                queue.addAll(regex.parse(c));
            }
            times++;
        }

        Set<Cursor> set = new HashSet<>(queue);
        queue = new LinkedList<>(set);

        // 继续消耗直到maxTimes次
        while (!queue.isEmpty() && (maxTimes < 0 || times <= maxTimes)) {
            int cnt = queue.size();
            while (cnt-- > 0) {
                Cursor c = queue.remove();
                results.add(c);
                for (Cursor cc : regex.parse(c)) {
                    if (!set.contains(cc)) {
                        set.add(cc);
                        queue.add(cc);
                    }
                }
            }
            times++;
        }

        return results;
    }
}
