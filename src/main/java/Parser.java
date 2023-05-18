import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;


class Parser {

    //List of tokens returned by the lexer
    private final List<Token> source;

    //Deque of tokens, currently unused.
//    private final ArrayDeque<Token> tokenQueue;

    //TypeMap for quickly finding NodeTypes
    private final HashMap<TokenType, NodeType> typeMap;

    //The current Token
    private Token currentToken;

    //The previous Token
    private Token prevToken;

    //The current position in the token list
    private int position;

    /**
     * Default Parser constructor. Sets up fields.
     *
     * @param source A list of Tokens.
     */
    Parser(List<Token> source) {
        this.source = source;
//        this.tokenQueue = new ArrayDeque<>(source);
        this.currentToken = null;
        this.prevToken = null;
        this.position = 0;
        this.typeMap = createTypeMap();
    }

    /**
     * Prints an error message and exits.
     *
     * @param line The line of the offending token.
     * @param pos  The column of the offending token.
     * @param msg  The error message to print.
     */
    private static void error(int line, int pos, String msg) {
        if (line > 0 && pos > 0) {
            System.out.printf("%s in line %d, pos %d\n", msg, line, pos);
        } else {
            System.out.println(msg);
        }
        System.exit(1);
    }

    /**
     * Write to a file. The filename almost certainly shouldn't be hard coded.
     *
     * @param result The string to write to a file.
     */
    private static void outputToFile(String result, String fileName) {
        try {
            FileWriter myWriter = new FileWriter("src/main/resources/" + fileName.substring(0, fileName.indexOf('.')) + ".par");
            myWriter.write(result);
            myWriter.close();
            System.out.printf("Successfully wrote to the file %s.lex.%n", fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a map between Token Types and Node Types
     *
     * @return A HashMap of Node Types
     */
    private static HashMap<TokenType, NodeType> createTypeMap() {
        HashMap<TokenType, NodeType> map = new HashMap<>();
        map.put(TokenType.Op_multiply, NodeType.nd_Mul);
        map.put(TokenType.Op_divide, NodeType.nd_Div);
        map.put(TokenType.Op_mod, NodeType.nd_Mod);
        map.put(TokenType.Op_add, NodeType.nd_Add);
        map.put(TokenType.Op_subtract, NodeType.nd_Sub);
        map.put(TokenType.Op_negate, NodeType.nd_Negate);
        map.put(TokenType.Op_not, NodeType.nd_Not);
        map.put(TokenType.Op_less, NodeType.nd_Lss);
        map.put(TokenType.Op_lessequal, NodeType.nd_Leq);
        map.put(TokenType.Op_greater, NodeType.nd_Gtr);
        map.put(TokenType.Op_greaterequal, NodeType.nd_Geq);
        map.put(TokenType.Op_equal, NodeType.nd_Eql);
        map.put(TokenType.Op_notequal, NodeType.nd_Neq);
        map.put(TokenType.Op_assign, NodeType.nd_Assign);
        map.put(TokenType.Op_and, NodeType.nd_And);
        map.put(TokenType.Op_or, NodeType.nd_Or);
        map.put(TokenType.Keyword_if, NodeType.nd_If);
        map.put(TokenType.Keyword_while, NodeType.nd_While);
        map.put(TokenType.Identifier, NodeType.nd_Ident);
        map.put(TokenType.Integer, NodeType.nd_Integer);
        map.put(TokenType.String, NodeType.nd_String);
        map.put(TokenType.Keyword_print, NodeType.nd_Prts);
        map.put(TokenType.Keyword_putc, NodeType.nd_Prtc);
        return map;
    }

    /**
     * Main function. Sets up things and drives the program.
     * TODO: Add args parsing
     * TODO: Refactor some of this code into other functions/constructors
     *
     * @param args Unused.
     */
    public static void main(String[] args) {
        try {
            String value, token;
            String result = " ";
            StringBuilder sb = new StringBuilder();
            int line, pos;
            Token t;
            boolean found;
            List<Token> list = new ArrayList<>();
            Map<String, TokenType> str_to_tokens = new HashMap<>();
            Arrays.stream(TokenType.values()).forEach(val -> str_to_tokens.put(val.toString(), val));

            Scanner s;
            File[] files = new File("src/test/resources").listFiles(file -> file.toString().endsWith(".lex"));

            //TODO : Uncomment line bellow to test specific files
            //files = new File[]{new File("src/test/resources/hello.lex")};

            for(File file : files)
            {
                s = new Scanner(file);

                String source = " ";
                while (s.hasNext()) {
                    String str = s.nextLine();
                    //StringTokenizer is officially discouraged in modern Java editions.
                    StringTokenizer st = new StringTokenizer(str);
                    line = Integer.parseInt(st.nextToken());
                    pos = Integer.parseInt(st.nextToken());
                    token = st.nextToken();
                    value = "";
                    while (st.hasMoreTokens()) {
                        value += st.nextToken() + " ";
                    }
                    //This could just be an if-else.
                    found = false;
                    if (str_to_tokens.containsKey(token)) {
                        found = true;
                        list.add(new Token(str_to_tokens.get(token), value, line, pos));
                    }
                    if (!found) {
                        throw new Exception("Token not found: '" + token + "'");
                    }
                }
                /*
                    This is the bread and butter, so to speak. A parser object is (finally) created at this point,
                    parse() is called, a traversal of the tree is saved to a result string, and finally that string is
                    written to a file.
                 */
                Parser p = new Parser(list);
                result = p.printAST(p.parse(), sb);
                outputToFile(result, file.getName());
            }
        } catch (Exception e) {
            error(-1, -1, "Exception: " + e.getMessage());
        }
    }

    /**
     * Gets the next token from the token list. No checks to see if it's actually in bound.
     *
     * @return The current token, which is a private field, and currently never used.
     */
    private Token nextToken() {
        this.prevToken = currentToken;
        this.currentToken = this.source.get(this.position++);
        return this.currentToken;
    }

    /**
     * Checks the type of the next token.
     *
     * @return The type of the next token in the list.
     */
    private TokenType nextTokenType() {
        return this.source.get(this.position).getTokentype();
    }

    /**
     * Checks for the beginning and end of a parenthetical bound expression, and returns the node of the contained expression.
     *
     * @return The root node of the bound expression.
     */
    private Node parentheticalExpression() {
        expect("paren_expr", TokenType.LeftParen);
        Node node = expressionParse();
        expect("paren_expr", TokenType.RightParen);
        return node;
    }

    /**
     * Checks if the current token is the expected type. If not, calls error.
     *
     * @param msg  The message to output in the event of an error.
     * @param type The expected type.
     * @return Boolean, if the value matched.
     */
    private boolean expect(String msg, TokenType type) {
        if (this.currentToken.tokentype == type) {
            nextToken();
            return true;
        }
        error(this.currentToken.line, this.currentToken.pos, msg + ": Expecting '" + type + "', found: '" + this.currentToken.tokentype + "'");
        return false;
    }

    /**
     * Checks if the passed token is the expected type. If not, calls error.
     *
     * @param msg   The message to output in the event of an error.
     * @param token The token to check.
     * @param type  The expected type.
     * @return Boolean, if the value matched.
     */
    private boolean expect(String msg, Token token, TokenType type) {
        if (token.tokentype == type) {
            return true;
        }
        error(token.line, token.pos, msg + ": Expecting '" + type + "', found: '" + token.tokentype + "'");
        return false;
    }

    /**
     * Checks if the passed token is the expected type, but does not call error if false.
     *
     * @param token The token to check.
     * @param type  The expected type.
     * @return Boolean, if the value matched.
     */
    private boolean softCheck(Token token, TokenType type) {
        return token.tokentype == type;
    }

    /**
     * Iterates through the token list until an end of input token is found, creating nodes along the way.
     * I don't feel like it works the way it's supposed to, but it was given like this, so~
     *
     * @return A node
     */
    Node parse() {
        Node node = null;
        nextToken();
        while (currentToken.getTokentype() != TokenType.End_of_input) {
            node = Node.makeNode(null, NodeType.nd_Sequence, node, statementParse());
            nextToken();
        }
        return node;
    }

    /**
     * Parses a value assignment.
     * TODO: This could actually probably be folded into expression.
     * TODO: On the other hand, needs to ensure it isn't a full expression on the left side.
     *
     * @return The assignment node.
     */
    private Node assignmentParse() {
        //Pretty sure this is wrong
        expect("Error with expected assignment", TokenType.Op_assign);
        expect("Error with expected assignment", prevToken, TokenType.Identifier);
        return Node.makeNode(currentToken, NodeType.nd_Assign, Node.makeNode(prevToken, NodeType.nd_Ident), expressionParse());
    }

    /**
     * Parses statements.
     * Should these go to the left or to the right?
     * TODO: Finish/do
     *
     * @return The statement node.
     */
    private Node statementParse() {
        Node node = null;
        NodeType nodeType = null;
        switch (currentToken.getTokentype()) {
            case Keyword_else:      //Else should only work if there is a corresponding if. Not sure how to parse that given the current restraints.
            case Keyword_if:
            case Keyword_print:
            case Keyword_putc:
            case Keyword_while:
                nextToken();
                return Node.makeNode(prevToken, typeMap.get(prevToken.getTokentype()), expressionParse(), null);
            case LeftBrace:
                return braceStatement();
            case LeftParen:
                return parentheticalExpression();
            case Identifier:    //Not confident on this one, last minute changes.
                nextToken();
                return expressionParse();
//                nextToken();
//                return assignmentParse();
            case Semicolon:
                return null;
            case Comma:
                //I can't come up with a clean enough rule for this to be sure what to do with it.
            case RightBrace:
                return null;
            default:
                error(currentToken.getLine(), currentToken.getPos(), "Unexpected " + currentToken.getTokentype() + " found ");
        }
        return null;
    }

    private Node braceStatement() {
        expect("brace_statement", TokenType.LeftBrace);
        Node node = statementParse();
        expect("brace_statement", TokenType.LeftBrace);
        return node;
    }

    /**
     * Checks if the next token is an operation.
     *
     * @return If the next token is an operation.
     */
    private boolean opCheck() {
        switch (nextTokenType()) {
            case Op_add, Op_and, Op_divide, Op_equal, Op_greater, Op_greaterequal, Op_less, Op_lessequal,Op_mod, Op_multiply, Op_negate, Op_not, Op_notequal, Op_or, Op_subtract:
                return true;
            //Other types are realistically an error.
            default:
                return false;
        }
    }

    /**
     * Parses expressions. For the sake of time and sanity, I didn't bother with precedence checking.
     *
     * @return The expression node.
     */
    private Node expressionParse() {
        Node node = null;
        switch (currentToken.getTokentype()) {
            case LeftParen:
                node = parentheticalExpression();
                if (opCheck()) {
                    nextToken();
                    return Node.makeNode(currentToken, typeMap.get(currentToken.getTokentype()), node, expressionParse());
                } else {
                    return node;
                }
            case Identifier, Integer, String:
                if (opCheck()) {
                    nextToken();
                    return Node.makeNode(currentToken, typeMap.get(currentToken.getTokentype()), Node.makeNode(prevToken, typeMap.get(prevToken.getTokentype())), expressionParse());
                } else {
                    nextToken();
                    return new Node(prevToken, typeMap.get(prevToken.getTokentype()));
                }
            case Op_assign: assignmentParse();
            case End_of_input, RightParen, RightBrace, Semicolon, Comma:         //I'm actually not sure about comma here, should probably call expression again.
                error(currentToken.getLine(), currentToken.getPos(), "Unexpected " + currentToken.getTokentype() + " found ");
            case Keyword_if, Keyword_else, Keyword_print, Keyword_putc, Keyword_while:
                error(currentToken.getLine(), currentToken.getPos(), "Unexpected " + currentToken.getTokentype() + " found ");
        }
        return null;
    }

    /**
     * Preforms a pre-order traversal of the AST tree to build a string to print representing the AST. As it stands, it
     * specifically looks for empty leaves, which may not mesh with the changes I've made.
     * <p>
     * I'm not confident this will get everything unless I change how things are structured.
     *
     * @param t  The token to parse.
     * @param sb A string builder.
     * @return The resulting string from the string builder.
     */
    String printAST(Node t, StringBuilder sb) {
        int i = 0;
        if (t == null) {
            sb.append(";");
            sb.append("\n");
            System.out.println(";");
        } else {
            sb.append(t.getType());
            System.out.printf("%-14s", t.getType());
            if (t.getType() == NodeType.nd_Ident || t.getType() == NodeType.nd_Integer || t.getType() == NodeType.nd_String) {
                sb.append(" " + t.value);
                sb.append("\n");
                System.out.println(" " + t.value);
            } else {
                sb.append("\n");
                System.out.println();
                printAST(t.left, sb);
                printAST(t.right, sb);
            }

        }
        return sb.toString();
    }

    /**
     * Enum of the different token types.
     */
    enum TokenType {
        Comma(false, false, false, -1, NodeType.nd_None),
        End_of_input(false, false, false, -1, NodeType.nd_None),
        Identifier(false, false, false, -1, NodeType.nd_Ident),
        Integer(false, false, false, -1, NodeType.nd_Integer),
        Keyword_else(false, false, false, -1, NodeType.nd_None),
        Keyword_if(false, false, false, -1, NodeType.nd_If),
        Keyword_print(false, false, false, -1, NodeType.nd_None),
        Keyword_putc(false, false, false, -1, NodeType.nd_None),
        Keyword_while(false, false, false, -1, NodeType.nd_While),
        LeftBrace(false, false, false, -1, NodeType.nd_None),
        LeftParen(false, false, false, -1, NodeType.nd_None),
        Op_add(false, true, false, 12, NodeType.nd_Add),
        Op_and(false, true, false, 5, NodeType.nd_And),
        Op_assign(false, false, false, -1, NodeType.nd_Assign),
        Op_divide(false, true, false, 13, NodeType.nd_Div),
        Op_equal(false, true, true, 9, NodeType.nd_Eql),
        Op_greater(false, true, false, 10, NodeType.nd_Gtr),
        Op_greaterequal(false, true, false, 10, NodeType.nd_Geq),
        Op_less(false, true, false, 10, NodeType.nd_Lss),
        Op_lessequal(false, true, false, 10, NodeType.nd_Leq),
        Op_mod(false, true, false, 13, NodeType.nd_Mod),
        Op_multiply(false, true, false, 13, NodeType.nd_Mul),
        Op_negate(false, false, true, 14, NodeType.nd_Negate),
        Op_not(false, false, true, 14, NodeType.nd_Not),
        Op_notequal(false, true, false, 9, NodeType.nd_Neq),
        Op_or(false, true, false, 4, NodeType.nd_Or),
        Op_subtract(false, true, false, 12, NodeType.nd_Sub),
        RightBrace(false, false, false, -1, NodeType.nd_None),
        RightParen(false, false, false, -1, NodeType.nd_None),
        Semicolon(false, false, false, -1, NodeType.nd_None),
        String(false, false, false, -1, NodeType.nd_String);

        private final int precedence;
        private final boolean right_assoc;
        private final boolean is_binary;
        private final boolean is_unary;
        private final NodeType node_type;

        TokenType(boolean right_assoc, boolean is_binary, boolean is_unary, int precedence, NodeType node) {
            this.right_assoc = right_assoc;
            this.is_binary = is_binary;
            this.is_unary = is_unary;
            this.precedence = precedence;
            this.node_type = node;
        }

        boolean isRightAssoc() {
            return this.right_assoc;
        }

        boolean isBinary() {
            return this.is_binary;
        }

        boolean isUnary() {
            return this.is_unary;
        }

        int getPrecedence() {
            return this.precedence;
        }

        NodeType getNodeType() {
            return this.node_type;
        }
    }

    /**
     * Enum of the different node types.
     */
    private enum NodeType {
        nd_Add("Add"),              //Expression
        nd_And("And"),              //Expression
        nd_Assign("Assign"), nd_Div("Divide"),           //Expression
        nd_Eql("Equal"),            //Expression
        nd_Geq("GreaterEqual"),     //Expression
        nd_Gtr("Greater"),          //Expression
        nd_Ident("Identifier"),     //Terminal
        nd_If("If"), nd_Integer("Integer"),      //Terminal
        nd_Leq("LessEqual"),        //Expression
        nd_Lss("Less"),             //Expression
        nd_Mod("Mod"),              //Expression
        nd_Mul("Multiply"),         //Expression
        nd_Negate("Negate"),        //Expression
        nd_Neq("NotEqual"),         //Expression
        nd_None(""),                //Special-ish
        nd_Not("Not"),              //Expression
        nd_Or("Or"),                //Expression
        nd_Prtc("Prtc"),            //Special-ish
        nd_Prti("Prti"),            //Special-ish
        nd_Prts("Prts"),            //Special-ish
        nd_Sequence("Sequence"),    //Special
        nd_String("String"),        //Terminal
        nd_Sub("Subtract"),         //Expression
        nd_While("While");
        private final String name;

        NodeType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    /**
     * Internal node class for the AST.
     */
    private static class Node {

        //Token held by the node.
        private final Token token;

        //The type of node based on the held value.
        private final NodeType type;

        //Currently not used??? No calls to the only constructor that uses this, and no make_node that uses this.
        public String value;
        private Node left;
        private Node right;

        //Valueless constructor.
        Node() {
            this(null, null, null, null, null);
        }

        Node(Token token, NodeType nodeType) {
            this(token, nodeType, null, null, null);
        }

        //Full constructor.
        Node(Token token, NodeType node_type, Node left, Node right, String value) {
            this.token = token;
            this.type = node_type;
            this.left = left;
            this.right = right;
            this.value = value;
        }

        public static Node makeNode(Token token, NodeType nodeType) {
            return makeNode(token, nodeType, null, null);
        }

        public static Node makeNode(Token token, NodeType nodeType, Node left, Node right) {
            return new Node(token, nodeType, left, right, null);
        }

        public TokenType getTokenType() {
            return token.tokentype;
        }

        public Token getToken() {
            return token;
        }

        public Node getLeft() {
            return left;
        }

        public void setLeft(Node left) {
            this.left = left;
        }

        public Node getRight() {
            return right;
        }

        public void setRight(Node right) {
            this.right = right;
        }

        public NodeType getType() {
            return type;
        }
    }

    /**
     * Static token class.
     * Should probably be in another file and shared with the lexer.
     */
    static class Token {
        private final TokenType tokentype;
        private final String value;
        private final int line;
        public int pos;

        Token(TokenType token, String value, int line, int pos) {
            this.tokentype = token;
            this.value = value;
            this.line = line;
            this.pos = pos;
        }

        public TokenType getTokentype() {
            return tokentype;
        }

        public String getValue() {
            return value;
        }

        public int getLine() {
            return line;
        }

        public int getPos() {
            return pos;
        }

        /**
         * Prints something
         *
         * @return The string to print.
         */
        @Override
        public String toString() {
            return String.format("%5d  %5d %-15s %s", this.line, this.pos, this.tokentype, this.value);
        }
    }
}