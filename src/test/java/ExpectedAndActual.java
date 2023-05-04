import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;


public class ExpectedAndActual {

    static Scanner in;

    public static String getExpected(String fileName, String extension)
    {
        StringBuilder sb = new StringBuilder();
        try {
            in = new Scanner(new File("src/test/resources/"+fileName+"."+extension));
            while(in.hasNextLine()) sb.append(in.nextLine()).append('\n');
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return sb.toString().strip();
    }

    public static String getActualLexer(String fileName, String extension)
    {
        StringBuilder sb = new StringBuilder();
        try{
            in = new Scanner(new File("src/main/resources/"+fileName+"."+extension));
            while(in.hasNextLine()) sb.append(in.nextLine()+ '\n');
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return new Lexer(sb.toString()).printTokens().strip();
    }

    public static String getActualParser(String fileName, String extension)
    {
        StringBuilder sb = new StringBuilder();
        List<Parser.Token> lst = new ArrayList<>();
        try{
            in = new Scanner(new File("src/main/resources/"+fileName+"."+extension));
            while(in.hasNextLine()) sb.append(in.nextLine()+ '\n');
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
        Lexer l = new Lexer(sb.toString());
        Lexer.Token t = l.getToken();

        while(t.tokentype != Lexer.TokenType.End_of_input)
        {
            StringTokenizer st = new StringTokenizer();
        }
    }
}
