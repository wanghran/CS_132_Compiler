import cs132.vapor.ast.VBuiltIn;

public class Instruction {

	public static String add(VBuiltIn v) {

		String code = "";

		if (v.args[0].toString().startsWith("$")){
			if (v.args[1].toString().startsWith("$")){
				code += MIPS_instr.add(v.dest.toString(), v.args[0].toString(), v.args[1].toString());
			}else {
				code += MIPS_instr.addi(v.dest.toString(), v.args[0].toString(), v.args[1].toString());
			}
		}else {
			if (v.args[1].toString().startsWith("$")){
				code += MIPS_instr.addi(v.dest.toString(), v.args[1].toString(), v.args[0].toString());
			}else {
				code += MIPS_instr.li("$t9", v.args[0].toString()) + "\n";
				code += MIPS_instr.addi(v.dest.toString(), "$t9", v.args[1].toString());
			}
		}
		return code;
	}

	public static String sub(VBuiltIn v) {

		String code = "";

		if (v.args[0].toString().startsWith("$")){
			if (v.args[1].toString().startsWith("$")){
				code = MIPS_instr.sub(v.dest.toString(), v.args[0].toString(), v.args[1].toString());
			}else {
				code += MIPS_instr.li("$t9", v.args[1].toString()) + "\n";
				code += MIPS_instr.sub(v.dest.toString(), v.args[0].toString(), "$t9"); 
			}
		}else {
			if (v.args[1].toString().startsWith("$")){
				code += MIPS_instr.li("$t9", v.args[0].toString()) + "\n";
				code += MIPS_instr.sub(v.dest.toString(), v.args[1].toString(), "$t9"); ;
			}else {
				String value = String.valueOf((Integer.valueOf(v.args[0].toString()) - 
							Integer.valueOf(v.args[1].toString()))); 
				code += MIPS_instr.addi(v.dest.toString(), "$zero", value);
			}
		}
		return code;
	}

	public static String muls(VBuiltIn v) {

		String code = "";

		if (v.args[0].toString().startsWith("$")){
			if (v.args[1].toString().startsWith("$")){
				code += MIPS_instr.mult(v.dest.toString(),  v.args[0].toString(), v.args[1].toString());
			}else {
				code += MIPS_instr.li("$t9", v.args[1].toString()) + "\n";
				code += MIPS_instr.mult(v.dest.toString(), v.args[0].toString(), "$t9"); 
			}
		}else {
			if (v.args[1].toString().startsWith("$")){
				code += MIPS_instr.li("$t9", v.args[0].toString()) + "\n";
				code += MIPS_instr.mult(v.dest.toString(),  v.args[1].toString(), "$t9"); 
			}else {
				String value = String.valueOf((Integer.valueOf(v.args[0].toString()) * 
							Integer.valueOf(v.args[1].toString())));
				code += MIPS_instr.li(v.dest.toString(), value);
			}
		}

		return code;
	}

	public static String eq(VBuiltIn v) {

		String code = "";

		if (v.args[0].toString().startsWith("$")){
			if (v.args[1].toString().startsWith("$")){
				code += MIPS_instr.xor("$t9", v.args[0].toString(), v.args[1].toString())+ "\n";
				code += MIPS_instr.sltiu(v.dest.toString(), "$t9", "1");
			}else {
				code += MIPS_instr.xori("$t9", v.args[0].toString(), v.args[1].toString()) + "\n";
				code += MIPS_instr.sltiu(v.dest.toString(), "$t9", "1");
			}
		}else {
			if (v.args[1].toString().startsWith("$")){
				code += MIPS_instr.xori("$t9", v.args[1].toString(), v.args[0].toString()) + "\n";
				code += MIPS_instr.sltiu(v.dest.toString(), "$t9", "1");
			}else {
				String value = (Integer.valueOf(v.args[0].toString()) == Integer.valueOf(v.args[1].toString()) 
									? "1" : "0") + "\n";
				code += MIPS_instr.li(v.dest.toString(), value);
			}
		}
		return code;
	} 

	public static String lt(VBuiltIn v) {

		String code = "";

		if (v.args[0].toString().startsWith("$")){
			if (v.args[1].toString().startsWith("$")){
				code += MIPS_instr.sltu(v.dest.toString(), v.args[0].toString(), v.args[1].toString());
			}else {
				code += MIPS_instr.sltiu(v.dest.toString(), v.args[0].toString(), v.args[1].toString());
			}
		}else {
			if (v.args[1].toString().startsWith("$")){
				code += MIPS_instr.li("$t9", v.args[0].toString()) + "\n";
				code += MIPS_instr.sltu(v.dest.toString(), "$t9", v.args[1].toString());
			}else {
				String value = (Integer.valueOf(v.args[0].toString()) < Integer.valueOf(v.args[1].toString()) 
									? "1" : "0") + "\n";
				code += MIPS_instr.li(v.dest.toString(), value);
			}
		}

		return code;
	}

	public static String lts(VBuiltIn v) {

		String code = "";

		if (v.args[0].toString().startsWith("$")){
			if (v.args[1].toString().startsWith("$")){
				code += MIPS_instr.slt(v.dest.toString(), v.args[0].toString(), v.args[1].toString());
			}else {
				code += MIPS_instr.slti(v.dest.toString(), v.args[0].toString(), v.args[1].toString());
			}

		}else {
			if (v.args[1].toString().startsWith("$")){
				code += MIPS_instr.li("$t9", v.args[0].toString()) + "\n";
				code += MIPS_instr.slt(v.dest.toString(), "$t9", v.args[1].toString());

			}else {
				String value = (Integer.valueOf(v.args[0].toString()) < Integer.valueOf(v.args[1].toString()) 
									? "1" : "0") + "\n";
				code += MIPS_instr.li(v.dest.toString(), value);
			}
		}

		return code;
	}

	public static String heapAlloc(VBuiltIn v) {

		String code = "";

		if (v.args[0].toString().startsWith("$"))
			code += MIPS_instr.cp("$a0", v.args[0].toString()) + "\n";
		else {
			code += MIPS_instr.li("$a0", v.args[0].toString()) + "\n";
		}
		
		code += "  jal _heapAlloc\n";
		code += MIPS_instr.cp(v.dest.toString(), "$v0");

		return code;
	} 

	public static String print(VBuiltIn v) {

		String code = "";

		if (v.args[0].toString().startsWith("$")){
			code += MIPS_instr.cp("$a0", v.args[0].toString()) + "\n";
		}else {
			code += MIPS_instr.li("$a0", v.args[0].toString()) + "\n";
		}
		code += "  jal _print";
		return code;
	}

}