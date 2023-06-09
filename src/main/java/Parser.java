import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

class Parser {
    private List<Token> source;
    private Token token;
    private int position;

    static class Node {
        public NodeType nt;
        public Node left, right;
        public String value;

        Node() {
            this.nt = null;
            this.left = null;
            this.right = null;
            this.value = null;
        }
        Node(NodeType node_type, Node left, Node right, String value) {
            this.nt = node_type;
            this.left = left;
            this.right = right;
            this.value = value;
        }
        public static Node make_node(NodeType nodetype, Node left, Node right) {
            return new Node(nodetype, left, right, "");
        }
        public static Node make_node(NodeType nodetype, Node left) {
            return new Node(nodetype, left, null, "");
        }
        public static Node make_leaf(NodeType nodetype, String value) {
            return new Node(nodetype, null, null, value);
        }
    }

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
            return String.format("%5d  %5d %-15s %s", this.line, this.pos, this.tokentype, this.value);
        }
    }

    static enum TokenType {
        End_of_input(false, false, false, -1, NodeType.nd_None),
        Op_multiply(false, true, false, 13, NodeType.nd_Mul),
        Op_divide(false, true, false, 13, NodeType.nd_Div),
        Op_mod(false, true, false, 13, NodeType.nd_Mod),
        Op_add(false, true, false, 12, NodeType.nd_Add),
        Op_subtract(false, true, false, 12, NodeType.nd_Sub),
        Op_negate(false, false, true, 14, NodeType.nd_Negate),
        Op_not(false, false, true, 14, NodeType.nd_Not),
        Op_less(false, true, false, 10, NodeType.nd_Lss),
        Op_lessequal(false, true, false, 10, NodeType.nd_Leq),
        Op_greater(false, true, false, 10, NodeType.nd_Gtr),
        Op_greaterequal(false, true, false, 10, NodeType.nd_Geq),
        Op_equal(false, true, true, 9, NodeType.nd_Eql),
        Op_notequal(false, true, false, 9, NodeType.nd_Neq),
        Op_assign(false, false, false, -1, NodeType.nd_Assign),
        Op_and(false, true, false, 5, NodeType.nd_And),
        Op_or(false, true, false, 4, NodeType.nd_Or),
        Keyword_if(false, false, false, -1, NodeType.nd_If),
        Keyword_else(false, false, false, -1, NodeType.nd_None),
        Keyword_while(false, false, false, -1, NodeType.nd_While),
        Keyword_print(false, false, false, -1, NodeType.nd_None),
        Keyword_putc(false, false, false, -1, NodeType.nd_None),
        LeftParen(false, false, false, -1, NodeType.nd_None),
        RightParen(false, false, false, -1, NodeType.nd_None),
        LeftBrace(false, false, false, -1, NodeType.nd_None),
        RightBrace(false, false, false, -1, NodeType.nd_None),
        Semicolon(false, false, false, -1, NodeType.nd_None),
        Comma(false, false, false, -1, NodeType.nd_None),
        Identifier(false, false, false, -1, NodeType.nd_Ident),
        Integer(false, false, false, -1, NodeType.nd_Integer),
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
        boolean isRightAssoc() { return this.right_assoc; }
        boolean isBinary() { return this.is_binary; }
        boolean isUnary() { return this.is_unary; }
        int getPrecedence() { return this.precedence; }
        NodeType getNodeType() { return this.node_type; }
    }
    static enum NodeType {
        nd_None(""), nd_Ident("Identifier"), nd_String("String"), nd_Integer("Integer"), nd_Sequence("Sequence"), nd_If("If"),
        nd_Prtc("Prtc"), nd_Prts("Prts"), nd_Prti("Prti"), nd_While("While"),
        nd_Assign("Assign"), nd_Negate("Negate"), nd_Not("Not"), nd_Mul("Multiply"), nd_Div("Divide"), nd_Mod("Mod"), nd_Add("Add"),
        nd_Sub("Subtract"), nd_Lss("Less"), nd_Leq("LessEqual"),
        nd_Gtr("Greater"), nd_Geq("GreaterEqual"), nd_Eql("Equal"), nd_Neq("NotEqual"), nd_And("And"), nd_Or("Or");

        private final String name;

        NodeType(String name) {
            this.name = name;
        }

        @Override
        public String toString() { return this.name; }
    }
    static void error(int line, int pos, String msg) {
        if (line > 0 && pos > 0) {
            System.out.printf("%s in line %d, pos %d\n", msg, line, pos);
        } else {
            System.out.println(msg);
        }
        System.exit(1);
    }
    Parser(List<Token> source) {
        this.source = source;
        this.token = null;
        this.position = 0;
    }
    Token getNextToken() {
        if(position == source.size()) return new Token(TokenType.End_of_input, "", 0, 0);
        this.token = this.source.get(this.position++);
        return this.token;
    }

    Node expr(int p) {
        // create nodes for token types such as LeftParen, Op_add, Op_subtract, etc.
        // be very careful here and be aware of the precedence rules for the AST tree
        Node result = null, node;
        switch (token.tokentype)
        {
            case LeftParen -> {
                result = paren_expr();
            }
            case Op_add, Op_subtract, Op_negate -> {
                TokenType op = (token.tokentype == TokenType.Op_add) ? TokenType.Op_add : TokenType.Op_negate;
                getNextToken();
                node = expr(op.getPrecedence());
                result = (op == TokenType.Op_add) ? node : Node.make_node(NodeType.nd_Negate, node);
            }
            case Identifier -> {
                result = Node.make_leaf(NodeType.nd_Ident, this.token.value);
                getNextToken();
            }
            case Integer -> {
                result = Node.make_leaf(NodeType.nd_Integer, this.token.value);
                getNextToken();
            }
            default -> {
                error(this.token.line, this.token.pos, "error with expr");
            }
        }

        while (this.token.tokentype.isBinary()&& this.token.tokentype.getPrecedence() >= p)
        {
            TokenType operation = this.token.tokentype;
            getNextToken();
            int precedence = operation.getPrecedence();
            if (!operation.isRightAssoc()){
                precedence++;
            }
            result = Node.make_node(operation.node_type, result, expr(precedence));
        }
        return result;
    }
    Node paren_expr() {
        expect("paren_expr", TokenType.LeftParen);
        Node node = expr(0);
        expect("paren_expr", TokenType.RightParen);
        return node;
    }
    void expect(String msg, TokenType s) {
        if (this.token.tokentype == s) {
            getNextToken();
            return;
        }
        error(this.token.line, this.token.pos, msg + ": Expecting '" + s + "', found: '" + this.token.tokentype + "'");
    }


    Node stmt() {
        Node s, s2, e = null, v;
        Node t = null;

        switch (token.tokentype)
        {
            case Keyword_if -> {
                getNextToken();
                e = paren_expr();
                s = stmt();
                s2 = null;
                if (this.token.tokentype == TokenType.Keyword_else) {
                    getNextToken();
                    s2 = stmt();
                }
                t = Node.make_node(NodeType.nd_If, e, Node.make_node(NodeType.nd_If, s, s2));
            }
            case Keyword_print -> {
                getNextToken();
                expect("", TokenType.LeftParen);
                while (1==1) {
                    if (this.token.tokentype == TokenType.String) {
                        e = Node.make_node(NodeType.nd_Prts, Node.make_leaf(NodeType.nd_String, this.token.value));
                        getNextToken();

                    } else {
                        e = Node.make_node(NodeType.nd_Prti, expr(0), null);
                    }
                    t = Node.make_node(NodeType.nd_Sequence, t, e);
                    if (this.token.tokentype != TokenType.Comma) {
                        break;
                    }
                    getNextToken();
                }
                expect("Print", TokenType.RightParen);
            }
            case Identifier -> {
                v = Node.make_leaf(NodeType.nd_Ident, this.token.value);
                getNextToken();
                expect("", TokenType.Op_assign);
                t = Node.make_node(NodeType.nd_Assign, v, expr(0));
                expect("", TokenType.Semicolon);
            }
            case Keyword_putc -> {
                getNextToken();
                t = Node.make_node(NodeType.nd_Prtc, paren_expr());
                expect("", TokenType.Semicolon);
            }
            case LeftBrace -> {
                getNextToken();
                while (this.token.tokentype != TokenType.End_of_input && this.token.tokentype != TokenType.RightBrace ) {
                    t = Node.make_node(NodeType.nd_Sequence, t, stmt());
                }
                expect("LBrace", TokenType.RightBrace);
            }
            case Keyword_while -> {
                getNextToken();
                t = Node.make_node(NodeType.nd_While, paren_expr(), stmt());
            }
            case Semicolon -> {
                getNextToken();
            }
            case End_of_input -> {
                return null;
            }
            default -> {
                error(this.token.line, this.token.pos, "error in stmt" + this.token.tokentype);
            }
        }
        return t;
    }
    Node parse() {
        Node t = null;
        getNextToken();
        while (this.token.tokentype != TokenType.End_of_input) {
            t = Node.make_node(NodeType.nd_Sequence, t, stmt());
        }
        return t;
    }
    String printAST(Node t, StringBuilder sb) {
        int i = 0;
        if (t == null) {
            sb.append(";");
            sb.append("\n");
            System.out.println("\t;");
        } else {
            sb.append(t.nt);
            System.out.printf("\t%-14s", t.nt);
            if (t.nt == NodeType.nd_Ident || t.nt == NodeType.nd_Integer || t.nt == NodeType.nd_String) {
                sb.append(" " + t.value);
                sb.append("\n");
                System.out.println("\t " + t.value);
            } else {
                sb.append("\n");
                System.out.println();
                printAST(t.left, sb);
                printAST(t.right, sb);
            }

        }
        return sb.toString();
    }

    static void outputToFile(String result, String filename) {
        try {
            FileWriter myWriter = new FileWriter("src/main/output/" + filename + ".par");
            myWriter.write(result);
            myWriter.close();
            System.out.println(String.format("Successfully wrote file : %s.par", filename.substring(0, (filename.indexOf('.') == -1) ? filename.length() :  filename.indexOf('.')).toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            String value, token;
            String result;
            StringBuilder sb;
            int line, pos;
            Token t;
            boolean found;
            List<Token> list = new ArrayList<>();
            Map<String, TokenType> str_to_tokens = new HashMap<>();

            for (TokenType tokenType : TokenType.values()) str_to_tokens.put(tokenType.toString(), tokenType);

            File[] files = new File("src/test/resources").listFiles(file -> file.toString().endsWith(".lex"));
            Scanner s;
            String source;

            for(File file : files)
            {
                sb = new StringBuilder();
                s = new Scanner(file);
                source = "";
                result = "";
                list.clear();
                System.out.println(String.format("\nParsing File : %s", file.getName()));

                while (s.hasNext()) {
                    String str = s.nextLine();
                    StringTokenizer st = new StringTokenizer(str);
                    line = Integer.parseInt(st.nextToken());
                    pos = Integer.parseInt(st.nextToken());
                    token = st.nextToken();
                    value = "";
                    while (st.hasMoreTokens()) {
                        value += st.nextToken() + " ";
                    }
                    found = false;
                    if (str_to_tokens.containsKey(token)) {
                        found = true;
                        list.add(new Token(str_to_tokens.get(token), value, line, pos));
                    }
                    if (found == false) {
                        throw new Exception("Token not found: '" + token + "'");
                    }
                }
                Parser p = new Parser(list);
                result = p.printAST(p.parse(), sb);
                //outputToFile(result, file.getName().substring(0, file.getName().indexOf('.')));
            }

        } catch (FileNotFoundException e) {
            error(-1, -1, "Exception: " + e.getMessage());
        } catch (Exception e) {
            error(-1, -1, "Exception: " + e.getMessage());
        }
    }
}