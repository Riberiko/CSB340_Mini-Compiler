import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LexerTest {

    Lexer l;

    Scanner in;
    String expected;
    String actual;


    @Test
    void countTest()
    {
        expected = getExpected("count");
        actual = getActual("count", "c");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void helloTest()
    {
        expected = getExpected("hello");
        actual = getActual("hello", "t");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void loopTest()
    {
        expected = getExpected("loop");
        actual = getActual("loop", "py");
        Assertions.assertEquals(expected, actual);
    }

    private String getExpected(String fileName)
    {
        StringBuilder sb = new StringBuilder();
        try {
            in = new Scanner(new File("src/test/resources/"+fileName+".lex"));
            while(in.hasNextLine()) sb.append(in.nextLine()).append('\n');
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return sb.toString().strip();
    }

    private String getActual(String fileName, String extension)
    {
        StringBuilder sb = new StringBuilder();
        try{
            in = new Scanner(new File("src/main/resources/"+fileName+"."+extension));
            while(in.hasNextLine()) sb.append(in.nextLine()).append('\n');
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
        l = new Lexer(sb.toString());

        return l.printTokens().strip();
    }
}
