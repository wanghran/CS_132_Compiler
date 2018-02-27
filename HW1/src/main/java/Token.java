import java.util.*;
public class Token {

	public LinkedList<String> tlist;
	public int current;
	
	public Token() {
		tlist = new LinkedList<String>();
		current = 0;
	}
	
	public String get() {
		return tlist.get(current);
	}
	
	public String next() {
		return tlist.get(current++);
	}
	
	public void add(String x) {
		tlist.add(x);
	}

}
