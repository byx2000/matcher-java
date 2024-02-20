package byx.regex;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static byx.regex.Regex.*;
import static org.junit.jupiter.api.Assertions.*;

public class RegexCombinatorTest {
    @Test
    public void testCh() {
        Regex r = ch('a');
        assertTrue(r.match("a"));
        assertFalse(r.match(""));
        assertFalse(r.match("b"));
        assertFalse(r.match("aa"));
        assertFalse(r.match("xy"));
    }

    @Test
    public void testChs1() {
        Regex r = chs('a', 'b', 'c');
        assertTrue(r.match("a"));
        assertTrue(r.match("b"));
        assertTrue(r.match("c"));
        assertFalse(r.match("d"));
        assertFalse(r.match("1"));
    }

    @Test
    public void testChs2() {
        Character[] arr = {'a', 'b', 'c'};
        Regex r = chs(arr);
        assertTrue(r.match("a"));
        assertTrue(r.match("b"));
        assertTrue(r.match("c"));
        assertFalse(r.match("d"));
        assertFalse(r.match("1"));
    }

    @Test
    public void testAny() {
        Regex r = any();
        assertTrue(r.match("a"));
        assertTrue(r.match("b"));
        assertFalse(r.match(""));
        assertFalse(r.match("xyz"));
    }

    @Test
    public void testRange() {
        Regex r = range('0', '9');
        assertTrue(r.match("0"));
        assertTrue(r.match("5"));
        assertTrue(r.match("9"));
        assertFalse(r.match(""));
        assertFalse(r.match("a"));
    }

    @Test
    public void testNot() {
        Regex r = not('a');
        assertTrue(r.match("b"));
        assertFalse(r.match("a"));
    }

    @Test
    public void testStr() {
        Regex r = str("abc");
        assertTrue(r.match("abc"));
        assertFalse(r.match(""));
        assertFalse(r.match("a"));
        assertFalse(r.match("ab"));
        assertFalse(r.match("ax"));
        assertFalse(r.match("abx"));
        assertFalse(r.match("abcx"));
    }

    @Test
    public void testAnd() {
        Regex r = ch('a').and(ch('b'));
        assertTrue(r.match("ab"));
        assertFalse(r.match("a"));
        assertFalse(r.match("abc"));
        assertFalse(r.match("ba"));
        assertFalse(r.match("x"));
        assertFalse(r.match("xy"));
        assertFalse(r.match(""));
    }

    @Test
    public void testOr() {
        Regex r = ch('a').or(ch('b'));
        assertTrue(r.match("a"));
        assertTrue(r.match("b"));
        assertFalse(r.match("x"));
        assertFalse(r.match("ax"));
        assertFalse(r.match("by"));
        assertFalse(r.match("mn"));
        assertFalse(r.match(""));
    }

    @Test
    public void testRepeat1() {
        Regex r = ch('a').repeat(3, 5);
        assertTrue(r.match("aaa"));
        assertTrue(r.match("aaaa"));
        assertTrue(r.match("aaaaa"));
        assertFalse(r.match(""));
        assertFalse(r.match("a"));
        assertFalse(r.match("aa"));
        assertFalse(r.match("aaaaaa"));
        assertFalse(r.match("aaaaaaa"));
    }

    @Test
    public void testRepeat2() {
        Regex r = ch('a').repeat(3);
        assertTrue(r.match("aaa"));
        assertFalse(r.match(""));
        assertFalse(r.match("a"));
        assertFalse(r.match("aa"));
        assertFalse(r.match("aaaa"));
        assertFalse(r.match("aaaaa"));
    }

    @Test
    public void testMany() {
        Regex r = ch('a').many();
        assertTrue(r.match(""));
        assertTrue(r.match("a"));
        assertTrue(r.match("aaaaa"));
        assertFalse(r.match("b"));
        assertFalse(r.match("bbbb"));
        assertFalse(r.match("aaab"));
        assertFalse(r.match("aaabaaaa"));
    }

    @Test
    public void testMany1() {
        Regex r = ch('a').many1();
        assertTrue(r.match("a"));
        assertTrue(r.match("aaaaa"));
        assertFalse(r.match(""));
        assertFalse(r.match("b"));
        assertFalse(r.match("bbbb"));
        assertFalse(r.match("aaab"));
        assertFalse(r.match("aaabaaaa"));
    }

    @Test
    public void testMany2() {
        Regex r = ch('a').many(3);
        assertFalse(r.match(""));
        assertFalse(r.match("a"));
        assertFalse(r.match("aa"));
        assertTrue(r.match("aaa"));
        assertTrue(r.match("aaaa"));
        assertTrue(r.match("aaaaa"));
    }

    @Test
    public void testFlatMap1() {
        Regex r = not(' ').many1().flatMap(s -> ch(' ').and(str("xxx ")).and(str(s)));
        assertTrue(r.match("m xxx m"));
        assertTrue(r.match("aaa xxx aaa"));
        assertTrue(r.match("bbbb xxx bbbb"));
        assertFalse(r.match("aaa xxx bbb"));
        assertFalse(r.match("aaaa xxx aaa"));
        assertFalse(r.match("aaa xxx aaaa"));
    }

    @Test
    public void testFlatMap2() {
        Regex r = any().many1().flatMap(s -> any().repeat(s.length()));
        assertTrue(r.match("aaabbb"));
        assertTrue(r.match("aaaabbbb"));
        assertTrue(r.match("xxxxxyyyyy"));
        assertFalse(r.match("aaabbbb"));
        assertFalse(r.match("xxxxyyy"));
        assertFalse(r.match("mmm"));
    }

    @Test
    public void testLazy() {
        AtomicInteger i = new AtomicInteger(123);
        Regex r = lazy(() -> {
            i.set(456);
            return ch('a');
        });
        assertEquals(123, i.get());
        assertTrue(r.match("a"));
        assertEquals(456, i.get());
    }
}
