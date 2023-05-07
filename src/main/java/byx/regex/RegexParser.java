package byx.regex;

import static byx.regex.Regex.*;

/**
 * 将正则表达式字符串解析成Regex
 */
public class RegexParser {
    public static Regex parse(String expr) throws RegexParseException {
        try {
            return parseExpr(new Cursor(expr, 0)).regex;
        } catch (RegexParseException e) {
            throw e;
        } catch (Exception e) {
            throw new RegexParseException("unknown error: " + e.getMessage(), e);
        }
    }

    private static class ParseResult {
        private final Regex regex;
        private final Cursor remain;

        private ParseResult(Regex regex, Cursor remain) {
            this.regex = regex;
            this.remain = remain;
        }
    }

    private static Cursor read(Cursor cursor, char c) {
        if (cursor.end() || cursor.current() != c) {
            throw new RegexParseException("expected: " + c);
        }
        return cursor.next();
    }

    // elem = char | (expr)
    private static ParseResult parseElem(Cursor cursor) {
        if (cursor.current() == '(') {
            ParseResult r = parseExpr(cursor.next());
            return new ParseResult(r.regex, read(r.remain, ')'));
        } else if (cursor.current() == '[') {
            return parseRange(cursor);
        } else if (cursor.current() == '.') {
            return new ParseResult(any(), cursor.next());
        } else if (cursor.current() == '\\') {
            cursor = cursor.next();
            return new ParseResult(ch(cursor.current()), cursor.next());
        } else {
            return new ParseResult(ch(cursor.current()), cursor.next());
        }
    }

    // factor = elem* | elem+ | elem
    private static ParseResult parseFactor(Cursor cursor) {
        ParseResult r = parseElem(cursor);
        Regex regex = r.regex;
        cursor = r.remain;

        if (!cursor.end() && cursor.current() == '*') {
            regex = regex.many();
            cursor = cursor.next();
        } else if (!cursor.end() && cursor.current() == '+') {
            regex = regex.many1();
            cursor = cursor.next();
        }
        return new ParseResult(regex, cursor);
    }

    // term = factor factor ... factor
    private static ParseResult parseTerm(Cursor cursor) {
        ParseResult r = parseFactor(cursor);
        Regex regex = r.regex;
        cursor = r.remain;
        while (!cursor.end() && cursor.current() != ')' && cursor.current() != '|') {
            ParseResult rr = parseFactor(cursor);
            regex = regex.concat(rr.regex);
            cursor = rr.remain;
        }
        return new ParseResult(regex, cursor);
    }

    // expr = term|term|...|term
    private static ParseResult parseExpr(Cursor cursor) {
        ParseResult r = parseTerm(cursor);
        Regex regex = r.regex;
        cursor = r.remain;
        while (!cursor.end() && cursor.current() == '|') {
            ParseResult rr = parseTerm(cursor.next());
            regex = regex.or(rr.regex);
            cursor = rr.remain;
        }
        return new ParseResult(regex, cursor);
    }

    private static ParseResult parseRange(Cursor cursor) {
        ParseResult r = parseRangeItem(read(cursor, '['));
        Regex regex = r.regex;
        cursor = r.remain;
        while (!cursor.end() && cursor.current() != ']') {
            ParseResult rr = parseRangeItem(cursor);
            regex = regex.or(rr.regex);
            cursor = rr.remain;
        }
        return new ParseResult(regex, read(cursor, ']'));
    }

    private static ParseResult parseRangeItem(Cursor cursor) {
        char c1 = cursor.current();
        cursor = cursor.next();
        if (cursor.current() == '-') {
            char c2 = cursor.next().current();
            return new ParseResult(range(c1, c2), cursor.next());
        } else {
            return new ParseResult(ch(c1), cursor);
        }
    }
}
