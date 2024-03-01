package byx.matcher;

import org.junit.jupiter.api.Test;

import static byx.matcher.Matcher.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 括号匹配校验
 * expr = term+
 * term = "()"
 *      | '(' expr ')'
 */
class BracketMatcher {
    private static final Matcher term = oneOf(
        str("()"),
        ch('(').and(lazy(() -> BracketMatcher.expr)).and(')')
    );
    private static final Matcher expr = term.many1();

    public static boolean isBracketMatch(String s) {
        return expr.match(s);
    }
}

/**
 * 算数表达式校验
 * expr = term ('+'|'-' term)+
 * term = fact ('*'|'/' fact)+
 * fact = [0-9]+
 *      | '-' fact
 *      | '(' expr ')'
 */
class ArithmeticExprValidator {
    private static final Matcher fact = oneOf(
        range('0', '9').many1(),
        ch('-').and(lazy(() -> ArithmeticExprValidator.fact)),
        ch('(').and(lazy(() -> ArithmeticExprValidator.expr)).and(')')
    );
    private static final Matcher term = fact.and(chs('*', '/').and(fact).many());
    private static final Matcher expr = term.and(chs('+', '-').and(term).many());

    public static boolean isValidExpr(String s) {
        return expr.match(s);
    }
}

/**
 * json字符串校验
 * jsonObj = number | string | bool | arr | obj
 * number  = integer | decimal
 * integer = [0-9]+
 * decimal = [0-9]+ '.' [0-9]+
 * string  = '"' (.*) '"'
 * bool    = "true" | "false"
 * arr     = "[]"
 *         | '[' jsonObj (',' jsonObj)* ']'
 * field   = string ':' jsonObj
 * obj     = "{}"
 *         | '{' field (',' field)* '}'
 */
class JsonValidator {
    private static final Matcher blank = chs(' ', '\t', '\n', '\r').many();
    private static final Matcher objStart = withBlank(ch('{'));
    private static final Matcher objEnd = withBlank(ch('}'));
    private static final Matcher arrStart = withBlank(ch('['));
    private static final Matcher arrEnd = withBlank(ch(']'));
    private static final Matcher colon = withBlank(ch(':'));
    private static final Matcher comma = withBlank(ch(','));

    private static final Matcher jsonObj = oneOf(
        lazy(() -> JsonValidator.number),
        lazy(() -> JsonValidator.string),
        lazy(() -> JsonValidator.bool),
        lazy(() -> JsonValidator.arr),
        lazy(() -> JsonValidator.obj)
    );
    private static final Matcher digits = range('0', '9').many1();
    private static final Matcher integer = digits;
    private static final Matcher decimal = seq(digits, ch('.'), digits);
    private static final Matcher number = integer.or(decimal);
    private static final Matcher string = seq(ch('"'), not('"').many(), ch('"'));
    private static final Matcher bool = strs("true", "false");
    private static final Matcher arr = oneOf(
        arrStart.and(arrEnd),
        seq(arrStart, jsonObj.and(comma.and(jsonObj).many()), arrEnd)
    );
    private static final Matcher field = seq(string, colon, jsonObj);
    private static final Matcher obj = oneOf(
        objStart.and(objEnd),
        seq(objStart, field.and(comma.and(field).many()), objEnd)
    );

    private static Matcher withBlank(Matcher m) {
        return seq(blank, m, blank);
    }

    public static boolean isValidJson(String s) {
        return jsonObj.match(s);
    }
}

public class RecursiveTest {
    @Test
    public void testBracketMatcher() {
        assertFalse(BracketMatcher.isBracketMatch(""));
        assertFalse(BracketMatcher.isBracketMatch("("));
        assertFalse(BracketMatcher.isBracketMatch(")"));
        assertTrue(BracketMatcher.isBracketMatch("()"));
        assertFalse(BracketMatcher.isBracketMatch(")("));
        assertFalse(BracketMatcher.isBracketMatch("(("));
        assertFalse(BracketMatcher.isBracketMatch("))"));
        assertTrue(BracketMatcher.isBracketMatch("()()"));
        assertTrue(BracketMatcher.isBracketMatch("(())"));
        assertFalse(BracketMatcher.isBracketMatch("(()"));
        assertFalse(BracketMatcher.isBracketMatch("())"));
        assertTrue(BracketMatcher.isBracketMatch("()()()"));
        assertTrue(BracketMatcher.isBracketMatch("()(())"));
        assertTrue(BracketMatcher.isBracketMatch("(())()"));
        assertTrue(BracketMatcher.isBracketMatch("(()())()"));
        assertTrue(BracketMatcher.isBracketMatch("(())()((()))()"));
        assertFalse(BracketMatcher.isBracketMatch("(())()((())()"));
        assertFalse(BracketMatcher.isBracketMatch("(())()(()))()"));
    }

    @Test
    public void testArithmeticExprValidator() {
        assertFalse(ArithmeticExprValidator.isValidExpr(""));
        assertTrue(ArithmeticExprValidator.isValidExpr("123"));
        assertTrue(ArithmeticExprValidator.isValidExpr("-6"));
        assertTrue(ArithmeticExprValidator.isValidExpr("2*(3+4)"));
        assertFalse(ArithmeticExprValidator.isValidExpr("abc"));
        assertFalse(ArithmeticExprValidator.isValidExpr("12+"));
        assertFalse(ArithmeticExprValidator.isValidExpr("12*"));
        assertFalse(ArithmeticExprValidator.isValidExpr("+3"));
        assertFalse(ArithmeticExprValidator.isValidExpr("/6"));
        assertFalse(ArithmeticExprValidator.isValidExpr("6+3-"));
        assertTrue(ArithmeticExprValidator.isValidExpr("(12+345)*(67-890)+10/6"));
        assertTrue(ArithmeticExprValidator.isValidExpr("-6*18+(-3/978)"));
        assertTrue(ArithmeticExprValidator.isValidExpr("24/5774*(6/357+637)-2*7/52+5"));
        assertFalse(ArithmeticExprValidator.isValidExpr("24/5774*(6/357+637-2*7/52+5"));
        assertTrue(ArithmeticExprValidator.isValidExpr("7758*(6/314+552234)-2*61/(10+2/(40-38*5))"));
        assertFalse(ArithmeticExprValidator.isValidExpr("7758*(6/314+552234)-2*61/(10+2/40-38*5))"));
    }

    @Test
    public void testJsonValidator() {
        assertTrue(JsonValidator.isValidJson("""
            {
                "a": 123,
                "b": 3.14,
                "c": "hello",
                "d": {
                    "x": 100,
                    "y": "world!"
                },
                "e": [
                    12,
                    34.56,
                    {
                        "name": "Xiao Ming",
                        "age": 18,
                        "score": [99.8, 87.5, 60.0]
                    },
                    "abc"
                ],
                "f": [],
                "g": {},
                "h": [true, {"m": false}]
            }"""));
        assertTrue(JsonValidator.isValidJson("123"));
        assertTrue(JsonValidator.isValidJson("34.56"));
        assertTrue(JsonValidator.isValidJson("\"hello\""));
        assertTrue(JsonValidator.isValidJson("true"));
        assertTrue(JsonValidator.isValidJson("false"));
        assertTrue(JsonValidator.isValidJson("{}"));
        assertTrue(JsonValidator.isValidJson("[]"));
        assertTrue(JsonValidator.isValidJson("[{}]"));
        assertFalse(JsonValidator.isValidJson(""));
        assertFalse(JsonValidator.isValidJson("{"));
        assertFalse(JsonValidator.isValidJson("}"));
        assertFalse(JsonValidator.isValidJson("{}}"));
        assertFalse(JsonValidator.isValidJson("[1, 2 3]"));
        assertFalse(JsonValidator.isValidJson("{1, 2, 3}"));
    }
}
