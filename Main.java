import src.lexer.Lexer;
import src.lexer.Token;

public class Main {

	/*
	 * =!=!=!=!=!=!=!=!=!=! INFO !=!=!=!=!=!=!=!=!=!=
	 * 
	 * This is a simple Lexer that will convert given input into tokens for further
	 * operations. To add your own rules you can modify Lexer.java. There are also
	 * keywords, which are used to separate specific identifiers like "int" from all
	 * the others.
	 */

	static String input = """
			hi, this is just a test. 12 + 14 == 26 && <= 30
			int abc = 1324;
			'wow a nice string'

			float pi = 3.14

			if( coffee()):
				work()
					""";

	public static void main(String[] args) {

		// There are two ways of using the lexer
		// 1. whole input at once
		Token[] tokens = new Lexer(input).tokenizeText();
		for (Token t : tokens)
			System.out.println(t.getDescription());

		// 2. one word at a time
		Lexer lexer = new Lexer(input);
		Token token;
		do {
			token = lexer.nextToken();
			System.out.println(token.getDescription());
		} while (!token.isEOF());
	}
}