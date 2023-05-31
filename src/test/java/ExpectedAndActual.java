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

    public static String getActualParser(String fileName)
    {
        StringBuilder result = new StringBuilder();
        try{
            StringBuilder sb = new StringBuilder();
            List<Parser.Token> lst = new ArrayList<>();
            String value;
            int pos, line;
            String token;
            boolean found;


            in = new Scanner(new File("src/main/output/"+fileName+".lex"));

            while (in.hasNext()) {
                String str = in.nextLine();
                StringTokenizer st = new StringTokenizer(str);
                line = Integer.parseInt(st.nextToken());
                pos = Integer.parseInt(st.nextToken());
                token = st.nextToken();
                value = "";
                while (st.hasMoreTokens()) {
                    value += st.nextToken() + " ";
                }
                found = false;
                if (strToMap.containsKey(token)) {
                    found = true;
                    lst.add(new Parser.Token(strToMap.get(token), value, line, pos));
                }
                if (found == false) {
                    throw new Exception("Token not found: '" + token + "'");
                }
            }
            Parser p = new Parser(lst);
            p.printAST(p.parse(), result);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result.toString().strip();
    }
}
