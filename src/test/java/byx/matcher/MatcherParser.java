package byx.matcher;

import java.util.concurrent.atomic.AtomicInteger;

import static byx.matcher.Matcher.*;

/**
 * 将正则表达式字符串解析成Matcher
 */
public class MatcherParser {
    public static Matcher parse(String expr) {
        try {
            return parseExpr(expr, new AtomicInteger(0));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unknown parse error", e);
        }
    }

    private static void read(String expr, AtomicInteger index, char c) {
        if (index.get() == expr.length() || expr.charAt(index.get()) != c) {
            throw new RuntimeException("expected: " + c);
        }
        index.incrementAndGet();
    }

    // expr = term ('|' term)*
    private static Matcher parseExpr(String expr, AtomicInteger index) {
        Matcher m = parseTerm(expr, index);
        while (index.get() < expr.length() && expr.charAt(index.get()) == '|') {
            index.incrementAndGet();
            m = m.or(parseTerm(expr, index));
        }
        return m;
    }

    // term = factor+
    private static Matcher parseTerm(String expr, AtomicInteger index) {
        Matcher m = parseFactor(expr, index);
        while (index.get() < expr.length() && expr.charAt(index.get()) != ')' && expr.charAt(index.get()) != '|') {
            m = m.and(parseFactor(expr, index));
        }
        return m;
    }

    // factor = elem '*'
    //        | elem '+'
    //        | elem
    private static Matcher parseFactor(String expr, AtomicInteger index) {
        Matcher m = parseElem(expr, index);
        if (index.get() < expr.length()) {
            if (expr.charAt(index.get()) == '*') {
                index.incrementAndGet();
                return m.many();
            } else if (expr.charAt(index.get()) == '+') {
                index.incrementAndGet();
                return m.many1();
            }
        }
        return m;
    }

    // elem = '(' expr ')'
    //      | '[' range ']'
    //      | '.'
    //      | '\' char
    //      | char
    private static Matcher parseElem(String expr, AtomicInteger index) {
        switch (expr.charAt(index.get())) {
            case '(' -> {
                index.incrementAndGet();
                Matcher m = parseExpr(expr, index);
                read(expr, index, ')');
                return m;
            }
            case '[' -> {
                index.incrementAndGet();
                Matcher m = parseRange(expr, index);
                read(expr, index, ']');
                return m;
            }
            case '.' -> {
                index.incrementAndGet();
                return any();
            }
            case '\\' -> {
                index.incrementAndGet();
                return ch(expr.charAt(index.getAndIncrement()));
            }
            default -> {
                return ch(expr.charAt(index.getAndIncrement()));
            }
        }
    }

    // range = rangeItem+
    private static Matcher parseRange(String expr, AtomicInteger index) {
        Matcher m = parseRangeItem(expr, index);
        while (index.get() < expr.length() && expr.charAt(index.get()) != ']') {
            m = m.or(parseRangeItem(expr, index));
        }
        return m;
    }

    // rangeItem = char '-' char
    //           | char
    private static Matcher parseRangeItem(String expr, AtomicInteger index) {
        char c = expr.charAt(index.getAndIncrement());
        if (expr.charAt(index.get()) == '-') {
            index.incrementAndGet();
            return range(c, expr.charAt(index.getAndIncrement()));
        } else {
            return ch(c);
        }
    }
}
