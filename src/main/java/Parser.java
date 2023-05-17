import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

class Parser {

    //List of tokens returned by the lexer
    private final List<Token> source;

    private final ArrayDeque<Token> tokenQueue;
    private Token currentToken;

    private Token prevToken;

    //Apparently this fucker is for keeping track of where the parser is in the Token list?????
    private int position;

    /**
     * Parser constructor. Sets up basic fields.
     * Wtf is position for?
     *
     * @param source A list of Tokens.
     */
    Parser(List<Token> source) {
        this.source = source;
        this.tokenQueue = new ArrayDeque<>(source);
        this.currentToken = null;
        this.prevToken = null;
        this.position = 0;
    }

    /**
     * Prints an error message and exits.
     *
     * @param line The line of the offending token.
     * @param pos  The column of the offending token.
     * @param msg  The error message to print.
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
     * Write to a file. The filename almost certainly shouldn't be hard coded.
     *
     * @param result The string to write to a file.
     */
    private static void outputToFile(String result) {
        try {
            FileWriter myWriter = new FileWriter("src/main/resources/hello.par");
            myWriter.write(result);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static HashMap<String, TokenType> createTokenMap() {
        HashMap<String, TokenType> map = new HashMap<>();
        map.put("End_of_input", TokenType.End_of_input);
        map.put("Op_multiply", TokenType.Op_multiply);          //Expression op
        map.put("Op_divide", TokenType.Op_divide);              //Expression op
        map.put("Op_mod", TokenType.Op_mod);                    //Expression op
        map.put("Op_add", TokenType.Op_add);                    //Expression op
        map.put("Op_subtract", TokenType.Op_subtract);          //Expression op
        map.put("Op_negate", TokenType.Op_negate);              //Expression op
        map.put("Op_not", TokenType.Op_not);                    //Expression op
        map.put("Op_less", TokenType.Op_less);                  //Expression op
        map.put("Op_lessequal", TokenType.Op_lessequal);        //Expression op
        map.put("Op_greater", TokenType.Op_greater);            //Expression op
        map.put("Op_greaterequal", TokenType.Op_greaterequal);  //Expression op
        map.put("Op_equal", TokenType.Op_equal);                //Expression op
        map.put("Op_notequal", TokenType.Op_notequal);          //Expression op
        map.put("Op_assign", TokenType.Op_assign);              //Expression op: Assignment
        map.put("Op_and", TokenType.Op_and);                    //Expression op
        map.put("Op_or", TokenType.Op_or);                      //Expression op
        map.put("Keyword_if", TokenType.Keyword_if);            //Statement op
        map.put("Keyword_else", TokenType.Keyword_else);        //Statement op
        map.put("Keyword_while", TokenType.Keyword_while);      //Statement op
        map.put("Keyword_print", TokenType.Keyword_print);      //Statement op
        map.put("Keyword_putc", TokenType.Keyword_putc);        //Statement op???
        map.put("LeftParen", TokenType.LeftParen);              //Starter token
        map.put("LeftBrace", TokenType.LeftBrace);              //Starter token
        map.put("RightParen", TokenType.RightParen);            //End token
        map.put("RightBrace", TokenType.RightBrace);            //End token
        map.put("Semicolon", TokenType.Semicolon);              //End token
        map.put("Comma", TokenType.Comma);
        map.put("Identifier", TokenType.Identifier);
        map.put("Integer", TokenType.Integer);
        map.put("String", TokenType.String);
        return map;
    }

    /**
     * TODO: Why in the cosmological fuck does it open with "if 1=1"????
     *
     * @param args Unused.
     */
    public static void main(String[] args) {
        if (1 == 1) {
            try {
                String value, token;
                String result = " ";
                StringBuilder sb = new StringBuilder();
                int line, pos;
                Token t;
                boolean found;
                List<Token> list = new ArrayList<>();
                Map<String, TokenType> str_to_tokens = new HashMap<>();


                //TODO: create the map.
                // finish creating your Hashmap. I left one as a model

                //Creates the list of tokens for the parser.
                Scanner s = new Scanner(new File("src/main/resources/hello.lex"));
                String source = " ";
                while (s.hasNext()) {
                    String str = s.nextLine();
                    //I had to look up this one. No wonder, it's officially discouraged.
                    StringTokenizer st = new StringTokenizer(str);
                    line = Integer.parseInt(st.nextToken());
                    pos = Integer.parseInt(st.nextToken());
                    token = st.nextToken();
                    value = "";
                    while (st.hasMoreTokens()) {
                        value += st.nextToken() + " ";
                    }
                    //This is an unnecessary value, this could just be an if-else.
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
                outputToFile(result);
            } catch (FileNotFoundException e) {
                error(-1, -1, "Exception: " + e.getMessage());
            } catch (Exception e) {
                error(-1, -1, "Exception: " + e.getMessage());
            }
        } else {
            error(-1, -1, "No args");
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
     * TODO: This is one of the primary functions to implement.
     * EXPRESSION: Something that evaluates to a value.
     *
     * @param p ???
     * @return A node ???
     */
    private Node expr(int p) {
        // create nodes for token types such as LeftParen, Op_add, Op_subtract, etc.
        // be very careful here and be aware of the precendence rules for the AST tree
        Node result = null, node;

        return result;
    }


    /**
     * Checks for the beginning and end of a parenthetical bound expression, and returns the node of the expression.
     *
     * @return The first node of the bound expression.
     */
    private Node paren_expr() {
        expect("paren_expr", TokenType.LeftParen);
        Node node = expr(0);
        expect("paren_expr", TokenType.RightParen);
        return node;
    }

    private void expect(String msg, TokenType s) {
        if (this.currentToken.tokentype == s) {
            nextToken();
            return;
        }
        error(this.currentToken.line, this.currentToken.pos, msg + ": Expecting '" + s + "', found: '" + this.currentToken.tokentype + "'");
    }

    /**
     * Iterates through the token list until an end of input token is found, creating nodes along the way.
     *
     * @return T
     */
    private Node parse() {
        Node entryNode = new Node(null, NodeType.nd_Sequence);
        

        return null;
    }

    private Node statementParse(Node node) {
        return null;
    }

    private Node expressionParse(Node node) {
        return null;
    }


    //
    //S
    //As

    /**
     * Preforms a pre-order traversal of the AST tree to build a string to print representing the AST. As it stands, it
     * specifically looks for empty leaves for some reason, probably want to change this. Additionally, t should *always*
     * be an End of Input token, even if the file to parse was empty.
     *
     * @param t  A token that should always be an End of Input token.
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
    private enum TokenType {
        Start_of_input(false, false, false, -1, NodeType.nd_None), End_of_input(false, false, false, -1, NodeType.nd_None), Op_multiply(false, true, false, 13, NodeType.nd_Mul), Op_divide(false, true, false, 13, NodeType.nd_Div), Op_mod(false, true, false, 13, NodeType.nd_Mod), Op_add(false, true, false, 12, NodeType.nd_Add), Op_subtract(false, true, false, 12, NodeType.nd_Sub), Op_negate(false, false, true, 14, NodeType.nd_Negate), Op_not(false, false, true, 14, NodeType.nd_Not), Op_less(false, true, false, 10, NodeType.nd_Lss), Op_lessequal(false, true, false, 10, NodeType.nd_Leq), Op_greater(false, true, false, 10, NodeType.nd_Gtr), Op_greaterequal(false, true, false, 10, NodeType.nd_Geq), Op_equal(false, true, true, 9, NodeType.nd_Eql), Op_notequal(false, true, false, 9, NodeType.nd_Neq), Op_assign(false, false, false, -1, NodeType.nd_Assign), Op_and(false, true, false, 5, NodeType.nd_And), Op_or(false, true, false, 4, NodeType.nd_Or), Keyword_if(false, false, false, -1, NodeType.nd_If), Keyword_else(false, false, false, -1, NodeType.nd_None), Keyword_while(false, false, false, -1, NodeType.nd_While), Keyword_print(false, false, false, -1, NodeType.nd_None), Keyword_putc(false, false, false, -1, NodeType.nd_None), LeftParen(false, false, false, -1, NodeType.nd_None), RightParen(false, false, false, -1, NodeType.nd_None), LeftBrace(false, false, false, -1, NodeType.nd_None), RightBrace(false, false, false, -1, NodeType.nd_None), Semicolon(false, false, false, -1, NodeType.nd_None), Comma(false, false, false, -1, NodeType.nd_None), Identifier(false, false, false, -1, NodeType.nd_Ident), Integer(false, false, false, -1, NodeType.nd_Integer), String(false, false, false, -1, NodeType.nd_String);

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
     * Enum of the different token types for the nodes.
     */
    private enum NodeType {
        nd_None(""), nd_Ident("Identifier"), nd_String("String"), nd_Integer("Integer"), nd_Sequence("Sequence"), nd_If("If"), nd_Prtc("Prtc"), nd_Prts("Prts"), nd_Prti("Prti"), nd_While("While"), nd_Assign("Assign"), nd_Negate("Negate"), nd_Not("Not"), nd_Mul("Multiply"), nd_Div("Divide"), nd_Mod("Mod"), nd_Add("Add"), nd_Sub("Subtract"), nd_Lss("Less"), nd_Leq("LessEqual"), nd_Gtr("Greater"), nd_Geq("GreaterEqual"), nd_Eql("Equal"), nd_Neq("NotEqual"), nd_And("And"), nd_Or("Or");

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
     *  TODO: Why the fuck is it made like this???
     */
    private static class Node {

        //The type of node based on the held value.

        private final Token token;
        private final NodeType type;
        //Currently not used??? No calls to the only constructor that uses this, and no make_node that uses this.
        public String value;
        private Node left;
        private Node right;

        private Node next = null;

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

        public Node makeNode(Token token, NodeType nodeType){
            return new Node(token, nodeType, null, null, null);
        }

        public NodeType getType() {
            return type;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }
    }

    /**
     * Static token class.
     */
    private static class Token {
        private final TokenType tokentype;
        private final String value;
        private final int line;
        public int pos;

        private Token(TokenType token, String value, int line, int pos) {
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