package main.java.com.API;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class ApiImpl implements Api {

  public    ArrayList<String> globalVariaNumList = new ArrayList<>();//由于不同名，暂时不用自定义类
  public   ArrayList<String> staticVariaNumList = new ArrayList<>();
  public   int i=0;
    String DBPATH="D:\\JavaSlicer7.1\\Repository\\Graphs\\src.db";
    GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(DBPATH);
    //    HashMap<MyLoopNode,ArrayList<String>>loop_localVarias = new HashMap<>();

    public ApiImpl() {
    }
    /**--------------------------------------------1 返回代码行数----------------------------------------------**/
    @Override
    public int getLoopLineNum(Node root,Node rootNode) {
        if (root.getChildNodes()!=null)
        {
            for (Node child: root.getChildNodes())
            {
               // System.out.println(child.getClass().getSimpleName());
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
    public int getCommentNum(Node root) {
        if (root.getChildNodes()!=null)
        {
            for (Node child: root.getChildNodes())
           {
               if(child.getClass().getSimpleName().equals("ClassOrInterfaceDeclaration"))
               {
                   i+= child.getAllContainedComments().size();
                   System.out.println(child.getAllContainedComments());
               }
            }
        }
        return i;
    }

//    public int count2(Node loopNode) {
//        System.out.println(loopNode.getAllContainedComments());
//        return loopNode.getAllContainedComments().size();
//    }

    /**--------------------------------------------3 返回代码API数量----------------------------------------------**/
    @Override
    public int getAPINum(Node loopNode,Node rootNode)
    {
        //函数调用数量-本地函数调用数量
        ArrayList<String> localFuncList = new ArrayList<>();
//        getAPINum_sub1(rootNode,localFuncList);
//        for (int i = 0; i < localFuncList.size(); i++)
//        {
//            System.out.println(localFuncList.get(i));
//        }
        int[] resultList = new int[1];
//        return getCallFuncNum(loopNode)-getAPINum_sub2(resultList,loopNode,localFuncList);
        return getAPINum_sub1(resultList,loopNode,localFuncList);
    }
    public int getAPINum_sub1( int[] result,Node rootNode,ArrayList<String> localFuncList){
        for (Node child: rootNode.getChildNodes()){
            if(child instanceof MethodDeclaration && ((MethodDeclaration) child).getModifiers().toString().contains("public")){
                localFuncList.add(((MethodDeclaration) child).getName().toString());
                result[0]+=1;
            }
            if (child.getChildNodes()!=null){
                getAPINum_sub1(result,child,localFuncList);
            }
        }
      return result[0];
    }
//    public int getAPINum_sub2(int[] result,Node loopNode,ArrayList<String> localFuncList){
//        for (Node child: loopNode.getChildNodes()){
//            if (child instanceof SimpleName && localFuncList.contains(child.toString())){
//                result[0]+=1;
//            }
//            if (child.getChildNodes()!=null){
//                getAPINum_sub2(result,child,localFuncList);
//            }
//        }
//        return result[0];
//    }

    /**--------------------------------------------4 5 获取编译单元全局变量 以及 静态变量----------------------------------------------**/
    @Override
    public void getFileGlobalVariaANDStatic(Node rootNode){
        //传入编译单元。
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
    /**--------------------------------------------6 返回代码中局部变量数量----------------------------------------------**/
    @Override
    public int getLocalVariableNum(Node loopNode) {
        int[] resultList = new int[1];
        return getLocalVariableNum_sub(resultList,loopNode);
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

    /**--------------------------------------------7 获取代码中全部变量数量----------------------------------------------**/
    public int getVariableNum(Node loopNode){
        int[] resultList = new int[1];
        return getVariableNum_sub(resultList,loopNode);
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

    /**--------------------------------------------9 获取代码中HashMap数量----------------------------------------------**/
    @Override
    public int getLoopHashNum(Node loopNode) {
        int[] resultList = new int[1];
        return getLoopHashNum_sub(resultList,loopNode);
    }
    public int getLoopHashNum_sub(int[] result,Node loopNode){
        for (Node child: loopNode.getChildNodes()){
            if ( child.toString().startsWith("HashMap<")){
                result[0] +=1;
            }
            if (child.getChildNodes()!= null){
                getLoopHashNum_sub(result,child);
            }
        }
        return result[0];
    }

    /**--------------------------------------------10 返回代码调用函数数量----------------------------------------------**/
//    @Override
//    public int getCallFuncNum(Node root,Node rootNode) {
//        if (root.getChildNodes()!=null)
//        {
//            for (Node child: root.getChildNodes())
//            {
//                if (child instanceof MethodDeclaration)
//                {
//                    i+=count3(child);
//                }
//                getCallFuncNum(child,rootNode);
//            }
//        }
//        return i;
//    }
//    public int count3(Node loopNode) {
//        int[] resultList = new int[1];
//        return getCallFuncNum_sub(resultList,loopNode);
//    }
    /**--------------------------------------------10 返回代码调用函数数量----------------------------------------------**/
    public int getCallFuncNum(Node loopNode){
        int[] resultList = new int[1];
        return getCallFuncNum_sub(resultList,loopNode);
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
    /**--------------------------------------------12 最大等级计算----------------------------------------------**/
    @Override
    public int getMaxDegree(Node loopNode) {
        int[] resultList = new int[1];
        int result = getMaxDegree_sub(loopNode,0,resultList);//因为递归计算过程中，最后会执行多余的+1操作，因此cunrrentDeep从0而不是1
        return result;
    }
    public int getMaxDegree_sub(Node loopNode, int currentDeep, int[] result){
        for (Node child: loopNode.getChildNodes()){
            if(currentDeep > result[0]){
                result[0] = currentDeep;
            }
            if(child instanceof BlockStmt){
                currentDeep +=1;
            }
            if (child.getChildNodes() !=null){
                getMaxDegree_sub(child,currentDeep,result);
            }
            continue;
        }
        return result[0];
    }
    /**--------------------------------------------13 中心性计算----------------------------------------------**/
    //度中心性：出度与入读之和
    public int get_degree_centrality(Node loopNode,GraphDatabaseService db,int diGuiFlag){
        return calIn(loopNode,db,diGuiFlag)+calOut(loopNode,db,diGuiFlag);
    }
    //紧密中心性：到其他节点的最短距离，越少越重要，无法到达的节点距离暂时定为10
    public int get_closeness_centrality(Node loopNode,GraphDatabaseService db){
        ArrayList<Integer> resultList = new ArrayList<>();//可以删除，用不到
        int resultNum = 0;
        int nodeNum = 0;
        int startline = loopNode.getBegin().get().line;
        System.out.println("startline is :"+startline);
        String sqlStr =
                "match (p:StatementNode) with collect(p) as nodes\n" +
                "match (source:LoopNode{Startline:'"+startline+"'})\n" +
                "unwind nodes as target\n" +
                "with source,target where id(source)<>id(target)\n" +
                "match path = shortestPath((source)-[*..10]->(target))\n" +
                "with path limit 25\n" +
                "return LENGTH(path)" ;
        try (Transaction tx = db.beginTx(); Result result = db.execute(sqlStr);){
            System.out.println(result);
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                resultList.add(Integer.parseInt(row.get("LENGTH(path)").toString()));
                resultNum += Integer.parseInt(row.get("LENGTH(path)").toString());
            }
        }
        String nodeNumSql = "match (p:StatementNode)return count(*)";
        try(Transaction tx = db.beginTx(); Result result = db.execute(nodeNumSql);){
            while (result.hasNext()){
                Map<String,Object> row = result.next();//只有一行结果，不需要数组。
                nodeNum = Integer.parseInt(row.get("count(*)").toString())-1;//除去本身
            }
        }
        if (resultList.size()<nodeNum){
            int addNum = nodeNum - resultList.size();
            resultNum +=addNum*10;
        }
        return resultNum;
    }


    /**--------------------------------------------14 15扇入扇出计算----------------------------------------------**/

    public void CountcalIn(Node root,Node rootNode) {
        if (root.getChildNodes()!=null){
            for (Node child: root.getChildNodes()){
                if (child instanceof WhileStmt || child instanceof ForStmt){
                    //System.out.println("callin is"+calIn(child,db,1));
                  System.out.println("callout is"+calOut(child,db,1));
                }
                CountcalIn(child,rootNode);
            }
        }
    }
    @Override
    public int calIn(Node loopNode, GraphDatabaseService db, int diGuiFlag){//后面可能还需要加上数据库路径
        ArrayList<Integer> resultList = new ArrayList<>();
        int startLine = loopNode.getBegin().get().line;
        String inSql;
        if (diGuiFlag==0){
            inSql = "match (n:LoopNode{Startline: '"+startLine+"'}) with n, size((n)<-[]-(:StatementNode)) as s return s";
        }else {
            inSql = "match (n:StatementNode{Startline: '"+startLine+"'}) with n, size((n)<-[]-(:StatementNode)) as s return s";
        }

        try (Transaction tx = db.beginTx(); Result result = db.execute(inSql);){
            System.out.println(result);
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                resultList.add(Integer.parseInt(row.get("s").toString()));
            }
        }
        if (resultList.size()!=0){
            return Collections.max(resultList);
        }else {
            return 0;
        }

    }

    @Override
    public int calOut(Node loopNode,GraphDatabaseService db,int diFuiFlag){
        ArrayList<Integer> resultList = new ArrayList<>();
        int startLine = loopNode.getBegin().get().line;
        String outSql;
        if (diFuiFlag ==0){
            outSql = "match (n:LoopNode{Startline: '"+startLine+"'}) with n, size((n)-[]->(:StatementNode)) as s return s";
        }else {
            outSql = "match (n:StatementNode{Startline: '"+startLine+"'}) with n, size((n)-[]->(:StatementNode)) as s return s";
        }
        try (Transaction tx = db.beginTx(); Result result = db.execute(outSql);){
            System.out.println(result);
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                resultList.add(Integer.parseInt(row.get("s").toString()));
            }
        }if (resultList.size() !=0){
            return Collections.max(resultList);
        }else {
            return 0;
        }

    }

    /**--------------------------------------------21 数据库读统计 22 数据库修改统计----------------------------------------------**/

    public int[] getDataBaseModifyNum(Node loopNode){
        //统计executeUpdate数量
        int[] resultList = new int[2];
        return getDataBaseModifyNum_sub(resultList,loopNode);
    }

    public int[] getDataBaseModifyNum_sub(int[] result, Node loopNode){
        for (Node child: loopNode.getChildNodes()){
            if (child instanceof SimpleName && child.toString().contains("executeUpdate")){
                result[0]+=1;
            }
            if (child instanceof SimpleName && child.toString().contains("executeQuery"))
            {
                result[1]+=1;
            }
            if (child.getChildNodes()!=null){
                getDataBaseModifyNum_sub(result,child);
            }
        }
        return result;
    }
    /**--------------------------------------------16 数据库相关性计算----------------------------------------------**/
    public int getDataBaseRelation(Node loopNode){
        int[] resultList = new int[2];
        resultList = getDataBaseModifyNum(loopNode);
       return resultList[0]+resultList[1];
    }


    /**--------------------------------------------17 循环结构体中无效代码行数----------------------------------------------**/
    //无效代码包括：打印语句，和未被使用的声明语句
    @Override
    public int getWuxiaoCode(Node loopNode) {
        int[] resultList = new int[1];
        return getWuxiaoCode_sub(resultList,loopNode);
    }

    /**--------------------------------------------18 catch语句统计----------------------------------------------**/
    @Override
    public int getCatchNum(Node loopNode){
        int[] resultList = new int[1];
        int result = getCatchNum_sub(resultList,loopNode);
        return result;
    }

    public int getCatchNum_sub(int[] result, Node loopNode){
        for (Node child: loopNode.getChildNodes()){
            if (child instanceof CatchClause){
                result[0] +=1;
            }
            if (child.getChildNodes()!=null){
                getCatchNum_sub(result,child);
            }
        }
        return result[0];
    }
    public int getWuxiaoCode_sub(int[]result,Node loopNode){
        for (Node child: loopNode.getChildNodes()){
            if (child instanceof MethodCallExpr && child.toString().contains("System.out.println(")){
                result[0] +=1;
            }
            if (child.getChildNodes() !=null){
                getWuxiaoCode_sub(result,child);
            }
        }
        return result[0];
    }


    /**--------------------------------------------可移动代码检测----------------------------------------------**/
    public int getMovelineNum(Node loopNode){
        int[] resultList = new int[1];
        return getMovelineNum_sub(resultList,loopNode);
    }
    public int getMovelineNum_sub(int[] result,Node loopNode){
        for (Node child: loopNode.getChildNodes()){
            if (child instanceof MethodCallExpr &&judgeLongCall(1,child)){
                System.out.println("here here");
                result[0]+=1;
            }
            if (child.getChildNodes()!=null){
                getMovelineNum_sub(result,child);
            }
        }
        return result[0];
    }
    public boolean judgeLongCall(int callNum,Node callNode){
        if (callNum >2){
            System.out.println("run here1");
            return true;
        }
        for (Node child: callNode.getChildNodes()){
            if (child instanceof MethodCallExpr){
                System.out.println("run here");
                callNum +=1;
                System.out.println(callNum);
                return judgeLongCall(callNum,child);
            }
        }
        return false;
    }

    public CompilationUnit getRoot(String filePath) throws FileNotFoundException {
        FileInputStream input = new FileInputStream(filePath);
        return StaticJavaParser.parse(input);
    }

}
