package byx.regex;

import org.junit.jupiter.api.Test;

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
    public void testStr() {
        Regex r = new Str("abc");
        assertTrue(r.match("abc"));
        assertFalse(r.match(""));
        assertFalse(r.match("a"));
        assertFalse(r.match("ab"));
        assertFalse(r.match("ax"));
        assertFalse(r.match("abx"));
        assertFalse(r.match("abcx"));
    }

    @Test
    public void testConcat() {
        Regex r = ch('a').concat(ch('b'));
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
    public void testZeroOrMore() {
        Regex r = ch('a').zeroOrMore();
        assertTrue(r.match(""));
        assertTrue(r.match("a"));
        assertTrue(r.match("aaaaa"));
        assertFalse(r.match("b"));
        assertFalse(r.match("bbbb"));
        assertFalse(r.match("aaab"));
        assertFalse(r.match("aaabaaaa"));
    }

    @Test
    public void testOneOrMore() {
        Regex r = ch('a').oneOrMore();
        assertTrue(r.match("a"));
        assertTrue(r.match("aaaaa"));
        assertFalse(r.match(""));
        assertFalse(r.match("b"));
        assertFalse(r.match("bbbb"));
        assertFalse(r.match("aaab"));
        assertFalse(r.match("aaabaaaa"));
    }
}
