package byx.matcher;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;

import static byx.matcher.Matcher.*;
import static org.junit.jupiter.api.Assertions.*;

public class MatcherCombinatorTest {
    @Test
    public void testCh() {
        Matcher m = ch('a');
        assertTrue(m.match("a"));
        assertFalse(m.match(""));
        assertFalse(m.match("b"));
        assertFalse(m.match("aa"));
        assertFalse(m.match("xy"));
    }

    @Test
    public void testChs1() {
        Matcher m = chs('a', 'b', 'c');
        assertTrue(m.match("a"));
        assertTrue(m.match("b"));
        assertTrue(m.match("c"));
        assertFalse(m.match("d"));
        assertFalse(m.match("1"));
    }

    @Test
    public void testChs2() {
        Character[] arr = {'a', 'b', 'c'};
        Matcher m = chs(arr);
        assertTrue(m.match("a"));
        assertTrue(m.match("b"));
        assertTrue(m.match("c"));
        assertFalse(m.match("d"));
        assertFalse(m.match("1"));
    }

    @Test
    public void testAny() {
        Matcher m = any();
        assertTrue(m.match("a"));
        assertTrue(m.match("b"));
        assertFalse(m.match(""));
        assertFalse(m.match("xyz"));
    }

    @Test
    public void testRange() {
        Matcher m = range('0', '9');
        assertTrue(m.match("0"));
        assertTrue(m.match("5"));
        assertTrue(m.match("9"));
        assertFalse(m.match(""));
        assertFalse(m.match("a"));
    }

    @Test
    public void testNot() {
        Matcher m = not('a');
        assertTrue(m.match("b"));
        assertFalse(m.match("a"));
    }

    @Test
    public void testStr() {
        Matcher m = str("abc");
        assertTrue(m.match("abc"));
        assertFalse(m.match(""));
        assertFalse(m.match("a"));
        assertFalse(m.match("ab"));
        assertFalse(m.match("ax"));
        assertFalse(m.match("abx"));
        assertFalse(m.match("abcx"));
    }

    @Test
    public void testAnd1() {
        Matcher m = ch('a').and(ch('b'));
        assertTrue(m.match("ab"));
        assertFalse(m.match("a"));
        assertFalse(m.match("abc"));
        assertFalse(m.match("ba"));
        assertFalse(m.match("x"));
        assertFalse(m.match("xy"));
        assertFalse(m.match(""));
    }

    @Test
    public void testAnd2() {
        Matcher m = ch('a').and('b');
        assertTrue(m.match("ab"));
        assertFalse(m.match("a"));
        assertFalse(m.match("abc"));
        assertFalse(m.match("ba"));
        assertFalse(m.match("x"));
        assertFalse(m.match("xy"));
        assertFalse(m.match(""));
    }

    @Test
    public void testAnd3() {
        Matcher m = ch('a').and("b");
        assertTrue(m.match("ab"));
        assertFalse(m.match("a"));
        assertFalse(m.match("abc"));
        assertFalse(m.match("ba"));
        assertFalse(m.match("x"));
        assertFalse(m.match("xy"));
        assertFalse(m.match(""));
    }

    @Test
    public void testOr1() {
        Matcher m = ch('a').or(ch('b'));
        assertTrue(m.match("a"));
        assertTrue(m.match("b"));
        assertFalse(m.match("x"));
        assertFalse(m.match("ax"));
        assertFalse(m.match("by"));
        assertFalse(m.match("mn"));
        assertFalse(m.match(""));
    }

    @Test
    public void testOr2() {
        Matcher m = ch('a').or('b');
        assertTrue(m.match("a"));
        assertTrue(m.match("b"));
        assertFalse(m.match("x"));
        assertFalse(m.match("ax"));
        assertFalse(m.match("by"));
        assertFalse(m.match("mn"));
        assertFalse(m.match(""));
    }

    @Test
    public void testOr3() {
        Matcher m = ch('a').or("b");
        assertTrue(m.match("a"));
        assertTrue(m.match("b"));
        assertFalse(m.match("x"));
        assertFalse(m.match("ax"));
        assertFalse(m.match("by"));
        assertFalse(m.match("mn"));
        assertFalse(m.match(""));
    }

    @Test
    public void testRepeat1() {
        Matcher m = ch('a').repeat(3, 5);
        assertTrue(m.match("aaa"));
        assertTrue(m.match("aaaa"));
        assertTrue(m.match("aaaaa"));
        assertFalse(m.match(""));
        assertFalse(m.match("a"));
        assertFalse(m.match("aa"));
        assertFalse(m.match("aaaaaa"));
        assertFalse(m.match("aaaaaaa"));
    }

    @Test
    public void testRepeat2() {
        Matcher m = ch('a').repeat(3);
        assertTrue(m.match("aaa"));
        assertFalse(m.match(""));
        assertFalse(m.match("a"));
        assertFalse(m.match("aa"));
        assertFalse(m.match("aaaa"));
        assertFalse(m.match("aaaaa"));
    }

    @Test
    public void testMany() {
        Matcher m = ch('a').many();
        assertTrue(m.match(""));
        assertTrue(m.match("a"));
        assertTrue(m.match("aaaaa"));
        assertFalse(m.match("b"));
        assertFalse(m.match("bbbb"));
        assertFalse(m.match("aaab"));
        assertFalse(m.match("aaabaaaa"));
    }

    @Test
    public void testMany1() {
        Matcher m = ch('a').many1();
        assertTrue(m.match("a"));
        assertTrue(m.match("aaaaa"));
        assertFalse(m.match(""));
        assertFalse(m.match("b"));
        assertFalse(m.match("bbbb"));
        assertFalse(m.match("aaab"));
        assertFalse(m.match("aaabaaaa"));
    }

    @Test
    public void testMany2() {
        Matcher m = ch('a').many(3);
        assertFalse(m.match(""));
        assertFalse(m.match("a"));
        assertFalse(m.match("aa"));
        assertTrue(m.match("aaa"));
        assertTrue(m.match("aaaa"));
        assertTrue(m.match("aaaaa"));
    }

    @Test
    public void testFlatMap1() {
        Matcher m = not(' ').many1().flatMap(s -> ch(' ').and(str("xxx ")).and(str(s)));
        assertTrue(m.match("m xxx m"));
        assertTrue(m.match("aaa xxx aaa"));
        assertTrue(m.match("bbbb xxx bbbb"));
        assertFalse(m.match("aaa xxx bbb"));
        assertFalse(m.match("aaaa xxx aaa"));
        assertFalse(m.match("aaa xxx aaaa"));
    }

    @Test
    public void testFlatMap2() {
        Matcher m = any().many1().flatMap(s -> any().repeat(s.length()));
        assertTrue(m.match("aaabbb"));
        assertTrue(m.match("aaaabbbb"));
        assertTrue(m.match("xxxxxyyyyy"));
        assertFalse(m.match("aaabbbb"));
        assertFalse(m.match("xxxxyyy"));
        assertFalse(m.match("mmm"));
    }

    @Test
    public void testLazy() {
        AtomicInteger i = new AtomicInteger(123);
        Matcher m = lazy(() -> {
            i.set(456);
            return ch('a');
        });
        assertEquals(123, i.get());
        assertTrue(m.match("a"));
        assertEquals(456, i.get());
    }
}
