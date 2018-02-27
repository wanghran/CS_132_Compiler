public class MIPS_instr {

	public static String addi (String Rdest, String Rsrc1, String immd){
		checkType_reg(Rdest);
		checkType_reg(Rsrc1);
		checkType_immd(immd);
		return "  addi " + Rdest + " " + Rsrc1 + " " + immd; 
	}

	public static String add (String Rdest, String Rsrc1, String Rsrc2){
		checkType_reg(Rdest);
		checkType_reg(Rsrc1);
		checkType_reg(Rsrc2);
		return "  add " + Rdest + " " + Rsrc1 + " " + Rsrc2; 
	}

	public static String sub (String Rdest, String Rsrc1, String Rsrc2){
		checkType_reg(Rdest);
		checkType_reg(Rsrc1);
		checkType_reg(Rsrc2);
		return "  sub " + Rdest + " " + Rsrc1 + " " + Rsrc2; 
	}

	public static String mult (String Rdest, String Rsrc1, String Rsrc2){
		checkType_reg(Rdest);
		checkType_reg(Rsrc1);
		checkType_reg(Rsrc2);
		return "  mul " + Rdest + " " + Rsrc1 + " " + Rsrc2; 
	}

	public static String slt (String Rdest, String Rsrc1, String Src2){
		checkType_reg(Rdest);
		checkType_reg(Rsrc1);
		checkType_reg(Src2);
		return "  slt " + Rdest + " " + Rsrc1 + " " + Src2; 
	}

	public static String slti (String Rdest, String Rsrc1, String immd){
		return "  slti " + Rdest + " " + Rsrc1 + " " + immd; 
	}

	public static String sltu (String Rdest, String Rsrc1, String Src2){
		checkType_reg(Rdest);
		checkType_reg(Rsrc1);
		checkType_reg(Src2);
		return "  sltu " + Rdest + " " + Rsrc1 + " " + Src2; 
	}

	public static String sltiu (String Rdest, String Rsrc1, String immd){
		return "  sltiu " + Rdest + " " + Rsrc1 + " " + immd; 
	}

	public static String li (String Rdest, String immd){
		checkType_reg(Rdest);
		checkType_immd(immd);
		return "  li " + Rdest + " " + immd;
	}

	public static String cp (String Rdest, String Rsrc1){
		checkType_reg(Rdest);
		checkType_reg(Rsrc1);
		return "  addi " + Rdest + " " + Rsrc1 + " 0";
	}

	public static String la (String Rdest, String label){
		return "  la " + Rdest + " " + label; 
	}

	public static String lw (String Rdest, String Rsrc1, String immd){
		checkType_reg(Rdest);
		checkType_reg(Rsrc1);
		checkType_immd(immd);
		return "  lw " + Rdest + " " + immd + "(" + Rsrc1 +")"; 
	}

	public static String sw (String Rdest, String Rsrc1, String immd){
		checkType_reg(Rdest);
		checkType_reg(Rsrc1);
		checkType_immd(immd);
		return "  sw " + Rdest + " " + immd + "(" + Rsrc1 +")"; 
	}

	public static String xor (String Rdest, String Rsrc1, String Rsrc2){
		return "  xor " + Rdest + " " + Rsrc1 + " " + Rsrc2; 
	}
	public static String xori (String Rdest, String Rsrc1, String immd){
		return "  xori " + Rdest + " " + Rsrc1 + " " + immd; 
	}

	public static void checkType_reg(String var){
		if (!var.contains("$"))
			System.out.println("Error operand type: " + var);
	}

	public static void checkType_immd(String var){
		if (var.contains("$"))
			System.out.println("Error operand type " + var);
	}
} 