package plc.project;

import java.util.ArrayList;
import java.util.List;

/**
 * The lexer works through three main functions:
 *
 *  - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 *  - {@link #lexToken()}, which lexes the next token
 *  - {@link CharStream}, which manages the state of the lexer and literals
 *
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException} with an index at the character which is
 * invalid.
 *
 * The {@link #peek(String...)} and {@link #match(String...)} functions are * helpers you need to use, they will make the implementation a lot easier. */
public final class Lexer {

    private final CharStream chars;

    public Lexer(String input) {
        chars = new CharStream(input);
    }

    /**
     * Repeatedly lexes the input using {@link #lexToken()}, also skipping over
     * whitespace where appropriate.
     */
    public List<Token> lex() {

        List<Token> tokens = new ArrayList<Token>();
        while (chars.has(0)) {
            if (match(" ")) {
                chars.skip();
            }
            else if (match("[\b\n\r\t]")) {
                chars.skip();
            }
            else {
                Token token = lexToken();
                tokens.add(token);
            }
        }
        return tokens;
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * This method determines the type of the next token, delegating to the
     * appropriate lex method. As such, it is best for this method to not change
     * the state of the char stream (thus, use peek not match).
     *
     * The next character should start a valid token since whitespace is handled
     * by {@link #lex()}
     */
    public Token lexToken() {

        // determine which method to call

        Token result = null;

        if (peek("@|[A-Za-z]")) {
            result = lexIdentifier();
        }

        else if (peek("-","[0-9]") || peek("[0-9]")) {
            result = lexNumber();
        }

        else if (peek("'")){
            result = lexCharacter();
        }

        else if (peek("\"")){
            result = lexString();
        }

        else {
            result = lexOperator();
        }
        System.out.println(result);
        return result;

        //throw new UnsupportedOperationException(); //TODO
    }

    // for each lex method, while next token is same as existing, continue to peek.
    // then advance when you can no longer extend the current token type (includes running
    // into whitespace). Last call emit and return newly created token.
    public Token lexIdentifier() {

        match("@|[A-Za-z]");
        while (peek("[A-Za-z0-9_-]")) {
            match("[A-Za-z0-9_-]");
        }
        Token identifier = chars.emit(Token.Type.IDENTIFIER);
        return identifier;

        //throw new UnsupportedOperationException(); //TODO
    }

    public Token lexNumber() {

        // negatives
        if (peek("-","0","\\.","[0-9]")) {
            match("-","0","\\.","[0-9]");
            while (peek("[0-9]")) {
                match("[0-9]");
            }
            Token dec = chars.emit(Token.Type.DECIMAL);
            return dec;
        }

        else if (peek("-","[1-9]")) {
            match("-","[1-9]");
            while (peek("[0-9]")) {
                match("[0-9]");
            }
            if (peek("\\.", "[0-9]")) {
                match("\\.","[0-9]");
                while (peek("[0-9]")) {
                    match("[0-9]");
                }
                Token dec = chars.emit(Token.Type.DECIMAL);
                return dec;
            }
            else {
                Token integer = chars.emit(Token.Type.INTEGER);
                return integer;
            }
        }
        // non-negatives

        // decimal case
        else if (peek("0","\\.","[0-9]")) {
            match("0","\\.","[0-9]");
            while (peek("[0-9]")) {
                match("[0-9]");
            }
            Token dec = chars.emit(Token.Type.DECIMAL);
            return dec;
        }
        // non-zero integer case
        else if (peek("[1-9]")) {
            match("[1-9]");
            while (peek("[0-9]")) {
                match("[0-9]");
            }
            if (peek("\\.", "[0-9]")) {
                match("\\.","[0-9]");
                while (peek("[0-9]")) {
                    match("[0-9]");
                }
                Token dec = chars.emit(Token.Type.DECIMAL);
                return dec;
            }
            else {
                Token integer = chars.emit(Token.Type.INTEGER);
                return integer;
            }
        }
        chars.advance();
        return chars.emit(Token.Type.INTEGER);
        //throw new UnsupportedOperationException(); //TODO
    }

    public Token lexCharacter() {
        match("\'");

        if (match("\\\\")){
            if (match("[bnrt\'\"\\\\]")){
                if (match("\'")) {
                    return chars.emit(Token.Type.CHARACTER);
                }
            }
            else{
                throw new ParseException("Invalid escape", chars.index);
            }
        }

        // Character literals cannot span multiple lines
        if (match("[\n\r]")){
            throw new ParseException("Character literals cannot span multiple lines", chars.index);
        }

        //The character cannot be a single quote ('), without being preceded by a backslash
        if (!match("[^']"))
        {
            throw new ParseException("The character cannot be a single quote", chars.index);
        }

        // check if ends with a single quote
        if (match("\'")) {
            return chars.emit(Token.Type.CHARACTER);
        }
        else {
            throw new ParseException("Not ending with a single quote", chars.index);
        }
    }

    public Token lexString() {
        match("\"");

        while (match("[^\"]")){
            if (match("[\n\r]")){
                throw new ParseException("String literals cannot span multiple lines", chars.index);
            }
            if (match("\\\\")){
                if (!match("[bnrt\'\"\\\\]")){
                    throw new ParseException("Invalid escape", chars.index);
                }
                else {
                    match("[bnrt\'\"\\\\]");
                }
            }
        }

        if (match("\"")){
            return chars.emit(Token.Type.STRING);
        }
        else {
            throw new ParseException("It is an unterminated string", chars.index);
        }
    }


    public void lexEscape() {
        match("\\\\");

        if(!match("[bnrt\'\"\\\\]")) {
            throw new ParseException ("Invalid escpae", chars.index);
        }
    }

    public Token lexOperator() {
        if (match("[!=]"))
        {
            System.out.println("= or !");
            if(match("="))
            {
                return chars.emit(Token.Type.OPERATOR);
            }
            else {
                return chars.emit(Token.Type.OPERATOR);
            }
        }
        if (match("[(&&)(||)]"))
        {
            return chars.emit(Token.Type.OPERATOR);
        }
        chars.advance();
        return chars.emit(Token.Type.OPERATOR);

    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true if the next characters are {@code 'a', 'b', 'c'}.
     */
    public boolean peek(String... patterns) {
        for (int i = 0; i < patterns.length; i++) {
            if (!chars.has(i) || !String.valueOf(chars.get(i)).matches(patterns[i])) {
                return false;
            }
        }
        return true;
        //throw new UnsupportedOperationException(); //TODO (in Lecture)
    }

    /**
     * Returns true in the same way as {@link #peek(String...)}, but also
     * advances the character stream past all matched characters if peek returns
     * true. Hint - it's easiest to have this method simply call peek.
     */
    public boolean match(String... patterns) {
        boolean peek = peek(patterns);
        if (peek) {
            for (int i = 0; i < patterns.length; ++i) {
                chars.advance();
            }
        }
        return peek;
        //throw new UnsupportedOperationException(); //TODO (in Lecture)
    }

    /**
     * A helper class maintaining the input string, current index of the char
     * stream, and the current length of the token being matched.
     *
     * You should rely on peek/match for state management in nearly all cases.
     * The only field you need to access is {@link #index} for any {@link
     * ParseException} which is thrown.
     */
    public static final class CharStream {

        private final String input;
        private int index = 0;
        private int length = 0;

        public CharStream(String input) {
            this.input = input;
        }

        public boolean has(int offset) {
            return index + offset < input.length();
        }

        public char get(int offset) {
            return input.charAt(index + offset);
        }

        public void advance() {
            index++;
            length++;
        }

        public void skip() {
            length = 0;
        }

        public Token emit(Token.Type type) {
            int start = index - length;
            skip();
            return new Token(type, input.substring(start, index), start);
        }

    }

}
