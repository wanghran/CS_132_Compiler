import java.io.*;
import java.util.*; 

import cs132.util.ProblemException;
import cs132.vapor.parser.VaporParser;
import cs132.vapor.ast.*;
import cs132.vapor.ast.VBuiltIn.Op;

public class VM2M {

	public static void main(String args[]) {
		try {
			VaporProgram program = parseVapor(System.in, System.err); 
			System.out.println(".data");
			if (program.dataSegments != null) {
				for (VDataSegment data: program.dataSegments) {
					System.out.println(data.ident + ":");
					if (data.values != null) {
						for (VOperand.Static v: data.values) {
							System.out.println("  " + v.toString().substring(1));
						}
					}
				}
			}
			System.out.println("");
			System.out.println(".text");
			System.out.println("  jal Main" + "\n" + "  li $v0 10" + "\n" + "  syscall" + "\n");

			LinkedList<Func_E> all = new LinkedList<Func_E>();
			Func_E prev_fe = null;

			for (VFunction func : program.functions) {

				Func_E fe = new Func_E(func);
				fe.local = func.stack.local; 
				fe.out = func.stack.out;
				fe.in = func.stack.in;
				fe.updateError(prev_fe);
				Integer stack_size =  4 * (func.stack.local + func.stack.out) + 8;
				fe.addCode(func.ident+":");
				fe.addCode("  sw $fp -8($sp)"); 
				fe.addCode("  addi $fp $sp 0");
				fe.addCode("  addi $sp $sp -" + stack_size);
				fe.addCode("  sw $ra -4($fp)");

				M_visitor visitor = new M_visitor(fe);
				for (VInstr ins : func.body) {
					for (int i = 0; i < func.labels.length; i++) {
						if (func.labels[i].instrIndex == fe.line) {
							fe.addCode(func.labels[i].ident + ": ");
						}
					}

					if (ins == func.body[func.body.length - 1]) {
						fe.addCode("  lw $ra -4($fp)");
						fe.addCode("  lw $fp -8($fp)");
						fe.addCode("  addi $sp $sp " + stack_size);
					}

					try {
						ins.accept(visitor);
					}catch (Exception e) {
						System.out.println("Current func is " + fe.func_name + 
							" Current instruciton is " + ins + " at " + fe.line);
						System.out.println("Failed to parse current instruction");
					}
				}
				prev_fe = fe;
				all.add(fe);
			}

			for (Func_E fe : all) {
				System.out.println(fe.code);
			}

			System.out.println(" ");

			System.out.println(
				"_print:\n" +
				"  li $v0 1\n" +
				"  syscall\n" +
				"  la $a0 _newline\n" +
				"  li $v0 4\n" +
				"  syscall\n" +
				"  jr $ra\n\n"
			); 

			System.out.println(
				"_heapAlloc:\n" +
				"  li $v0 9\n" +
				"  syscall\n" +
				"  jr $ra\n\n"
			);

			System.out.println(
				"_error:\n" +
				"  li $v0 4\n" +
				"  syscall\n" +
				"  li $v0 10\n" +
				"  syscall\n\n"
			);

			System.out.print(".data\n.align 0\n");
			System.out.print("_newline: .asciiz \"\\n\"\n");

			Func_E lastOne = all.getLast();
			for (String str : lastOne.error_message) {
				System.out.println("_str" + lastOne.error_message.indexOf(str) + ": .asciiz " + str + "\"\\n\"");
			}

		}catch (IOException e) {
			System.out.println("Parse Error");
		}

	}


	public static VaporProgram parseVapor(InputStream in, PrintStream err) throws IOException
	{
  		Op[] ops = {Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
    		Op.PrintIntS, Op.HeapAllocZ, Op.Error,};
  		boolean allowLocals = false;
  		String[] registers = {
   			"v0", "v1",
    		"a0", "a1", "a2", "a3",
    		"t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
    		"s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
    		"t8",
  		};
  		boolean allowStack = true;

  		VaporProgram program;
  		try {
    		program = VaporParser.run(new InputStreamReader(in), 1, 1,
                              java.util.Arrays.asList(ops),
                              allowLocals, registers, allowStack);
  		}
  		catch (ProblemException ex) {
    		err.println(ex.getMessage());
    		return null;
  		}

  		return program;
	}
}