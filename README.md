# matcher-java

matcher-java是一个基于解析器组合子的字符串匹配库：

* 支持正则表达式的常用操作
* 支持递归定义的语法规则，如括号匹配和算术表达式校验等

## 示例1：正则表达式

```java
// a(mn|xy)[0-9]+
Matcher m = ch('a').and(str("mn").or("xy")).and(range('0', '9').many1());

System.out.println(m.match("amn12345")); // true
System.out.println(m.match("axy123")); // true
System.out.println(m.match("ax12345")); // false
System.out.println(m.match("amnxy")); // false
```

## 示例2：括号匹配

```java
/**
 * 括号匹配校验
 * expr = term+
 * term = "()"
 *      | '(' expr ')'
 */
public class BracketMatcher {
    private static final Matcher term = oneOf(
        str("()"),
        ch('(').and(lazy(() -> BracketMatcher.expr)).and(')')
    );
    private static final Matcher expr = term.many1();

    public static boolean isBracketMatch(String s) {
        return expr.match(s);
    }
}

System.out.println(BracketMatcher.isBracketMatch("(())()")); // true  
System.out.println(BracketMatcher.isBracketMatch("(()()")); // false
```

## 示例3：算数表达式校验

```java
/**
 * 算数表达式校验
 * expr = term ('+'|'-' term)+
 * term = fact ('*'|'/' fact)+
 * fact = [0-9]+
 *      | '-' fact
 *      | '(' expr ')'
 */
public class ArithmeticExprValidator {
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

System.out.println(ArithmeticExprValidator.isValidExpr("(12+345)*(67-890)+10/6")); // true
System.out.println(ArithmeticExprValidator.isValidExpr("6+3-")); // false
```

## 示例4：JSON字符串校验

```java
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

// true
System.out.println(JsonValidator.isValidJson("""
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
```
