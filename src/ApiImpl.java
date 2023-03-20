import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;



import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class ApiImpl implements Api {

    ArrayList<String> globalVariaNumList = new ArrayList<>();//由于不同名，暂时不用自定义类
    ArrayList<String> staticVariaNumList = new ArrayList<>();
     int i=0;

    //    HashMap<MyLoopNode,ArrayList<String>>loop_localVarias = new HashMap<>();
    String DB_PATH = "F:\\Repository\\Graphs\\BattleTank.db";

    public ApiImpl() {
    }

    /**--------------------------------------------获取编译单元----------------------------------------------**/
    public CompilationUnit getRoot(String filePath) throws FileNotFoundException {
        FileInputStream input = new FileInputStream(filePath);
        return StaticJavaParser.parse(input);
    }

    /**--------------------------------------------1 返回代码行数----------------------------------------------**/
    @Override
    public int getLoopLineNum(Node root,Node rootNode) {
        if (root.getChildNodes()!=null)
        {
            for (Node child: root.getChildNodes())
            {
                if (child instanceof MethodDeclaration)
                {
                    i+=Count(child);
                }
                getLoopLineNum(child,rootNode);

            }
        }
        return i;
    }
    public int Count(Node loopNode) {
        int startLine = loopNode.getBegin().get().line;
        int endLine = loopNode.getEnd().get().line;
        return endLine - startLine +1;
    }
    /**--------------------------------------------2 返回代码注释行数----------------------------------------------**/
    @Override
    public int getCommentNum(Node root,Node rootNode) {
        if (root.getChildNodes()!=null)
        {
            for (Node child: root.getChildNodes())
            {
                if (child instanceof MethodDeclaration)
                {
                    i+=count2(child);
                }
                getLoopLineNum(child,rootNode);
            }
        }
        return i;
    }

    public int count2(Node loopNode) {
        return loopNode.getAllContainedComments().size();
    }

    /**--------------------------------------------返回代码调用函数数量----------------------------------------------**/

    @Override
    public int getCallFuncNum(Node root,Node rootNode) {
        if (root.getChildNodes()!=null)
        {
            for (Node child: root.getChildNodes())
            {
                if (child instanceof MethodDeclaration)
                {
                    i+=count3(child);
                }
                getLoopLineNum(child,rootNode);
            }
        }
        return i;
    }
    public int count3(Node loopNode) {
        int[] resultList = new int[1];
        return getCallFuncNum_sub(resultList,loopNode);
    }





    /**--------------------------------------------3 返回代码API数量----------------------------------------------**/
    @Override
    public int getAPINum(Node loopNode,Node rootNode)
    {
        //函数调用数量-本地函数调用数量
        ArrayList<String> localFuncList = new ArrayList<>();
        getAPINum_sub1(rootNode,localFuncList);
        int[] resultList = new int[1];
//        return getCallFuncNum(loopNode)-getAPINum_sub2(resultList,loopNode,localFuncList);
        return getAPINum_sub2(resultList,loopNode,localFuncList);
    }
    public void getAPINum_sub1(Node rootNode,ArrayList<String> localFuncList){
        for (Node child: rootNode.getChildNodes()){
            if(child instanceof MethodDeclaration){
                localFuncList.add(((MethodDeclaration) child).getName().toString());
            }
            if (child.getChildNodes()!=null){
                getAPINum_sub1(child,localFuncList);
            }
        }
    }
    public int getAPINum_sub2(int[] result,Node loopNode,ArrayList<String> localFuncList){
        for (Node child: loopNode.getChildNodes()){
            if (child instanceof SimpleName && localFuncList.contains(child.toString())){
                result[0]+=1;
            }
            if (child.getChildNodes()!=null){
                getAPINum_sub2(result,child,localFuncList);
            }
        }
        return result[0];
    }
    @Override
    public int getObjNum(Node loopNode) {
        return 0;
    }
    /**--------------------------------------------6 返回代码中局部变量数量----------------------------------------------**/
    @Override
    public int getLocalVariableNum(Node loopNode) {
        int[] resultList = new int[1];
        return getLocalVariableNum_sub(resultList,loopNode);
    }



    @Override
    public Node getLoopMethodNode(Node indexNode) {
        return null;
    }
    /**--------------------------------------------7 获取编译单元全局变量 以及 静态变量----------------------------------------------**/
    @Override
    public void getFileGlobalVariaANDStatic(Node rootNode) {
        for (Node child: rootNode.getChildNodes()){
            if (child instanceof MethodDeclaration){
                continue;//避免陷入局部变量
            }
            if (child instanceof FieldDeclaration){//找到全局变量和静态变量
                globalVariaNumList.add(((FieldDeclaration) child).getVariables().get(0).getName().toString());
                if (child.getChildNodes().get(0).getClass().getSimpleName().toString().equals("Modifier") && child.getChildNodes().get(0).toString().equals("static ")){//这边竟然多了一个宫格，巨离谱。
                    staticVariaNumList.add(((FieldDeclaration) child).getVariables().get(0).getName().toString());
                }
            }
            if (child.getChildNodes()!=null){
                getFileGlobalVariaANDStatic(child);
            }
        }
    }
    public int getFileGlobalVariaANDStaticNum(int[] result, Node rootNode) {
        for (Node child: rootNode.getChildNodes()){
            if (child instanceof MethodDeclaration){
                continue;//避免陷入局部变量
            }
            if (child instanceof FieldDeclaration){//找到全局变量和静态变量
                result[0] +=1;
            }
            if (child.getChildNodes()!=null){
                getFileGlobalVariaANDStaticNum(result,child);
            }
        }
        return  result[0];
    }
    /**--------------------------------------------8 获取代码中全部变量数量----------------------------------------------**/
    //用数组存储局部变量,循环结构体所属函数包含的局部变量
    public int getVariableNum(Node loopNode){
        int[] resultList = new int[1];
        return getLocalVariableNum(loopNode) + getFileGlobalVariaANDStaticNum(resultList,loopNode);
    }

    /**--------------------------------------------9获取代码中HashMap数量----------------------------------------------**/
    @Override
    public int getLoopHashNum(Node loopNode) {
        int[] resultList = new int[1];
        return getLoopHashNum_sub(resultList,loopNode);
    }

    /**--------------------------------------------扇入扇出计算----------------------------------------------**/
    public int calIn(Node loopNode, GraphDatabaseService db){//后面可能还需要加上数据库路径
        ArrayList<Integer> resultList = new ArrayList<>();
        int startLine = loopNode.getBegin().get().line;
        System.out.println("结点为StartLine："+startLine);
        String inSql;
        inSql = "match (n:StatementNode{Startline: '"+startLine+"'}) with n, size((n)<-[]-(:StatementNode)) as s return s";
        try (
                Transaction tx = db.beginTx();
                Result result = db.execute(inSql);
             ){
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                resultList.add(Integer.parseInt(row.get("s").toString()));
            }

            tx.success();
        }
        if (resultList.size()!=0){
            return Collections.max(resultList);
        }else {
            return 0;
        }

    }

    public int calOut(Node loopNode, GraphDatabaseService db){//后面可能还需要加上数据库路径
        ArrayList<Integer> resultList = new ArrayList<>();
        int startLine = loopNode.getBegin().get().line;
        System.out.println("结点为StartLine："+startLine);
        String inSql;
        inSql = "match (n:StatementNode{Startline: '"+startLine+"'}) with n, size((n)-[]->(:StatementNode)) as s return s";
        try (
                Transaction tx = db.beginTx();
                Result result = db.execute(inSql);
        ){
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                resultList.add(Integer.parseInt(row.get("s").toString()));
            }

            tx.success();
        }
        if (resultList.size()!=0){
            return Collections.max(resultList);
        }else {
            return 0;
        }

    }

    public int getVariableNum_sub(int[] result, Node node){
        for (Node child: node.getChildNodes()){
            if (child instanceof VariableDeclarationExpr || child instanceof FieldDeclaration){
                result[0] +=1;
            }
            if (child.getChildNodes() !=null){
                getLocalVariableNum_sub(result,child);
            }
        }
        return result[0];
    }

    public int getLocalVariableNum_sub(int[] result, Node node){
        for (Node child: node.getChildNodes()){
            if (child instanceof VariableDeclarationExpr){
                result[0] +=1;
            }
            if (child.getChildNodes() !=null){
                getLocalVariableNum_sub(result,child);
            }
        }
        return result[0];
    }

    public int getLoopHashNum_sub(int[] result,Node loopNode){
        for (Node child: loopNode.getChildNodes()){
            if ( child.toString().startsWith("HashMap<")){
                result[0] +=1;
                return result[0];
            }
            if (child.getChildNodes()!= null){
                getLoopHashNum_sub(result,child);
            }
        }
        return result[0];
    }
    public int getCallFuncNum_sub(int[] result,Node node){
        for (Node child: node.getChildNodes()){
            if (child instanceof MethodCallExpr){
                result[0] +=1;
            }
            if (child.getChildNodes()!=null){
                getCallFuncNum_sub(result,child);
            }
        }
        return result[0];
    }

}
