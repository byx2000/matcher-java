package byx.regex;

import java.util.*;

/**
 * 解析器组合子
 */
public interface Regex {
    /**
     * 从指定光标处解析
     * @param cursor 光标位置
     * @return 解析后的光标位置集合
     */
    Set<Cursor> parse(Cursor cursor);

    /**
     * 判断指定字符串是否与当前Regex匹配
     * @param input 输入字符串
     * @return 匹配结果
     */
    default boolean match(String input) {
        return parse(new Cursor(input, 0)).stream().anyMatch(Cursor::end);
    }

    /**
     * 从字符串构造Regex
     * @param expr 表达式
     */
    static Regex of(String expr) throws RegexParseException {
        return RegexParser.parse(expr);
    }

    /**
     * 匹配任意单个字符
     */
    static Regex any() {
        return cursor -> {
            if (cursor.end()) {
                return Collections.emptySet();
            }
            return Set.of(cursor.next());
        };
    }

    /**
     * 匹配单个指定字符c
     * @param c c
     */
    static Regex ch(char c) {
        return cursor -> {
            if (cursor.end() || cursor.current() != c) {
                return Collections.emptySet();
            }
            return Set.of(cursor.next());
        };
    }

    /**
     * 匹配处于[c1, c2]或[c2, c1]范围内的单个字符
     * @param c1 c1
     * @param c2 c2
     */
    static Regex range(char c1, char c2) {
        return cursor -> {
            if (cursor.end()) {
                return Collections.emptySet();
            }
            char c = cursor.current();
            if ((c - c1) * (c - c2) > 0) {
                return Collections.emptySet();
            }
            return Set.of(cursor.next());
        };
    }

    /**
     * 匹配字符串prefix
     * @param prefix prefix
     */
    static Regex str(String prefix) {
        return cursor -> {
            for (int i = 0; i < prefix.length(); ++i) {
                if (cursor.end() || cursor.current() != prefix.charAt(i)) {
                    return Collections.emptySet();
                }
                cursor = cursor.next();
            }
            return Set.of(cursor);
        };
    }

    /**
     * 将当前Regex连续应用多次，最少应用minTimes次，最多应用maxTimes次
     * <p>minTimes=-1表示可以应用0次</p>
     * <p>maxTimes=-1表示应用次数无上限</p>
     * @param minTimes minTimes
     * @param maxTimes maxTimes
     */
    default Regex repeat(int minTimes, int maxTimes) {
        return cursor -> {
            Set<Cursor> results = new HashSet<>();
            Queue<Cursor> queue = new LinkedList<>(List.of(cursor));

            // 消耗minTimes次
            int times = 0;
            while (!queue.isEmpty() && times < minTimes) {
                int cnt = queue.size();
                while (cnt-- > 0) {
                    Cursor c = queue.remove();
                    queue.addAll(parse(c));
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
                    for (Cursor cc : parse(c)) {
                        if (!set.contains(cc)) {
                            set.add(cc);
                            queue.add(cc);
                        }
                    }
                }
                times++;
            }

            return results;
        };
    }

    /**
     * 将当前Regex连续应用times次
     * @param times times
     */
    default Regex repeat(int times) {
        return repeat(times, times);
    }

    /**
     * 连接两个Regex
     * @param rhs rhs
     */
    default Regex concat(Regex rhs) {
        return cursor -> {
            Set<Cursor> r = new HashSet<>();
            for (Cursor c : parse(cursor)) {
                r.addAll(rhs.parse(c));
            }
            return r;
        };
    }

    /**
     * 使用or连接两个Regex
     * @param rhs rhs
     */
    default Regex or(Regex rhs) {
        return cursor -> {
            Set<Cursor> result = new HashSet<>(parse(cursor));
            result.addAll(rhs.parse(cursor));
            return result;
        };
    }

    /**
     * 将当前Regex连续应用0次或多次
     */
    default Regex many() {
        return repeat(0, -1);
    }

    /**
     * 将当前Regex连续应用1次或多次
     */
    default Regex many1() {
        return repeat(1, -1);
    }
}
