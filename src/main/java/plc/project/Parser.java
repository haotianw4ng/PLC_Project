package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).
 *
 * The parser has a similar architecture to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and {@link
 * #match(Object...)} are helpers to make the implementation easier.
 *
 * This type of parser is called <em>recursive descent</em>. Each rule in our
 * grammar will have it's own function, and reference to other rules correspond
 * to calling that functions.
 */
public final class Parser {

    private final TokenStream tokens;

    public Parser(List<Token> tokens) {
        this.tokens = new TokenStream(tokens);
    }

    public int getIndex() {
        if (tokens.has(0)) return tokens.get(0).getIndex();
        else return tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length();
    }

    /**
     * Parses the {@code source} rule.
     */
    public Ast.Source parseSource() throws ParseException {
        List<Ast.Function> functions = new ArrayList<>();
        List<Ast.Global> globals = new ArrayList<>();

        // Add globals
        while (peek("VAL") || peek("VAR") || peek("LIST")) {
            Ast.Global global = parseGlobal();
            globals.add(global);
        }
        // Add functions
        while (peek("FUN")) {
            Ast.Function function = parseFunction();
            functions.add(function);
        }

        return new Ast.Source(globals, functions);

        //throw new UnsupportedOperationException(); //TODO

    }

    /**
     * Parses the {@code field} rule. This method should only be called if the
     * next tokens start a field, aka {@code LET}.
     */
    public Ast.Global parseGlobal() throws ParseException {

        while (peek("VAL") || peek("VAR") || peek("LIST")) {
            Ast.Global result = null;
            if (peek("LIST")) {
                result = parseList();
            } else if (peek("VAL")) {
                result = parseImmutable();
            } else {
                result = parseMutable();
            }
            if (!match(";")) {
                throw new ParseException("Missing closing semi-colon", getIndex());
            } else {
                match(";");
                return result;
            }
        }
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code list} rule. This method should only be called if the
     * next token declares a list, aka {@code LIST}.
     */
    public Ast.Global parseList() throws ParseException {
        String name = null;
        List<Ast.Expression> exprList = new ArrayList<>();
        match("LIST");
        if (!match(Token.Type.IDENTIFIER)) throw new ParseException("No identifier",getIndex());
        name = tokens.get(-1).getLiteral();
        if (!match("=")) throw new ParseException("No =", getIndex());
        if (!match("[")) throw new ParseException("No [", getIndex());
        Ast.Expression expr = parseExpression();
        exprList.add(expr);
        while (match(",")) {
            expr = parseExpression();
            exprList.add(expr);
        }
        if (!match("]")) throw new ParseException("Missing ]", getIndex());
        return new Ast.Global(name, true, Optional.of(new Ast.Expression.PlcList(exprList)));

        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code mutable} rule. This method should only be called if the
     * next token declares a mutable global variable, aka {@code VAR}.
     */
    public Ast.Global parseMutable() throws ParseException {
        String name = null;
        match("VAR");
        if (!match(Token.Type.IDENTIFIER)) throw new ParseException("Not identifier", getIndex());
        name = tokens.get(-1).getLiteral();
        if (match("=")) {
            Ast.Expression expr = parseExpression();
            //return new Ast.Global(name, true, Optional.of(new Ast.Expression.Access(Optional.empty(), expr.toString())));
            return new Ast.Global(name, true, Optional.of(new Ast.Expression.Access(Optional.empty(), tokens.get(-1).getLiteral())));
        } else {
            return new Ast.Global(name, true, Optional.empty());
        }
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code immutable} rule. This method should only be called if the
     * next token declares an immutable global variable, aka {@code VAL}.
     */
    public Ast.Global parseImmutable() throws ParseException {
        String name = null;
        match("VAL");
        if (!match(Token.Type.IDENTIFIER)) throw new ParseException("Not identifier", getIndex());
        name = tokens.get(-1).getLiteral();
        if (!match("=")) throw new ParseException("Missing initialization", getIndex());
        Ast.Expression expr = parseExpression();
        return new Ast.Global(name, false, Optional.of(new Ast.Expression.Access(Optional.empty(), tokens.get(-1).getLiteral())));
       // throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code method} rule. This method should only be called if the
     * next tokens start a method, aka {@code DEF}.
     */
    public Ast.Function parseFunction() throws ParseException {
        String name = null;
        List<String> parameters = new ArrayList<>();
        List<Ast.Statement> statements = null;
        match("FUN");
        if (!match(Token.Type.IDENTIFIER)) throw new ParseException("Missing identifier", getIndex());
        name = tokens.get(-1).getLiteral();
        if (!match("(")) throw new ParseException("Missing opening parentheses", getIndex());
        if (match(Token.Type.IDENTIFIER)) {
            parameters.add(tokens.get(-1).getLiteral());
            while (match(",")) {
                match(Token.Type.IDENTIFIER);
                parameters.add(tokens.get(-1).getLiteral());
            }
        }
        if (!match(")")) throw new ParseException("Missing closing parentheses", getIndex());
        if (!match("DO")) throw new ParseException("Missing DO", getIndex());
        statements = parseBlock();
        if (!match("END")) throw new ParseException("Missing END", getIndex());
        return new Ast.Function(name, parameters, statements);
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code block} rule. This method should only be called if the
     * preceding token indicates the opening a block.
     */
    public List<Ast.Statement> parseBlock() throws ParseException {
        List<Ast.Statement> statements = new ArrayList<>();
        Ast.Statement statement = parseStatement();
        statements.add(statement);
        while (match(";") || match("END")) {
            statement = parseStatement();
            statements.add(statement);
        }
        return statements;
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method.
     * If the next tokens do not start a declaration, if, while, or return
     * statement, then it is an expression/assignment statement.
     */
    public Ast.Statement parseStatement() throws ParseException {
        if (match("LET"))
        {
            return parseDeclarationStatement();
        }
        else if (match("SWITCH"))
        {
            return parseSwitchStatement();
        }
        else if (match("IF"))
        {
            return parseIfStatement();
        }
        else if (match("WHILE"))
        {
            return parseWhileStatement();
        }
        else if (match("RETURN"))
        {
            return parseReturnStatement();
        }

        Ast.Expression left = parseExpression();
        if (match("=")){
            Ast.Expression right = parseExpression();
            if (match(";")) {
                return new Ast.Statement.Assignment(left, right);
            }
            throw new ParseException("Missing semicolon", getIndex());
        }
        else if (match(";")){
            return new Ast.Statement.Expression(left);
        }
        throw new ParseException("Error", getIndex());
    }

    /**
     * Parses a declaration statement from the {@code statement} rule. This
     * method should only be called if the next tokens start a declaration
     * statement, aka {@code LET}.
     */
    public Ast.Statement.Declaration parseDeclarationStatement() throws ParseException {
        if (match(Token.Type.IDENTIFIER)) {
            String identifier = tokens.get(-1).getLiteral();

            if (match("=")) {
                Ast.Expression val = parseExpression();
                if (match(";")) {
                    return new Ast.Statement.Declaration(identifier, Optional.of(val));
                }
                else{
                    throw new ParseException("Missing ending semicolon", getIndex());
                }
            } else {
                if (match(";")) {
                    return new Ast.Statement.Declaration(identifier, Optional.empty());
                }
                else{
                    throw new ParseException("Missing ending semicolon", getIndex());
                }
            }
        } else {
            throw new ParseException("Missing identifier", getIndex());
        }
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method
     * should only be called if the next tokens start an if statement, aka
     * {@code IF}.
     */
    public Ast.Statement.If parseIfStatement() throws ParseException {
        if (match("DO")){
            throw new ParseException("Missing expression", getIndex());
        }

        List<Ast.Statement> if_statement = new ArrayList<>();
        List<Ast.Statement> else_statement = new ArrayList<>();
        Ast.Expression expression_val = parseExpression();

        if (!match("DO")){
            throw new ParseException("Missing DO block", getIndex());
        }

        while (tokens.has(0) && !peek("END")){
            if_statement.add(parseStatement());
            if(match("ELSE"))
            {
                while (tokens.has(0) && !peek("END"))
                {
                    else_statement.add(parseStatement());
                }
            }
        }

        if (match("END")){
            return new Ast.Statement.If(expression_val, if_statement, else_statement);
        }
        else {
            throw new ParseException("Missing END", getIndex());
        }
    }

    /**
     * Parses a switch statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a switch statement, aka
     * {@code SWITCH}.
     */
    public Ast.Statement.Switch parseSwitchStatement() throws ParseException {
        Ast.Expression expression_val = parseExpression();

        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a case or default statement block from the {@code switch} rule.
     * This method should only be called if the next tokens start the case or
     * default block of a switch statement, aka {@code CASE} or {@code DEFAULT}.
     */
    public Ast.Statement.Case parseCaseStatement() throws ParseException {
        match("CASE");
        Ast.Expression expression_val = parseExpression();


        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a while statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a while statement, aka
     * {@code WHILE}.
     */
    public Ast.Statement.While parseWhileStatement() throws ParseException {
        if (match("DO")){
            throw new ParseException("Missing expression", getIndex());
        }

        List<Ast.Statement> statement_list = new ArrayList<>();
        Ast.Expression expression_val = parseExpression();

        if (!match("DO")){
            throw new ParseException("Missing DO block", getIndex());
        }

        while (tokens.has(0) && !peek("END")){
            statement_list.add(parseStatement());
        }

        if (match("END")){
            return new Ast.Statement.While(expression_val, statement_list);
        }
        else {
            throw new ParseException("Missing END", getIndex());
        }
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     */
    public Ast.Statement.Return parseReturnStatement() throws ParseException {
        Ast.Expression expression_val = parseExpression();

        if (match(";")) {
            return new Ast.Statement.Return(expression_val);
        }
        else {
            throw new ParseException("Missing ending semicolon",  getIndex());
        }
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expression parseExpression() throws ParseException {
        return parseLogicalExpression();
    }

    /**
     * Parses the {@code logical-expression} rule.
     */
    public Ast.Expression parseLogicalExpression() throws ParseException {
        Ast.Expression left = parseComparisonExpression();
        while (match("&&") || match("||")) {
            String op = tokens.get(-1).getLiteral();

            if (!tokens.has(0)){
                throw new ParseException("Error", getIndex());
            }

            Ast.Expression right = parseComparisonExpression();
            left = new Ast.Expression.Binary(op, left, right);
        }
        return left;
    }

    /**
     * Parses the {@code equality-expression} rule.
     */
    public Ast.Expression parseComparisonExpression() throws ParseException {
        Ast.Expression left = parseAdditiveExpression();
        while (match("!=") || match("==") || match(">") || match("<")) {
            String op = tokens.get(-1).getLiteral();

            if (!tokens.has(0)){
                throw new ParseException("Error", getIndex());
            }

            Ast.Expression right = parseAdditiveExpression();
            left = new Ast.Expression.Binary(op, left, right);
        }
        return left;
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expression parseAdditiveExpression() throws ParseException {
        Ast.Expression left = parseMultiplicativeExpression();
        while (match("+") || match("-")) {
            String op = tokens.get(-1).getLiteral();

            if (!tokens.has(0)){
                throw new ParseException("Error", getIndex());
            }

            Ast.Expression right = parseMultiplicativeExpression();
            left = new Ast.Expression.Binary(op, left, right);
        }
        return left;
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expression parseMultiplicativeExpression() throws ParseException {
       Ast.Expression left = parsePrimaryExpression();
       while (match("*") || match("\\") || match("^")) {
           String op = tokens.get(-1).getLiteral();

           if (!tokens.has(0)){
               throw new ParseException("Error", getIndex());
           }

           Ast.Expression right = parsePrimaryExpression();
           left = new Ast.Expression.Binary(op, left, right);
       }
       return left;
    }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule
     * for expressions and includes literal values, grouping, variables, and
     * functions. It may be helpful to break these up into other methods but is
     * not strictly necessary.
     */
    public Ast.Expression parsePrimaryExpression() throws ParseException {
        if (peek("NIL")) {
            match("NIL");
            return new Ast.Expression.Literal(null);
        } else if (peek("TRUE")) {
            match("TRUE");
            Boolean result = new Boolean("TRUE");
            return new Ast.Expression.Literal(result);
        } else if (peek("FALSE")) {
            match("FALSE");
            Boolean result = new Boolean("FALSE");
            return new Ast.Expression.Literal(result);
        } else if (peek(Token.Type.INTEGER)) {
            BigInteger result = new BigInteger(this.tokens.get(0).getLiteral());
            match(Token.Type.INTEGER);
            return new Ast.Expression.Literal(result);
        } else if (peek(Token.Type.DECIMAL)) {
            BigDecimal result = new BigDecimal(this.tokens.get(0).getLiteral());
            match(Token.Type.DECIMAL);
            return new Ast.Expression.Literal(result);
        } else if (peek(Token.Type.CHARACTER)) {
            String newToken = this.tokens.get(0).getLiteral().replace("\'", "");
            //String newToken = this.tokens.get(0).getLiteral().substring(1,this.tokens.get(0).getLiteral().length()-1);
            //newToken = newToken.replace("\\n", "\n");
            //System.out.println(newToken);
            Character result = newToken.charAt(0);
            switch (newToken) {
                case "\\n": result = '\n';
                    break;
                case "\\t": result = '\t';
                    break;
                case "\\r": result = '\r';
                    break;
                case "\\b": result = '\b';
                    break;
                case "\\'": result = '\'';
                    break;
                case "\\\"": result= '\"';
            }
            result = new Character(result);
            match(Token.Type.CHARACTER);
            return new Ast.Expression.Literal(result);
        } else if (peek(Token.Type.STRING)) {
            String newToken = this.tokens.get(0).getLiteral().replace("\"", "");
            newToken = newToken.replace("\\r", "\r");
            newToken = newToken.replace("\\t", "\t");
            newToken = newToken.replace("\\n", "\n");
            newToken = newToken.replace("\\b", "\b");
            newToken = newToken.replace("\\\"", "\"");
            newToken = newToken.replace("\\\'", "\'");
            newToken = newToken.replace("\\\\", "\\");
            match(Token.Type.STRING);
            return new Ast.Expression.Literal(newToken);
        } else if (peek("(")) {
            match("(");
            Ast.Expression expr = parseExpression();
            if (match(")")) {
                return new Ast.Expression.Group(expr);
            } else {
                throw new ParseException("Error: no closing parentheses", getIndex());
            }
        } else if (peek(Token.Type.IDENTIFIER)) {
            String token = this.tokens.get(0).getLiteral();
            List<Ast.Expression> exprList = new ArrayList<>();
            match(Token.Type.IDENTIFIER);
            if (!peek("(")) {
                if (!peek("[")) {
                    return new Ast.Expression.Access(Optional.empty(),token);
                }
                else {
                    match("[");
                    Ast.Expression expr = parseExpression();
                    if (!peek("]")) {
                        throw new ParseException("Missing closing bracket", getIndex());
                    } else {
                        match("]");
                        // what to do here
                        return new Ast.Expression.Access(Optional.of(expr),token);
                    }
                }
            }
            else if (peek("(",")")) {
                match("(", ")");
            } else {
                match("(");
                Ast.Expression expr = parseExpression();
                exprList.add(expr);
                while (match(",")) {
                    expr = parseExpression();
                    exprList.add(expr);
                }
                if (!peek(")")) throw new ParseException("Missing closing parentheses", getIndex());
                else match(")");
            }
            return new Ast.Expression.Function(token, exprList);
        }
        throw new ParseException("Error", getIndex());
    }

    /**
     * As in the lexer, returns {@code true} if the current sequence of tokens
     * matches the given patterns. Unlike the lexer, the pattern is not a regex;
     * instead it is either a {@link Token.Type}, which matches if the token's
     * type is the same, or a {@link String}, which matches if the token's
     * literal is the same.
     *
     * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
     * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
     */
    private boolean peek(Object... patterns) {
        for (int i = 0; i < patterns.length; i++){
            if(!tokens.has(i)){
                return false;
            } else if(patterns[i] instanceof Token.Type){
                if(patterns[i] != tokens.get(i).getType()){
                    return false;
                }
            } else if(patterns[i] instanceof String){
                if(!patterns[i].equals(tokens.get(i).getLiteral())){
                    return false;
                }
            } else {
                throw new AssertionError("Invalid pattern object: " + patterns[i].getClass());
            }
        }
        return true;
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {
        boolean peek = peek(patterns);
        if (peek){
            for(int i = 0; i < patterns.length;i++){
                tokens.advance();
            }
        }
        return peek;
    }

    private static final class TokenStream {

        private final List<Token> tokens;
        private int index = 0;

        private TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        /**
         * Returns true if there is a token at index + offset.
         */
        public boolean has(int offset) {
            return index + offset < tokens.size();
        }

        /**
         * Gets the token at index + offset.
         */
        public Token get(int offset) {
            return tokens.get(index + offset);
        }

        /**
         * Advances to the next token, incrementing the index.
         */
        public void advance() {
            index++;
        }

    }

}
