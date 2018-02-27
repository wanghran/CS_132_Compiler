import java.util.*;
import visitor.*;
import syntaxtree.*;

public class S_method_local extends GJVoidDepthFirst<Map<String, List<Map<String, String>>>>{
	static Map<String, String> argument = new LinkedHashMap<String,String>();
	static Map<String, String> local_v = new HashMap<String,String>();
	static List<Map<String, String>> list = new LinkedList<Map<String, String>>();
	static String[] obj_name = new String[1];
	
	public void visit(MethodDeclaration n, Map<String, List<Map<String, String>>> method_local) {
		method_local.put(n.f2.f0.toString(), new LinkedList<Map<String,String>>());
		n.f4.accept(this, method_local);
		n.f7.accept(this, method_local);
		method_local.get(n.f2.f0.toString()).add(new LinkedHashMap<String, String>());
		method_local.get(n.f2.f0.toString()).add(new HashMap<String, String>());
		for (String key : local_v.keySet()) {
			method_local.get(n.f2.f0.toString()).get(1).put(key, local_v.get(key));
		}
		for (String key : argument.keySet()) {
			method_local.get(n.f2.f0.toString()).get(0).put(key, argument.get(key));
		}
	}
	
	public void visit(FormalParameter n, Map<String, List<Map<String, String>>> method_local) {
		if (argument.containsKey(n.f1.f0.toString())) {
			System.out.println("Type error");
			System.exit(0);
		}
		if (n.f0.f0.choice instanceof ArrayType) {
			argument.put(n.f1.f0.toString(), "int[]");
		}else if (n.f0.f0.choice instanceof BooleanType) {
			argument.put(n.f1.f0.toString(), "boolean");
		}else if (n.f0.f0.choice instanceof IntegerType) {
			argument.put(n.f1.f0.toString(), "int");
		}else if (n.f0.f0.choice instanceof Identifier) {
			n.f0.accept(this, method_local);
			argument.put(n.f1.f0.toString(), obj_name[0]);
		}
	}

	public void visit(VarDeclaration n, Map<String, List<Map<String, String>>> method_local) {
		if (argument.containsKey(n.f1.f0.toString())) {
			System.out.println("Type error");
			System.exit(0);
		}
		if (local_v.containsKey(n.f1.f0.toString())) {
			System.out.println("Type error");
			System.exit(0);
		}
		if (n.f0.f0.choice instanceof ArrayType) {
			local_v.put(n.f1.f0.toString(), "int[]");
		}else if (n.f0.f0.choice instanceof BooleanType) {
			local_v.put(n.f1.f0.toString(), "boolean");
		}else if (n.f0.f0.choice instanceof IntegerType) {
			local_v.put(n.f1.f0.toString(), "int");
		}else if (n.f0.f0.choice instanceof Identifier) {
			n.f0.accept(this, method_local);
			local_v.put(n.f1.f0.toString(), obj_name[0]);
		}
		
	}

	public void visit(Identifier n, Map<String, List<Map<String, String>>> method_local) {
		obj_name[0] = n.f0.toString();
	}
}
