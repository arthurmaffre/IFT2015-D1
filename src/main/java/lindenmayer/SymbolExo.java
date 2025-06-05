package lindenmayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SymbolExo {
    private static final Map<Character, SymbolExo> POOL = new ConcurrentHashMap<>();

    public static SymbolExo of(char ch) {
        return POOL.computeIfAbsent(ch, SymbolExo::new);
    }

    private final char value;

    private SymbolExo(char value) {
        this.value = value;
    }

    public char getChar() {
        return value;
    }



    @Override
    public String toString() {
        return "<" + value + ">";
    }

    @Override
    public int hashCode() {
        return Character.hashCode(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof SymbolExo s)
            return this.value == s.value;
        if (o instanceof Symbol original)
            return this.value == original.toString().charAt(0);
        return false;
    }

}
