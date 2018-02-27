public class crt {
	
	Token token;
	int ecall;
	boolean flag;
	
	public crt() {
		token = new Token();
	}
	
	public String checkspace(String x) {
		if (x.equals("")) {
			return x;
		}
		while (x.charAt(0) == ' ') {
			x = x.substring(1);
		}
		return x;
	}
	
	public void write(String x) {
		while (x.length() != 0) {
			x = checkspace(x);
			if (x.charAt(0) == '{') {
				token.add(x.substring(0, 1));
				x = x.substring(1);
			}else if (x.charAt(0) == '}') {
				token.add(x.substring(0, 1));
				x = x.substring(1);
			}else if (x.charAt(0) == '(') {
				token.add(x.substring(0, 1));
				x = x.substring(1);
			}else if (x.charAt(0) == ')') {
				token.add(x.substring(0, 1));
				x = x.substring(1);
			}else if (x.substring(0, 1).equals(";")) {
				token.add(x.substring(0, 1));
				x = x.substring(1);
			}else if (x.substring(0, 1).equals("!")) {
				token.add(x.substring(0, 1));
				x = x.substring(1);
			}else if (x.length() >= 2 && x.substring(0, 2).equals("if")) {
				token.add(x.substring(0, 2));
				x = x.substring(2);
			}else if (x.length() >= 4 && x.substring(0, 4).equals("true")) {		
				token.add(x.substring(0, 4));
				x = x.substring(4);
			}else if(x.length() >= 4 && x.substring(0, 4).equals("else")) {
				token.add(x.substring(0, 4));
				x = x.substring(4);
			}else if (x.length() >= 5 && x.substring(0, 5).equals("false")) {
				token.add(x.substring(0, 5));
				x = x.substring(5);
			}else if (x.length() >= 5 && x.substring(0, 5).equals("while")) {
				token.add(x.substring(0, 5));
				x = x.substring(5);
			}else if (x.length() >= 18 && x.substring(0, 18).equals("System.out.println")) {
				token.add(x.substring(0, 18));
				x = x.substring(18);
			}else {
				System.out.println("Parse error");
				flag = true;
				error();
				return;
			}
		}
		
	}
	
	public void eat(String x) {
		if (token.current == token.tlist.size()) {
			error();
			return;
		}
		if (token.get().equals(x)) {
			token.next();
			return;
		}else {
			error();
			return;
		}	
	}
	
	public void S() {
		if (token.tlist.size() == 0) {
			return;
		}
		if (token.current == token.tlist.size()) {
			error();
			return;
		}
		if (token.get().equals("{")) {
			eat("{");
			L();
			eat("}");
			return;
		}else if (token.get().equals("System.out.println")) {
			eat("System.out.println");
			eat("(");
			E();
			eat(")");
			eat(";");
			return;
		}else if (token.get().equals("if")) {
			eat("if");
			eat("(");
			E();
			eat(")");
			S();
			eat("else");
			S();
			return;
		}else if (token.get().equals("while")) {
			eat("while");
			eat("(");
			E();
			eat(")");
			S();
			return;
		}else {
			error();
			return;
		}
	}
	
	public void L() {
		if (token.current == token.tlist.size()) {
			error();
			return;
		}
		if (token.get().equals("{") || token.get().equals("System.out.println") || token.get().equals("if") || token.get().equals("while")) {
			S();
			L();
			return;
		}else {
			return;
		}
	}
	
	public void E() {
		if (token.current == token.tlist.size()) {
			error();
			return;
		}
		if (token.get().equals("true")) {
			eat("true");
			return;
		}else if (token.get().equals("false")) {
			eat("false");
			return;
		}else if (token.get().equals("!")) {
			eat("!");
			E();
			return;
		}else {
			error();
			return;
		}
	}
	
	public void error() {
		ecall++;
	}
}
