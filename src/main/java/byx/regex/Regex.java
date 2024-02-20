package byx.regex;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
     * 匹配任意单个字符
     */
    static Regex any() {
        return ch(c -> true);
    }

    /**
     * 匹配单个指定字符c
     * @param c c
     */
    static Regex ch(char c) {
        return ch(ch -> ch == c);
    }

    /**
     * 匹配指定字符集内的字符
     * @param chs 字符集
     */
    static Regex chs(Character... chs) {
        Set<Character> set = Arrays.stream(chs).collect(Collectors.toSet());
        return ch(set::contains);
    }

    /**
     * 匹配不等于指定字符c的字符
     * @param c c
     */
    static Regex not(char c) {
        return ch(ch -> ch != c);
    }

    /**
     * 匹配处于[c1, c2]或[c2, c1]范围内的单个字符
     * @param c1 c1
     * @param c2 c2
     */
    static Regex range(char c1, char c2) {
        return ch(c -> (c - c1) * (c - c2) <= 0);
    }

    /**
     * 匹配满足条件的单个字符
     * @param predicate 判断字符是否满足条件
     */
    static Regex ch(Predicate<Character> predicate) {
        return cursor -> {
            if (cursor.end() || !predicate.test(cursor.current())) {
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
     * 惰性Regex
     * @param supplier 返回Regex的工厂函数
     */
    static Regex lazy(Supplier<Regex> supplier) {
        return cursor -> supplier.get().parse(cursor);
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
            // 消耗minTimes次
            Set<Cursor> set = new HashSet<>(List.of(cursor));
            for (int i = 0; i < minTimes; i++) {
                set = set.stream().flatMap(c -> parse(c).stream()).collect(Collectors.toSet());
            }

            // 继续消耗直到maxTimes次
            Set<Cursor> results = new HashSet<>(set);
            for (int i = 0; i < maxTimes - minTimes; i++) {
                set = set.stream().flatMap(c -> parse(c).stream()).collect(Collectors.toSet());
                results.addAll(set);
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
    default Regex and(Regex rhs) {
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
     * 将当前Regex连续应用至少minTimes次
     * @param minTimes 最少应用次数
     */
    default Regex many(int minTimes) {
        return cursor -> {
            Set<Cursor> result = new HashSet<>(Set.of(cursor));
            for (int i = 0; i < minTimes; i++) {
                result = result.stream()
                    .flatMap(c -> parse(c).stream())
                    .collect(Collectors.toSet());
            }

            Queue<Cursor> queue = new ArrayDeque<>(result);
            while (!queue.isEmpty()) {
                int cnt = queue.size();
                while (cnt-- > 0) {
                    for (Cursor c : parse(queue.remove())) {
                        if (!result.contains(c)) {
                            result.add(c);
                            queue.add(c);
                        }
                    }
                }
            }

            return result;
        };
    }

    /**
     * 将当前Regex连续应用0次或多次
     */
    default Regex many() {
        return many(0);
    }

    /**
     * 将当前Regex连续应用1次或多次
     */
    default Regex many1() {
        return many(1);
    }

    /**
     * 应用当前Regex，并根据解析结果生成下一个Regex
     * @param mapper 将解析结果映射为下一个Regex
     */
    default Regex flatMap(Function<String, Regex> mapper) {
        return cursor -> {
            Set<Cursor> result = new HashSet<>();
            parse(cursor).forEach(c -> {
                String matchStr = cursor.input().substring(cursor.index(), c.index());
                Regex next = mapper.apply(matchStr);
                result.addAll(next.parse(c));
            });
            return result;
        };
    }
}
