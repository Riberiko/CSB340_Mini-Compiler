import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParserTest {

    String expected;
    String actual;

    @Test
    void countTest()
    {
        expected = ExpectedAndActual.getExpected("count", "par");
        actual = ExpectedAndActual.getActualParser("count", "c");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void helloTest()
    {
        expected = ExpectedAndActual.getExpected("hello", "par");
        actual = ExpectedAndActual.getActualParser("hello", "t");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void primeTest()
    {
        expected = ExpectedAndActual.getExpected("prime", "par");
        actual = ExpectedAndActual.getActualParser("prime", "c");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void loopTest()
    {
        expected = ExpectedAndActual.getExpected("loop", "par");
        actual = ExpectedAndActual.getActualParser("loop", "py");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void bottleTest()
    {
        expected = ExpectedAndActual.getExpected("99bottles", "par");
        actual = ExpectedAndActual.getActualParser("99bottles", "c");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void fizzbuzzTest()
    {
        expected = ExpectedAndActual.getExpected("fizzbuzz", "par");
        actual = ExpectedAndActual.getActualParser("fizzbuzz", "c");
        Assertions.assertEquals(expected, actual);
    }
}
