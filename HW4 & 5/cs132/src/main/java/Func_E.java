import java.util.*;
import cs132.vapor.ast.VFunction;

public class Func_E {

	public static final int MAX_REG = 8;
 	public String func_name; 
	private VFunction vf;
	private HashMap<String, Integer> stack;
	private HashMap<String, Integer> reg; 
	private Integer stack_num;
	private Integer reg_num;
	public String code; 
	public LinkedList<String> params;
	public int out_num;
	public int line;
	
	public HashMap<String, Integer> var_left;
	public HashMap<String, Integer> var_right;  
	public LinkedList<String> sub_call;
	public LinkedList<String> assigned_reg;

	public Integer local;
	public Integer out;
	public Integer in;
	public LinkedList<String> error_message;

	public Func_E(VFunction vf) {
		
		this.vf = vf;
		func_name = vf.ident;
		stack = new HashMap<String, Integer>();
		reg = new HashMap<String, Integer>();
		stack_num = 0;
		reg_num = 0;
		code = "";
		params = new LinkedList<String>();
		out_num = 0;
		line = 0;

		var_left = new HashMap<String, Integer>();
		var_right = new HashMap<String, Integer>();
		sub_call = new LinkedList<String>();
		assigned_reg = new LinkedList<String>();

		error_message = new LinkedList<String>();
	}

	public void addCode(String line) {
		code = code + "\n" + line;
	}

	public void updateError(Func_E prev) {
		if (prev != null) {
			for (String str : prev.error_message) {
				this.error_message.add(str);
			}
		}
	}

	public void addError(String err) {
		error_message.add(err);
	}

}