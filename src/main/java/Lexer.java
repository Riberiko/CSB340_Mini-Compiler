import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Lexical Analyzer
 *
 * Given a stream characters the Lexer will group the various character
 * categorizing then into tokens
 *
 * @author Riberiko Niyomwungere
 * @version 1.0
 */
public class Lexer {
    private int line;
    private int pos;
    private int position;
    private char chr;
    private final String s;

    Map<String, TokenType> keywords = new HashMap<>();

    /**
     * Lexical Analyzer Token
     *
     * Stores the information on the group of characters passed in as the value
     */
    static class Token {
        public TokenType tokentype;
        public String value;
        public int line;
        public int pos;
        Token(TokenType token, String value, int line, int pos) {
            this.tokentype = token; this.value = value; this.line = line; this.pos = pos;
        }
        @Override
        public String toString() {
            String result = String.format("%5d  %5d %-15s", this.line, this.pos, this.tokentype);
            switch (this.tokentype) {
                case Integer -> result += String.format("  %4s", value);
                case Identifier -> result += String.format(" %s", value);
                case String -> result += String.format(" \"%s\"", value);
            }
            return result;
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    enum TokenType {
        End_of_input, Op_multiply,  Op_divide, Op_mod, Op_add, Op_subtract,
        Op_negate, Op_not, Op_less, Op_lessequal, Op_greater, Op_greaterequal,
        Op_equal, Op_notequal, Op_assign, Op_and, Op_or, Keyword_if,
        Keyword_else, Keyword_while, Keyword_print, Keyword_putc, LeftParen, RightParen,
        LeftBrace, RightBrace, Semicolon, Comma, Identifier, Integer, String
    }

    /**
     * Error handling
     *
     * @param line  where error occurs
     * @param pos   where error occurs
     * @param msg   what has happened
     */
    static void error(int line, int pos, String msg) {
        if (line > 0 && pos > 0) {
            System.out.printf("%s in line %d, pos %d\n", msg, line, pos);
        } else {
            System.out.println(msg);
        }
        System.exit(1);
    }

    /**
     * Lexer Parser
     * Given source code this parser categorizes them into tokens
     *
     * @param source    the source code
     */
    @SuppressWarnings("SpellCheckingInspection")
    Lexer(String source) {
        this.line = 1;
        this.pos = 0;
        this.position = -1;
        this.s = source+'\u0000';   //added the end string character so that I would know when the input was done
        this.chr = getNextChar();
        this.keywords.put("if", TokenType.Keyword_if);
        this.keywords.put("else", TokenType.Keyword_else);
        this.keywords.put("print", TokenType.Keyword_print);
        this.keywords.put("putc", TokenType.Keyword_putc);
        this.keywords.put("while", TokenType.Keyword_while);

    }

    /**
     * When there exsits a posibility that the next character can change the meaning of the current
     *
     * @param expect    the character that could change the meaning of the current character
     * @param ifyes the token type assuming that the expected character is found
     * @param ifno  the token type assuming the expected character is not found
     * @param line  the current line
     * @param pos   the current position on the line
     * @return  a token based on where the expected character was found
     */
    @SuppressWarnings("SpellCheckingInspection")
    Token follow(char expect, TokenType ifyes, TokenType ifno, int line, int pos) {
        if (getNextChar() == expect) {
            getNextChar();
            return new Token(ifyes, Character.toString(chr)+expect, line, pos);
        }
        if (ifno == TokenType.End_of_input) {
            error(line, pos, String.format("follow: unrecognized character: (%d) '%c'", (int) chr, chr));
        }
        return new Token(ifno, Character.toString(chr)+expect, line, pos);
    }

    /**
     * Handles character literals
     *
     * @param line  the current line
     * @param pos   the current position on the line
     * @return  an integer token with the character representation for the value
     */
    Token char_lit(int line, int pos) {
        StringBuilder sb = new StringBuilder();
        sb.append(chr);

        if(isDigit(chr)) while(isDigit(getNextChar())) sb.append(chr);
        else
        {
            if(getNextChar() == '\'') getNextChar();    //skips the ending '
            else error(line, pos, String.format("follow: unrecognized character: (%d) '%c'", (int) chr, chr));
        }
        return new Token(TokenType.Integer, sb.toString(), line, pos);
    }

    /**
     * Handles string literals
     *
     * @param start the first character
     * @param line  the current line
     * @param pos   the current position on the line
     * @return  a string token
     */
    Token string_lit(char start, int line, int pos) {
        StringBuilder sb = new StringBuilder();
        sb.append(start);
        while(getNextChar() != '"') sb.append(chr);
        getNextChar();  //ignores the ending "
        return new Token(TokenType.String, sb.toString(), line, pos);
    }

    /**
     * Handles comments
     * This will ignore every thing until the comment has ended
     *
     * @param line the current line
     * @param pos   the position on the line
     * @return  the next token after the comment
     */
    @SuppressWarnings("unused") //I did not want to change the method signature
    Token div_or_comment(int line, int pos) { //should have handled both the division or comment but chose to only have this handle comment
        char prv;
        if(chr == '/') {
            this.line++;
            while (chr != '\n' && chr != '\u0000') getNextChar();
        }
        else if(chr == '*') {
            do {
                if(chr == '\n') {this.line++; this.pos = 0;}
                getNextChar();
                prv = chr;
            } while ((prv != '*' && chr != '/') && chr != '\u0000');
            getNextChar();
            getNextChar();
        }
        return getToken();
    }

    /**
     * Handles identifiers
     * @param line  the current line
     * @param pos   the position on the line
     * @return  an identifiers
     */
    Token identifier_or_integer(int line, int pos) { // should have handled identifiers or integer but chose to have this only handle identifiers
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(chr);
            getNextChar();
        }while(isAlpha(this.chr) || isDigit(this.chr) || chr == '_');
        return new Token( (keywords.get(sb.toString()) == null) ? TokenType.Identifier : keywords.get(sb.toString()), sb.toString(), line, pos);
    }

    /**
     * Handles Operators
     *
     * @param line  the current line
     * @param pos   the position on the line
     * @return  an operator token
     */
    Token operator(int line, int pos){
        if(chr == '-')
        {
            getNextChar();
            while(Character.isWhitespace(chr)) //determining if we are dealing with a negative number or a subtraction operator
            { if(chr == '\n') {this.line++; this.pos = 0;} getNextChar();}
            return new Token( (isDigit(chr)) ? TokenType.Op_negate : TokenType.Op_subtract, "-", line, pos);
        }

        switch (chr){
            case '*' : {
                getNextChar();
                return new Token(TokenType.Op_multiply, "*", line, pos);
            }
            case '/' : {
                getNextChar();
                if(chr == '*' || chr == '/') return div_or_comment(line, pos);
                return new Token(TokenType.Op_divide, "/", line, pos);
            }
            case '%' : {
                getNextChar();
                return new Token(TokenType.Op_mod, "%", line, pos);
            }
            case '+' : {
                getNextChar();
                return new Token(TokenType.Op_add, "+", line, pos);
            }
            case '<' : return follow('=', TokenType.Op_lessequal, TokenType.Op_less, line, pos);
            case '>' : return follow('=', TokenType.Op_greaterequal, TokenType.Op_greater, line, pos);
            case '=' : return follow('=', TokenType.Op_equal, TokenType.Op_assign, line, pos);
            case '!' : return follow('=', TokenType.Op_notequal, TokenType.Op_not, line, pos);
            case '&' : return follow('&', TokenType.Op_and, TokenType.End_of_input, line, pos);
            case '|' : return follow('|', TokenType.Op_or, TokenType.End_of_input, line, pos);
            default: return null; //this is unreachable because all operators have been handled
        }
    }

    /**
     * Retrieves the next token
     *
     * @return the next token
     */
    Token getToken() {
        while (Character.isWhitespace(this.chr)) {
            if(chr == '\n') {this.line++; this.pos = 0;}
            getNextChar();
        }

        if(chr == '\u0000') return new Token(TokenType.End_of_input, "", line, pos);
        else if(isAlpha(chr) || chr == '_') return identifier_or_integer(line, pos);
        else if(isDigit(chr)) return char_lit(line, pos);
        else if(isOperator(chr)) return operator(line, pos);
        else{
            char prv = chr;
            int pos = this.pos;
            getNextChar();
            switch (prv) {
                case '\'' : return char_lit(line, pos);
                case '\"' : return string_lit(chr, line, pos);
                case ';' : return new Token(TokenType.Semicolon, ";", line, pos);
                case ',' : return new Token(TokenType.Comma, ",", line, pos);
                case '}' : return new Token(TokenType.RightBrace, "}", line, pos);
                case '{' : return new Token(TokenType.LeftBrace, "{", line, pos);
                case ')' : return new Token(TokenType.RightParen, ")", line, pos);
                case '(' : return new Token(TokenType.LeftParen, "(", line, pos);
                case '#' : return div_or_comment(line, pos);
                default: error(line, pos, "Lexer Incapable of Handling this char unless as string or character");
            }
        }
        return null;    //this is unreachable because the default error method will terminate the program
    }

    /**
     * Retrieves the next character from the source code
     * @return  the next character
     */
    char getNextChar() {
        pos++;
        chr = s.charAt(++position);
        return this.chr;
    }

    boolean isDigit(char c)
    {
        return (int) c <= 57 && (int) c >= 48;
    }

    boolean isAlpha(char c)
    {
        return ((int) c <= 122 && (int) c >= 97) || ((int) c >= 65 && (int) c <= 90);
    }

    boolean isOperator(char c)
    {
        return switch (c) {
            case '*', '/', '%', '+', '-', '<', '=', '>', '!', '|', '&' -> true;
            default -> false;
        };
    }

    /**
     * Builds string of the result but will not print
     * @return  result from the lexer
     */
    String printTokens() {
        Token t;
        StringBuilder sb = new StringBuilder();
        while ((t = getToken()).tokentype != TokenType.End_of_input) {
            sb.append(t);
            sb.append("\n");
        }
        sb.append(t);
        return sb.toString();
    }

    /**
     * Writes results to a file
     *
     * @param result to write to file
     * @param fileName  the name of the file to write to
     */
    static void outputToFile(String result, String fileName) {
        try {
            FileWriter myWriter = new FileWriter("src/main/resources/" + fileName + ".lex");
            myWriter.write(result);
            myWriter.close();
            System.out.printf("Successfully wrote to the file %s.lex.%n", fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        File[] files = new File("src/main/resources").listFiles(file -> !file.toString().endsWith(".lex") && !file.toString().endsWith(".par"));
        Scanner s;
        String source = "";
        assert files != null;
        for(File file : files){
            try {
                s = new Scanner(file);
                while (s.hasNext()) {
                    source += s.nextLine() + "\n";
                }
                outputToFile(new Lexer(source).printTokens(), file.getName().substring(0, file.getName().indexOf('.')));
            } catch (FileNotFoundException e) {
                error(-1, -1, "Exception: " + e.getMessage());
            }
            source = "";
        }
    }
}