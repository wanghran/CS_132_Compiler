import java.util.Scanner;
import java.util.regex.*;


public class Parse { 
  
    public static void main (String [] args) {
    	crt ct = new crt();
    	String re = "";
		
    	Scanner sc = new Scanner(System.in);
    	//scan the whole file into a string without space
    	while(sc.hasNext()) {
    		String buf = sc.nextLine();
    		re += buf;
    	}
    	sc.close();
    	ct.write(re);
    	if (!ct.flag) {
    		ct.S();
    	}
    	if ((ct.token.current == ct.token.tlist.size()) && ct.ecall == 0) {
    		System.out.println("Program parsed successfully");
    	}else if (!ct.flag){
    		System.out.println("Parse error");
    	}
    	

    	
    	
    	
      
      
      
      
    }

}
