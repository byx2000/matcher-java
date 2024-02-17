# 基于ParserCombinator的正则表达式引擎

## Cursor

`Cursor`封装了字符串的状态，表示一个光标位置。光标位置只能向后移动，可以方便地获取光标指向的字符，以及判断是否到达字符串末尾。

```java
public class Cursor {
    private final String input;
    private final int index;

    public Cursor(String input, int index) {
        this.input = input;
        this.index = index;
    }

    /**
     * 是否到达字符串结尾
     */
    public boolean end() {
        return index == input.length();
    }

    /**
     * 当前指向的字符
     */
    public char current() {
        return input.charAt(index);
    }

    /**
     * 光标向后移动一个字符
     */
    public Cursor next() {
        return new Cursor(input, index + 1);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Cursor cursor = (Cursor) o;
        return index == cursor.index && Objects.equals(input, cursor.input);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, index);
    }
    
    /**
     * 方便输出显示
     */
    @Override
    public String toString() {
        return String.format("Cursor{parsed: '%s', remain: '%s'}",
                input.substring(0, index), input.substring(index));
    }
}
```

测试代码：

```java
Cursor cursor = new Cursor("hello", 0);
while (!cursor.end()) {
    System.out.println(cursor);
    System.out.println("current: " + cursor.current());
    cursor = cursor.next();
}
```

输出结果：

```
Cursor{parsed: '', remain: 'hello'}
current: h
Cursor{parsed: 'h', remain: 'ello'}
current: e
Cursor{parsed: 'he', remain: 'llo'}
current: l
Cursor{parsed: 'hel', remain: 'lo'}
current: l
Cursor{parsed: 'hell', remain: 'o'}
current: o
```

`Cursor`被设计成不可变的，主要是为了简化Parser的实现。如果`Cursor`是可变的，则在解析的过程中需要时刻注意保存当前光标的位置，这样十分麻烦。

`Cursor`的`equals`方法和`hashCode`方法用于判断重复的`Cursor`状态，因为接下来我们要把`Cursor`放进集合里。

## Regex

`Regex`封装了对字符串的解析操作，它从一个光标位置开始解析字符串，返回解析后所有可能的光标位置。

```java
public interface Regex {
    Set<Cursor> parse(Cursor input);

    default boolean match(String input) {
        return parse(new Cursor(input, 0)).stream().anyMatch(Cursor::end);
    }
}
```

`parse`方法就是解析操作的具体实现，它从一个光标开始尝试向后解析，返回解析之后所有可能的光标位置。使用`Set`作为返回值类型，可以对匹配结果进行去重，避免重复解析相同的状态。

`match`方法是对`parse`方法的简单封装，它用来判断当前`Regex`是否能消耗掉整个字符串。只要在解析结果中存在一个到达字符串末尾的光标位置，就说明当前解析器能把字符串消耗完。

接下来，我们会实现一些基本的`Regex`实现类，使用它们可以将任意正则表达式转换成一个`Regex`的实例。调用`Regex`的`match`方法，并传入待匹配的字符串，就能判断这个字符串是否与正则表达式匹配。

## 原子Regex

下面实现两个基本的`Regex`，虽然它们只能实现简单的功能，但是后面将非常有用。

首先是匹配单个指定字符的解析器：

```java
public class Ch implements Regex {
    private final char c;

    public Ch(char c) {
        this.c = c;
    }

    @Override
    public Set<Cursor> parse(Cursor input) {
        if (input.end() || input.current() != c) {
            return Collections.emptySet();
        }
        return Set.of(input.next());
    }
}
```

如果当前光标没有到达字符串结尾，且指向的字符等于给定字符，它会消耗掉这个字符，然后返回剩余的部分，否则返回一个空列表作为解析失败的结果。

测试代码：

```java
Regex r = new Ch('a');
System.out.println(r.parse(new Cursor("abc", 0)));
System.out.println(r.parse(new Cursor("xyz", 0)));
System.out.println(r.parse(new Cursor("", 0)));
```

输出结果：

```
[Cursor{parsed: 'a', remain: 'bc'}]
[]
[]
```

与之类似的，还有匹配任意单个字符的解析器，对应于正则表达式中的元字符`.`：

```java
public class Any implements Regex {
    @Override
    public Set<Cursor> parse(Cursor input) {
        if (input.end()) {
            return Collections.emptySet();
        }
        return Set.of(input.next());
    }
}
```

测试代码：

```java
Regex r = new Any();
System.out.println(r.parse(new Cursor("abc", 0)));
System.out.println(r.parse(new Cursor("xyz", 0)));
System.out.println(r.parse(new Cursor("", 0)));
```

输出结果：

```
[Cursor{parsed: 'a', remain: 'bc'}]
[Cursor{parsed: 'x', remain: 'yz'}]
[]
```

我们现在已经有了能够匹配单个字符的`Regex`，但是它们并不能做很多事情，最多只能让光标向前移动一个字符。我们急需一种能够将多个小的`Regex`组合成一个复杂的`Regex`的机制。

## Regex的组合

接下来这几个`Regex`非常重要，它们可以将一个或多个`Regex`包装成一个功能更强大的`Regex`。

假设我们有一个`Ch('a')`和一个`Ch('b')`，如何用它们组合成一个能够匹配字符串`ab`的`Regex`呢？请看下面`Concat`的实现，它表示对输入串依次应用`lhs`和`rhs`这两个`Regex`，并返回所有能够得到的结果，对应于正则表达式中的连接操作。

```java
public class Concat implements Regex {
    private final Regex lhs, rhs;

    public Concat(Regex lhs, Regex rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public Set<Cursor> parse(Cursor input) {
        Set<Cursor> r = new HashSet<>();
        for (Cursor c : lhs.parse(input)) {
            r.addAll(rhs.parse(c));
        }
        return r;
    }
}
```

测试代码：

```java
Regex r = new Concat(new Ch('a'), new Ch('b'));
System.out.println(r.parse(new Cursor("abc", 0)));
System.out.println(r.parse(new Cursor("xyz", 0)));
System.out.println(r.parse(new Cursor("ac", 0)));
```

输出结果：

```
[Cursor{parsed: 'ab', remain: 'c'}]
[]
[]
```

只要`lhs`和`rhs`任意一个解析器解析失败，都会导致返回结果为空。

注意，`lhs`和`rhs`可能返回多个解析结果，因此在实现的过程中需要遍历所有可能的结果。

返回值类型为`Set`确保了结果中不会存在重复的状态。

多个`Concat`可以串联使用，例如，以下`Regex`匹配以`abc`开头的字符串：

```java
Regex r = new Concat(new Ch('a'), new Concat(new Ch('b'), new Ch('c')));
```

与此对应的还有`Or`，它表示从`lhs`或`rhs`中选择一个执行，对应于正则表达式中的`|`运算符：

```java
public class Or implements Regex {
    private final Regex lhs, rhs;

    public Or(Regex lhs, Regex rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public Set<Cursor> parse(Cursor input) {
        Set<Cursor> result = new HashSet<>(lhs.parse(input));
        result.addAll(rhs.parse(input));
        return result;
    }
}
```

测试代码：

```java
Regex r = new Or(new Ch('a'), new Ch('b'));
System.out.println(r.parse(new Cursor("apple", 0)));
System.out.println(r.parse(new Cursor("banana", 0)));
System.out.println(r.parse(new Cursor("cat", 0)));
```

输出结果：

```
[Cursor{parsed: 'a', remain: 'pple'}]
[Cursor{parsed: 'b', remain: 'anana'}]
[]
```

接下来的`ZeroOrMore`是一个很重要的`Regex`，它实现了正则表达式中`*`运算符的功能。它尝试对当前光标多次应用`parser`，每次应用都会产生一个不同的光标位置。在实现的过程中，使用`Queue`来进行广度优先搜索，同时用一个额外的`Set`来避免重复搜索。

```java
public class ZeroOrMore implements Regex {
    private final Regex parser;

    public ZeroOrMore(Regex parser) {
        this.parser = parser;
    }

    @Override
    public Set<Cursor> parse(Cursor input) {
        Set<Cursor> result = new HashSet<>();
        Queue<Cursor> queue = new LinkedList<>(List.of(input));
        Set<Cursor> set = new HashSet<>(Set.of(input));

        while (!queue.isEmpty()) {
            int cnt = queue.size();
            while (cnt-- > 0) {
                Cursor cursor = queue.remove();
                result.add(cursor);
                for (Cursor c : parser.parse(cursor)) {
                    if (!set.contains(c)) {
                        queue.add(c);
                        set.add(c);
                    }
                }
            }
        }

        return result;
    }
}
```

测试代码：

```java
Regex r = new ZeroOrMore(new Ch('a'));
System.out.println(r.parse(new Cursor("aaa", 0)));
```

输出结果：

```
[
	Cursor{parsed: '', remain: 'aaa'},
	Cursor{parsed: 'a', remain: 'aa'},
    Cursor{parsed: 'aa', remain: 'a'},
	Cursor{parsed: 'aaa', remain: ''}
]
```

与之类似的，还有`OneOrMore`，对应于正则表达式中的`+`运算符。它的实现与`ZeroOrMore`及其类似，只是搜索的起始条件不同。

```java
public class OneOrMore implements Regex {
    private final Regex parser;

    public OneOrMore(Regex parser) {
        this.parser = parser;
    }

    @Override
    public Set<Cursor> parse(Cursor input) {
        Set<Cursor> result = new HashSet<>();
        Set<Cursor> start = parser.parse(input);
        Queue<Cursor> queue = new LinkedList<>(start);
        Set<Cursor> set = new HashSet<>(start);

        while (!queue.isEmpty()) {
            int cnt = queue.size();
            while (cnt-- > 0) {
                Cursor cursor = queue.remove();
                result.add(cursor);
                for (Cursor c : parser.parse(cursor)) {
                    if (!set.contains(c)) {
                        queue.add(c);
                        set.add(c);
                    }
                }
            }
        }

        return result;
    }
}
```

测试代码：

```java
Regex r = new OneOrMore(new Ch('a'));
System.out.println(r.parse(new Cursor("aaa", 0)));
```

输出结果：

```
[
	Cursor{parsed: 'a', remain: 'aa'}, 
	Cursor{parsed: 'aa', remain: 'a'},
	Cursor{parsed: 'aaa', remain: ''} 
]
```

## 构造复杂的解析器

有了以上的原子`Regex`和各种组合手段，就可以构造出任意复杂的`Regex`了，以下是正则表达式`((a|b)c*)+`对应的`Regex`：

```java
Regex r = new OneOrMore(new Concat(new Or(new Ch('a'), new Ch('b')), new ZeroOrMore(new Ch('c'))));
```

可以看到，这样的写法嵌套很深，用起来不方便，因此我们在`Regex`接口中添加一些静态方法和默认方法：

```java
public interface Regex {
    ...
    static Regex any() {
        return new Any();
    }

    static Regex ch(char c) {
        return new Ch(c);
    }

    default Regex concat(Regex rhs) {
        return new Concat(this, rhs);
    }

    default Regex or(Regex rhs) {
        return new Or(this, rhs);
    }

    default Regex zeroOrMore() {
        return new ZeroOrMore(this);
    }

    default Regex oneOrMore() {
        return new OneOrMore(this);
    }
}
```

然后就可以通过链式调用构造复杂的`Regex`了：

```java
import static xxx.Regex;

// ((a|b)c*)+
Regex r = ch('a').or(ch('b')).concat(ch('c').zeroOrMore()).oneOrMore();
```

测试代码：

```java
System.out.println(r.match("acccbcccc"));
System.out.println(r.match("abcc"));
```

输出结果：

```
true
false
```

## 从字符串生成Regex

到这里，其实我们已经实现了一个简易的正则表达式执行引擎，支持正则表达式中常用的操作，包括连接、选择、重复等，并可以很容易地进行扩展，只需添加新的`Regex`实现类。

我们可以手动调用方法来构造任意复杂的`Regex`，但是，当表达式比较复杂时，手动构造的方式还是比较麻烦，所以下面实现了一个简易的正则表达式语法解析器`RegexParser`，它使用递归下降算法把一个正则表达式的字符串转换成一个`Regex`对象。这个解析器仅仅支持有限的正则表达式元素，包括普通字符、括号优先级，以及`.`、`*`、`+`等元字符。有兴趣的读者可以很容易地进行扩展。

```java
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
```

然后就可以像下面这样构造一个正则表达式：

```java
Regex r = RegexParser.parse("((a|b)c*)+");
```

上面这行代码生成的`Regex`等价于：

```java
Regex r = ch('a').or(ch('b')).concat(ch('c').zeroOrMore()).oneOrMore();
```

## 项目地址

[https://github.com/byx2000/RegexCombinator](https://github.com/byx2000/RegexCombinator)

