package main.java.com.testNeo4j;//package com.iseu.nanhang;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * @projectName: 切片工具代码
 * @package: com.iseu.nanhang
 * @className: FactorExtract
 * @author: Eric
 * @description: TODO
 * @date: 2022/5/13 15:37
 * @version: 1.0
 */
//目的：提取20个底层指标设计，后面要改成接口，先这样写着吧
public class test2 {
    ArrayList<String> globalVariaNumList = new ArrayList<>();//由于不同名，暂时不用自定义类
    ArrayList<String> staticVariaNumList = new ArrayList<>();
    int i=0;
//    HashMap<MyLoopNode,ArrayList<String>>loop_localVarias = new HashMap<>();
//    String DB_PATH = "D:\\JavaSlicer7.1\\Repository\\Graphs\\Calculator.db";
//    GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);

    /**--------------------------------------------获取编译单元----------------------------------------------**/
    public CompilationUnit getRoot(String filePath) throws FileNotFoundException {
        FileInputStream input = new FileInputStream(filePath);
        return StaticJavaParser.parse(input);
    }

    /**--------------------------------------------抽象语法树遍历----------------------------------------------**/
    public void ParseAST(Node root,Node rootNode) throws Exception {
        if (root.getChildNodes()!=null)
        {
            for (Node child: root.getChildNodes())
            {
                if (child instanceof MethodDeclaration)
                {
                // if (child instanceof WhileStmt || child instanceof ForStmt){
                //if (child instanceof ObjectCreationExpr){
                  //  System.out.println(getLoopMethodNode(child).toString());
                        System.out.println("wuxiao line is: "+getWuxiaoCode(child));
                        System.out.println("loop line is: "+getLoopLineNum(child));
                        i+=getLoopLineNum(child);
                        System.out.println("注释 line is: "+getCommentNum(child));
                        System.out.println("CALL line is: "+getCallFuncNum(child));
                        System.out.println("obj line is: "+getObjNum(child));
                     // System.out.println("obj line is: "+getAPINum(child,child));
                        System.out.println("LocalVariableNum line is: "+getLocalVariableNum(child));
                        System.out.println("HashNum line is: "+getLoopHashNum(child));
                        System.out.println("\n");
                      //  VisitNode(child);
                 }
                ParseAST(child,rootNode);
           }
        }
    }


    /**--------------------------------------------循环结构体中无效代码行数----------------------------------------------**/
    //无效代码包括：打印语句，和未被使用的声明语句。MethodCallExpr为什么是未被使用的函数调用
    public int getWuxiaoCode(Node loopNode){
        int[] resultList = new int[1];
        return getWuxiaoCode_sub(resultList,loopNode);
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

    public void VisitNode(Node loopNode){
        for (Node child: loopNode.getChildNodes()){
            System.out.println(child.getBegin().get().line+": "+child.getClass().getSimpleName()+": "+child);
            if (child.getChildNodes() !=null){
                VisitNode(child);
            }
        }
    }
    /**--------------------------------------------返回循环代码行数----------------------------------------------**/
    public int getLoopLineNum(Node loopNode){
        int startLine = loopNode.getBegin().get().line;
        int endLine = loopNode.getEnd().get().line;
        return endLine - startLine +1;
    }
    /**--------------------------------------------返回循环代码注释行数----------------------------------------------**/
    public int getCommentNum(Node loopNode) {
        return loopNode.getAllContainedComments().size();
    }

    /**--------------------------------------------返回循环代码调用函数数量----------------------------------------------**/
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
    /**--------------------------------------------返回循环代码API数量----------------------------------------------**/
    public int getAPINum(Node loopNode,Node rootNode){
        //函数调用数量-本地函数调用数量
        ArrayList<String> localFuncList = new ArrayList<>();
        getAPINum_sub1(rootNode,localFuncList);
        int[] resultList = new int[1];
        return getCallFuncNum(loopNode)-getAPINum_sub2(resultList,loopNode,localFuncList);
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
    /**--------------------------------------------返回循环中对象数量----------------------------------------------**/
    public int getObjNum(Node loopNode){
        int[] resultList = new int[1];
        return getObjNum_sub(loopNode,resultList);
    }
    public int getObjNum_sub(Node loopNode, int[] result){
        for (Node child: loopNode.getChildNodes()){
            if (child instanceof ObjectCreationExpr){
                result[0]+=1;
            }
            if (child.getChildNodes() != null){
                getObjNum_sub(child,result);
            }
        }
        return result[0];
    }
    /**--------------------------------------------返回循环中局部变量数量----------------------------------------------**/
    //包括静态局部变量的数量！！！是否要上升到循环---所属函数---中局部变量。
    public int getLocalVariableNum(Node loopNode){
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
    //返回循环结构所属函数节点
    public Node getLoopMethodNode(Node indexNode){

        while (!(indexNode.getParentNode().get()instanceof MethodDeclaration)){
            indexNode = indexNode.getParentNode().get();
            if (indexNode.getParentNode().get()==null){
                break;
            }
        }
        return indexNode;
    }
    /**--------------------------------------------获取编译单元全局变量 以及 静态变量----------------------------------------------**/
    public void getFileGlobalVariaANDStatic(Node rootNode){
        //传入编译单元。
        for (Node child: rootNode.getChildNodes()){
            if (child instanceof MethodDeclaration){
                continue;//避免陷入局部变量
            }
            if (child instanceof FieldDeclaration)
            {  //找到全局变量和静态变量
                globalVariaNumList.add(((FieldDeclaration) child).getVariables().get(0).getName().toString());
                System.out.println(((FieldDeclaration) child).getVariables());
                if (child.getChildNodes().get(0).getClass().getSimpleName().toString().equals("Modifier") && child.getChildNodes().get(0).toString().equals("static "))
                {//这边竟然多了一个宫格，巨离谱。
                    staticVariaNumList.add(((FieldDeclaration) child).getVariables().get(0).getName().toString());
                }
            }
            if (child.getChildNodes()!=null){
                getFileGlobalVariaANDStatic(child);
            }
        }
    }
    /**--------------------------------------------catch语句统计----------------------------------------------**/
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
    /**--------------------------------------------获取循环中HashMap数量----------------------------------------------**/
    public int getLoopHashNum(Node loopNode){
        int[] resultList = new int[1];
        return getLoopHashNum_sub(resultList,loopNode);
    }

    public int getLoopHashNum_sub(int[] result,Node loopNode){
        for (Node child: loopNode.getChildNodes()){
            if ( child.toString().startsWith("HashMap<")){
                result[0] +=1;
                System.out.println(child);
            }
            if (child.getChildNodes()!= null){
                getLoopHashNum_sub(result,child);
            }
        }
        return result[0];
    }

    /**--------------------------------------------最大等级计算----------------------------------------------**/
    public int getMaxDegree(Node loopNode){
        int[] resultList = new int[1];
        int result = getMaxDegree_sub(loopNode,0,resultList);
        //因为递归计算过程中，最后会执行多余的+1操作，因此cunrrentDeep从0而不是1
        return result;
    }
    /**--------------------------------------------扇入扇出计算----------------------------------------------**/
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
    /**--------------------------------------------可移动代码检测----------------------------------------------**/
    //检测过长无参调用数量,若过长则表示该语句应该被在函数体外计算
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

    public static void main(String[] args) throws Exception {
        String FilePath = "D:\\springboot-study\\testNeo4j\\src\\main\\java\\com\\test\\HeapSort.java";
        test2 obj = new test2();
        CompilationUnit compilationUnit = obj.getRoot(FilePath);
//        obj.getFileGlobalVariaAcompilationUnitNDStatic(compilationUnit);
        obj.getFileGlobalVariaANDStatic(compilationUnit);
       // obj.ParseAST(compilationUnit,compilationUnit);
        System.out.println(obj.i);
//        obj.CC_test(FilePath);
    }

}
