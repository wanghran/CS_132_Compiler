import java.util.*;
import visitor.*;
import syntaxtree.*;

public class Typecheck {
	
	static Set<String> class_name = new HashSet<String>();
	static Map<String, Map<String, String>> variable= new HashMap<String, Map<String, String>>();
	static Map<String, Map<String, String>> method = new HashMap<String, Map<String, String>>();
	static Map<String, Map<String, List<Map<String, String>>>> method_local = new HashMap<String, Map<String, List<Map<String, String>>>>();
	static String[] current_class = new String[1];
	static String[] current_m = new String[1];
	static String[] PexpType = new String[1];
	static String[] expType = new String[1];
	static List<String> argu_T = new ArrayList<String>();
	static int arg_counter = 0;
	static String[] MS_cn = new String[1];
	static String[] MS_mn = new String[1];
	static String M;
	
	
	public static class F_visitor extends DepthFirstVisitor {
		public void visit(MainClass n) {
			M = n.f1.f0.toString();
			if (class_name.contains(n.f1.f0.toString())) {
				System.out.println("Type error");
				System.exit(0);
			}
			class_name.add(n.f1.f0.toString());
			variable.put(n.f1.f0.toString(), new HashMap<String, String>());
			n.f14.accept(new F_nameCheck(), variable.get(n.f1.f0.toString()));
		}
		
		public void visit(ClassDeclaration n) {
			if (class_name.contains(n.f1.f0.toString())) {
				System.out.println("Type error");
				System.exit(0);
			}
			class_name.add(n.f1.f0.toString());
			variable.put(n.f1.f0.toString(), new HashMap<String, String>());
			n.f3.accept(new F_nameCheck(), variable.get(n.f1.f0.toString()));
			method.put(n.f1.f0.toString(), new HashMap<String, String>());
			n.f4.accept(new F_nameCheck(), method.get(n.f1.f0.toString()));
		}
	}
	
	public static class S_visitor extends DepthFirstVisitor {

		public void visit(ClassDeclaration n) {
			method_local.put(n.f1.f0.toString(), new HashMap<String, List<Map<String, String>>>());
			n.f4.accept(new S_method_local(), method_local.get(n.f1.f0.toString()));
			for (String key_m : method_local.get(n.f1.f0.toString()).keySet()) {
				for (String key_v : method_local.get(n.f1.f0.toString()).get(key_m).get(1).keySet()) {
					boolean b1 = method_local.get(n.f1.f0.toString()).get(key_m).get(1).get(key_v).equals("int[]");
					boolean b2 = method_local.get(n.f1.f0.toString()).get(key_m).get(1).get(key_v).equals("int");
					boolean b3 = method_local.get(n.f1.f0.toString()).get(key_m).get(1).get(key_v).equals("boolean");
					if (!b1 && !b2 && !b3 && !class_name.contains(method_local.get(n.f1.f0.toString()).get(key_m).get(1).get(key_v))) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
			}
			for (String key_m : method_local.get(n.f1.f0.toString()).keySet()) {
				for (String key_v : method_local.get(n.f1.f0.toString()).get(key_m).get(0).keySet()) {
					boolean b1 = method_local.get(n.f1.f0.toString()).get(key_m).get(0).get(key_v).equals("int[]");
					boolean b2 = method_local.get(n.f1.f0.toString()).get(key_m).get(0).get(key_v).equals("int");
					boolean b3 = method_local.get(n.f1.f0.toString()).get(key_m).get(0).get(key_v).equals("boolean");
					if (!b1 && !b2 && !b3 && !class_name.contains(method_local.get(n.f1.f0.toString()).get(key_m).get(0).get(key_v))) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
			}
		}
		
	}
	
	public static class T_visitor extends DepthFirstVisitor { //statement expression
		
		public void visit(MainClass n) {
			current_class[0] = n.f1.f0.toString();
			n.f15.accept(this);
			current_class[0] =null;
		}
				
		public void visit(ClassDeclaration n) {
			current_class[0] = n.f1.f0.toString();
			n.f4.accept(this);
			current_class[0] = null;
		}
		
		public void visit(MethodDeclaration n) {
			current_m[0] = n.f2.f0.toString();
			n.f8.accept(this);
			n.f10.accept(this);
			String c_n = current_class[0];
			String ret_t = expType[0];
			if (!method.get(c_n).get(n.f2.f0.toString()).equals(ret_t)) {
				System.out.println("Type error");
				System.exit(0);
			}
			current_m[0] = null;
		}

		public void visit(ThisExpression n) {
			String c_n = current_class[0];
			PexpType[0] = c_n;
			expType[0] = c_n;
		}
		
		public void visit(Identifier n) {
			PexpType[0] = n.f0.toString();
		}
		
		public void visit(ArrayAllocationExpression n) {
			String c_n = current_class[0];
			String m_n = current_m[0];
			PexpType[0] = null;
			expType[0] = null;
			n.f3.accept(this);
			if (expType[0] != null) {
				if (!expType[0].equals("int")) {
					System.out.println("Type error");
					System.exit(0);
				}else {
					PexpType[0] = "int[]";
					expType[0] = "int[]";
				}
			}else {
				String v = PexpType[0];
				if (c_n.equals(M)) {
					if (!variable.get(c_n).containsKey(v)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!variable.get(c_n).get(v).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
					PexpType[0] = "int[]";
					expType[0] = "int[]";
				}else {
					if (method_local.get(c_n).get(m_n).get(1).containsKey(v) && !method_local.get(c_n).get(m_n).get(1).get(v).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v) && !method_local.get(c_n).get(m_n).get(0).get(v).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (variable.get(c_n).containsKey(v) && !variable.get(c_n).get(v).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!method_local.get(c_n).get(m_n).get(1).containsKey(v) && !method_local.get(c_n).get(m_n).get(0).containsKey(v) && !variable.get(c_n).containsKey(v)) {
						System.out.println("Type error");
						System.exit(0);
					}
					PexpType[0] = "int[]";
					expType[0] = "int[]";
				}
			}
		}

		public void visit(AllocationExpression n ) {
			PexpType[0] = null;
			expType[0] = null;
			n.f1.accept(this);
			String o_n = PexpType[0];
			if (!class_name.contains(o_n)) {
				System.out.println("Type error");
				System.exit(0);
			}
			PexpType[0] = o_n;
			expType[0] = o_n;
		}
	
		public void visit(NotExpression n) {
			String c_n = current_class[0];
			String m_n = current_m[0];
			PexpType[0] = null;
			expType[0] = null;
			n.f1.accept(this);
			if (expType[0] != null) {
				if (!expType[0].equals("boolean")) {
					System.out.println("Type error");
					System.exit(0);
				}else {
					PexpType[0] = "boolean";
					expType[0] = "boolean";
				}
			}else {
				String v = PexpType[0];
				if (c_n.equals(M)) {
					if (!variable.get(c_n).containsKey(v)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!variable.get(c_n).get(v).equals("boolean")) {
						System.out.println("Type error");
						System.exit(0);
					}
					PexpType[0] = "boolean";
					expType[0] = "boolean";
				}else {
					if (method_local.get(c_n).get(m_n).get(1).containsKey(v) && !method_local.get(c_n).get(m_n).get(1).get(v).equals("boolean")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v) && !method_local.get(c_n).get(m_n).get(0).get(v).equals("boolean")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (variable.get(c_n).containsKey(v) && !variable.get(c_n).get(v).equals("boolean")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!method_local.get(c_n).get(m_n).get(1).containsKey(v) && !method_local.get(c_n).get(m_n).get(0).containsKey(v) && ! variable.get(c_n).containsKey(v)) {
						System.out.println("Type error");
						System.exit(0);
					}
					PexpType[0] = "boolean";
					expType[0] = "boolean";
				}
			}
		}

		public void visit(PrimaryExpression n) {
			if (n.f0.choice instanceof IntegerLiteral) {
				expType[0] = "int";
				PexpType[0] = "int";
			}
			if (n.f0.choice instanceof TrueLiteral || n.f0.choice instanceof FalseLiteral) {
				expType[0] = "boolean";
				PexpType[0] = "boolean";
			}
			n.f0.accept(this);
		}
		
		public void visit(AndExpression n) {
			boolean b1 = (n.f0.f0.choice instanceof TrueLiteral) || (n.f0.f0.choice instanceof FalseLiteral);
			boolean b2 = (n.f2.f0.choice instanceof TrueLiteral) || (n.f2.f0.choice instanceof FalseLiteral);
			if (b1 && b2) {
				expType[0] = "boolean";
			}else if (n.f0.f0.choice instanceof Identifier && n.f2.f0.choice instanceof Identifier) {
				String c_n = current_class[0];
				String m_n = current_m[0];
				n.f0.accept(this);
				String v1 = PexpType[0];
				n.f2.accept(this);
				String v2 = PexpType[0];
				if (c_n.equals(M)) {
					if (!variable.get(c_n).containsKey(v1) || !variable.get(c_n).containsKey(v2)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!variable.get(c_n).get(v1).equals("boolean") || !variable.get(c_n).get(v2).equals("boolean")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else {
					if (method_local.get(c_n).get(m_n).get(1).containsKey(v1) && method_local.get(c_n).get(m_n).get(1).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v1).equals("boolean") || !method_local.get(c_n).get(m_n).get(1).get(v2).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v1) && method_local.get(c_n).get(m_n).get(0).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v1).equals("boolean") || !method_local.get(c_n).get(m_n).get(0).get(v2).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v1) && method_local.get(c_n).get(m_n).get(1).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v2).equals("boolean") || !method_local.get(c_n).get(m_n).get(0).get(v1).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v1) && method_local.get(c_n).get(m_n).get(0).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(0).get(v1).equals("boolean") || !method_local.get(c_n).get(m_n).get(0).get(v2).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v1) && variable.get(c_n).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v1).equals("boolean") || !variable.get(c_n).get(v2).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v2) && variable.get(c_n).containsKey(v1)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v2).equals("boolean") || !variable.get(c_n).get(v1).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v1) && variable.get(c_n).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(0).get(v1).equals("boolean") || !variable.get(c_n).get(v2).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v2) && variable.get(c_n).containsKey(v1)) {
						if (!method_local.get(c_n).get(m_n).get(0).get(v2).equals("boolean") || !variable.get(c_n).get(v1).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (variable.get(c_n).containsKey(v1) && variable.get(c_n).containsKey(v2)) {
						if (!variable.get(c_n).get(v1).equals("boolean") || !variable.get(c_n).get(v2).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (!method_local.get(c_n).get(m_n).get(1).containsKey(v1) && !method_local.get(c_n).get(m_n).get(0).containsKey(v1) && !variable.get(c_n).containsKey(v1)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).containsKey(v2) && !variable.get(c_n).containsKey(v2)) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
				expType[0] = "boolean";
			}else if (n.f0.f0.choice instanceof NotExpression && n.f2.f0.choice instanceof NotExpression) {
				n.f0.accept(this);
				n.f2.accept(this);
				expType[0] = "boolean";
			}else if (n.f0.f0.choice instanceof NotExpression) {
				String c_n = current_class[0];
				String m_n = current_m[0];
				n.f0.accept(this);
				if (!((n.f2.f0.choice instanceof TrueLiteral) || (n.f2.f0.choice instanceof FalseLiteral)) && !(n.f2.f0.choice instanceof Identifier)) {
					System.out.println("Type error");
					System.exit(0);
				}else if (n.f2.f0.choice instanceof Identifier) {
					n.f2.accept(this);
					String v2 = PexpType[0];
					if (c_n.equals(M)) {
						if (!variable.get(c_n).containsKey(v2)) {
							System.out.println("Type error");
							System.exit(0);
						}else if (!variable.get(c_n).get(v2).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}
						expType[0] = "boolean";
					}else {
						if (method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !method_local.get(c_n).get(m_n).get(1).get(v2).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).get(v2).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (variable.get(c_n).containsKey(v2) && !variable.get(c_n).get(v2).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (!method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).containsKey(v2) && !variable.get(c_n).containsKey(v2)) {
							System.out.println("Type error");
							System.exit(0);
						}
					}
				expType[0] = "boolean";
				}
			}else if (n.f2.f0.choice instanceof NotExpression) {
				n.f2.accept(this);
				String c_n = current_class[0];
				String m_n = current_m[0];
				if (!((n.f0.f0.choice instanceof TrueLiteral) || (n.f0.f0.choice instanceof FalseLiteral)) && !(n.f0.f0.choice instanceof Identifier)) {
					System.out.println("Type error");
					System.exit(0);
				}else if (n.f0.f0.choice instanceof Identifier) {
					n.f0.accept(this);
					String v0 = PexpType[0];
					if (c_n.equals(M)) {
						if (!variable.get(c_n).containsKey(v0)) {
							System.out.println("Type error");
							System.exit(0);
						}else if (!variable.get(c_n).get(v0).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}
						expType[0] = "boolean";
					}else {
						if (method_local.get(c_n).get(m_n).get(1).containsKey(v0) && !method_local.get(c_n).get(m_n).get(1).get(v0).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v0) && !method_local.get(c_n).get(m_n).get(0).get(v0).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (variable.get(c_n).containsKey(v0) && !variable.get(c_n).get(v0).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (!method_local.get(c_n).get(m_n).get(1).containsKey(v0) && !method_local.get(c_n).get(m_n).get(0).containsKey(v0) && !variable.get(c_n).containsKey(v0)) {
							System.out.println("Type error");
							System.exit(0);
						}
					}
				}
				expType[0] = "boolean";
			}else if (n.f0.f0.choice instanceof BracketExpression) {
				String c_n = current_class[0];
				String m_n= current_m[0];
				n.f0.accept(this);
				String t1 = expType[0];
				if (!t1.equals("boolean")) {
					System.out.println("Type error");
					System.exit(0);
				}
				if (n.f2.f0.choice instanceof TrueLiteral || n.f2.f0.choice instanceof FalseLiteral) {
					expType[0] = "boolean";
				}
				if (n.f2.f0.choice instanceof NotExpression) {
					n.f2.accept(this);
					expType[0] = "boolean";
				}
				if (n.f2.f0.choice instanceof BracketExpression) {
					n.f2.accept(this);
					String t2 = expType[0];
					if (!t2.equals("boolean")) {
						System.out.println("Type error");
						System.exit(0);
					}
					expType[0] = "boolean";
				}
				if (n.f2.f0.choice instanceof Identifier) {
					n.f2.accept(this);
					String v2 = PexpType[0];
					if (c_n.equals(M)) {
						if (!variable.get(c_n).containsKey(v2)) {
							System.out.println("Type error");
							System.exit(0);
						}else if (!variable.get(c_n).get(v2).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else {
						if (!method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !variable.get(c_n).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).containsKey(v2)) {
							System.out.println("Type error");
							System.exit(0);
						}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !method_local.get(c_n).get(m_n).get(1).get(v2).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).get(v2).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (variable.get(c_n).containsKey(v2) && !variable.get(c_n).get(v2).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}
					expType[0] = "boolean";
				}
			}else if (n.f2.f0.choice instanceof BracketExpression) {
				String c_n = current_class[0];
				String m_n= current_m[0];
				n.f2.accept(this);
				String t2 = expType[0];
				if (!t2.equals("boolean")) {
					System.out.println("Type error");
					System.exit(0);
				}
				if (n.f0.f0.choice instanceof TrueLiteral || n.f0.f0.choice instanceof FalseLiteral) {
					expType[0] = "boolean";
				}
				if (n.f0.f0.choice instanceof NotExpression) {
					n.f0.accept(this);
					expType[0] = "boolean";
				}
				if (n.f0.f0.choice instanceof BracketExpression) {
					n.f0.accept(this);
					String t1 = expType[0];
					if (!t1.equals("boolean")) {
						System.out.println("Type error");
						System.exit(0);
					}
					expType[0] = "boolean";
				}
				if (n.f0.f0.choice instanceof Identifier) {
					n.f0.accept(this);
					String v0 = PexpType[0];
					if (c_n.equals(M)) {
						if (!variable.get(c_n).containsKey(v0)) {
							System.out.println("Type error");
							System.exit(0);
						}else if (!variable.get(c_n).get(v0).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else {
						if (!method_local.get(c_n).get(m_n).get(1).containsKey(v0) && !variable.get(c_n).containsKey(v0) && !method_local.get(c_n).get(m_n).get(0).containsKey(v0)) {
							System.out.println("Type error");
							System.exit(0);
						}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v0) && !method_local.get(c_n).get(m_n).get(1).get(v0).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v0) && !method_local.get(c_n).get(m_n).get(0).get(v0).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (variable.get(c_n).containsKey(v0) && !variable.get(c_n).get(v0).equals("boolean")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}
					expType[0] = "boolean";
				}
			}else {
				System.out.println("Type error");
				System.exit(0);
			}
		}
		
		public void visit(CompareExpression n) {
			boolean b1 = (n.f0.f0.choice instanceof IntegerLiteral);
			boolean b2 = (n.f2.f0.choice instanceof IntegerLiteral);
			if (b1 && b2) {
				expType[0] = "boolean";
			}else if (n.f0.f0.choice instanceof Identifier && n.f2.f0.choice instanceof Identifier) {
				String c_n = current_class[0];
				String m_n= current_m[0];
				n.f0.accept(this);
				String v1 = PexpType[0];
				n.f2.accept(this);
				String v2 = PexpType[0];
				if (c_n.equals(M)) {
					if (!variable.get(c_n).containsKey(v1) || !variable.get(c_n).containsKey(v2)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!variable.get(c_n).get(v1).equals("int") || !variable.get(c_n).get(v2).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else {
					if (method_local.get(c_n).get(m_n).get(1).containsKey(v1) && method_local.get(c_n).get(m_n).get(1).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v1).equals("int") || !method_local.get(c_n).get(m_n).get(1).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v1) && method_local.get(c_n).get(m_n).get(0).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v1).equals("int") || !method_local.get(c_n).get(m_n).get(0).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v1) && method_local.get(c_n).get(m_n).get(1).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v2).equals("int") || !method_local.get(c_n).get(m_n).get(0).get(v1).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v1) && method_local.get(c_n).get(m_n).get(0).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(0).get(v1).equals("int") || !method_local.get(c_n).get(m_n).get(0).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v1) && variable.get(c_n).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v1).equals("int") || !variable.get(c_n).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v2) && variable.get(c_n).containsKey(v1)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v2).equals("int") || !variable.get(c_n).get(v1).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v1) && variable.get(c_n).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(0).get(v1).equals("int") || !variable.get(c_n).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v2) && variable.get(c_n).containsKey(v1)) {
						if (!method_local.get(c_n).get(m_n).get(0).get(v2).equals("int") || !variable.get(c_n).get(v1).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (variable.get(c_n).containsKey(v1) && variable.get(c_n).containsKey(v2)) {
						if (!variable.get(c_n).get(v1).equals("int") || !variable.get(c_n).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (!method_local.get(c_n).get(m_n).get(1).containsKey(v1) && !method_local.get(c_n).get(m_n).get(0).containsKey(v1) && !variable.get(c_n).containsKey(v1)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).containsKey(v2) && !variable.get(c_n).containsKey(v2)) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
				expType[0] = "boolean";
			}else if (n.f0.f0.choice instanceof Identifier && n.f2.f0.choice instanceof IntegerLiteral) {
				String c_n = current_class[0];
				String m_n= current_m[0];
				n.f0.accept(this);
				String v1 = PexpType[0];
				if (c_n.equals(M)) {
					if (!variable.get(c_n).get(v1).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else {
					if (!method_local.get(c_n).get(m_n).get(1).containsKey(v1) && !variable.get(c_n).containsKey(v1) && !method_local.get(c_n).get(m_n).get(0).containsKey(v1)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v1) && !method_local.get(c_n).get(m_n).get(1).get(v1).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v1) && !method_local.get(c_n).get(m_n).get(0).get(v1).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (variable.get(c_n).containsKey(v1) && variable.get(c_n).get(v1).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!method_local.get(c_n).get(m_n).get(1).containsKey(v1) && !method_local.get(c_n).get(m_n).get(0).containsKey(v1) && !variable.get(c_n).containsKey(v1)) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
				expType[0] = "boolean";
			}else if (n.f2.f0.choice instanceof Identifier && n.f0.f0.choice instanceof IntegerLiteral) {
				String c_n = current_class[0];
				String m_n= current_m[0];
				n.f2.accept(this);
				String v2 = PexpType[0];
				if (c_n.equals(M)) {
					if (!variable.get(c_n).get(v2).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else {
					if (!method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !variable.get(c_n).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).containsKey(v2)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !method_local.get(c_n).get(m_n).get(1).get(v2).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).get(v2).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (variable.get(c_n).containsKey(v2) && variable.get(c_n).get(v2).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).containsKey(v2) && !variable.get(c_n).containsKey(v2)) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
				expType[0] = "boolean";
			}else if (n.f0.f0.choice instanceof BracketExpression) {
					String c_n = current_class[0];
					String m_n= current_m[0];
					n.f0.accept(this);
					String t1 = expType[0];
					if (!t1.equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
					if (n.f2.f0.choice instanceof IntegerLiteral) {
						expType[0] = "boolean";
					}
					if (n.f2.f0.choice instanceof BracketExpression) {
						n.f2.accept(this);
						String t2 = expType[0];
						if (!t2.equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
						expType[0] = "boolean";
					}
					if (n.f2.f0.choice instanceof Identifier) {
						n.f2.accept(this);
						String v2 = PexpType[0];
						if (c_n.equals(M)) {
							if (!variable.get(c_n).containsKey(v2)) {
								System.out.println("Type error");
								System.exit(0);
							}else if (!variable.get(c_n).get(v2).equals("int")) {
								System.out.println("Type error");
								System.exit(0);
							}
						}else {
							if (!method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !variable.get(c_n).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).containsKey(v2)) {
								System.out.println("Type error");
								System.exit(0);
							}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !method_local.get(c_n).get(m_n).get(1).get(v2).equals("int")) {
								System.out.println("Type error");
								System.exit(0);
							}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).get(v2).equals("int")) {
								System.out.println("Type error");
								System.exit(0);
							}else if (variable.get(c_n).containsKey(v2) && !variable.get(c_n).get(v2).equals("int")) {
								System.out.println("Type error");
								System.exit(0);
							}
						}
						expType[0] = "boolean";
					}
				}else if (n.f2.f0.choice instanceof BracketExpression) {
					String c_n = current_class[0];
					String m_n= current_m[0];
					n.f2.accept(this);
					String t2 = expType[0];
					if (!t2.equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
					if (n.f0.f0.choice instanceof IntegerLiteral) {
						expType[0] = "boolean";
					}
					if (n.f0.f0.choice instanceof BracketExpression) {
						n.f0.accept(this);
						String t1 = expType[0];
						if (!t1.equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
						expType[0] = "boolean";
					}
					if (n.f0.f0.choice instanceof Identifier) {
						n.f0.accept(this);
						String v0 = PexpType[0];
						if (c_n.equals(M)) {
							if (!variable.get(c_n).containsKey(v0)) {
								System.out.println("Type error");
								System.exit(0);
							}else if (!variable.get(c_n).get(v0).equals("int")) {
								System.out.println("Type error");
								System.exit(0);
							}
						}else {
							if (!method_local.get(c_n).get(m_n).get(1).containsKey(v0) && !variable.get(c_n).containsKey(v0) && !method_local.get(c_n).get(m_n).get(0).containsKey(v0)) {
								System.out.println("Type error");
								System.exit(0);
							}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v0) && !method_local.get(c_n).get(m_n).get(1).get(v0).equals("int")) {
								System.out.println("Type error");
								System.exit(0);
							}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v0) && !method_local.get(c_n).get(m_n).get(0).get(v0).equals("int")) {
								System.out.println("Type error");
								System.exit(0);
							}else if (variable.get(c_n).containsKey(v0) && !variable.get(c_n).get(v0).equals("int")) {
								System.out.println("Type error");
								System.exit(0);
							}
						}
						expType[0] = "boolean";
					}
				}else {
				System.out.println("Type error");
				System.exit(0);
			}
		}
	
		public void visit(PlusExpression n) {
			boolean b1 = (n.f0.f0.choice instanceof IntegerLiteral);
			boolean b2 = (n.f2.f0.choice instanceof IntegerLiteral);
			if (b1 && b2) {
				expType[0] = "int";
			}else if (n.f0.f0.choice instanceof Identifier && n.f2.f0.choice instanceof Identifier) {
				String c_n = current_class[0];
				String m_n= current_m[0];
				n.f0.accept(this);
				String v1 = PexpType[0];
				n.f2.accept(this);
				String v2 = PexpType[0];
				if (c_n.equals(M)) {
					if (!variable.get(c_n).containsKey(v1) || !variable.get(c_n).containsKey(v2)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!variable.get(c_n).get(v1).equals("int") || !variable.get(c_n).get(v2).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else {
					if (method_local.get(c_n).get(m_n).get(1).containsKey(v1) && method_local.get(c_n).get(m_n).get(1).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v1).equals("int") || !method_local.get(c_n).get(m_n).get(1).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v1) && method_local.get(c_n).get(m_n).get(0).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v1).equals("int") || !method_local.get(c_n).get(m_n).get(0).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v1) && method_local.get(c_n).get(m_n).get(1).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v2).equals("int") || !method_local.get(c_n).get(m_n).get(0).get(v1).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v1) && method_local.get(c_n).get(m_n).get(0).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(0).get(v1).equals("int") || !method_local.get(c_n).get(m_n).get(0).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v1) && variable.get(c_n).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v1).equals("int") || !variable.get(c_n).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v2) && variable.get(c_n).containsKey(v1)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v2).equals("int") || !variable.get(c_n).get(v1).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v1) && variable.get(c_n).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(0).get(v1).equals("int") || !variable.get(c_n).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v2) && variable.get(c_n).containsKey(v1)) {
						if (!method_local.get(c_n).get(m_n).get(0).get(v2).equals("int") || !variable.get(c_n).get(v1).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (variable.get(c_n).containsKey(v1) && variable.get(c_n).containsKey(v2)) {
						if (!variable.get(c_n).get(v1).equals("int") || !variable.get(c_n).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (!method_local.get(c_n).get(m_n).get(1).containsKey(v1) && !method_local.get(c_n).get(m_n).get(0).containsKey(v1) && !variable.get(c_n).containsKey(v1)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).containsKey(v2) && !variable.get(c_n).containsKey(v2)) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
				expType[0] = "int";
			}else if (n.f0.f0.choice instanceof Identifier && n.f2.f0.choice instanceof IntegerLiteral) {
				String c_n = current_class[0];
				String m_n= current_m[0];
				n.f0.accept(this);
				String v1 = PexpType[0];
				if (c_n.equals(M)) {
					if (!variable.get(c_n).containsKey(v1)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!variable.get(c_n).get(v1).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else {
					if (!method_local.get(c_n).get(m_n).get(1).containsKey(v1) && !variable.get(c_n).containsKey(v1) && !method_local.get(c_n).get(m_n).get(0).containsKey(v1)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v1) && !method_local.get(c_n).get(m_n).get(1).get(v1).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v1) && !method_local.get(c_n).get(m_n).get(0).get(v1).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (variable.get(c_n).containsKey(v1) && !variable.get(c_n).get(v1).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
				expType[0] = "int";
			}else if (n.f2.f0.choice instanceof Identifier && n.f0.f0.choice instanceof IntegerLiteral) {
				String c_n = current_class[0];
				String m_n= current_m[0];
				n.f2.accept(this);
				String v2 = PexpType[0];
				if (c_n.equals(M)) {
					if (!variable.get(c_n).containsKey(v2)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!variable.get(c_n).get(v2).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else {
					if (!method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !variable.get(c_n).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).containsKey(v2)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !method_local.get(c_n).get(m_n).get(1).get(v2).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).get(v2).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (variable.get(c_n).containsKey(v2) && !variable.get(c_n).get(v2).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
				expType[0] = "int";
			}else if (n.f0.f0.choice instanceof BracketExpression) {
				String c_n = current_class[0];
				String m_n= current_m[0];
				n.f0.accept(this);
				String t1 = expType[0];
				if (!t1.equals("int")) {
					System.out.println("Type error");
					System.exit(0);
				}
				if (n.f2.f0.choice instanceof IntegerLiteral) {
					expType[0] = "int";
				}
				if (n.f2.f0.choice instanceof BracketExpression) {
					n.f2.accept(this);
					String t2 = expType[0];
					if (!t2.equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
					expType[0] = "int";
				}
				if (n.f2.f0.choice instanceof Identifier) {
					n.f2.accept(this);
					String v2 = PexpType[0];
					if (c_n.equals(M)) {
						if (!variable.get(c_n).containsKey(v2)) {
							System.out.println("Type error");
							System.exit(0);
						}else if (!variable.get(c_n).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else {
						if (!method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !variable.get(c_n).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).containsKey(v2)) {
							System.out.println("Type error");
							System.exit(0);
						}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !method_local.get(c_n).get(m_n).get(1).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (variable.get(c_n).containsKey(v2) && !variable.get(c_n).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}
					expType[0] = "int";
				}
			}else if (n.f2.f0.choice instanceof BracketExpression) {
				String c_n = current_class[0];
				String m_n= current_m[0];
				n.f2.accept(this);
				String t2 = expType[0];
				if (!t2.equals("int")) {
					System.out.println("Type error");
					System.exit(0);
				}
				if (n.f0.f0.choice instanceof IntegerLiteral) {
					expType[0] = "int";
				}
				if (n.f0.f0.choice instanceof BracketExpression) {
					n.f0.accept(this);
					String t1 = expType[0];
					if (!t1.equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
					expType[0] = "int";
				}
				if (n.f0.f0.choice instanceof Identifier) {
					n.f0.accept(this);
					String v0 = PexpType[0];
					if (c_n.equals(M)) {
						if (!variable.get(c_n).containsKey(v0)) {
							System.out.println("Type error");
							System.exit(0);
						}else if (!variable.get(c_n).get(v0).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else {
						if (!method_local.get(c_n).get(m_n).get(1).containsKey(v0) && !variable.get(c_n).containsKey(v0) && !method_local.get(c_n).get(m_n).get(0).containsKey(v0)) {
							System.out.println("Type error");
							System.exit(0);
						}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v0) && !method_local.get(c_n).get(m_n).get(1).get(v0).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v0) && !method_local.get(c_n).get(m_n).get(0).get(v0).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (variable.get(c_n).containsKey(v0) && !variable.get(c_n).get(v0).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}
					expType[0] = "int";
				}
			}else {
				System.out.println("Type error");
				System.exit(0);
			}
		}
		
		public void visit(MinusExpression n) {
			boolean b1 = (n.f0.f0.choice instanceof IntegerLiteral);
			boolean b2 = (n.f2.f0.choice instanceof IntegerLiteral);
			if (b1 && b2) {
				expType[0] = "int";
			}else if (n.f0.f0.choice instanceof Identifier && n.f2.f0.choice instanceof Identifier) {
				String c_n = current_class[0];
				String m_n= current_m[0];
				n.f0.accept(this);
				String v1 = PexpType[0];
				n.f2.accept(this);
				String v2 = PexpType[0];
				if (c_n.equals(M)) {
					if (!variable.get(c_n).containsKey(v1) || !variable.get(c_n).containsKey(v2)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!variable.get(c_n).get(v1).equals("int") || !variable.get(c_n).get(v2).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else {
					if (method_local.get(c_n).get(m_n).get(1).containsKey(v1) && method_local.get(c_n).get(m_n).get(1).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v1).equals("int") || !method_local.get(c_n).get(m_n).get(1).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v1) && method_local.get(c_n).get(m_n).get(0).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v1).equals("int") || !method_local.get(c_n).get(m_n).get(0).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v1) && method_local.get(c_n).get(m_n).get(1).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v2).equals("int") || !method_local.get(c_n).get(m_n).get(0).get(v1).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v1) && method_local.get(c_n).get(m_n).get(0).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(0).get(v1).equals("int") || !method_local.get(c_n).get(m_n).get(0).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v1) && variable.get(c_n).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v1).equals("int") || !variable.get(c_n).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v2) && variable.get(c_n).containsKey(v1)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v2).equals("int") || !variable.get(c_n).get(v1).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v1) && variable.get(c_n).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(0).get(v1).equals("int") || !variable.get(c_n).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v2) && variable.get(c_n).containsKey(v1)) {
						if (!method_local.get(c_n).get(m_n).get(0).get(v2).equals("int") || !variable.get(c_n).get(v1).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (variable.get(c_n).containsKey(v1) && variable.get(c_n).containsKey(v2)) {
						if (!variable.get(c_n).get(v1).equals("int") || !variable.get(c_n).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (!method_local.get(c_n).get(m_n).get(1).containsKey(v1) && !method_local.get(c_n).get(m_n).get(0).containsKey(v1) && !variable.get(c_n).containsKey(v1)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).containsKey(v2) && !variable.get(c_n).containsKey(v2)) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
				expType[0] = "int";
			}else if (n.f0.f0.choice instanceof Identifier && n.f2.f0.choice instanceof IntegerLiteral) {
				String c_n = current_class[0];
				String m_n= current_m[0];
				n.f0.accept(this);
				String v1 = PexpType[0];
				if (c_n.equals(M)) {
					if (!variable.get(c_n).containsKey(v1)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!variable.get(c_n).get(v1).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else {
					if (!method_local.get(c_n).get(m_n).get(1).containsKey(v1) && !variable.get(c_n).containsKey(v1) && !method_local.get(c_n).get(m_n).get(0).containsKey(v1)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v1) && !method_local.get(c_n).get(m_n).get(1).get(v1).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v1) && !method_local.get(c_n).get(m_n).get(0).get(v1).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (variable.get(c_n).containsKey(v1) && !variable.get(c_n).get(v1).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
				expType[0] = "int";
			}else if (n.f2.f0.choice instanceof Identifier && n.f0.f0.choice instanceof IntegerLiteral) {
				String c_n = current_class[0];
				String m_n= current_m[0];
				n.f2.accept(this);
				String v2 = PexpType[0];
				if (c_n.equals(M)) {
					if (!variable.get(c_n).containsKey(v2)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!variable.get(c_n).get(v2).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else {
					if (!method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !variable.get(c_n).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).containsKey(v2)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !method_local.get(c_n).get(m_n).get(1).get(v2).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).get(v2).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (variable.get(c_n).containsKey(v2) && !variable.get(c_n).get(v2).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
				expType[0] = "int";
			}else if (n.f0.f0.choice instanceof BracketExpression) {
				String c_n = current_class[0];
				String m_n= current_m[0];
				n.f0.accept(this);
				String t1 = expType[0];
				if (!t1.equals("int")) {
					System.out.println("Type error");
					System.exit(0);
				}
				if (n.f2.f0.choice instanceof IntegerLiteral) {
					expType[0] = "int";
				}
				if (n.f2.f0.choice instanceof BracketExpression) {
					n.f2.accept(this);
					String t2 = expType[0];
					if (!t2.equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
					expType[0] = "int";
				}
				if (n.f2.f0.choice instanceof Identifier) {
					n.f2.accept(this);
					String v2 = PexpType[0];
					if (c_n.equals(M)) {
						if (!variable.get(c_n).containsKey(v2)) {
							System.out.println("Type error");
							System.exit(0);
						}else if (!variable.get(c_n).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else {
						if (!method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !variable.get(c_n).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).containsKey(v2)) {
							System.out.println("Type error");
							System.exit(0);
						}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !method_local.get(c_n).get(m_n).get(1).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (variable.get(c_n).containsKey(v2) && !variable.get(c_n).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}
					expType[0] = "int";
				}
			}else if (n.f2.f0.choice instanceof BracketExpression) {
				String c_n = current_class[0];
				String m_n= current_m[0];
				n.f2.accept(this);
				String t2 = expType[0];
				if (!t2.equals("int")) {
					System.out.println("Type error");
					System.exit(0);
				}
				if (n.f0.f0.choice instanceof IntegerLiteral) {
					expType[0] = "int";
				}
				if (n.f0.f0.choice instanceof BracketExpression) {
					n.f0.accept(this);
					String t1 = expType[0];
					if (!t1.equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
					expType[0] = "int";
				}
				if (n.f0.f0.choice instanceof Identifier) {
					n.f0.accept(this);
					String v0 = PexpType[0];
					if (c_n.equals(M)) {
						if (!variable.get(c_n).containsKey(v0)) {
							System.out.println("Type error");
							System.exit(0);
						}else if (!variable.get(c_n).get(v0).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else {
						if (!method_local.get(c_n).get(m_n).get(1).containsKey(v0) && !variable.get(c_n).containsKey(v0) && !method_local.get(c_n).get(m_n).get(0).containsKey(v0)) {
							System.out.println("Type error");
							System.exit(0);
						}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v0) && !method_local.get(c_n).get(m_n).get(1).get(v0).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v0) && !method_local.get(c_n).get(m_n).get(0).get(v0).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (variable.get(c_n).containsKey(v0) && !variable.get(c_n).get(v0).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}
					expType[0] = "int";
				}
			}else {
				System.out.println("Type error");
				System.exit(0);
			}
		}

		public void visit(TimesExpression n) {
			boolean b1 = (n.f0.f0.choice instanceof IntegerLiteral);
			boolean b2 = (n.f2.f0.choice instanceof IntegerLiteral);
			if (b1 && b2) {
				expType[0] = "int";
			}else if (n.f0.f0.choice instanceof Identifier && n.f2.f0.choice instanceof Identifier) {
				String c_n = current_class[0];
				String m_n= current_m[0];
				n.f0.accept(this);
				String v1 = PexpType[0];
				n.f2.accept(this);
				String v2 = PexpType[0];
				if (c_n.equals(M)) {
					if (!variable.get(c_n).containsKey(v1) || !variable.get(c_n).containsKey(v2)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!variable.get(c_n).get(v1).equals("int") || !variable.get(c_n).get(v2).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else {
					if (method_local.get(c_n).get(m_n).get(1).containsKey(v1) && method_local.get(c_n).get(m_n).get(1).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v1).equals("int") || !method_local.get(c_n).get(m_n).get(1).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v1) && method_local.get(c_n).get(m_n).get(0).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v1).equals("int") || !method_local.get(c_n).get(m_n).get(0).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v1) && method_local.get(c_n).get(m_n).get(1).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v2).equals("int") || !method_local.get(c_n).get(m_n).get(0).get(v1).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v1) && method_local.get(c_n).get(m_n).get(0).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(0).get(v1).equals("int") || !method_local.get(c_n).get(m_n).get(0).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v1) && variable.get(c_n).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v1).equals("int") || !variable.get(c_n).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v2) && variable.get(c_n).containsKey(v1)) {
						if (!method_local.get(c_n).get(m_n).get(1).get(v2).equals("int") || !variable.get(c_n).get(v1).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v1) && variable.get(c_n).containsKey(v2)) {
						if (!method_local.get(c_n).get(m_n).get(0).get(v1).equals("int") || !variable.get(c_n).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v2) && variable.get(c_n).containsKey(v1)) {
						if (!method_local.get(c_n).get(m_n).get(0).get(v2).equals("int") || !variable.get(c_n).get(v1).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (variable.get(c_n).containsKey(v1) && variable.get(c_n).containsKey(v2)) {
						if (!variable.get(c_n).get(v1).equals("int") || !variable.get(c_n).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else if (!method_local.get(c_n).get(m_n).get(1).containsKey(v1) && !method_local.get(c_n).get(m_n).get(0).containsKey(v1) && !variable.get(c_n).containsKey(v1)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).containsKey(v2) && !variable.get(c_n).containsKey(v2)) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
				expType[0] = "int";
			}else if (n.f0.f0.choice instanceof Identifier && n.f2.f0.choice instanceof IntegerLiteral) {
				String c_n = current_class[0];
				String m_n= current_m[0];
				n.f0.accept(this);
				String v1 = PexpType[0];
				if (c_n.equals(M)) {
					if (!variable.get(c_n).containsKey(v1)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!variable.get(c_n).get(v1).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else {
					if (!method_local.get(c_n).get(m_n).get(1).containsKey(v1) && !variable.get(c_n).containsKey(v1) && !method_local.get(c_n).get(m_n).get(0).containsKey(v1)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v1) && !method_local.get(c_n).get(m_n).get(1).get(v1).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v1) && !method_local.get(c_n).get(m_n).get(0).get(v1).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (variable.get(c_n).containsKey(v1) && !variable.get(c_n).get(v1).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
				expType[0] = "int";
			}else if (n.f2.f0.choice instanceof Identifier && n.f0.f0.choice instanceof IntegerLiteral) {
				String c_n = current_class[0];
				String m_n= current_m[0];
				n.f2.accept(this);
				String v2 = PexpType[0];
				if (c_n.equals(M)) {
					if (!variable.get(c_n).containsKey(v2)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!variable.get(c_n).get(v2).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else {
					if (!method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !variable.get(c_n).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).containsKey(v2)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !method_local.get(c_n).get(m_n).get(1).get(v2).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).get(v2).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}else if (variable.get(c_n).containsKey(v2) && !variable.get(c_n).get(v2).equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
				expType[0] = "int";
			}else if (n.f0.f0.choice instanceof BracketExpression) {
				String c_n = current_class[0];
				String m_n= current_m[0];
				n.f0.accept(this);
				String t1 = expType[0];
				if (!t1.equals("int")) {
					System.out.println("Type error");
					System.exit(0);
				}
				if (n.f2.f0.choice instanceof IntegerLiteral) {
					expType[0] = "int";
				}
				if (n.f2.f0.choice instanceof BracketExpression) {
					n.f2.accept(this);
					String t2 = expType[0];
					if (!t2.equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
					expType[0] = "int";
				}
				if (n.f2.f0.choice instanceof Identifier) {
					n.f2.accept(this);
					String v2 = PexpType[0];
					if (c_n.equals(M)) {
						if (!variable.get(c_n).containsKey(v2)) {
							System.out.println("Type error");
							System.exit(0);
						}else if (!variable.get(c_n).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else {
						if (!method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !variable.get(c_n).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).containsKey(v2)) {
							System.out.println("Type error");
							System.exit(0);
						}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !method_local.get(c_n).get(m_n).get(1).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (variable.get(c_n).containsKey(v2) && !variable.get(c_n).get(v2).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}
					expType[0] = "int";
				}
			}else if (n.f2.f0.choice instanceof BracketExpression) {
				String c_n = current_class[0];
				String m_n= current_m[0];
				n.f2.accept(this);
				String t2 = expType[0];
				if (!t2.equals("int")) {
					System.out.println("Type error");
					System.exit(0);
				}
				if (n.f0.f0.choice instanceof IntegerLiteral) {
					expType[0] = "int";
				}
				if (n.f0.f0.choice instanceof BracketExpression) {
					n.f0.accept(this);
					String t1 = expType[0];
					if (!t1.equals("int")) {
						System.out.println("Type error");
						System.exit(0);
					}
					expType[0] = "int";
				}
				if (n.f0.f0.choice instanceof Identifier) {
					n.f0.accept(this);
					String v0 = PexpType[0];
					if (c_n.equals(M)) {
						if (!variable.get(c_n).containsKey(v0)) {
							System.out.println("Type error");
							System.exit(0);
						}else if (!variable.get(c_n).get(v0).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}else {
						if (!method_local.get(c_n).get(m_n).get(1).containsKey(v0) && !variable.get(c_n).containsKey(v0) && !method_local.get(c_n).get(m_n).get(0).containsKey(v0)) {
							System.out.println("Type error");
							System.exit(0);
						}else if (method_local.get(c_n).get(m_n).get(1).containsKey(v0) && !method_local.get(c_n).get(m_n).get(1).get(v0).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v0) && !method_local.get(c_n).get(m_n).get(0).get(v0).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}else if (variable.get(c_n).containsKey(v0) && !variable.get(c_n).get(v0).equals("int")) {
							System.out.println("Type error");
							System.exit(0);
						}
					}
					expType[0] = "int";
				}
			}else {
				System.out.println("Type error");
				System.exit(0);
			}
		}
		
		public void visit(ArrayLookup n) {
			boolean v1C = false;
			boolean v2C = false;
			if (!(n.f0.f0.choice instanceof Identifier)) {
				if (n.f0.f0.choice instanceof BracketExpression) {
					n.f0.accept(this);
					if (!expType[0].equals("int[]")) {
						System.out.println("Type error");
						System.exit(0);
					}
					v1C = true;
				}else {
					System.out.println("Type error");
					System.exit(0);
				}
			}
			String c_n = current_class[0];
		    String m_n = current_m[0];
			n.f0.accept(this);
		    String v1 = PexpType[0];
		    if (c_n.equals(M)) {
		    	if (!variable.get(m_n).containsKey(v1)) {
		    		System.out.println("Type error");
					System.exit(0);
		    	}else if (!variable.get(m_n).get(v1).equals("int[]")) {
		    		System.out.println("Type error");
					System.exit(0);
		    	}
		    	v1C = true;
		    }else {
		    	if (method_local.get(c_n).get(m_n).get(1).containsKey(v1) && !method_local.get(c_n).get(m_n).get(1).get(v1).equals("int[]")) {
			    	System.out.println("Type error");
					System.exit(0);
			    }else if (method_local.get(c_n).get(m_n).get(0).containsKey(v1) && !method_local.get(c_n).get(m_n).get(0).get(v1).equals("int[]")) {
			    	System.out.println("Type error");
					System.exit(0);
			    }else if (variable.get(m_n).containsKey(v1) && !variable.get(m_n).get(v1).equals("int[]")) {
			    		System.out.println("Type error");
			    		System.exit(0);
			    }else if (!method_local.get(c_n).get(m_n).get(1).containsKey(v1) && !variable.get(c_n).containsKey(v1) && !method_local.get(c_n).get(m_n).get(0).containsKey(v1)) {
					System.out.println("Type error");
					System.exit(0);
				}else {
			    	v1C = true;
			    }	
		    }
		    String v2;
		    if (n.f2.f0.choice instanceof IntegerLiteral) {
		    	v2C = true;
		    }else if (n.f2.f0.choice instanceof Identifier) {
		    	n.f2.accept(this);
			    v2 = PexpType[0];
			    if (c_n.equals(M)) {
			    	if (!variable.get(m_n).containsKey(v2)) {
			    		System.out.println("Type error");
						System.exit(0);
			    	}else if (!variable.get(m_n).get(v2).equals("int")) {
			    		System.out.println("Type error");
						System.exit(0);
			    	}
			    	v2C = true;
			    }else {
			    	if (method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !method_local.get(c_n).get(m_n).get(1).get(v2).equals("int")) {
				    	System.out.println("Type error");
						System.exit(0);
				    }else if (method_local.get(c_n).get(m_n).get(0).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).get(v2).equals("int")) {
				    	System.out.println("Type error");
						System.exit(0);
				    }else if (variable.get(m_n).containsKey(v2) && !variable.get(m_n).get(v2).equals("int")) {
				    		System.out.println("Type error");
				    		System.exit(0);
				    }else if (!method_local.get(c_n).get(m_n).get(1).containsKey(v2) && !variable.get(c_n).containsKey(v2) && !method_local.get(c_n).get(m_n).get(0).containsKey(v2)) {
						System.out.println("Type error");
						System.exit(0);
					}else {
				    	v1C = true;
				    }	
			    }
		    }else if (n.f2.f0.choice instanceof BracketExpression) {
		    	n.f2.accept(this);
				if (!expType[0].equals("int[]")) {
					System.out.println("Type error");
					System.exit(0);
				}
				v2C = true;
		    }else {
		    	System.out.println("Type error");
				System.exit(0);
		    }
		    if (v1C && v2C) {
		    	expType[0] = "int";
		    }else {
		    	System.out.println("Type error");
				System.exit(0);
		    }
		}

		public void visit(ArrayLength n) {
			if (!(n.f0.f0.choice instanceof Identifier)) {
				if (n.f0.f0.choice instanceof BracketExpression) {
					n.f0.accept(this);
					if (!expType[0].equals("int[]")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
				System.out.println("Type error");
				System.exit(0);
			}
			String c_n = current_class[0];
			String m_n = current_m[0];
			n.f0.accept(this);
			String v = PexpType[0];
			if (c_n.equals(M)) {
		    	if (!variable.get(m_n).containsKey(v)) {
		    		System.out.println("Type error");
					System.exit(0);
		    	}else if (!variable.get(m_n).get(v).equals("int[]")) {
		    		System.out.println("Type error");
					System.exit(0);
		    	}else {
		    		expType[0] = "int";
		    	}
		    }else {
		    	if (method_local.get(c_n).get(m_n).get(1).containsKey(v) && !method_local.get(c_n).get(m_n).get(1).get(v).equals("int[]")) {
			    	System.out.println("Type error");
					System.exit(0);
			    }else if (method_local.get(c_n).get(m_n).get(0).containsKey(v) && !method_local.get(c_n).get(m_n).get(0).get(v).equals("int[]")) {
			    	System.out.println("Type error");
					System.exit(0);
			    }else if (variable.get(m_n).containsKey(v) && !variable.get(m_n).get(v).equals("int[]")) {
			    		System.out.println("Type error");
			    		System.exit(0);
			    }else if (!method_local.get(c_n).get(m_n).get(1).containsKey(v) && !variable.get(c_n).containsKey(v) && !method_local.get(c_n).get(m_n).get(0).containsKey(v)) {
					System.out.println("Type error");
					System.exit(0);
				}else {
			    	expType[0] = "int";
			    }	
		    }
		}
	
		public void visit(MessageSend n) {
			String c_n = current_class[0];
			String m_n = current_m[0];
			MS_cn[0] = c_n;
			MS_mn[0] = m_n;
			String ob = "";
			String ob_t = "";
			String c_method = n.f2.f0.toString();
			n.f0.accept(this);
			if (n.f0.f0.choice instanceof AllocationExpression || n.f0.f0.choice instanceof ThisExpression) {
				ob_t = expType[0];
			}else if (n.f0.f0.choice instanceof BracketExpression) {
				expType[0] = null;
				PexpType[0] = null;
				n.f0.accept(this);		
				if (expType[0].equals("int") || expType[0].equals("int[]") || expType[0].equals("boolean")) {
					ob_t = expType[0];
				}else {
					ob = PexpType[0];
					if (c_n.equals(M)) {
						if (variable.get(c_n).containsKey(ob) && !class_name.contains(variable.get(c_n).get(ob))) {
							System.out.println("Type error");
							System.exit(0);
						}
						ob_t = variable.get(c_n).get(ob);
					}else {
						if (method_local.get(c_n).get(m_n).get(1).containsKey(ob) && class_name.contains(method_local.get(c_n).get(m_n).get(1).get(ob))) {
							ob_t = method_local.get(c_n).get(m_n).get(1).get(ob);
						}else if (method_local.get(c_n).get(m_n).get(0).containsKey(ob) && class_name.contains(method_local.get(c_n).get(m_n).get(0).get(ob))) {
							ob_t = method_local.get(c_n).get(m_n).get(0).get(ob);
						}else if (variable.get(c_n).containsKey(ob) && class_name.contains(variable.get(c_n).get(ob))) {
							ob_t = variable.get(c_n).get(ob);
						}else {
							System.out.println("Type error");
							System.exit(0);
						}
					}
				}
			}else {
				ob = PexpType[0];
				if (c_n.equals(M)) {
					if (variable.get(c_n).containsKey(ob) && !class_name.contains(variable.get(c_n).get(ob))) {
						System.out.println("Type error");
						System.exit(0);
					}
					ob_t = variable.get(c_n).get(ob);
				}else {
					if (method_local.get(c_n).get(m_n).get(1).containsKey(ob) && class_name.contains(method_local.get(c_n).get(m_n).get(1).get(ob))) {
						ob_t = method_local.get(c_n).get(m_n).get(1).get(ob);
					}else if (method_local.get(c_n).get(m_n).get(0).containsKey(ob) && class_name.contains(method_local.get(c_n).get(m_n).get(0).get(ob))) {
						ob_t = method_local.get(c_n).get(m_n).get(0).get(ob);
					}else if (variable.get(c_n).containsKey(ob) && class_name.contains(variable.get(c_n).get(ob))) {
						ob_t = variable.get(c_n).get(ob);
					}else {
						System.out.println("Type error");
						System.exit(0);
					}
				}
			}
			if (!method.get(ob_t).containsKey(c_method)) {
				System.out.println("Type error");
				System.exit(0);
			}
			String ret_t = method.get(ob_t).get(c_method);
			for (String arg : method_local.get(ob_t).get(c_method).get(0).keySet()) {
				argu_T.add(method_local.get(ob_t).get(c_method).get(0).get(arg));
			}
			n.f4.accept(this);
			expType[0] = ret_t;
			argu_T.clear();
			MS_cn[0] = null;
			MS_mn[0] = null;
		}
		
		public void visit(ExpressionList n) {
			arg_counter = 0;
			PexpType[0] = null;
			expType[0] = null;
			String ms_cn = MS_cn[0];
			String ms_mn = MS_mn[0];
			n.f0.accept(this);
			if (expType[0] != null) {
				if (expType[0].equals("int") || expType[0].equals("int[]") || expType[0].equals("boolean")) {
					if (!expType[0].equals(argu_T.get(arg_counter))) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
			}else {
				String v = PexpType[0];
				if (ms_cn.equals(M)) {
					if (!variable.get(ms_cn).containsKey(v)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!variable.get(ms_cn).get(v).equals(argu_T.get(arg_counter))) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else {
					if (method_local.get(ms_cn).get(ms_mn).get(1).containsKey(v) && !method_local.get(ms_cn).get(ms_mn).get(1).get(v).equals(argu_T.get(arg_counter))) {
						System.out.println("Type error");
						System.exit(0);
					}else if (method_local.get(ms_cn).get(ms_mn).get(0).containsKey(v) && !method_local.get(ms_cn).get(ms_mn).get(0).get(v).equals(argu_T.get(arg_counter))) {
						System.out.println("Type error");
						System.exit(0);
					}else if (variable.get(ms_cn).containsKey(v) && !variable.get(ms_cn).get(v).equals(argu_T.get(arg_counter))) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!method_local.get(ms_cn).get(ms_mn).get(1).containsKey(v) && !variable.get(ms_cn).containsKey(v) && !method_local.get(ms_cn).get(ms_mn).get(0).containsKey(v)) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
			}
			n.f1.accept(this);
		}
		
		public void visit(ExpressionRest n) {
			arg_counter++;
			PexpType[0] = null;
			expType[0] = null;
			String ms_cn = MS_cn[0];
			String ms_mn = MS_mn[0];
			n.f1.accept(this);
			if (expType[0] != null) {
				if (expType[0].equals("int") || expType[0].equals("int[]") || expType[0].equals("boolean")) {
					if (!expType[0].equals(argu_T.get(arg_counter))) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
			}else {
				String v = PexpType[0];
				if (ms_cn.equals(M)) {
					if (!variable.get(ms_cn).containsKey(v)) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!variable.get(ms_cn).get(v).equals(argu_T.get(arg_counter))) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else {
					if (method_local.get(ms_cn).get(ms_mn).get(1).containsKey(v) && !method_local.get(ms_cn).get(ms_mn).get(1).get(v).equals(argu_T.get(arg_counter))) {
						System.out.println("Type error");
						System.exit(0);
					}else if (method_local.get(ms_cn).get(ms_mn).get(0).containsKey(v) && !method_local.get(ms_cn).get(ms_mn).get(0).get(v).equals(argu_T.get(arg_counter))) {
						System.out.println("Type error");
						System.exit(0);
					}else if (variable.get(ms_cn).containsKey(v) && !variable.get(ms_cn).get(v).equals(argu_T.get(arg_counter))) {
						System.out.println("Type error");
						System.exit(0);
					}else if (!method_local.get(ms_cn).get(ms_mn).get(1).containsKey(v) && !variable.get(ms_cn).containsKey(v) && !method_local.get(ms_cn).get(ms_mn).get(0).containsKey(v)) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
			}
		}

		public void visit(AssignmentStatement n) {
			String v = n.f0.f0.toString();
			String c_n = current_class[0];
			String m_n = current_m[0];
			n.f2.accept(this);
			String expT = expType[0];            
			if (c_n.equals(M)) {
				if (!variable.get(c_n).containsKey(v)) {
					System.out.println("Type error");
					System.exit(0);
				}
				if (!variable.get(c_n).get(v).equals(expT)) {
					System.out.println("Type error");
					System.exit(0);
				}
			}else {
				if (method_local.get(c_n).get(m_n).get(1).containsKey(v)) {
					if (!method_local.get(c_n).get(m_n).get(1).get(v).equals(expT)) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v)) {
					if (!method_local.get(c_n).get(m_n).get(0).get(v).equals(expT)) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else if (variable.get(c_n).containsKey(v)) {
					if (!variable.get(c_n).get(v).equals(expT)) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else if (!method_local.get(c_n).get(m_n).get(1).containsKey(v) && !method_local.get(c_n).get(m_n).get(0).containsKey(v) && !variable.get(c_n).containsKey(v)) {
					System.out.println("Type error");
					System.exit(0);
				}
			}
			expType[0] = null;
			PexpType[0] = null;
		}

		public void visit(ArrayAssignmentStatement n) {
			String v = n.f0.f0.toString();
			String c_n = current_class[0];
			String m_n = current_m[0];
			if (c_n.equals(M)) {
				if (variable.get(c_n).containsKey(v)) {
					if (!variable.get(c_n).get(v).equals("int[]")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else {
					System.out.println("Type error");
					System.exit(0);
				}
			}else {
				if (method_local.get(c_n).get(m_n).get(1).containsKey(v)) {
					if (!method_local.get(c_n).get(m_n).get(1).get(v).equals("int[]")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else if (method_local.get(c_n).get(m_n).get(0).containsKey(v)) {
					if (!method_local.get(c_n).get(m_n).get(0).get(v).equals("int[]")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else if (variable.get(c_n).containsKey(v)) {
					if (!variable.get(c_n).get(v).equals("int[]")) {
						System.out.println("Type error");
						System.exit(0);
					}
				}else if (!method_local.get(c_n).get(m_n).get(1).containsKey(v) && !method_local.get(c_n).get(m_n).get(0).containsKey(v) && !variable.get(c_n).containsKey(v)) {
					System.out.println("Type error");
					System.exit(0);
				}
			}
			if (!(n.f2.f0.choice instanceof PrimaryExpression)) {
				n.f2.accept(this);
				String expT1 = expType[0];
				if (!expT1.equals("int")) {
					System.out.println("Type error");
					System.exit(0);
				}
			}else {
				n.f2.accept(this);
				String expT1 = expType[0];
				String PexpT1 = PexpType[0];
				if (expT1.equals(PexpT1) && !expT1.equals("int")) {
					System.out.println("Type error");
					System.exit(0);
				}else if (!expT1.equals(PexpT1)){
					if (c_n.equals(M)) {
						if (variable.get(c_n).containsKey(PexpT1)) {
							if (!variable.get(c_n).get(PexpT1).equals("int")) {
								System.out.println("Type error");
								System.exit(0);
							}
						}else {
							System.out.println("Type error");
							System.exit(0);
						}
					}else {
						if (method_local.get(c_n).get(m_n).get(1).containsKey(PexpT1)) {
							if (!method_local.get(c_n).get(m_n).get(1).get(PexpT1).equals("int")) {
								System.out.println("Type error");
								System.exit(0);
							}
						}else if (method_local.get(c_n).get(m_n).get(0).containsKey(PexpT1)) {
							if (!method_local.get(c_n).get(m_n).get(0).get(PexpT1).equals("int")) {
								System.out.println("Type error");
								System.exit(0);
							}
						}else if (variable.get(c_n).containsKey(PexpT1)) {
							if (!variable.get(c_n).get(PexpT1).equals("int")) {
								System.out.println("Type error");
								System.exit(0);
							}
						}else if (!variable.get(c_n).containsKey(PexpT1) && !method_local.get(c_n).get(m_n).get(0).containsKey(PexpT1) && method_local.get(c_n).get(m_n).get(1).containsKey(PexpT1)) {
							System.out.println("Type error");
							System.exit(0);
						}
					}
				}
			}
			if (!(n.f5.f0.choice instanceof PrimaryExpression)) {
				n.f5.accept(this);
				String expT2 = expType[0];
				if (!expT2.equals("int")) {
					System.out.println("Type error");
					System.exit(0);
				}
			}else {
				n.f5.accept(this);
				String expT2 = expType[0];
				String PexpT2 = PexpType[0];
				if (expT2.equals(PexpT2) && !expT2.equals("int")) {
					System.out.println("Type error");
					System.exit(0);
				}else if (!expT2.equals(PexpT2)){
					if (c_n.equals(M)) {
						if (variable.get(c_n).containsKey(PexpT2)) {
							if (!variable.get(c_n).get(PexpT2).equals("int")) {
								System.out.println("Type error");
								System.exit(0);
							}
						}else {
							System.out.println("Type error");
							System.exit(0);
						}
					}else {
						if (method_local.get(c_n).get(m_n).get(1).containsKey(PexpT2)) {
							if (!method_local.get(c_n).get(m_n).get(1).get(PexpT2).equals("int")) {
								System.out.println("Type error");
								System.exit(0);
							}
						}else if (method_local.get(c_n).get(m_n).get(0).containsKey(PexpT2)) {
							if (!method_local.get(c_n).get(m_n).get(0).get(PexpT2).equals("int")) {
								System.out.println("Type error");
								System.exit(0);
							}
						}else if (variable.get(c_n).containsKey(PexpT2)) {
							if (!variable.get(c_n).get(PexpT2).equals("int")) {
								System.out.println("Type error");
								System.exit(0);
							}
						}else if (!variable.get(c_n).containsKey(PexpT2) && !method_local.get(c_n).get(m_n).get(0).containsKey(PexpT2) && method_local.get(c_n).get(m_n).get(1).containsKey(PexpT2)) {
							System.out.println("Type error");
							System.exit(0);
						}
					}
				}
			}
			expType[0] = null;
			PexpType[0] = null;
		}

		public void visit(IfStatement n) {
			n.f2.accept(this);
			String expT = expType[0];
			if (!expT.equals("boolean")) {
				System.out.println("Type error");
				System.exit(0);
			}
			n.f4.accept(this);
			n.f6.accept(this);
			expType[0] = null;
			PexpType[0] = null;
		}

		public void visit(WhileStatement n) {
			n.f2.accept(this);
			String expT = expType[0];
			if (!expT.equals("boolean")) {
				System.out.println("Type error");
				System.exit(0);
			}
			n.f4.accept(this);
			expType[0] = null;
			PexpType[0] = null;
		}
		
	}
	
	public static void main(String[] args) {
		new MiniJavaParser(System.in);
		try {
			Goal g = MiniJavaParser.Goal();
			F_visitor v1 = new F_visitor();
			v1.visit(g);
			for (String cla : class_name) {
				for (String key : variable.get(cla).keySet()) {
					if (!variable.get(cla).get(key).equals("int") && !variable.get(cla).get(key).equals("int[]") && !variable.get(cla).get(key).equals("boolean") && !class_name.contains(variable.get(cla).get(key))) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
				if (cla.equals(M)) {
					continue;
				}
				for (String key : method.get(cla).keySet()) {
					if (!method.get(cla).get(key).equals("int") && !method.get(cla).get(key).equals("int[]") && !method.get(cla).get(key).equals("boolean") && !class_name.contains(method.get(cla).get(key))) {
						System.out.println("Type error");
						System.exit(0);
					}
				}
			}
			S_visitor v2 = new S_visitor();
			v2.visit(g);
			T_visitor v3 = new T_visitor();
			v3.visit(g);
			System.out.println("Program type checked");
			
		}
		catch(ParseException e) {
			e.printStackTrace();
		    System.exit(-1);
		}
	}
	
}
