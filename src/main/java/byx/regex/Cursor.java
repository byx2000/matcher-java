package byx.regex;

import java.util.Objects;

/**
 * 封装当前解析位置
 */
public final class Cursor {
    private final String input;
    private final int index;

    /**
     * 构造Cursor
     * @param input 输入字符串
     * @param index 下标
     */
    public Cursor(String input, int index) {
        this.input = input;
        this.index = index;
    }

    /**
     * 判断是否到达输入末尾
     */
    public boolean end() {
        return index == input.length();
    }

    /**
     * 获取当前光标指向的字符
     */
    public char current() {
        return input.charAt(index);
    }

    /**
     * 返回向前移动一个位置的光标
     */
    public Cursor next() {
        return new Cursor(input, index + 1);
    }

    /**
     * 获取输入字符串
     */
    public String input() {
        return input;
    }

    /**
     * 获取当前索引
     */
    public int index() {
        return index;
    }

    @Override
    public String toString() {
        return String.format("Cursor{parsed: '%s', remain: '%s'}",
                input.substring(0, index), input.substring(index));
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
}
