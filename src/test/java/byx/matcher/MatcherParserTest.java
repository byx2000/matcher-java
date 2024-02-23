package byx.matcher;

import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MatcherParserTest {
    @Test
    public void test1() {
        Matcher m = MatcherParser.parse("a");
        assertTrue(m.match("a"));
        assertFalse(m.match(""));
        assertFalse(m.match("b"));
        assertFalse(m.match("aa"));

        m = MatcherParser.parse("(a)");
        assertTrue(m.match("a"));
        assertFalse(m.match(""));
        assertFalse(m.match("b"));
        assertFalse(m.match("aa"));
    }

    @Test
    public void test2() {
        Matcher m = MatcherParser.parse(".");
        assertTrue(m.match("a"));
        assertTrue(m.match("b"));
        assertTrue(m.match(" "));
        assertFalse(m.match(""));
    }

    @Test
    public void test3() {
        Matcher m = MatcherParser.parse("a*");
        assertTrue(m.match(""));
        assertTrue(m.match("a"));
        assertTrue(m.match("aaaaa"));
        assertFalse(m.match("aaab"));
        assertFalse(m.match("bbbbb"));
    }

    @Test
    public void test4() {
        Matcher m = MatcherParser.parse("a+");
        assertTrue(m.match("a"));
        assertTrue(m.match("aaaaa"));
        assertFalse(m.match(""));
    }

    @Test
    public void test5() {
        Matcher m = MatcherParser.parse("ab");
        assertTrue(m.match("ab"));
        assertFalse(m.match(""));
        assertFalse(m.match("a"));
        assertFalse(m.match("b"));
        assertFalse(m.match("xy"));
        assertFalse(m.match("abc"));
    }

    @Test
    public void test6() {
        Matcher m = MatcherParser.parse("apple");
        assertTrue(m.match("apple"));
        assertFalse(m.match(""));
        assertFalse(m.match("a"));
        assertFalse(m.match("app"));
        assertFalse(m.match("applex"));
        assertFalse(m.match("xyz"));
        assertFalse(m.match("sdjgfshgffgj"));
    }

    @Test
    public void test7() {
        Matcher m = MatcherParser.parse("a|b");
        assertTrue(m.match("a"));
        assertTrue(m.match("b"));
        assertFalse(m.match(""));
        assertFalse(m.match("x"));
        assertFalse(m.match("ab"));
        assertFalse(m.match("xyz"));
    }

    @Test
    public void test8() {
        Matcher m = MatcherParser.parse("a|b|cd|e");
        assertTrue(m.match("a"));
        assertTrue(m.match("b"));
        assertTrue(m.match("cd"));
        assertTrue(m.match("e"));
        assertFalse(m.match(""));
        assertFalse(m.match("x"));
        assertFalse(m.match("ab"));
        assertFalse(m.match("c"));
        assertFalse(m.match("bcd"));
        assertFalse(m.match("abcd"));
        assertFalse(m.match("xyz"));
    }

    @Test
    public void test9() {
        Matcher m = MatcherParser.parse("(a|b)*");
        assertTrue(m.match(""));
        assertTrue(m.match("a"));
        assertTrue(m.match("b"));
        assertTrue(m.match("aa"));
        assertTrue(m.match("ab"));
        assertTrue(m.match("ba"));
        assertTrue(m.match("bb"));
        assertTrue(m.match("aabbabaabababaabababa"));
        assertTrue(m.match("babbabaabababaabababa"));
        assertTrue(m.match("aaaaaaaaaaa"));
        assertTrue(m.match("bbbbbbbbbbb"));
        assertFalse(m.match("c"));
        assertFalse(m.match("xyz"));
        assertFalse(m.match("aabbabaxabababaabababa"));
    }

    @Test
    public void test10() {
        Matcher m = MatcherParser.parse("(a|b)+");
        assertTrue(m.match("a"));
        assertTrue(m.match("b"));
        assertTrue(m.match("aa"));
        assertTrue(m.match("ab"));
        assertTrue(m.match("ba"));
        assertTrue(m.match("bb"));
        assertTrue(m.match("aabbabaabababaabababa"));
        assertTrue(m.match("babbabaabababaabababa"));
        assertTrue(m.match("aaaaaaaaaaa"));
        assertTrue(m.match("bbbbbbbbbbb"));
        assertFalse(m.match(""));
        assertFalse(m.match("c"));
        assertFalse(m.match("xyz"));
        assertFalse(m.match("aabbabaxabababaabababa"));
    }

    @Test
    public void test11() {
        Matcher m = MatcherParser.parse("(ab)*");
        assertTrue(m.match(""));
        assertTrue(m.match("ab"));
        assertTrue(m.match("ababababababababab"));
        assertFalse(m.match("a"));
        assertFalse(m.match("b"));
        assertFalse(m.match("abba"));
        assertFalse(m.match("aba"));
        assertFalse(m.match("ababababababababa"));
    }

    @Test
    public void test12() {
        Matcher m = MatcherParser.parse("(a|ab)c");
        assertTrue(m.match("abc"));
        assertTrue(m.match("ac"));
        assertFalse(m.match("ab"));
        assertFalse(m.match("abx"));
        assertFalse(m.match("xc"));
        assertFalse(m.match("a"));
    }

    @Test
    public void test13() {
        Matcher m = MatcherParser.parse("(a*)*");
        assertTrue(m.match(""));
        assertTrue(m.match("a"));
        assertTrue(m.match("aaaaaaaaaaaaa"));
        assertFalse(m.match("b"));
        assertFalse(m.match("aaab"));
        assertFalse(m.match("bbbbbbbbbbbbb"));
    }

    @Test
    public void test14() {
        Matcher m = MatcherParser.parse("(a*)+");
        assertTrue(m.match(""));
        assertTrue(m.match("a"));
        assertTrue(m.match("aaaaaaaaaaaaa"));
        assertFalse(m.match("b"));
        assertFalse(m.match("aaab"));
        assertFalse(m.match("bbbbbbbbbbbbb"));
    }

    @Test
    public void test15() {
        Matcher m = MatcherParser.parse("(0|1(01*0)*1)*");
        assertTrue(m.match("0"));
        assertFalse(m.match("1"));
        assertFalse(m.match("10"));
        assertTrue(m.match("11"));
        assertFalse(m.match("100"));
        assertFalse(m.match("101"));
        assertTrue(m.match("110"));
        assertFalse(m.match("111"));
        assertFalse(m.match("1000"));
        assertTrue(m.match("1000001001001111010"));
        assertFalse(m.match("1000001001001111011"));
        assertFalse(m.match("1000001001001111100"));
        assertTrue(m.match("1000001001001111101"));
        assertTrue(m.match("111010010101011001000001110011010111101110101111101110110"));
        assertFalse(m.match("111010010101011001000001110011010111101110101111101110111"));
        assertFalse(m.match("111010010101011001000001110011010111101110101111101111000"));
        assertTrue(m.match("111010010101011001000001110011010111101110101111101111001"));
    }

    @Test
    public void test16() {
        Matcher m = MatcherParser.parse("a*a*a*a*a*a*a*a*a*a*a*a*a*a*a*a*a*a*a*a*a*a*a*a*a*a*a*a*a*a*aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertTrue(m.match("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        assertFalse(m.match("aaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
    }

    @Test
    public void test17() {
        Matcher m = MatcherParser.parse("(((a*)*)*)*");
        assertTrue(m.match("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        assertFalse(m.match("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab"));
        assertFalse(m.match("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
    }

    @Test
    public void test18() {
        Matcher m = MatcherParser.parse("(((a+)+)+)+");
        assertTrue(m.match("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        assertFalse(m.match("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab"));
        assertFalse(m.match("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
    }

    @Test
    public void test19() {
        Matcher m = MatcherParser.parse("X(.+)+X");
        assertTrue(m.match("X==============================================X"));
        assertFalse(m.match("==XX=============================================="));
    }

    @Test
    public void test20() {
        Matcher m = MatcherParser.parse("((0|1)+)+b");
        assertFalse(m.match("10101010110101001100101010101010101010101010101010000"));
        assertTrue(m.match("10101010110101001100101010101010101010101010101010000b"));
    }

    @Test
    public void test21() {
        Matcher m = MatcherParser.parse("(.*)adidas(.*)");
        assertTrue(m.match("adidas"));
        assertTrue(m.match("adidasxxxxxxx"));
        assertTrue(m.match("yyyyyyyadidas"));
        assertTrue(m.match("asfdjaasadaadidasidgfsaaaasdfkjsdskdf"));
        assertFalse(m.match("adida"));
        assertFalse(m.match("adixdas"));
        assertFalse(m.match("didas"));
        assertFalse(m.match("sadgvajsvfjkdbfjcsadidaakjgkahfdjksdbfjks"));
    }

    @Test
    public void test22() {
        Matcher m = MatcherParser.parse("[0-9]");
        assertTrue(m.match("0"));
        assertTrue(m.match("5"));
        assertTrue(m.match("9"));
        assertFalse(m.match(""));
        assertFalse(m.match("a"));
    }

    @Test
    public void test23() {
        Matcher m = MatcherParser.parse("[abc]");
        assertTrue(m.match("a"));
        assertTrue(m.match("b"));
        assertTrue(m.match("c"));
        assertFalse(m.match(""));
        assertFalse(m.match("x"));
    }

    @Test
    public void test24() {
        Matcher m = MatcherParser.parse("[a-zA-Z]");
        assertTrue(m.match("a"));
        assertTrue(m.match("x"));
        assertTrue(m.match("z"));
        assertTrue(m.match("A"));
        assertTrue(m.match("X"));
        assertTrue(m.match("Z"));
        assertFalse(m.match(""));
        assertFalse(m.match("5"));
        assertFalse(m.match("?"));
    }

    @Test
    public void test25() {
        Matcher m = MatcherParser.parse("[_a-zA-Z][_0-9a-zA-Z]*");
        assertTrue(m.match("_"));
        assertTrue(m.match("_var"));
        assertTrue(m.match("count"));
        assertTrue(m.match("Add"));
        assertTrue(m.match("_____"));
        assertTrue(m.match("push123"));
        assertTrue(m.match("abc123xXYy"));
        assertFalse(m.match(""));
        assertFalse(m.match("123abc"));
        assertFalse(m.match("dfg3fd fsgbsdf23"));
    }

    @Test
    public void test26() {
        // 匹配3的倍数
        Matcher m = MatcherParser.parse("[0369]*(([147][0369]*|[258][0369]*[258][0369]*)([147][0369]*[258][0369]*)*([258][0369]*|[147][0369]*[147][0369]*)|[258][0369]*[147][0369]*)*");

        for (int i = 0; i <= 100000; ++i) {
            if (i % 3 == 0) {
                assertTrue(m.match(String.valueOf(i)));
            } else {
                assertFalse(m.match(String.valueOf(i)));
            }
        }

        assertTrue(m.match("1306037620370620974"));
        assertFalse(m.match("1306037620370620975"));
        assertFalse(m.match("1306037620370620976"));
        assertTrue(m.match("1306037620370620977"));
    }

    @Test
    public void test27() {
        Matcher m = MatcherParser.parse("\\.");
        assertTrue(m.match("."));
        assertFalse(m.match(""));
        assertFalse(m.match("a"));

        m = MatcherParser.parse("\\\\");
        assertTrue(m.match("\\"));
        assertFalse(m.match(""));
        assertFalse(m.match("a"));
    }

    @Test
    public void test28() {
        Matcher m = MatcherParser.parse("\\**");
        assertTrue(m.match(""));
        assertTrue(m.match("*"));
        assertTrue(m.match("*****"));

        m = MatcherParser.parse("(\\(\\)|\\[\\])+");
        assertTrue(m.match("()"));
        assertTrue(m.match("[]"));
        assertTrue(m.match("()[][]()()[]"));
        assertTrue(m.match("[][]()()()[][]()"));
        assertFalse(m.match(""));
        assertFalse(m.match("(())"));
        assertFalse(m.match("[)"));
    }

    @Test
    public void test29() {
        Matcher m = MatcherParser.parse("(a*)*");
        assertTrue(m.match("a".repeat(1000)));
        assertFalse(m.match("a".repeat(1000) + "b"));
    }

    @Test
    public void testFileCases() {
        // 测试数据来源：https://loj.ac/p/118
        for (int i = 1; i <= 11; ++i) {
            String inputFile = "regular" + i + ".in";
            String outputFile = "./regular" + i + ".out";

            Scanner scanner1 = new Scanner(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(inputFile)));
            Scanner scanner2 = new Scanner(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(outputFile)));
            while (scanner1.hasNext()) {
                String expr = scanner1.nextLine();
                System.out.println("expr: " + expr);
                String str = scanner1.nextLine();
                System.out.println("str: " + str);
                String ans = scanner2.nextLine();
                System.out.println("ans: " + ans);
                System.out.println("==============================");

                Matcher m = MatcherParser.parse(expr);
                if ("Yes".equals(ans)) {
                    assertTrue(m.match(str));
                } else {
                    assertFalse(m.match(str));
                }
            }
        }
    }
}
