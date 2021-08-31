package src.lexer;

import java.util.ArrayList;
import java.util.HashMap;

/*
TODO
 - unicode characters are difficult to lex as a char, the cursor column and the error message are inaccurate because letters like 'ä' are stored as two chars
*/
public class Lexer {

	private final boolean TOKENIZE_SPACES = true;
	private final boolean SPACES_TO_TABS = true;
	// how many spaces form a tab
	private final int TAB_SIZE = 4;

	public static String VERSION = "V 0.8.1";

	private final String text;
	private final HashMap<Character, TokenType> tokenMap = new HashMap<>();

	private String[] keywords;

	private int index = -1;
	private char currentChar;

	private int row = 1;
	private int col = 0;

	public Lexer(String text) {
		this.text = text;
		initSingleCharTokens();
		initKeywords();
		advance();
	}

	// ===============================================
	// CONFIG
	// ===============================================

	protected void initSingleCharTokens() {
		tokenMap.put('+', TokenType.ADD);
		tokenMap.put('-', TokenType.SUB);
		tokenMap.put('*', TokenType.MUL);
		tokenMap.put('/', TokenType.DIV);
		tokenMap.put('(', TokenType.O_ROUND_BRACKET);
		tokenMap.put(')', TokenType.C_ROUND_BRACKET);
		tokenMap.put('\t', TokenType.TAB);
		tokenMap.put('.', TokenType.DOT);
		tokenMap.put(',', TokenType.COMMA);
		tokenMap.put(':', TokenType.COLON);
		tokenMap.put(';', TokenType.COLON);
	}

	protected void initKeywords() {
		keywords = new String[] { "int", "float", "double", "public", "private", "protected", "static", "class", "enum",
				"interface" };
	}

	// ===============================================
	//
	// ===============================================

	private void advance() {
		col++;
		index++;
		if (index < text.length())
			currentChar = text.charAt(index);
	}

	// tokenizes the input in one take
	public Token[] tokenize() {
		ArrayList<Token> tokens = new ArrayList<>();

		Token t;
		do {
			t = nextToken();
			tokens.add(t);
		} while (!t.isEOF());
		return Token.toArray(tokens);
	}

	//tokenizes one token at a time
	public Token nextToken() {

		if (index >= text.length())
			return new Token(TokenType.EOF);

		Token t = null;
		do {
			if (tokenMap.containsKey(currentChar)) {
				t = new Token(tokenMap.get(currentChar));
				advance();
			} else if (Character.isAlphabetic(currentChar)) {
				t = buildIdentifierToken();
			} else if (Character.isDigit(currentChar)) {
				t = buildNumberToken();
			} else if (currentChar == ' ') {
				if (TOKENIZE_SPACES) {
					if (SPACES_TO_TABS)
						t = buildTabToken();
					else {
						t = new Token(TokenType.WHITESPACE);
						advance();
					}
				} else
					advance();
			} else {
				switch (currentChar) {
					case '\'' -> t = buildStringToken();
					case '"' -> t = buildStringToken();
					case '=' -> t = buildBooleanToken();
					case '<' -> t = buildBooleanToken();
					case '>' -> t = buildBooleanToken();
					case '!' -> t = buildBooleanToken();
					case '#' -> skipComment();
					case '\n' -> t = newLine();
					default -> t = throwError();
				}
			}
		} while (t == null);
		return t;
	}

	private Token newLine() {
		row++;
		col = 0;
		advance();
		return new Token(TokenType.NEWLINE);
	}

	private Token throwError() {
		System.out.println("Found illegal character '" + currentChar + "' at " + row + "," + col);
		//Evtl einen Error Token einführen
		return new Token(TokenType.EOF);
	}

	private Token buildIdentifierToken() {
		int start = index;
		advance();

		while (index < text.length())
			if (Character.isLetterOrDigit(currentChar))
				advance();
			else
				break;

		String subString = text.substring(start, index);
		for (String s : keywords)
			if (subString.equals(s))
				return new Token(TokenType.KEYWORD, subString);

		return new Token(TokenType.IDENTIFIER, subString);
	}

	private Token buildTabToken() {
		int whitespaceCount = 1;
		advance();

		while (currentChar == ' ' && index < text.length()) {

			whitespaceCount++;
			advance();

			if (whitespaceCount == TAB_SIZE)
				return new Token(TokenType.TAB);
		}
		return null;
	}

	private Token buildNumberToken() {
		int start = index;
		boolean isFloat = false;
		advance();

		while (index < text.length()) {
			if (Character.isDigit(currentChar) || currentChar == '.') {
				if (currentChar == '.')
					isFloat = true;
				advance();
			} else
				break;
		}
		if (isFloat)
			return new Token(TokenType.FLOAT, text.substring(start, index));
		return new Token(TokenType.NUMBER, text.substring(start, index));
	}

	private Token buildBooleanToken() {
		char c = currentChar;
		advance();

		Token t = new Token(TokenType.EQUALS);
		if (index < text.length() && currentChar == '=') {
			advance();
			switch (c + "=") {
				case "<=" -> t = new Token(TokenType.LESSOREQUAL);
				case ">=" -> t = new Token(TokenType.GREATEROREQUAL);
				case "==" -> t = new Token(TokenType.EQUAL);
				case "!=" -> t = new Token(TokenType.NOTEQUAL);
			}
		} else
			switch (c) {
				case '<' -> t = new Token(TokenType.LESSTHAN);
				case '>' -> t = new Token(TokenType.GREATERTHAN);
				case '!' -> t = new Token(TokenType.NOT);
			}
		return t;
	}

	private Token buildStringToken() {
		char c = currentChar;
		advance();

		int start = index;
		while (index < text.length()) {
			if (currentChar != c) {
				advance();
			} else {
				advance();
				break;
			}
		}
		return new Token(TokenType.STRING, text.substring(start, index - 1));
	}

	//skips one- and multi-lined comments 
	private void skipComment() {

		boolean multiLine = false;
		advance();

		if (currentChar == '#')
			multiLine = true;

		while (index < text.length() - 1) {
			if (multiLine) {
				if (currentChar == '#') {
					advance();
					if (currentChar == '#') {
						advance();
						return;
					}
				}
			} else if (currentChar == '\n')
				return;
			advance();
		}
	}
}
