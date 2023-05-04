import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LexerTest {

    String expected;
    String actual;

    @Test
    void countTest()
    {
        expected = ExpectedAndActual.getExpected("count", "lex");
        actual = ExpectedAndActual.getActualLexer("count", "c");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void helloTest()
    {
        expected = ExpectedAndActual.getExpected("hello", "lex");
        actual = ExpectedAndActual.getActualLexer("hello", "t");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void primeTest()
    {
        expected = ExpectedAndActual.getExpected("prime", "lex");
        actual = ExpectedAndActual.getActualLexer("prime", "c");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void loopTest()
    {
        expected = ExpectedAndActual.getExpected("loop", "lex");
        actual = ExpectedAndActual.getActualLexer("loop", "py");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void bottleTest()
    {
        expected = ExpectedAndActual.getExpected("99bottles", "lex");
        actual = ExpectedAndActual.getActualLexer("99bottles", "c");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void fizzbuzzTest()
    {
        expected = ExpectedAndActual.getExpected("fizzbuzz", "lex");
        actual = ExpectedAndActual.getActualLexer("fizzbuzz", "c");
        Assertions.assertEquals(expected, actual);
    }
}
