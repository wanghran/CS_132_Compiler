import java.util.*;
import visitor.*;
import syntaxtree.*;

public class F_nameCheck extends GJVoidDepthFirst<Map<String, String>>{
	
	static String[] obj_name = new String[1];
	
	public void visit(VarDeclaration n, Map<String, String> variable) {
		if (variable.containsKey(n.f1.f0.toString())) {
			System.out.println("Type error");
			System.exit(0);
		}
		if (n.f0.f0.choice instanceof ArrayType) {
			variable.put(n.f1.f0.toString(), "int[]");
		}else if (n.f0.f0.choice instanceof BooleanType) {
			variable.put(n.f1.f0.toString(), "boolean");
		}else if (n.f0.f0.choice instanceof IntegerType) {
			variable.put(n.f1.f0.toString(), "int");
		}else if (n.f0.f0.choice instanceof Identifier) {
			n.f0.accept(this, variable);
			variable.put(n.f1.f0.toString(), obj_name[0]);
		}
	}
	
	public void visit(MethodDeclaration n, Map<String, String> method) {
		if (method.containsKey(n.f2.f0.toString())) {
			System.out.println("Type error");
			System.exit(0);
		}
		if (n.f1.f0.choice instanceof ArrayType) {
			method.put(n.f2.f0.toString(), "int[]");
		}else if (n.f1.f0.choice instanceof BooleanType) {
			method.put(n.f2.f0.toString(), "boolean");
		}else if (n.f1.f0.choice instanceof IntegerType) {
			method.put(n.f2.f0.toString(), "int");
		}else if (n.f1.f0.choice instanceof Identifier) {
			n.f1.accept(this, method);
			method.put(n.f2.f0.toString(), obj_name[0]);
		}
	}
	
	public void visit(Identifier n, Map<String, String> x) {
	      obj_name[0] = n.f0.toString();
	}

}
