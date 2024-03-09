package byx.matcher;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 解析器组合子
 */
public interface Matcher {
    /**
     * 解析字符串
     * @param s 字符串
     * @param index 当前索引
     * @return 解析后的索引集合
     */
    Set<Integer> parse(String s, int index);

    /**
     * 判断指定字符串是否与当前Matcher匹配
     * @param s 字符串
     * @return 是否匹配
     */
    default boolean match(String s) {
        return parse(s, 0).stream().anyMatch(i -> i == s.length());
    }

    /**
     * 匹配任意单个字符
     */
    Matcher any = ch(c -> true);

    /**
     * 匹配单个指定字符c
     * @param c c
     */
    static Matcher ch(char c) {
        return ch(ch -> ch == c);
    }

    /**
     * 匹配指定字符集内的字符
     * @param chs 字符集
     */
    static Matcher chs(Character... chs) {
        Set<Character> set = Arrays.stream(chs).collect(Collectors.toSet());
        return ch(set::contains);
    }

    /**
     * 匹配不等于指定字符c的字符
     * @param c c
     */
    static Matcher not(char c) {
        return ch(ch -> ch != c);
    }

    /**
     * 匹配处于[c1, c2]或[c2, c1]范围内的单个字符
     * @param c1 c1
     * @param c2 c2
     */
    static Matcher range(char c1, char c2) {
        return ch(c -> (c - c1) * (c - c2) <= 0);
    }

    /**
     * 匹配满足条件的单个字符
     * @param predicate 判断字符是否满足条件
     */
    static Matcher ch(Predicate<Character> predicate) {
        return (s, index) -> {
            if (index < s.length() && predicate.test(s.charAt(index))) {
                return Set.of(index + 1);
            } else {
                return Collections.emptySet();
            }
        };
    }

    /**
     * 匹配字符串str
     * @param str str
     */
    static Matcher str(String str) {
        return (s, index) -> {
            if (s.startsWith(str, index)) {
                return Set.of(index + str.length());
            } else {
                return Collections.emptySet();
            }
        };
    }

    /**
     * 匹配字符串集合
     * @param s1 s1
     * @param s2 s2
     * @param strs strs
     */
    static Matcher strs(String s1, String s2, String... strs) {
        return Arrays.stream(strs).map(Matcher::str).reduce(str(s1).or(str(s2)), Matcher::or);
    }

    /**
     * 惰性Matcher
     * @param supplier 返回Matcher的工厂函数
     */
    static Matcher lazy(Supplier<Matcher> supplier) {
        return (s, index) -> supplier.get().parse(s, index);
    }

    /**
     * 使用and连接多个Matcher
     * @param m1 m1
     * @param m2 m2
     * @param matchers matchers
     */
    static Matcher seq(Matcher m1, Matcher m2, Matcher... matchers) {
        return Arrays.stream(matchers).reduce(m1.and(m2), Matcher::and);
    }

    /**
     * 使用or连接多个Matcher
     * @param m1 m1
     * @param m2 m2
     * @param matchers matchers
     */
    static Matcher oneOf(Matcher m1, Matcher m2, Matcher... matchers) {
        return Arrays.stream(matchers).reduce(m1.or(m2), Matcher::or);
    }

    /**
     * 将当前Matcher连续应用多次，最少应用minTimes次，最多应用maxTimes次
     * @param minTimes minTimes
     * @param maxTimes maxTimes
     */
    default Matcher repeat(int minTimes, int maxTimes) {
        return (s, index) -> {
            // 应用minTimes次
            Set<Integer> set = Set.of(index);
            for (int i = 0; i < minTimes; i++) {
                set = set.stream()
                    .flatMap(idx -> parse(s, idx).stream())
                    .collect(Collectors.toSet());
            }

            // 继续应用直到maxTimes次
            Set<Integer> result = new HashSet<>(set);
            Queue<Integer> queue = new ArrayDeque<>(set);
            int times = minTimes;
            while (!queue.isEmpty() && times < maxTimes) {
                int cnt = queue.size();
                while (cnt-- > 0) {
                    for (int i : parse(s, queue.remove())) {
                        if (!result.contains(i)) {
                            result.add(i);
                            queue.add(i);
                        }
                    }
                }
                times++;
            }

            return result;
        };
    }

    /**
     * 将当前Matcher连续应用times次
     * @param times times
     */
    default Matcher repeat(int times) {
        return repeat(times, times);
    }

    /**
     * 连接两个Matcher
     * @param rhs rhs
     */
    default Matcher and(Matcher rhs) {
        return (s, index) -> {
            Set<Integer> r = new HashSet<>();
            for (int i : parse(s, index)) {
                r.addAll(rhs.parse(s, i));
            }
            return r;
        };
    }

    /**
     * 将c转换成Matcher再与当前Matcher连接
     * @param c c
     */
    default Matcher and(char c) {
        return and(ch(c));
    }

    /**
     * 将s转换成Matcher再与当前Matcher连接
     * @param s s
     */
    default Matcher and(String s) {
        return and(str(s));
    }

    /**
     * 使用or连接两个Matcher
     * @param rhs rhs
     */
    default Matcher or(Matcher rhs) {
        return (s, index) -> {
            Set<Integer> result = new HashSet<>(parse(s, index));
            result.addAll(rhs.parse(s, index));
            return result;
        };
    }

    /**
     * 将c转换成Matcher再与当前Matcher使用or连接
     * @param c c
     */
    default Matcher or(char c) {
        return or(ch(c));
    }

    /**
     * 将s转换成Matcher再与当前Matcher使用or连接
     * @param s s
     */
    default Matcher or(String s) {
        return or(str(s));
    }

    /**
     * 将当前Matcher连续应用至少minTimes次
     * @param minTimes 最少应用次数
     */
    default Matcher many(int minTimes) {
        return (s, index) -> {
            // 应用minTimes次
            Set<Integer> set = Set.of(index);
            for (int i = 0; i < minTimes; i++) {
                set = set.stream()
                    .flatMap(idx -> parse(s, idx).stream())
                    .collect(Collectors.toSet());
            }

            Queue<Integer> queue = new ArrayDeque<>(set);
            Set<Integer> result = new HashSet<>(set);
            while (!queue.isEmpty()) {
                for (int i : parse(s, queue.remove())) {
                    if (!result.contains(i)) {
                        result.add(i);
                        queue.add(i);
                    }
                }
            }

            return result;
        };
    }

    /**
     * 将当前Matcher连续应用0次或多次
     */
    default Matcher many() {
        return many(0);
    }

    /**
     * 将当前Matcher连续应用1次或多次
     */
    default Matcher many1() {
        return many(1);
    }

    /**
     * 应用当前Matcher，并根据解析结果生成下一个Matcher
     * @param mapper 将解析结果映射为下一个Matcher
     */
    default Matcher flatMap(Function<String, Matcher> mapper) {
        return (s, index) -> {
            Set<Integer> result = new HashSet<>();
            parse(s, index).forEach(i -> {
                String matchStr = s.substring(index, i);
                Matcher next = mapper.apply(matchStr);
                result.addAll(next.parse(s, i));
            });
            return result;
        };
    }
}
