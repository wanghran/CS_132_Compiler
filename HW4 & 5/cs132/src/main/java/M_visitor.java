import java.util.*;
import cs132.vapor.ast.*;
import cs132.vapor.ast.VAssign;
import cs132.vapor.ast.VBranch;
import cs132.vapor.ast.VBuiltIn;
import cs132.vapor.ast.VCall;
import cs132.vapor.ast.VGoto;
import cs132.vapor.ast.VMemRead;
import cs132.vapor.ast.VMemRef.Global;
import cs132.vapor.ast.VMemRef.Stack;
import cs132.vapor.ast.VMemWrite;
import cs132.vapor.ast.VReturn;

public class M_visitor extends VInstr.VisitorR<Object, Exception> {

	public static Func_E fe;

	public M_visitor(Func_E curr) {
		fe = curr;
	}

	public Object visit(VAssign va) throws Exception {

		if (va.source.toString().startsWith("$")){
			fe.addCode(MIPS_instr.cp(va.dest.toString(), va.source.toString()));
		}else if (va.source.toString().startsWith(":")){
			fe.addCode(MIPS_instr.la(va.dest.toString(), va.source.toString().substring(1)));
		}else {
			fe.addCode(MIPS_instr.li(va.dest.toString(), va.source.toString()));
		}
		fe.line++;
		return null;
	}

	public Object visit(VBuiltIn vb) throws Exception {

		switch (vb.op.name) {
			case "Add": 
				fe.addCode(Instruction.add(vb)); 
				break;
			case "Sub": 
				fe.addCode(Instruction.sub(vb)); 
				break;
			case "MulS":
				fe.addCode(Instruction.muls(vb)); 
				break;
			case "Eq":
				fe.addCode(Instruction.eq(vb)); 
				break;	
			case "Lt":
				fe.addCode(Instruction.lt(vb)); 
				break;
			case "LtS":
				fe.addCode(Instruction.lts(vb)); 
				break;
			case "PrintIntS":
				fe.addCode(Instruction.print(vb)); 
				break;
			case "HeapAllocZ":
				fe.addCode(Instruction.heapAlloc(vb)); 
				break;
			case "Error":
				Integer ind = fe.error_message.indexOf(vb.args[0].toString());
				if (ind == -1){
					fe.addError(vb.args[0].toString());
					ind = fe.error_message.indexOf(vb.args[0].toString());
				}
				fe.addCode("  la $a0 _str" + Integer.toString(ind));
				fe.addCode("  j _error");
				break;
			default: 
				System.out.println("Invalid operation");
		}
		fe.line++;
		return null;
	}

	public Object visit(VMemRead vr) throws Exception {

		if (Global.class.isInstance(vr.source)){
			VMemRef.Global source = (VMemRef.Global) vr.source;
			fe.addCode(MIPS_instr.lw(vr.dest.toString(), source.base.toString(), Integer.toString(source.byteOffset)));
			fe.line++;
			return null;
		}else if (Stack.class.isInstance(vr.source)) {
			VMemRef.Stack source = (VMemRef.Stack) vr.source;
			if (source.region == VMemRef.Stack.Region.Local){
				Integer ind = (fe.in + source.index) * 4; 
				fe.addCode(MIPS_instr.lw(vr.dest.toString(), "$sp" ,Integer.toString(ind)));
			}else if (source.region == VMemRef.Stack.Region.In){
				Integer ind = source.index * 4; 
				fe.addCode(MIPS_instr.lw(vr.dest.toString(), "$fp" ,Integer.toString(ind)));
			}else {
				System.out.println("Memory read error");
			}
		}
		fe.line++;
		return null;
	}

	public Object visit(VMemWrite vw) throws Exception {

		String r = "";

		if (vw.source.toString().startsWith("$")){
			r =  vw.source.toString();
		}else if (vw.source.toString().startsWith(":")){
			fe.addCode(MIPS_instr.la("$t9", vw.source.toString().substring(1))); 
			r = "$t9";
		}else {
			fe.addCode(MIPS_instr.li("$t9", vw.source.toString()));
			r = "$t9";
		}

		if (Stack.class.isInstance(vw.dest)) {
			VMemRef.Stack dest = (VMemRef.Stack) vw.dest;

			if (dest.region == VMemRef.Stack.Region.Local){
				Integer ind = (fe.out + dest.index) * 4; 
				fe.addCode(MIPS_instr.sw(r, "$sp", Integer.toString(ind)));
			}else if (dest.region == VMemRef.Stack.Region.Out){
				Integer ind = dest.index * 4; 
				fe.addCode(MIPS_instr.sw(r, "$sp", Integer.toString(ind)));
			}else {
				System.out.println("Memory write fail");
			}
			fe.line++;
			return null;
		}

		if (Global.class.isInstance(vw.dest)){
			VMemRef.Global dest = (VMemRef.Global) vw.dest;
			fe.addCode(MIPS_instr.sw(r, dest.base.toString(), Integer.toString(dest.byteOffset)));
			fe.line++; 
			return null;
		}

		return null;
	}

	public Object visit(VReturn r) throws Exception {
		if (r.value != null) {
			fe.addCode(MIPS_instr.cp("$v0", r.value.toString() ));
		}
		fe.addCode("  jr $ra");

		return null;
	}

	public Object visit(VCall vc) throws Exception {

		if (vc.addr.toString().startsWith(":")) {
			fe.addCode("  jal " +  vc.addr.toString().substring(1));
		}else {
			fe.addCode("  jalr " + vc.addr.toString());
		}

		fe.line++;
		return null;
	}

	public Object visit(VGoto vg) throws Exception {
		fe.addCode("  j " + vg.target.toString().substring(1));
		fe.line++;
		return null;
	}

	public Object visit(VBranch b) throws Exception {
		if (b.positive) {
			fe.addCode("  bnez " + b.value.toString() + " " + b.target.ident);
		}else {
			fe.addCode("  beqz " + b.value.toString() + " " + b.target.ident);
		}

		fe.line++; 
		return null;
	}
}