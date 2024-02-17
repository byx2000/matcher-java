package byx.regex;

import java.util.concurrent.atomic.AtomicInteger;

import static byx.regex.Regex.*;

/**
 * 将正则表达式字符串解析成Regex
 */
public class RegexParser {
    public static Regex parse(String expr) {
        try {
            return parseExpr(expr, new AtomicInteger(0));
        }
        catch (RuntimeException e) {
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
    private static Regex parseExpr(String expr, AtomicInteger index) {
        Regex r = parseTerm(expr, index);
        while (index.get() < expr.length() && expr.charAt(index.get()) == '|') {
            index.incrementAndGet();
            r = r.or(parseTerm(expr, index));
        }
        return r;
    }

    // term = factor+
    private static Regex parseTerm(String expr, AtomicInteger index) {
        Regex r = parseFactor(expr, index);
        while (index.get() < expr.length() && expr.charAt(index.get()) != ')' && expr.charAt(index.get()) != '|') {
            r = r.concat(parseFactor(expr, index));
        }
        return r;
    }

    // factor = elem '*'
    //        | elem '+'
    //        | elem
    private static Regex parseFactor(String expr, AtomicInteger index) {
        Regex r = parseElem(expr, index);
        if (index.get() < expr.length()) {
            if (expr.charAt(index.get()) == '*') {
                index.incrementAndGet();
                return r.many();
            } else if (expr.charAt(index.get()) == '+') {
                index.incrementAndGet();
                return r.many1();
            }
        }
        return r;
    }

    // elem = '(' expr ')'
    //      | '[' range ']'
    //      | '.'
    //      | '\' char
    //      | char
    private static Regex parseElem(String expr, AtomicInteger index) {
        switch (expr.charAt(index.get())) {
            case '(' -> {
                index.incrementAndGet();
                Regex r = parseExpr(expr, index);
                read(expr, index, ')');
                return r;
            }
            case '[' -> {
                index.incrementAndGet();
                Regex r = parseRange(expr, index);
                read(expr, index, ']');
                return r;
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
    private static Regex parseRange(String expr, AtomicInteger index) {
        Regex r = parseRangeItem(expr, index);
        while (index.get() < expr.length() && expr.charAt(index.get()) != ']') {
            r = r.or(parseRangeItem(expr, index));
        }
        return r;
    }

    // rangeItem = char '-' char
    //           | char
    private static Regex parseRangeItem(String expr, AtomicInteger index) {
        char c = expr.charAt(index.getAndIncrement());
        if (expr.charAt(index.get()) == '-') {
            index.incrementAndGet();
            return range(c, expr.charAt(index.getAndIncrement()));
        } else {
            return ch(c);
        }
    }
}
