package byx.regex;

import static byx.regex.Regex.*;

public class RegexParser {
    private final String expr;
    private int index;

    public RegexParser(String expr) {
        this.expr = expr;
    }

    private void init() {
        index = 0;
    }

    private char peek() {
        return expr.charAt(index);
    }

    private char next() {
        return expr.charAt(index++);
    }

    private void read(char c) throws RegexParseException {
        if (c != next()) {
            throw new RegexParseException("expected: " + c);
        }
    }

    private boolean end() {
        return index == expr.length();
    }

    public Regex parse() throws RegexParseException {
        init();
        try {
            return parseExpr();
        } catch (RegexParseException e) {
            throw e;
        } catch (Exception e) {
            throw new RegexParseException("unknown error: " + e.getMessage());
        }

    }

    // elem = char | (expr)
    private Regex parseElem() throws RegexParseException {
        if (peek() == '(') {
            next();
            Regex r = parseExpr();
            read(')');
            return r;
        } else if (peek() == '[') {
            return parseRange();
        } else if (peek() == '.') {
            next();
            return any();
        } else if (peek() == '\\') {
            next();
            return ch(next());
        } else {
            return ch(next());
        }
    }

    // factor = elem* | elem+ | elem
    private Regex parseFactor() throws RegexParseException {
        Regex r = parseElem();
        if (!end() && peek() == '*') {
            r = r.zeroOrMore();
            next();
        } else if (!end() && peek() == '+') {
            r = r.oneOrMore();
            next();
        }
        return r;
    }

    // term = factor factor ... factor
    private Regex parseTerm() throws RegexParseException {
        Regex r = parseFactor();
        while (!end() && peek() != ')' && peek() != '|') {
            r = r.concat(parseFactor());
        }
        return r;
    }

    // expr = term|term|...|term
    private Regex parseExpr() throws RegexParseException {
        Regex r = parseTerm();
        while (!end() && peek() == '|') {
            next();
            r = r.or(parseTerm());
        }
        return r;
    }

    private Regex parseRange() throws RegexParseException {
        read('[');
        Regex r = parseRangeItem();
        while (!end() && peek() != ']') {
            r = r.or(parseRangeItem());
        }
        read(']');
        return r;
    }

    private Regex parseRangeItem() throws RegexParseException {
        char c1 = next();
        if (peek() == '-') {
            next();
            char c2 = next();
            return range(c1, c2);
        } else {
            return ch(c1);
        }
    }
}
