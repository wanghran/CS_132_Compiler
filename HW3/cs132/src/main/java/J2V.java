import syntaxtree.*;
import visitor.*;

import java.sql.Time;
import java.util.*;


public class J2V {
    public static void main(String[] args) {
        try {
            Node root = new MiniJavaParser(System.in).Goal();
            //System.out.println("Parse successful");

            //Load all classes
            List<ClassType> classList = new ArrayList<ClassType>();

            ClassVisitor cv = new ClassVisitor();
            root.accept(cv, classList);

            //Load extends class
            ClassExtendsVisitor cev = new ClassExtendsVisitor();
            root.accept(cev, classList);

            //Load all methods to classes
            MethodVisitor mv = new MethodVisitor(classList);
            root.accept(mv, null);

            //Fields load
            FieldVisitor fv = new FieldVisitor(classList);
            root.accept(fv, null);

            VaporPaser v = new VaporPaser(classList);

            vmtBuild builder = new vmtBuild(classList);
            builder.vmtPrint();

            J2V_visitor jv = new J2V_visitor(v);
            root.accept(jv);
//            ClassPrinter printer = new ClassPrinter(classList);
//            root.accept(printer, classList);

        }catch (ParseException e) {
            Helper.exit(0);
        }
    }

}


//generic type for mini java
class GType {

    public static GType getType(Type n, List<ClassType> classList) {
        GType obj;
        if (n.f0.choice instanceof IntegerType) {
            obj = new IntType();
        } else if (n.f0.choice instanceof BooleanType) {
            obj = new BoolType();
        } else if (n.f0.choice instanceof syntaxtree.ArrayType) {
            obj = new ArrayType();
        } else if (n.f0.choice instanceof Identifier) {
            obj = Helper.getClass(((Identifier)(n.f0.choice)).f0.toString(), classList);
        } else {
            obj = null;
        }
        return obj;
    }

}

class ClassType extends GType{
    public String class_name;
    public ClassType super_class;
    public List<Method> methods = new ArrayList<Method>();
    public List<GType> fields = new ArrayList<GType>();
    public List<String> fields_name = new ArrayList<String>();
    public boolean main = false;
    String register_loc = null;

    ClassType(String class_name){
        this.class_name = class_name;
        this.super_class=null;
    }

    public String toString() {
        return class_name;
    }

}

class Method extends GType{
    String method_name;
    List<GType> args = new ArrayList<GType>();
    List<String> args_name = new ArrayList<String>();
    List<GType> vars = new ArrayList<GType>();
    List<String> vars_name = new ArrayList<String>();
    List<String> real_args = new ArrayList<>();
    GType return_value;

    Method(String method_name){
        this.method_name = method_name;
    }

    public String toString() {
        return method_name;
    }
}

class ClassVisitor extends GJVoidDepthFirst<List<ClassType>>{

    public void visit(MainClass n, List<ClassType> classList) {
        String cname = n.f1.f0.toString();
        ClassType newclass = new ClassType(cname);
        newclass.main = true;
        classList.add(newclass);
        Method main = new Method("main");
        newclass.methods.add(main);
    }

    public void visit(ClassDeclaration n, List<ClassType> classList) {
        String cname = n.f1.f0.toString();
        ClassType newclass = new ClassType(cname);
        classList.add(newclass);
    }


    public void visit(ClassExtendsDeclaration n, List<ClassType> classList) {
        String cname = n.f1.f0.toString();
        ClassType newclass = new ClassType(cname);
        classList.add(newclass);
    }

}

class ClassExtendsVisitor extends GJVoidDepthFirst<List<ClassType>> {

    public void visit(ClassExtendsDeclaration n, List<ClassType> classList) {
        String cname = n.f1.f0.toString();
        String super_cname = n.f3.f0.toString();
        ClassType super_class = Helper.getClass(super_cname, classList);
        ClassType curr_class = Helper.getClass(cname, classList);
        curr_class.super_class = super_class;
    }
}

class MethodVisitor extends GJVoidDepthFirst<List<GType>>{

    List<ClassType> classList;
    private String className;
    private String methodName;

    MethodVisitor(List<ClassType> classList){
        this.classList = classList;
    }

    public void visit(Goal n,  List<GType> list) {
        n.f1.accept(this, list);
    }

    public void visit(ClassDeclaration n, List<GType> list) {
        this.className = n.f1.f0.toString();
        n.f4.accept(this, list);
    }

    public void visit(ClassExtendsDeclaration n, List<GType> list) {
        this.className = n.f1.f0.toString();
        n.f6.accept(this, list);
    }

    public void visit(MethodDeclaration n, List<GType> list) {

        ClassType curr_class = Helper.getClass(className,classList);
        this.methodName = n.f2.f0.toString();
        Method m = new Method(methodName);
        curr_class.methods.add(m);

        GType returnVal = GType.getType(n.f1, classList);
        m.return_value = returnVal;
        //FormalParameterList
        n.f4.accept(this, list);
        //VarDeclaration
        n.f7.accept(this, list);
    }

    public void visit(VarDeclaration n, List<GType> list) {
        ClassType curr_class = Helper.getClass(this.className, classList);
        Method curr_method = Helper.getMethod(methodName, curr_class);

        GType new_var = GType.getType(n.f0, classList);
        curr_method.vars.add(new_var);
        curr_method.vars_name.add(n.f1.f0.toString());
    }

    public void visit(FormalParameter n, List<GType> list){
        ClassType curr_class = Helper.getClass(this.className, classList);
        Method curr_method = Helper.getMethod(methodName, curr_class);

        GType new_arg = GType.getType(n.f0, classList);
        curr_method.args.add(new_arg);
        curr_method.args_name.add(n.f1.f0.toString());
    }

}

class FieldVisitor extends GJVoidDepthFirst<List<GType>>{

    List<ClassType> classList;
    private String className;

    FieldVisitor(List<ClassType> classList){
        this.classList = classList;
    }


    public void visit(Goal n,  List<GType> list) {
        n.f1.accept(this, list);
    }


    public void visit(ClassDeclaration n, List<GType> list) {
        this.className = n.f1.f0.toString();
        n.f3.accept(this, list);
    }


    public void visit(ClassExtendsDeclaration n, List<GType> list) {
        this.className = n.f1.f0.toString();
        n.f5.accept(this, list);
    }


    public void visit(VarDeclaration n, List<GType> list) {

        ClassType curr_class = Helper.getClass(className,classList);

        String field_name = n.f1.f0.toString();
        GType var = GType.getType(n.f0, classList);
        curr_class.fields.add(var);
        curr_class.fields_name.add(field_name);
    }

}

class vmtBuild {

    List<ClassType> list;

    public vmtBuild (List<ClassType> list) {
        this.list = list;
    }

    public void vmtPrint() {
        for (int i = 1; i < list.size(); i++) {
            ClassType this_class = list.get(i);
            String curr_class = this_class.class_name;
            System.out.println("const vmt_" + curr_class);
            LinkedList<Method> this_method= new LinkedList<>();
            LinkedList<Method> all_method = new LinkedList<>();
            LinkedList<ClassType> inheritance = new LinkedList<>();
            ClassType sup = Helper.getClass(curr_class, list).super_class;
            ClassType tmp = sup;
            if (sup != null) {
                inheritance.add(this_class);
                while (tmp != null) {
                    inheritance.addLast(tmp);
                    tmp = tmp.super_class;
                }
                ClassType top = inheritance.getLast();
                inheritance.removeLast();
                while (inheritance.getLast() != null) {
                    ClassType next = inheritance.getLast();
                    inheritance.removeLast();
                    for (int a = 0; a < top.methods.size(); a++) {
                        if (next.methods.contains(top.methods.get(a))) {

                        }
                    }
                }






                for (int j = 0 ; j < sup.methods.size(); j++) {
                    this_method.add(sup.methods.get(j));
                }
                for (int k = 0; k < this_method.size(); k++) {
                    if (this_class.methods.contains(this_method.get(k))) {
                        System.out.print("\t");
                        System.out.println(":" + curr_class + "." + this_class.methods.get(k).method_name);
                        all_method.add(this_method.get(k));
                    }else {
                        System.out.print("\t");
                        System.out.println(":" + sup.class_name + "." + this_method.get(k).method_name);
                        all_method.add(this_method.get(k));
                    }
                }
                if (this_method.size() < this_class.methods.size()) {
                    for (int l = 0; l < this_class.methods.size(); l++) {
                        if (!this_method.contains(this_class.methods.get(l))) {
                            System.out.print("\t");
                            System.out.println(":" + curr_class + "." + this_class.methods.get(l).method_name);
                            all_method.add(this_method.get(l));
                        }
                    }
                }
            }else {
                for (Method x : this_class.methods) {
                    System.out.print("\t");
                    System.out.println(":" + curr_class + "." +x.method_name);
                    all_method.add(x);
                }
            }
            System.out.println(" ");
            this_class.methods = all_method;
        }
        System.out.println(" ");
    }

}

class ClassPrinter extends GJDepthFirst<Object, List<ClassType>> {

    List<ClassType> list;
    int reg_counter = 0;
    int null_counter = 1;
    int bound_counter = 1;
    int level = 0;
    int while_counter = 1;
    int if_counter = 1;
    int and_counter = 1;
    String curr_cname;
    String curr_mname;
    String curr_super;
    Method messagesend_m;
    ClassType messagesend_c;
    boolean num = false;
    boolean field = false;

    public ClassPrinter(List<ClassType> list) {
        this.list = list;
    }


    public String visit(MainClass n, List<ClassType> list) {
        System.out.println("func Main()");
        this.level = 1;
        this.curr_cname = n.f1.f0.toString();
        n.f15.accept(this, list);
        Helper.print("ret", this.level);
        System.out.println(" ");
        this.level--;
        return null;
    }

    public String visit(ClassDeclaration n, List<ClassType> list) {
        this.level = 0;
        this.reg_counter = 0;
        this.curr_cname = n.f1.f0.toString();
        n.f4.accept(this, list);
        return null;
    }

    public String visit(ClassExtendsDeclaration n, List<ClassType> list) {
        this.level = 0;
        this.reg_counter =0;
        this.curr_cname = n.f1.f0.toString();
        this.curr_super = n.f3.f0.toString();
        n.f6.accept(this, list);
        return null;
    }

    public String visit(MethodDeclaration n, List<ClassType> list) {
        this.level = 0;
        this.reg_counter = 0;
        this.curr_mname = n.f2.f0.toString();
        String print = "func " + curr_cname + "." + curr_mname + "(this";
        Method curr_method = Helper.getMethod(n.f2.f0.toString(), Helper.getClass(this.curr_cname, list));
        String arg = "";
        for (String str : curr_method.args_name) {
            arg += " " + str;
        }
        print += arg + ")";
        System.out.println(print);
        this.level++;
        n.f7.accept(this, list);
        n.f8.accept(this, list);
        this.level--;
        Object ret_var = n.f10.accept(this ,list);
        this.level++;
        Helper.print("ret " + (String)ret_var, this.level);
        this.level--;
        System.out.println(" ");
        return null;
    }

    public String visit(AssignmentStatement n, List<ClassType> list) {
        Object var = n.f0.accept(this, list);
        Object item = n.f2.accept(this, list);
        if (Helper.check_field((String) var, this.field, this.reg_counter, this.level) != null){
            var = Helper.check_field((String) var, this.field, this.reg_counter, this.level);
            this.field = false;
            this.reg_counter++;
        }
        ClassType this_class = Helper.getClass(this.curr_cname, list);
        Method this_method = Helper.getMethod(this.curr_mname, this_class);
        if (!this.num) {
            if (!this_method.vars_name.contains(item)){
                Helper.print(var + " = [" + item + "]", this.level);
            }else {
                Helper.print(var + " = " + item, this.level);
            }

        }else {
            Helper.print(var + " = " + item, this.level);
        }
        this.num = false;
        return  null;
    }

    public String visit(ArrayAssignmentStatement n, List<ClassType> list) {
        Object arr = n.f0.accept(this, list);
        Helper.check_null((String )arr, this.null_counter, this.level);
        this.null_counter++;
        Object num = n.f2.accept(this, list);
        String index_check = "t." + this.reg_counter;
        this.reg_counter++;
        Helper.print(index_check + " = [" + arr + "]", this.level);
        Helper.check_ob(index_check,(String)num, this.bound_counter, this.level);
        this.bound_counter++;
        Object var = n.f5.accept(this, list);
        Helper.print(index_check + " = MulS(" + num + " 4)", this.level);
        Helper.print(index_check + " = Add(" + arr + " " + index_check +")", this.level);
        Helper.print("[" + index_check + "+4] = " + var, this.level);
        return null;
    }

    public String visit(IfStatement n, List<ClassType> list) {
        Object condition = n.f2.accept(this, list);
        if (Helper.check_field((String)condition, this.field, this.reg_counter, this.level) != null) {
            condition = Helper.check_field((String)condition, this.field, this.reg_counter, this.level);
            this.reg_counter++;
            this.field = false;
        }
        Helper.print("if0 " + condition + " goto :if" + this.if_counter + "_else", this.level);
        this.level++;
        n.f4.accept(this, list);
        Helper.print("goto :if" + this.if_counter + "_end", this.level);
        this.level--;
        Helper.print("if" + this.if_counter + "_else:", this.level);
        this.level++;
        n.f6.accept(this, list);
        this.level--;
        Helper.print("if" + this.if_counter + "_end:", this.level);
        this.if_counter++;
        return null;
    }

    public String visit(WhileStatement n, List<ClassType> list) {
        Helper.print("while" + this.while_counter + "_top:", this.level);
        Object condition = n.f2.accept(this, list);
        if (Helper.check_field((String )condition, this.field, this.reg_counter, this.level) != null) {
            condition = Helper.check_field((String )condition, this.field, this.reg_counter, this.level);
            this.reg_counter++;
            this.field = false;
        }
        Helper.print("if0 " + condition + " goto :while" + this.while_counter + "_end", this.level);
        this.level++;
        n.f4.accept(this, list);
        Helper.print(" goto :while" + this.while_counter + "_top", this.level);
        this.level--;
        Helper.print("while" + this.while_counter +"_end:", this.level);
        return null;
    }

    public String visit(PrintStatement n, List<ClassType> list) {
        Object item = n.f2.accept(this, list);
        if (Helper.check_field((String) item, this.field, this.reg_counter, this.level) != null) {
            item = Helper.check_field((String) item, this.field, this.reg_counter, this.level);
            this.reg_counter++;
            this.field = false;
        }
        Helper.print("PrintIntS(" + item + ")", this.level);
        return null;
    }

//    public String visit(Block n, List<ClassType> list) {
//        this.level++;
//        n.f1.accept(this, list);
//        this.level--;
//        return null;
//    }

    public String visit(AndExpression n, List<ClassType> list) {
        String and_store = "t." + this.reg_counter;
        this.reg_counter++;
        Object l = n.f0.accept(this, list);
        if (Helper.check_field((String) l, this.field, this.reg_counter, this.level) != null){
            l = Helper.check_field((String) l, this.field, this.reg_counter, this.level);
            this.field = false;
            this.reg_counter++;
        }
        Helper.print("if0 " + l + "goto :ss" + this.and_counter + "_else", this.level);
        this.level++;
        Object r = n.f2.accept(this, list);
        if (Helper.check_field((String) r, this.field, this.reg_counter, this.level) != null){
            r = Helper.check_field((String) r, this.field, this.reg_counter, this.level);
            this.field = false;
            this.reg_counter++;
        }
        Helper.print(and_store + " = " + r, this.level);
        Helper.print("goto :ss" + this.and_counter +"_end", this.level);
        this.level--;
        Helper.print("ss" + this.and_counter + "_else:", this.level);
        this.level++;
        Helper.print(and_store + " = 0", this.level);
        this.level--;
        Helper.print("ss" + this.and_counter + "_end:", this.level);
        return and_store;
    }

    public String visit(CompareExpression n, List<ClassType> list) {
        String cmp_store = "t." + this.reg_counter;
        this.reg_counter++;
        Object l = n.f0.accept(this, list);
        if (Helper.check_field((String) l, this.field, this.reg_counter, this.level) != null){
            l = Helper.check_field((String) l, this.field, this.reg_counter, this.level);
            this.field = false;
            this.reg_counter++;
        }
        Object r = n.f2.accept(this, list);
        if (Helper.check_field((String) r, this.field, this.reg_counter, this.level) != null){
            r = Helper.check_field((String) r, this.field, this.reg_counter, this.level);
            this.field = false;
            this.reg_counter++;
        }
        Helper.print(cmp_store + " = " + "LtS(" + l + " " + r + ")", this.level);
        return  cmp_store;
    }

    public String visit(PlusExpression n, List<ClassType> list) {
        String plus_store = "t." + this.reg_counter;
        this.reg_counter++;
        Object l = n.f0.accept(this, list);
        if (Helper.check_field((String) l, this.field, this.reg_counter, this.level) != null){
            l = Helper.check_field((String) l, this.field, this.reg_counter, this.level);
            this.field = false;
            this.reg_counter++;
        }
        Object r = n.f2.accept(this, list);
        if (Helper.check_field((String) r, this.field, this.reg_counter, this.level) != null){
            r = Helper.check_field((String) r, this.field, this.reg_counter, this.level);
            this.field = false;
            this.reg_counter++;
        }
        Helper.print(plus_store + " = Add(" + l + " " + r +")", this.level);
        return plus_store;
    }

    public String visit(MinusExpression n, List<ClassType> list) {
        String minus_store = "t." + this.reg_counter;
        this.reg_counter++;
        Object l = n.f0.accept(this, list);
        if (Helper.check_field((String) l, this.field, this.reg_counter, this.level) != null){
            l = Helper.check_field((String) l, this.field, this.reg_counter, this.level);
            this.field = false;
            this.reg_counter++;
        }
        Object r = n.f2.accept(this, list);if (Helper.check_field((String) r, this.field, this.reg_counter, this.level) != null){
            r = Helper.check_field((String) r, this.field, this.reg_counter, this.level);
            this.field = false;
            this.reg_counter++;
        }
        Helper.print(minus_store + " = Sub(" + l + " " + r +")", this.level);
        return minus_store;
    }

    public String visit(TimesExpression n, List<ClassType> list) {
        String times_store = "t." + this.reg_counter;
        this.reg_counter++;
        Object l = n.f0.accept(this, list);
        if (Helper.check_field((String) l, this.field, this.reg_counter, this.level) != null){
            l = Helper.check_field((String) l, this.field, this.reg_counter, this.level);
            this.field = false;
            this.reg_counter++;
        }
        Object r = n.f2.accept(this, list);
        if (Helper.check_field((String) r, this.field, this.reg_counter, this.level) != null){
            r = Helper.check_field((String) r, this.field, this.reg_counter, this.level);
            this.field = false;
            this.reg_counter++;
        }
        Helper.print(times_store + " = MulS(" + l + " " + r +")", this.level);
        return times_store;
    }

    public String visit(ArrayLookup n, List<ClassType> list) {
        String arr_store = "t." + this.reg_counter;
        this.reg_counter++;
        Object arr = n.f0.accept(this, list);
        Helper.print(arr_store + " = [" + arr + "]", this.level);
        Helper.check_null(arr_store, this.null_counter, this.level);
        this.null_counter++;
        String index_check = "t." + this.reg_counter;
        this.reg_counter++;
        Helper.print(index_check + " = [" + arr_store +"]", this.level);
        Object num = n.f2.accept(this, list);
        Helper.check_ob(index_check, (String)num, this.bound_counter, this.level);
        this.bound_counter++;
        Helper.print(index_check + " = MulS(" + num + " 4)", this.level);
        Helper.print(index_check + " = Add(" + index_check + " " + arr_store +")", this.level);
        return index_check + "+4";
    }

    public String visit(ArrayLength n, List<ClassType> list) { //TODO: store the length of any array in a map
        String arr_store = "t." + this.reg_counter;
        this.reg_counter++;
        Object arr = n.f0.accept(this, list);
        Helper.print(arr_store + " = [" + arr + "]", this.level);
        Helper.check_null(arr_store, this.null_counter, this.level);
        this.null_counter++;
        String length = "t." + this.reg_counter;
        this.reg_counter++;
        Helper.print(length + " = [" + arr_store + "]", this.level);
        return length;
    }

    public String visit(MessageSend n, List<ClassType> list) { //TODO: FIX Method argument
        Object this_class = n.f0.accept(this, list);
        ClassType curr_class;
        if (this_class.equals("this")) {
            curr_class = Helper.getClass(this.curr_cname, list);
        }else {
            curr_class = Helper.getClass((String)this_class, list);
            Helper.check_null((String) curr_class.register_loc, this.null_counter, this.level);
            this.null_counter++;
        }
        this.messagesend_c = curr_class;
        String vmt = "t." + this.reg_counter;
        this.reg_counter++;
        if (this_class.equals("this")) {
            Helper.print(vmt + " = [" + "this" + "]", this.level);
        }else {
            Helper.print(vmt + " = [" + curr_class.register_loc + "]", this.level);
        }
        Method this_method = Helper.getMethod(n.f2.f0.toString(), curr_class);
        this.messagesend_m = this_method;
        int offset = curr_class.methods.indexOf(this_method) * 4;
        Helper.print(vmt + " = [" + vmt + "+" + offset + "]", this.level);
        String func;
        if (this_class.equals("this")) {
            func = "call " + vmt + "(" + this_class;
        }else {
            func = "call " + vmt + "(" + curr_class.register_loc;
        }
        n.f4.accept(this, list); //get arguments, no return needed
        for (int i = 0; i < this_method.real_args.size(); i++) {
            func += " " + this_method.real_args.get(i);
        }
        func += ")";
        String reg_ret = "t." + this.reg_counter++;
        Helper.print(reg_ret + " = " + func, this.level);
        this.num = false;
        this_method.real_args.clear();
        return reg_ret;
    }

    public String visit(ExpressionList n, List<ClassType> list) {
        Object arg1 = n.f0.accept(this, list);
        if (Helper.check_field((String) arg1, this.field, this.reg_counter, this.level) != null){
            arg1 = Helper.check_field((String) arg1, this.field, this.reg_counter, this.level);
            this.field = false;
            this.reg_counter++;
        }
        messagesend_m.real_args.add((String)arg1);
        n.f1.accept(this ,list);
        return null;
    }

    public String visit(ExpressionRest n, List<ClassType> list) {
        Object arg2 = n.f1.accept(this, list);
        if (Helper.check_field((String) arg2, this.field, this.reg_counter, this.level) != null){
            arg2 = Helper.check_field((String) arg2, this.field, this.reg_counter, this.level);
            this.field = false;
            this.reg_counter++;
        }
        messagesend_m.real_args.add((String) arg2);
        return null;
    }

    public String visit(TrueLiteral n, List<ClassType> list) {
        this.num = true;
        return "1";
    }

    public String visit(FalseLiteral n, List<ClassType> list) {
        this.num = true;
        return "0";
    }

    public String visit(IntegerLiteral n, List<ClassType> list) {
        this.num = true;
        return n.f0.toString();
    }

    public String  visit(Identifier n, List<ClassType> list) { //todo: deal with superclass
        GType obj;

        if (Helper.getClass(this.curr_cname, list).main) {
            obj = Helper.getlocalField(n.f0.toString(), list.get(0));
            if (obj != null) {
                return n.f0.toString();
            }
        }

        ClassType curr_class = Helper.getClass(this.curr_cname, list);
        Method curr_method = Helper.getMethod(this.curr_mname, curr_class);
        obj = Helper.getlocalVar(n.f0.toString(), curr_method, curr_class);
        if (obj != null) {
            return n.f0.toString();
        }
        obj = Helper.getlocalField(n.f0.toString(), curr_class);  //TODO: STORE CLASS REGISTER
        if (obj != null) {
            int index = curr_class.fields_name.indexOf(n.f0.toString());
            this.field = true;
            return "this+" + (index + 1) * 4;
        }
        obj = Helper.getsuperField(n.f0.toString(), curr_class.super_class);
        if (obj != null) {
            int index = curr_class.super_class.fields_name.indexOf(n.f0.toString());
            String sup_reg = curr_class.super_class.register_loc;
            if (sup_reg == null) {
                sup_reg = "t." + this.reg_counter;
                Helper.print(sup_reg + " = HeapAllocZ(" + (curr_class.super_class.fields.size() + 1) * 4 + ")", this.level);
                Helper.print("[" + sup_reg + "] = :vmt_" + curr_class.super_class.class_name, this.level );
                curr_class.super_class.register_loc = sup_reg;
                this.field = true;
                return sup_reg + "+" + (index + 1) * 4;
            }
        }
        obj = Helper.getMethod(n.f0.toString(), curr_class);
        if (obj != null) {
            return n.f0.toString();
        }

        obj = Helper.getClass(n.f0.toString(), list);
        if (obj != null) {
            return n.f0.toString();
        }

        return null;
    }

    public String visit(ThisExpression n, List<ClassType> list) {
        return "this";
    }

    public String visit(ArrayAllocationExpression n, List<ClassType> list) {
        String ret = "t." + this.reg_counter;
        this.reg_counter++;
        Object num = n.f3.accept(this, list);
        Helper.print(ret + " = call :AllocArray(" + (String)num + ")", this.level);
        return ret;
    }

    public String visit(AllocationExpression n, List<ClassType> list) {
        ClassType obj = Helper.getClass(n.f1.f0.toString(), list);
        String cname = obj.class_name;
        String class_reg = "t." + this.reg_counter;
        Helper.print("t." + this.reg_counter + " = HeapAllocZ(" + (obj.fields.size() + 1) * 4 + ")", this.level);
        Helper.print("[t." + this.reg_counter +"] = :vmt_" + cname, this.level);
        Helper.check_null("t." + this.reg_counter, this.null_counter, this.level);
        obj.register_loc = class_reg;
        this.null_counter++;
        this.reg_counter++;
        return cname;
    }

    public String visit(NotExpression n, List<ClassType> list) {
        String bool_reg = "t." + this.reg_counter;
        Object bool = n.f1.accept(this, list);
        Helper.print(bool_reg + " = Sub(1 " + (String) bool + ")", this.level);
        return bool_reg;
    }

    public String visit(BracketExpression n, List<ClassType> list) {
        Object ret = n.f1.accept(this, list);
        return (String)ret;
    }

    public String visit(PrimaryExpression n, List<ClassType> list) {
        Object ret = n.f0.accept(this, list);
        return (String)ret;
    }

    public String visit(Expression n, List<ClassType> list) {
        Object ret = n.f0.accept(this, list);
        return (String)ret;
    }


}

class VaporValue {
    String id;
    String class_name;

    public VaporValue(String id) {
        this.id = id;
        class_name = null;
    }
}

class VaporPaser {

    List<ClassType> list;
    ClassType curr_class;
    String this_method;
    int indent;
    int[] label_collect = new int[5];
    int tem_num;
    int var_num;

    HashMap<Integer, VaporValue> variable_map;
    HashMap<String , Integer> id_map;
    List<Integer> param_ticket;
    List<String> param_const;
    String const_num;

    public VaporPaser(List<ClassType> list) {
        this.list = list;
        indent = 0;
        for (int i = 1; i < 5; i++) {
            label_collect[i] = 1;
        }
        tem_num = 0;
        var_num = 0;
        variable_map = null;
        id_map = null;
        param_ticket = new ArrayList<Integer>();
        param_const = new ArrayList<String>();
        const_num = "";
    }

    void startClass(String class_name) {
        curr_class = Helper.getClass(class_name, list);
    }

    void endClass() {
        curr_class = null;
    }

    void startMethod() {

        variable_map = new HashMap<>();
        id_map = new HashMap<>();
        var_num = 0;
        tem_num = 0;
        int ticket;
        ticket = getID("this");
        variable_map.get(ticket).class_name = curr_class.class_name;

        for (int i = 0; i < curr_class.fields_name.size(); i++) {
            String obj = curr_class.fields_name.get(i);
            ticket = getID(obj);
            variable_map.get(ticket).class_name = Helper.getObject(obj, curr_class).toString();
        }

        ClassType sup = curr_class.super_class;
        while(sup != null) {
            for (int i = 0; i < sup.fields_name.size(); i++) {
                String obj = sup.fields_name.get(i);
                ticket = getID(obj);
                variable_map.get(ticket).class_name = Helper.getObject(obj, sup).toString();
            }
        }

        if (!this_method.equals("main")) {
            Method curr_method = Helper.getMethod(this_method, curr_class);
            for (int i = 0; i < curr_method.vars_name.size(); i++) {
                String var = curr_method.vars_name.get(i);
                ticket = getID(var);
                VaporValue v = new VaporValue(var);
                variable_map.put(ticket, v);
                id_map.put(var, ticket);
                variable_map.get(ticket).class_name = curr_method.vars.get(i).toString();
            }

            for (int j = 0; j < curr_method.args_name.size(); j++) {
                String arg = curr_method.args_name.get(j);
                ticket = getID(arg);
                VaporValue v = new VaporValue(arg);
                variable_map.put(ticket, v);
                id_map.put(arg, ticket);
                variable_map.get(ticket).class_name = curr_method.args.get(j).toString();
            }
        }
    }

    void endMethod() {
        variable_map = null;
        id_map = null;
        var_num = 0;
        tem_num = 0;
    }

    void clearParam() {
        param_const.clear();;
        param_ticket.clear();;
    }

    int add_var_num() {
        this.var_num++;
        return this.var_num - 1;
    }

    int add_tem_num() {
        this.tem_num++;
        return this.tem_num - 1;
    }

    int addLabel(String str) {
        if (str.equals("if_else")) {
            label_collect[0]++;
            return label_collect[0] - 1;
        }else if (str.equals("while")) {
            label_collect[1]++;
            return label_collect[1] - 1;
        }else if (str.equals("null")) {
            label_collect[2]++;
            return label_collect[2] - 1;
        }else if (str.equals("bounds")) {
            label_collect[3]++;
            return label_collect[3] - 1;
        }else {
            label_collect[4]++;
            return label_collect[4] - 1;
        }
    }

    int getID(String id) {
        Integer t = id_map.get(id);
        int ret;
        int ticket;

        if (t == null) {
            ticket = add_var_num();
            VaporValue v = new VaporValue(id);
            variable_map.put(ticket, v);
            id_map.put(id, ticket);
            ret = ticket;
        } else {
            ret = t;
        }
        return ret;
    }

    int addTem() {
        int ticket = add_var_num();
        int tem = add_tem_num();
        VaporValue v = new VaporValue("t." + tem);
        variable_map.put(ticket, v);
        return ticket;
    }

    int getLabel(String t) {
        int ticket = add_var_num();
        int tem = addLabel(t);
        VaporValue v;

        if (t.equals("if_else")) {
            v = new VaporValue("if" + tem + "_end");
        } else if (t.equals("while")) {
            v = new VaporValue("while" + tem + "_top");
            variable_map.put(ticket, v);
            ticket = add_var_num();
            v = new VaporValue("while" + tem + "_end");
        } else if (t.equals("null")) {
            v = new VaporValue("null" + tem);
        } else if (t.equals("bounds")) {
            v = new VaporValue("bounds" + tem);
        } else {
            v = new VaporValue("label" + tem);
        }
        variable_map.put(ticket, v);
        return ticket;
    }

    String findVarEnv(int ticket) {
        if (ticket == -1) {
            return this.const_num;
        }
        String s = variable_map.get(ticket).id;
        String t;
        int offset = 0;

        if (this.curr_class.fields_name.contains(s)) {
            for (int i = 0; i < this.indent; i++) {
                System.out.print("  ");
            }
            offset = this.curr_class.fields_name.indexOf(s);
            t = "[this+" + (offset + 1) * 4 + "]";
            ticket = addTem();
            s = findVarEnv(ticket);
            System.out.println(s + " = " + t);
        }
        return s;
    }

    String findVarEnv_l(int ticket) {
        if (ticket == -1) {
            return this.const_num;
        }
        String s = variable_map.get(ticket).id;
        String t;
        int offset = 0;

        if(this.curr_class.fields_name.contains(s)) {
            for (int i = 0; i < this.indent; i++) {
                System.out.print("  ");
            }
            offset = this.curr_class.fields_name.indexOf(s);
            s = "[this+" + (offset + 1) * 4 + "]";
        }
        return s;
    }

}

class J2V_visitor extends GJNoArguDepthFirst<Integer> {
    VaporPaser env;
    List<ClassType> list;
    String expression;
    String type;

    public J2V_visitor(VaporPaser env) {
        this.env = env;
        this.list = env.list;
    }

    public Integer visit(MainClass n) {
        String class_name = n.f1.f0.toString();
        env.this_method = "main";
        env.startClass(class_name);
        env.startMethod();
        staMethodParam(class_name, "main");
        pushIndent();
        n.f14.accept(this);
        n.f15.accept(this);
        indentVapor();
        System.out.println("ret");
        popIndent();
        env.endMethod();
        env.endClass();
        return null;
    }

    public Integer visit(ClassDeclaration n) {
        String class_name = n.f1.f0.toString();
        env.startClass(class_name);
        n.f4.accept(this);
        env.endClass();;
        return null;
    }

    public Integer visit(ClassExtendsDeclaration n) {
        String class_name = n.f1.f0.toString();
        env.startClass(class_name);
        n.f6.accept(this);
        env.endClass();
        return null;
    }

    public Integer visit(VarDeclaration n) {
        GType obj = GType.getType(n.f0, list);
        String type = obj.toString();
        int ticket = n.f1.accept(this);
        env.variable_map.get(ticket).class_name = type;
        return null;
    }

    public Integer visit(MethodDeclaration n) {
        env.this_method = n.f2.f0.toString();
        String class_name = env.curr_class.class_name;
        env.startMethod();
        staMethodParam(class_name, env.this_method);
        n.f4.accept(this);
        pushIndent();
        n.f7.accept(this);
        n.f8.accept(this);
        Integer i = n.f10.accept(this);
        indentVapor();
        System.out.println("ret " + env.findVarEnv(i));
        popIndent();
        env.endMethod();;
        return null;
    }

    public Integer visit(AssignmentStatement n) {
        String id = n.f0.f0.toString();
        Integer i = n.f2.accept(this);
        int ticket = env.getID(id);
        VaporValue v1 = env.variable_map.get(ticket);
        staAssignment(ticket, env.findVarEnv(i));
        if (i != -1) {
            VaporValue v2 = env.variable_map.get(i);
            if (v1.class_name != null) {
                v1.class_name = v2.class_name;
            }
        }else {
            if (v1.class_name != null) {
                v1.class_name = "Int";
            }
        }
        return null;
    }

    public Integer visit(ArrayAssignmentStatement n) {
        int i = n.f0.accept(this);
        Integer b = n.f2.accept(this);
        Integer c = n.f5.accept(this);

        int ticket1 = env.addTem();
        int ticket2 = env.addTem();
        int ticket3 = env.addTem();
        int ticket4 = env.addTem();
        int ticket5 = env.addTem();
        int bounds1 = env.getLabel("bounds");

        staMemoryAccess(ticket1, env.findVarEnv(i));
        staAssignment(ticket2, "Lt(" + env.findVarEnv(b) + " " + env.findVarEnv(ticket1) + ")");
        staIfGoto(ticket2, bounds1);
        pushIndent();
        indentVapor();
        System.out.println("Error(\"array index out of bounds\")");
        popIndent();
        staLabel(bounds1);
        staAssignment(ticket3, "MulS(" + env.findVarEnv(b) + " 4)");
        staAssignment(ticket4, "Add(" + env.findVarEnv(i) + " " + env.findVarEnv(ticket3) + ")");
        staAssignment(ticket5, "Add(" + env.findVarEnv(ticket4) + " 4)");
        staMemoryAssignment(ticket5, env.findVarEnv(c));
        return null;
    }

    public Integer visit(IfStatement n) {
        Integer i = n.f2.accept(this);
        int label1 = env.getLabel("if_else");
        int label2 = env.getLabel("if_else");
        int ticket1 = env.addTem();

        staAssignment(ticket1, "LtS(" + env.findVarEnv(i) + " 1)");
        staIfGoto(ticket1, label1);

        pushIndent();
        n.f4.accept(this);
        staGoto(label2);
        popIndent();

        staLabel(label1);
        pushIndent();
        n.f6.accept(this);
        popIndent();
        staLabel(label2);
        return null;
    }

    public Integer visit(WhileStatement n) {
        int label1 = env.getLabel("while");
        int label2 = label1 - 1;

        staLabel(label2);
        Integer i = n.f2.accept(this);

        staIf0Goto(i, label1);
        pushIndent();
        n.f4.accept(this);
        staGoto(label2);
        popIndent();

        staLabel(label1);
        return null;
    }

    public Integer visit(PrintStatement n) {
        Integer i = n.f2.accept(this);

        indentVapor();
        System.out.println("PrintIntS(" + env.findVarEnv(i) + ")");
        return null;
    }

    public Integer visit(Expression n) {
        Integer ret = n.f0.accept(this);
        return ret;
    }

    public Integer visit(AndExpression n) {
        Integer ret;
        int a = n.f0.accept(this);
        int b = n.f2.accept(this);
        int ticket1 = env.addTem();
        int ticket2 = env.addTem();
        int ticket3 = env.addTem();
        int label1 = env.getLabel("label");
        int label2 = env.getLabel("label");
        staAssignment(ticket1, "LtS(" + env.findVarEnv(a) + " 1)");
        staAssignment(ticket2, "LtS(" + env.findVarEnv(b) + " 1)");

        staIfGoto(ticket1, label1);
        pushIndent();
        staIfGoto(ticket2, label2);
        pushIndent();
        staAssignment(ticket3, "1");
        popIndent();
        popIndent();
        staGoto(label2);
        staLabel(label1);
        pushIndent();
        staAssignment(ticket3, "0");
        popIndent();
        staLabel(label2);
        ret = ticket3;
        return ret;
    }

    public Integer visit(CompareExpression n) {
        Integer ret = null;
        String e1 = "";
        Integer i = n.f0.accept(this);

        e1 = env.findVarEnv(i);
        int b = n.f2.accept(this);
        int ticket = env.addTem();
        staAssignment(ticket, "LtS(" + env.findVarEnv(i) + " " + env.findVarEnv(b) + ")");
        ret = ticket;
        return ret;
    }

    public Integer visit(PlusExpression n) {
        Integer ret = null;
        String e1 = "";
        int i = n.f0.accept(this);
        e1 = env.findVarEnv(i);
        int b = n.f2.accept(this);
        int ticket = env.addTem();
        staAssignment(ticket, "Add(" + e1 + " " + env.findVarEnv(b) + ")");
        ret = ticket;
        return ret;
    }

    public Integer visit(MinusExpression n) {
        Integer ret = null;
        String e1 = "";
        int i = n.f0.accept(this);
        e1 = env.findVarEnv(i);
        int b = n.f2.accept(this);
        int ticket = env.addTem();
        staAssignment(ticket, "Sub(" + e1 + " " + env.findVarEnv(b) + ")");
        ret = ticket;
        return ret;
    }

    public Integer visit(TimesExpression n) {
        Integer ret = null;
        String e1 = "";
        int i = n.f0.accept(this);
        e1 = env.findVarEnv(i);
        int b = n.f2.accept(this);
        int ticket = env.addTem();
        staAssignment(ticket, "MulS(" + e1 + " " + env.findVarEnv(b) + ")");
        ret = ticket;
        return ret;
    }

    public Integer visit(ArrayLookup n) {
        Integer ret = null;
        int i = n.f0.accept(this);
        int b = n.f2.accept(this);

        int ticket1 = env.addTem();
        int ticket2 = env.addTem();
        int ticket3 = env.addTem();
        int ticket4 = env.addTem();
        int ticket5 = env.addTem();
        int bounds1 = env.getLabel("bounds");

        staMemoryAccess(ticket1, env.findVarEnv(i));
        staAssignment(ticket2, "Lt(" + env.findVarEnv(b) + " " + env.findVarEnv(ticket1) + ")");
        staIfGoto(ticket2, bounds1);
        pushIndent();
        staPrint("Error(\"array index out of bounds\")");
        popIndent();
        staLabel(bounds1);

        staAssignment(ticket3, "MulS(" + env.findVarEnv(b) + " 4)");
        staAssignment(ticket4, "Add(" + env.findVarEnv(i) + " " + env.findVarEnv(ticket3) + ")");
        staMemoryAccess(ticket5, env.findVarEnv(ticket4) + "+4");
        ret = ticket5;
        return ret;
    }

    public Integer visit(ArrayLength n) {
        int i = n.f0.accept(this);
        int ticket = env.addTem();
        staMemoryAccess(ticket, env.findVarEnv(i));
        return null;
    }

    public Integer visit(MessageSend n) {
        Integer ret = null;
        int i = n.f0.accept(this);
        if (i != 0) {
            int null1 = env.getLabel("null");
            staIfGoto(i, null1);
            pushIndent();
            staPrint("Error(\"null pointer\")");
            popIndent();
            staLabel(null1);
        }

        int ticket1 = env.addTem();
        String method_name = n.f2.f0.toString();
        String class_name;
        if (i == 0) {
            class_name = env.curr_class.class_name;
        } else {
            class_name = env.variable_map.get(i).class_name;
        }
        ClassType curr_class = Helper.getClass(class_name, list);
        int offset = Helper.getOffset(method_name, curr_class);

        staMemoryAccess(ticket1, env.findVarEnv(i));
        staMemoryAccess(ticket1, env.findVarEnv(ticket1) + "+" + offset * 4);

        env.clearParam();
        n.f4.accept(this);
        String param = "";

        for (int j = 0; j < env.param_ticket.size(); j++) {
            param += " ";
            Integer ticket = env.param_ticket.get(j);
            if (ticket == -1) {
                param += env.param_const.get(j);
            }else {
                param += env.findVarEnv(ticket);
            }
        }
        int ticket2 = env.addTem();
        staAssignment(ticket2, "call " + env.findVarEnv(ticket1) + "(" + env.findVarEnv(i) + param + ")");
        VaporValue v = env.variable_map.get(ticket2);
        v.class_name = Helper.getMethod(method_name, curr_class).return_value.toString();
        ret = ticket2;
        return ret;
    }

    public Integer visit(ExpressionList n) {
        Integer i = n.f0.accept(this);
        env.param_ticket.add(i);
        if (i == -1) {
            env.param_const.add(env.const_num);
        }else {
            env.param_const.add("NULL");
        }
        n.f1.accept(this);
        return null;
    }

    public Integer visit(ExpressionRest n) {
        Integer i = n.f1.accept(this);

        env.param_ticket.add(i);
        if (i == -1) {
            env.param_const.add(env.const_num);
        }else {
            env.param_const.add("NULL");
        }
        return null;
    }

    public Integer visit(PrimaryExpression n) {
        Integer ret = n.f0.accept(this);
        return ret;
    }

    public Integer visit(IntegerLiteral n) {
        Integer ret = null;
        int ticket = env.addTem();
        staAssignment(ticket, n.f0.toString());
        ret = ticket;
        return ret;
    }

    public Integer visit(TrueLiteral n) {
        Integer ret = null;
        int ticket = env.addTem();
        staAssignment(ticket, "1");
        ret = ticket;
        return ret;
    }

    public Integer visit(FalseLiteral n) {
        Integer ret = null;
        int ticket = env.addTem();
        staAssignment(ticket, "0");
        ret = ticket;
        return ret;
    }

    public Integer visit(Identifier n) {
        Integer ret = env.getID(n.f0.toString());
        return ret;
    }

    public Integer visit(ThisExpression n) {
        return 0;
    }

    public Integer visit(ArrayAllocationExpression n) {
        Integer ret = null;
        int i = n.f3.accept(this);

        int ticket1 = env.addTem();
        int ticket2 = env.addTem();
        int ticket3 = env.addTem();

        staAssignment(ticket1, "MulS(" + env.findVarEnv(i) + " 4)");
        staAssignment(ticket2, "Add(" + env.findVarEnv(ticket1) + " 4)");
        staAssignment(ticket3, "HeapAllocZ(" + env.findVarEnv(ticket2) + ")");
        staMemoryAssignment(ticket3, env.findVarEnv(i));
        ret = ticket3;
        return ret;
    }

    public Integer visit(AllocationExpression n) {
        Integer ret = null;
        int ticket = 0;
        String class_name  = n.f1.f0.toString();
        ClassType curr_class = Helper.getClass(class_name, list);
        ticket = env.addTem();
        VaporValue v = env.variable_map.get(ticket);
        v.class_name = class_name;

        staAssignment(ticket, "HeapAllocZ(" + (curr_class.fields.size() + 1) * 4 + ")");
        staMemoryAssignment(ticket, ":vmt_" + curr_class.class_name);
        ret = ticket;
        return ret;
    }

    public Integer visit(NotExpression n) {
        Integer ret = null;
        int i = n.f1.accept(this);
        int ticket = env.addTem();
        staAssignment(ticket, "LtS(" + env.findVarEnv(i) + " 1");
        ret = ticket;
        return ret;
    }

    public Integer visit(BracketExpression n) {
        Integer ret = n.f1.accept(this);
        return ret;
    }

    void staMethodParam(String class_name, String method) {
        if (method.equals("main")) {
            System.out.print("func Main(");
        }else {
            System.out.print("func " + class_name + "." + method + "(this");
            ClassType curr_class = Helper.getClass(class_name, list);
            Method curr_method = Helper.getMethod(method, curr_class);
            for (String arg : curr_method.args_name) {
                System.out.print(" " + arg);
            }
        }
        System.out.println(")");
    }

    void pushIndent() {
        env.indent++;
    }

    void popIndent() {
        env.indent--;
    }

    void indentVapor() {
        for (int i = 0; i < env.indent; i++) {
            System.out.print("  ");
        }
    }

    void staAssignment(int l, String r) {
        indentVapor();
        System.out.println(env.findVarEnv_l(l) + " = " + r);
    }

    void staMemoryAssignment(int l, String r) {
        indentVapor();
        System.out.println("[" + env.findVarEnv_l(l) + "] = " + r);
    }

    void staMemoryAccess(int l, String r) {
        indentVapor();
        System.out.println(env.findVarEnv_l(l) + " = [" + r + "]");
    }

    void staLabel(int label) {
        indentVapor();
        System.out.println(env.findVarEnv(label) + ":");
    }

    void staIf0Goto(int ticket, int label) {
        indentVapor();
        System.out.println("if0 " + env.findVarEnv(ticket) + " goto :" + env.findVarEnv(label));
    }

    void staIfGoto(int ticket, int label) {
        indentVapor();
        System.out.println("if " + env.findVarEnv(ticket) + " goto :" + env.findVarEnv(label));
    }

    void staGoto(int label) {
        indentVapor();
        System.out.println("goto :" + env.findVarEnv(label));
    }

    void staPrint(String in) {
        indentVapor();
        System.out.println(in);
    }
}

class Helper{

    public static int getOffset(String method_name, ClassType curr_class) {
        for (int i = 0; i < curr_class.methods.size(); i++) {
            if (curr_class.methods.get(i).method_name.equals(method_name)) {
                return  i;
            }
        }
        return -1;
    }

    public static String check_field(String ret_reg, boolean field, int reg_count, int level) {
        if (field) {
            String field_reg = "t." + reg_count;
            Helper.print(field_reg + " = [" + ret_reg + "]", level);
            return field_reg;
        }else {
            return null;
        }
    }

    //found error and print error message and quit
    public static void exit(int i){
        switch (i) {
            case 1:
                System.out.println("Error (\"null pointer\")");
                break;
            case 2:
                System.out.println("Error (\"array index out of bounds\")");
                break;
        }
    }

    public static void tab(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("\t");
        }
    }
    //find class with given class name
    public static ClassType getClass(String class_name, List<ClassType> classList) {
        for (ClassType ct : classList){
            if (class_name.equals(ct.class_name)){
                return ct;
            }
        }
        return null;
    }

    //find method with given class
    public static Method getMethod(String method_name, ClassType curr_class){

        //check current class
        for (Method m : curr_class.methods){
            if (m.method_name.equals(method_name)){
                return m;
            }
        }

        //check superclass
        ClassType tmp = curr_class.super_class;
        while(tmp!= null){
            for (Method m : tmp.methods){
                if (m.method_name.equals(method_name)){
                    return m;
                }
                tmp = tmp.super_class;
            }
        }
        return null;
    }

    //find object with given object name and linkedlist
    public static GType getlocalVar(String obj_name, Method curr_method, ClassType curr_class){
        int index;

        //Find obj in current method args and var
        index = curr_method.vars_name.indexOf(obj_name);
        if ( index != -1 ){
            return curr_method.vars.get(index);
        }

        index = curr_method.args_name.indexOf(obj_name);
        if ( index != -1 ){
            return curr_method.args.get(index);
        }

        return null;
    }

    public static GType getlocalField(String obj_name, ClassType curr_class) {
        int index;

        index = curr_class.fields_name.indexOf(obj_name);
        if ( index != -1 ){
            return curr_class.fields.get(index);
        }

        return null;
    }

    public static GType getsuperField(String obj_name, ClassType curr_class){
        ClassType tmp = curr_class.super_class;
        int index;

        while(tmp!= null){
            index = tmp.fields_name.indexOf(obj_name);
            if ( index != -1 ){
                return tmp.fields.get(index);
            }
            tmp = tmp.super_class;
        }

        return null;
    }

    public  static  void print(String str, int level) {
        Helper.tab(level);
        System.out.println(str);
    }

    public static void check_null(String register, int null_counter,int level) {
        Helper.print("if " + register + " goto :null" + null_counter, level);
        level++;
        Helper.tab(level);
        Helper.exit(1);
        level--;
        Helper.print("null" + null_counter +":", level);
    }

    public static void check_ob(String arr_register, String index, int bound_counter, int level) {
        Helper.print(arr_register + " = " + "Lt(" + index + " " + arr_register + ")", level);
        Helper.print("if " + arr_register + " goto :bound" + bound_counter, level);
        level++;
        Helper.tab(level);
        Helper.exit(2);
        level--;
        Helper.print("bound" + bound_counter + ":", level);
    }

    public static GType getObject(String obj_name, Method curr_method, ClassType curr_class){
        ClassType tmp = curr_class.super_class;
        int index;

        //Find obj in current method args and var
        index = curr_method.vars_name.indexOf(obj_name);
        if ( index != -1 ){
            return curr_method.vars.get(index);
        }

        index = curr_method.args_name.indexOf(obj_name);
        if ( index != -1 ){
            return curr_method.args.get(index);
        }

        //Find obj in current class fields
        index = curr_class.fields_name.indexOf(obj_name);
        if ( index != -1 ){
            return curr_class.fields.get(index);
        }

        //check fields from super class
        while(tmp!= null){
            index = tmp.fields_name.indexOf(obj_name);
            if ( index != -1 ){
                return tmp.fields.get(index);
            }
            tmp = tmp.super_class;
        }

        return null;
    }

    public static GType getObject(String obj_name, ClassType curr_class){
        ClassType tmp = curr_class.super_class;
        int index;

        //Find obj in current class fields
        index = curr_class.fields_name.indexOf(obj_name);
        if ( index != -1 ){
            return curr_class.fields.get(index);
        }

        //check fields from super class
        while(tmp!= null){
            index = tmp.fields_name.indexOf(obj_name);
            if ( index != -1 ){
                return tmp.fields.get(index);
            }
            tmp = tmp.super_class;
        }
        return null;
    }
}

class IntType extends GType{

    public String toString(){
        return "Int";
    }
}

class BoolType extends GType{
    public String toString(){
        return "Bool";
    }
}

class ArrayType extends GType{
    public String toString(){
        return "Array";
    }
}

