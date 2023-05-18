import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;


public class ExpectedAndActual {

    static Scanner in;
    static Map<String, Parser.TokenType> strToMap;

    static
    {
        strToMap = new HashMap<>();
        Arrays.stream(Parser.TokenType.values()).forEach(var -> strToMap.put(var.toString(), var));
    }

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
        StringBuilder result = new StringBuilder();
        try{
            StringBuilder sb = new StringBuilder();
            List<Parser.Token> lst = new ArrayList<>();
            Lexer.Token lexerT;

            in = new Scanner(new File("src/main/resources/"+fileName+"."+extension));
            while(in.hasNextLine()) sb.append(in.nextLine()+ '\n');

            Lexer l = new Lexer(sb.toString());
            while ((lexerT = l.getToken()).tokentype != Lexer.TokenType.End_of_input)
            {
                lst.add(new Parser.Token(strToMap.get(lexerT.tokentype.toString()), lexerT.value, lexerT.line, lexerT.pos));
                System.out.println(lst.get(lst.size()-1));
                if(lst.get(lst.size()-1) == null) throw new RuntimeException("Lexer Token Type was Could not be mapped to a Parser Token Type");
            }
            //gets the end of input token as well and adds to list
            lst.add(new Parser.Token(strToMap.get(lexerT.tokentype.toString()), lexerT.value, lexerT.line, lexerT.pos));
            Parser p = new Parser(lst);
            p.printAST(p.parse(), result);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }

        return result.toString().strip();
    }
}
