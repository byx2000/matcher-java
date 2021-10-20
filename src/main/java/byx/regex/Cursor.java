package byx.regex;

import java.util.Objects;

public class Cursor {
    private final String input;
    private final int index;

    public Cursor(String input, int index) {
        this.input = input;
        this.index = index;
    }

    public boolean end() {
        return index == input.length();
    }

    public char current() {
        return input.charAt(index);
    }

    public Cursor next() {
        return new Cursor(input, index + 1);
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
