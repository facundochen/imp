import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
//import com.iseu.nanhang.util.MyCFGNode;
//import com.iseu.nanhang.util.GraphViz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import org.eclipse.jdt.core.dom.*;
//import com.iseu.nanhang.util.FileUtil;
//import com.iseu.Prdg.cfg.CFG;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
//import com.iseu.nanhang.util.duplicateCode;

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
public class ThirdFactor {
    ArrayList<String> globalVariaNumList = new ArrayList<>();//由于不同名，暂时不用自定义类
    ArrayList<String> staticVariaNumList = new ArrayList<>();

//    HashMap<MyLoopNode,ArrayList<String>>loop_localVarias = new HashMap<>();
    String DB_PATH = "F:\\Repository\\Graphs\\BattleTank.db";
//    GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);

    /**--------------------------------------------获取编译单元----------------------------------------------**/
    public CompilationUnit getRoot(String filePath) throws FileNotFoundException {
        FileInputStream input = new FileInputStream(filePath);
        return StaticJavaParser.parse(input);
    }

    /**--------------------------------------------抽象语法树遍历----------------------------------------------**/
    public void ParseAST(Node root,Node rootNode) throws Exception {
        if (root.getChildNodes()!=null){
            for (Node child: root.getChildNodes()){
                if (true){
                    //插入操作
                    System.out.println("wuxiao line is: "+getWuxiaoCode(child));
                    System.out.println("youxiao line is: "+getLoopLineNum(child));
                    System.out.println("注释 line is: "+getCommentNum(child));
                    System.out.println("CALL line is: "+getCallFuncNum(child));
                    System.out.println("obj api line is: "+getAPINum(child,child));
                    System.out.println("obj line is: "+getObjNum(child));
                    System.out.println("LocalVariableNum line is: "+getLocalVariableNum(child));
                    System.out.println("catch num is: " +getCatchNum(child));
                    System.out.println("hash num is: "+getLoopHashNum(child));
                    System.out.println("maxDegree is: " + getMaxDegree(child));
                    System.out.println("moveable line num is "+ getMovelineNum(child));
//                    VisitNode(child);
//                    System.out.println(getMaxCodeSim(child,rootNode));
//                    System.out.println("zhong xin xing is :"+get_closeness_centrality(child));
//                    System.out.println("lujing guilv is "+ judgeLoopRule(child));
//                    System.out.println("----------------------");
//                    break;
//                    getLocalVariableNum(child);
//                    getLoopGlobalVariaNums(child);
//                    System.out.println("static num is:"+getLoopStaticVariaNums(child));

//                    System.out.println("callin is"+calIn(child));
//                    System.out.println("callout is"+calOut(child));
                }
//                System.out.println(child.getClass().getSimpleName()+": "+child);
//                VisitNode(child);
                ParseAST(child,rootNode);
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

    /**--------------------------------------------获取循环中全局变量数量----------------------------------------------**/
    //用数组存储局部变量,循环结构体所属函数包含的局部变量
    public ArrayList<String> getlocalVariaList(ArrayList<String>localVarias,Node methodNode){
        for (Node child: methodNode.getChildNodes()){
            if (child instanceof VariableDeclarationExpr){
                localVarias.add(((VariableDeclarationExpr) child).getVariables().get(0).getName().toString());
            }
            if (child instanceof Parameter){
                localVarias.add((((Parameter) child).getName().toString()));

            }
            if (child.getChildNodes() !=null){
                getlocalVariaList(localVarias,child);
            }
        }
        return localVarias;
    }
    //传入loop根节点，返回循环中全局变量
    public int getLoopGlobalVariaNums(Node loopNode,int diGuiFlag){
        ArrayList<String> localVarias = new ArrayList<>();
        Node methodNode;
        if (diGuiFlag ==1){
            methodNode = loopNode;
        }else {
            methodNode = getLoopMethodNode(loopNode);
        }
        localVarias = getlocalVariaList(localVarias,methodNode);
        Set<String> resultList = new HashSet<>();
        return getLoopGlobalVariaNums_sub(resultList,localVarias,loopNode);
    }
    public int getLoopGlobalVariaNums_sub(Set<String> result,ArrayList<String>localVarias,Node loopNode){
        for (Node child:loopNode.getChildNodes()){
            if (child instanceof ThisExpr){
                result.add(child.toString());
            }
            if (child instanceof NameExpr && globalVariaNumList.contains(child.toString()) && !localVarias.contains(child.toString())){
                result.add(child.toString());
                continue;
            }
            if (child.getChildNodes()!=null){
                getLoopGlobalVariaNums_sub(result,localVarias,child);
            }
        }
        return result.size();
    }

    /**--------------------------------------------获取循环中静态变量数量----------------------------------------------**/
    public int getLoopStaticVariaNums(Node loopNode, int diGuiFlag){
        ArrayList<String> localVarias = new ArrayList<>();
        Node methodNode;
        if (diGuiFlag ==1){
            methodNode = loopNode;
        }else {
            methodNode = getLoopMethodNode(loopNode);
        }

        localVarias = getlocalVariaList(localVarias,methodNode);
        Set<String> resultList = new HashSet<>();
        return getLoopStaticVariaNums_sub(resultList,localVarias,loopNode);
    }

    public int getLoopStaticVariaNums_sub(Set<String> result,ArrayList<String>localVarias,Node loopNode){
        for (Node child:loopNode.getChildNodes()){
            if (child instanceof ThisExpr && staticVariaNumList.contains(child.toString())){
                result.add(child.toString());
            }
            if (child instanceof NameExpr && staticVariaNumList.contains(child.toString()) && !localVarias.contains(child.toString())){
                result.add(child.toString());
                continue;
            }
            if (child.getChildNodes()!=null){
                getLoopGlobalVariaNums_sub(result,localVarias,child);
            }
        }
        return result.size();
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
            }
            if (child.getChildNodes()!= null){
                getLoopHashNum_sub(result,child);
            }
        }
        return result[0];
    }

//    /**--------------------------------------------循环圈复杂度计算----------------------------------------------**/
////尝试调用已有接口，编译单元是JDT而不是Jparser。
//    //输入：编译-!!!-jdt编译单元-!!!-，或jdt编译单元中循环对应的block。CFG具体代码细节不清楚
//    public org.eclipse.jdt.core.dom.CompilationUnit getComintUnit_jdt(String FilePath){
//        File f = new File(FilePath);
//        String filePath = f.getAbsolutePath();
//        if (f.isFile()){
//            try {
//                ASTParser parser = ASTParser.newParser(AST.JLS8);
//                parser.setSource(FileUtil.readFileToString(filePath).toCharArray());
//                parser.setKind(ASTParser.K_COMPILATION_UNIT);
//                parser.setResolveBindings(true);
//                parser.setBindingsRecovery(true);
//                parser.setStatementsRecovery(true);
//                org.eclipse.jdt.core.dom.CompilationUnit cu = (org.eclipse.jdt.core.dom.CompilationUnit)parser.createAST(null);
//                return cu;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }else {
//            System.out.println("not a file");
//
//        }
//        return null;
//    }
//    //根据控制流图计算圈复杂度e-n+2。输入：编译单元，循环结构体WhileStatemt节点,当然也可以是block节点
//    public int calLoopCC(org.eclipse.jdt.core.dom.CompilationUnit cu, ASTNode loopNode){
//        int CC_result=0;
//        NodeVisitor nv = new NodeVisitor();
//        loopNode.accept(nv);
//        for (ASTNode child:nv.getASTNodes()){
//            if (child instanceof Block){
//                CFG cfgObj = new CFG(cu, (Block) child);
//                int nNum = cfgObj.getCFGnodes().size();
//                int edgeNum = cfgObj.getCFGedges().size();
//                CC_result = edgeNum - nNum +2;
//                break;
//            }
//        }
//        return CC_result;
//    }
//
//    public void CC_test(String filePath){
//        org.eclipse.jdt.core.dom.CompilationUnit cu =getComintUnit_jdt(filePath);
//        MethodNodeVisitor mv = new MethodNodeVisitor();
//        cu.accept(mv);
//        for (org.eclipse.jdt.core.dom.MethodDeclaration m:mv.getMethodDecs()){
//            NodeVisitor nv = new NodeVisitor();
//            m.accept(nv);
//            List<ASTNode> astNodes = nv.getASTNodes();
//            for (ASTNode node:astNodes){
//                if (node instanceof WhileStatement){
//                    int result = calLoopCC(cu,node);
//                    System.out.println("quan fu za du is："+result);
//                }
//            }
//        }
//    }

    /**--------------------------------------------最大等级计算----------------------------------------------**/
    public int getMaxDegree(Node loopNode){
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
    /**--------------------------------------------扇入扇出计算----------------------------------------------**/
    public int calIn(Node loopNode,GraphDatabaseService db, int diGuiFlag){//后面可能还需要加上数据库路径
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

    /**--------------------------------------------中心性计算----------------------------------------------**/
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
        String sqlStr = "match (p:StatementNode) with collect(p) as nodes\n" +
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
    /**--------------------------------------------数据库修改统计----------------------------------------------**/
    //统计修改语句，后期需要加入相关修改函数调用统计
    public int getDataBaseModifyNum(Node loopNode){
        //统计executeUpdate数量
        int[] resultList = new int[1];
        return getDataBaseModifyNum_sub(resultList,loopNode);
    }
    public int getDataBaseModifyNum_sub(int[] result, Node loopNode){
        for (Node child: loopNode.getChildNodes()){
            if (child instanceof SimpleName && child.toString().equals("executeUpdate")){
                result[0]+=1;
            }
            if (child.getChildNodes()!=null){
                getDataBaseModifyNum_sub(result,child);
            }
        }
        return result[0];
    }
    /**--------------------------------------------数据库读统计----------------------------------------------**/
    public int getDataBaseReadNum(Node loopNode){
        int[] resultList = new int[1];
        return 0;
    }
    public int getDataBaseReadNum_sub(int[] result, Node loopNode){
        for (Node child: loopNode.getChildNodes()){
            if (child instanceof SimpleName && child.toString().equals("executeQuery")){
                result[0] +=1;
            }
            if (child.getChildNodes()!=null){
                getDataBaseReadNum_sub(result,loopNode);
            }

        }
        return result[0];
    }
    /**--------------------------------------------数据库相关性计算----------------------------------------------**/
    public int getDataBaseRelation(Node loopNode){
        return getDataBaseModifyNum(loopNode) + getDataBaseReadNum(loopNode);
    }

    /**--------------------------------------------循环结构体中无效代码行数----------------------------------------------**/
    //无效代码包括：打印语句，和未被使用的声明语句
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

    /**--------------------------------------------路径自动依赖机----------------------------------------------**/
//    //1 控制流图构建
//    public boolean judgeLoopRule(Node loopNode){
//        //条件只有true和false两种，除去循环外，其余都是内部执行
//        ArrayList<MyCFGNode> childList = new ArrayList<>();
//        MyCFGNode rootNode = new MyCFGNode(null,childList,"loop",0);
//        //建立主框架
//        generatePathCFG_sub(rootNode,loopNode);
//        //增加else节点
//        //根节点先补充
//        ArrayList<MyCFGNode> elseNodeChilds = new ArrayList<>();
//        MyCFGNode rootelseNode = new MyCFGNode(rootNode,elseNodeChilds,"else",0);
//        rootNode.getChilds().add(rootelseNode);
//        generateELSEnode(rootNode);
//        //打编号
//        setNodeID(rootNode);
//        //增加else连接
//        generateElseEdge(rootNode);
//        //生成可执行路径
//
//        ArrayList<ArrayList<String>>testHash= generetaPath(rootNode);
//        for (int num=0;num<testHash.size();num++){
//            for (String child: testHash.get(num)){
//                System.out.println(child);
//            }
//            System.out.println("===========================");
//        }
//        //规律判断
////        System.out.println(judgePDA(generetaPath(rootNode)));
////        generetaPath(rootNode);
//
//        //打印节点之间的关系
////        printNode(rootNode);
//        //画出控制流图
////        printCFGGraph(rootNode);
//        return judgePDA(generetaPath(rootNode));
//    }
//
//    public void printNode(MyCFGNode rootNode){
//        for (MyCFGNode child:rootNode.getChilds()){
//            System.out.println("----------");
//            System.out.println(child.getChilds().size());
//            System.out.println("parent: "+child.getParent().getNodeType());
//            System.out.println("node: "+child.getNodeType());
//            System.out.println("nodeID: "+child.getNodeId());
//            if (child.getChilds()!=null){
//                printNode(child);
//            }
//        }
//    }
//    public void generatePathCFG_sub(MyCFGNode subRoot,Node subNode){
//        for (Node child:subNode.getChildNodes()){
//            MyCFGNode currentNode = null;
//            boolean flag=false; //若是subRoot被修改，则为true，未修改则为false
//            if (child instanceof IfStmt){
////                System.out.println("+++++++++++++++++");
////                System.out.println("IfStmt: " + child);
//                ArrayList<MyCFGNode> childList = new ArrayList<>();
//                currentNode = new MyCFGNode(subRoot,childList,"if",0);
//                subRoot.getChilds().add(currentNode);
//                flag = true;
//            }
//
//            if (child instanceof WhileStmt || child instanceof ForStmt){//等会补全循环结构
////                System.out.println("-------------------");
////                System.out.println("whileStmt: "+ child);
//                ArrayList<MyCFGNode> childList = new ArrayList<>();
//                currentNode = new MyCFGNode(subRoot,childList,"loop",0);
//                subRoot.getChilds().add(currentNode);
//                flag = true;
//            }
//
//            if (child instanceof BlockStmt && (child.getParentNode().get() instanceof IfStmt || child.getParentNode().get() instanceof WhileStmt||child.getParentNode().get() instanceof ForStmt )){
////                System.out.println("========================");
////                System.out.println("block:"+ child);
//                ArrayList<MyCFGNode> childList = new ArrayList<>();
//                currentNode = new MyCFGNode(subRoot,childList,"block",0);
//                subRoot.getChilds().add(currentNode);
//                flag = true;
//            }
//            if (child.getChildNodes() !=null){
//                if (flag){
//                    generatePathCFG_sub(currentNode,child);
//                }else {
//                    generatePathCFG_sub(subRoot,child);
//                }
//
//            }
//        }
//    }
//
//    public void generateELSEnode(MyCFGNode rootNode){
//        //补充else节点,为了方便遍历而不陷入死循环，先不给else节点增加连接
//        for (MyCFGNode child: rootNode.getChilds()){
//            if (child.getNodeType().equals("if") && child.getChilds().size()==1){//有可能是0吗？
//                ArrayList<MyCFGNode> ifElseNodeChild = new ArrayList<>();
////                ifElseNodeChild.add(getFirstLoopNode(child));
//                MyCFGNode ifElseNode = new MyCFGNode(child,ifElseNodeChild,"else",0);
//                child.getChilds().add(ifElseNode);
//            }
//            if (child.getNodeType().equals("loop")&&child.getChilds().size()==1){
//                ArrayList<MyCFGNode> whileElseNodeChild = new ArrayList<>();
////                whileElseNodeChild.add(getFirstLoopNode(child));
//                MyCFGNode whileElseNode = new MyCFGNode(child,whileElseNodeChild,"else",0);
//                child.getChilds().add(whileElseNode);
//            }
////            if (child.getNodeType().equals("block") && child.getChilds().size() ==0){//叶子节点，连接边
////                child.getChilds().add(getFirstLoopNode(child));
////            }
//            if (child.getChilds()!=null){
//                generateELSEnode(child);
//            }
//        }
//
//    }
//
//    public void generateElseEdge(MyCFGNode rootNode){
//        for (MyCFGNode child: rootNode.getChilds()){
//            if (child.getNodeType().equals("else")&&child.getParent().getNodeId()!=0&&child.getChilds().size()==0){
//                child.getChilds().add(getFirstLoopNode(child.getParent()));//必须从父节点开始寻找
//                continue;
//            }
//            if (child.getNodeType().equals("block")&&child.getChilds().size() ==0){
//                //叶子节点
//                child.getChilds().add(getFirstLoopNode(child));//从叶子节点出发
//                continue;
//            }
//            if (child.getChilds() != null){
//                generateElseEdge(child);
//            }
//        }
//    }
//
//    public void setNodeID(MyCFGNode rootNode){
//        //队列实现层次遍历
//        Queue<MyCFGNode> nodeQueue = new LinkedList<>();
//        nodeQueue.add(rootNode);
//        int ID_index=0;
//        while (nodeQueue.size()!=0){
//            MyCFGNode outputNode = nodeQueue.remove();
//            outputNode.setNodeId(ID_index);
//            ID_index+=1;
//            if (outputNode.getChilds()!=null){
//                for (MyCFGNode childNode: outputNode.getChilds()){
//                    nodeQueue.add(childNode);
//                }
//            }
//        }
//    }
//
//    public MyCFGNode getFirstLoopNode(MyCFGNode node){//后面要进行类型补充
//        while (!node.getParent().getNodeType().equals("loop")){
//            node = node.getParent();
//        }
//        return node.getParent();
//    }
//
//    //打印图
//    public void printCFGGraph(MyCFGNode rootNode){
//        //层次遍历增加边的联系
//        Queue<MyCFGNode> nodeQueue = new LinkedList<>();
//        ArrayList<int[]> startEndList= new ArrayList<>();//存储边，防止死循环。[[start,end],[start,end]]
//        nodeQueue.add(rootNode);
//        GraphViz gViz = new GraphViz("F:\\CProjectWorkspace", "D:\\Graphviz2.38\\bin\\dot.exe","parse");
//        gViz.start_graph();
//        while (nodeQueue.size() !=0){
//            MyCFGNode outputNode = nodeQueue.remove();
//            if (outputNode.getChilds()!=null){
//                for (MyCFGNode childNode: outputNode.getChilds()){
//                    int[] startEndSub = new int[2];
//                    startEndSub[0]=outputNode.getNodeId();
//                    startEndSub[1]=childNode.getNodeId();
//                    if (!judge_StartEnd(startEndList,startEndSub)){
//                        startEndList.add(startEndSub);
//                        gViz.addln((startEndSub[0] + "->" + startEndSub[1]+";"));
//                        nodeQueue.add(childNode);
//                    }
//                }
//            }
//        }
//        gViz.end_graph();
//        try{
//            gViz.run();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
    public Boolean judge_StartEnd(ArrayList<int[]> start_end_list, int[] start_end){
        if (start_end_list ==null){
            return false;
        }else {
            for (int[]child:start_end_list){
                if (child[0]==start_end[0]&&child[1]==start_end[1]){
                    return true;
                }
            }
        }
        return false;//不包含
    }
    public void VisitNode(Node loopNode){
        for (Node child: loopNode.getChildNodes()){
            System.out.println(child.getBegin().get().line+": "+child.getClass().getSimpleName()+": "+child);
            if (child.getChildNodes() !=null){
                VisitNode(child);
            }
        }

    }

    //深搜打印所有路径，两步：while节点->邻接while节点， while节点->自身while节点
//    public HashMap<Integer,Set<Integer>> getLinJieWhileHash(MyCFGNode loopNode){
//        HashMap<Integer,Set<Integer>> resultHash = new HashMap<>();
//        Queue<MyCFGNode> nodeQueue = new LinkedList<>();
//        ArrayList<Integer> existingNodeNums = new ArrayList<>();
//        nodeQueue.add(loopNode);
//        existingNodeNums.add(loopNode.getNodeId());
//        while (nodeQueue.size()!=0){
//            MyCFGNode outputNode = nodeQueue.remove();
//            if (outputNode.getNodeType().equals("loop")){
//                Set<Integer> linJieSet = LinJieWhileSet(outputNode);
//                if (!resultHash.keySet().contains(outputNode.getNodeId())){
//                    resultHash.put(outputNode.getNodeId(),linJieSet);
//                }
//            }
//            if (outputNode.getChilds()!=null){
//                for (MyCFGNode child:outputNode.getChilds()){
//                    if (!existingNodeNums.contains(child.getNodeId())){
//                        nodeQueue.add(child);
//                        existingNodeNums.add(child.getNodeId());
//                    }
//                }
//            }
//        }
//        return resultHash;
//    }
//    public Set<Integer> LinJieWhileSet(MyCFGNode whileNode){
//        Queue<MyCFGNode> nodeQueue = new LinkedList();
//        ArrayList<Integer> existingNodeNums = new ArrayList<>();
//        Set<Integer> result = new HashSet<>();
//        nodeQueue.add(whileNode);
//
//        existingNodeNums.add(whileNode.getNodeId());
//        while(nodeQueue.size()!=0){
//            MyCFGNode outputNode = nodeQueue.remove();
//            if (outputNode.getChilds()!=null){
//                for (MyCFGNode child: outputNode.getChilds()){
//                    if (child.getNodeType().equals("loop") && !existingNodeNums.contains(child.getNodeId())){
//                        result.add(child.getNodeId());
//                    }
//                    if (!existingNodeNums.contains(child.getNodeId())){
//                        existingNodeNums.add(child.getNodeId());
//                        nodeQueue.add(child);
//                    }
//                }
//            }
//        }
//        return result;
//    }
//    //返回whilenode的自身调用路径，调用路劲过程中不涉及其他while
//    public HashMap<Integer,Set<Integer>> getInterLoopPath(MyCFGNode whileNode){
//        HashMap<Integer,Set<Integer>> resultHash = new HashMap<>();
//        Queue<MyCFGNode> nodeQueue = new LinkedList<>();
//        ArrayList<Integer> existingNodeNums = new ArrayList<>();
//        nodeQueue.add(whileNode);
//        existingNodeNums.add(whileNode.getNodeId());
//        while (nodeQueue.size()!=0){
//            MyCFGNode outputNode = nodeQueue.remove();
//            if (outputNode.getNodeType().equals("loop")){
//                Set<Integer> ziShenSet = getInterLoopPath_SUB(outputNode);
//                if (!resultHash.keySet().contains(outputNode.getNodeId())){
//                    resultHash.put(outputNode.getNodeId(),ziShenSet);
//                }
//            }
//            if (outputNode.getChilds()!=null){
//                for (MyCFGNode child:outputNode.getChilds()){
//                    if (!existingNodeNums.contains(child.getNodeId())){
//                        nodeQueue.add(child);
//                        existingNodeNums.add(child.getNodeId());
//                    }
//                }
//            }
//        }
//        return resultHash;
//    }
//
//    public Set<Integer> getInterLoopPath_SUB(MyCFGNode subNode){
//        Queue<MyCFGNode> nodeQueue = new LinkedList<>();
//        nodeQueue.add(subNode);
//        ArrayList<Integer> existingNodeNums= new ArrayList<>();
//        existingNodeNums.add(subNode.getNodeId());
//        Set<Integer> result = new HashSet<>();
//        while (nodeQueue.size()!=0){
//            MyCFGNode outputNode = nodeQueue.remove();
//            if (outputNode.getChilds()!=null){
//                for (MyCFGNode child: outputNode.getChilds()){
//                    if (child.getNodeId()==subNode.getNodeId()){
//                        result.add(outputNode.getNodeId());
//                    }
//                    if (!existingNodeNums.contains(child.getNodeId()) && !child.getNodeType().equals("loop")){
//                        existingNodeNums.add(child.getNodeId());
//                        nodeQueue.add(child);
//                    }
//                }
//            }
//        }
//        return result;
//    }


//    public ArrayList<ArrayList<String>> generetaPath(MyCFGNode rootNode){
//        ArrayList<ArrayList<String>> resultPathList = new ArrayList<>();
//        int graph_length = getNodeNum(rootNode);
//        int[] visit = new int[graph_length];//用于在dfs中记录已访问顶点
//        ArrayList<Integer> path = new ArrayList<>();//存储每一条可能路径
//        ArrayList<String> ans = new ArrayList<>();//存储所有路径
//
//        int[][] graphMap = generateLinJieGraph(rootNode);
//        //while->邻接while
//        HashMap<Integer,Set<Integer>>linjieWhileHash = getLinJieWhileHash(rootNode);
//        for (int keyIndex:linjieWhileHash.keySet()){
//            if (linjieWhileHash.get(keyIndex).size()!=0){
//                for (int end:linjieWhileHash.get(keyIndex)){
//                    generatePath_dfs(graphMap,visit,path,ans,keyIndex,end,graph_length);
//                    for (String resultString:ans){
//                        resultPathList.add(String_to_list(resultString));
//                    }
//                    path=new ArrayList<>();
//                    ans=new ArrayList<>();
//                    visit=new int[graph_length];
//                }
//            }
//        }
//
//        //while->自身while
//        HashMap<Integer,Set<Integer>>ziShenWhileHash = getInterLoopPath(rootNode);
//        for (int keyIndex:ziShenWhileHash.keySet()){
//            if (ziShenWhileHash.get(keyIndex).size()!=0){
//                for (int end: ziShenWhileHash.get(keyIndex)){
//                    generatePath_dfs(graphMap,visit,path,ans,keyIndex,end,graph_length);
//                    for (String resultString:ans){
//                        ArrayList<String> getResultList = String_to_list(resultString);
//                        getResultList.add(String.valueOf(keyIndex));
//                        resultPathList.add(getResultList);
//                    }
//                    path= new ArrayList<>();
//                    ans = new ArrayList<>();
//                    visit=new int[graph_length];
//                }
//            }
//        }
//
//        //开始路径，结束路径
//        resultPathList.add(new ArrayList<String>(Arrays.asList("-1", "0")));
//        resultPathList.add(new ArrayList<>(Arrays.asList("0","2")));
//
//        return resultPathList;
//    }
    public ArrayList<String> String_to_list(String inputString){
        //[3,4,5]
        return new ArrayList<String>(Arrays.asList(inputString.replaceAll("\\[","").replaceAll("\\]","").replaceAll(" ","").split(",")));
    }
    public void generatePath_dfs(int[][] graphMap,int[] visit,ArrayList<Integer> path,ArrayList<String> ans, int u, int end, int graph_length){
        visit[u] =1;
        path.add(u);
        if(u == end){
            ans.add(path.toString());
        }else{
            for (int i = 0; i < graph_length; i++) {
                if(visit[i]==0&&i!=u&&graphMap[u][i]==1){
                    generatePath_dfs(graphMap,visit,path,ans,i,end,graph_length);
                }
            }
        }
        path.remove(path.size()-1);
        visit[u] = 0;
    }
    //生成邻接表
//    public int[][] generateLinJieGraph(MyCFGNode rootNode){
//        //统计节点个数
//        int graphSize = getNodeNum(rootNode);
//        int [][] graphMap = new int[graphSize][graphSize];
//        //层次遍历，填写表格
//        Queue<MyCFGNode> nodeQueue = new LinkedList<>();
//        ArrayList<Integer> existingNodeNum = new ArrayList<>();
//        nodeQueue.add(rootNode);
//        existingNodeNum.add(0);
//        while (nodeQueue.size()!=0){
//            MyCFGNode outputNode = nodeQueue.remove();
//            if (outputNode.getChilds()!=null){
//                for (MyCFGNode child: outputNode.getChilds()){
//                    graphMap[outputNode.getNodeId()][child.getNodeId()] =1;
//                    if (!existingNodeNum.contains(child.getNodeId())){
//                        existingNodeNum.add(child.getNodeId());
//                        nodeQueue.add(child);
//                    }
//                }
//            }
//        }
//        return graphMap;
//    }
//
//    public int getNodeNum(MyCFGNode rootNode){
//        //深搜太容易死循环了，还是广搜吧
//        Queue<MyCFGNode> nodeQueue = new LinkedList<>();
//        ArrayList<Integer> existIngNumList = new ArrayList<>();//存储已经遍历的数字
//        existIngNumList.add(0);//存入根节点
//        nodeQueue.add(rootNode);
//        while (nodeQueue.size()!=0){
//            MyCFGNode outputNode = nodeQueue.remove();
//            if (outputNode.getChilds().size()!=0){
//                for (MyCFGNode child:outputNode.getChilds()){
//                    if (!existIngNumList.contains(child.getNodeId())){
//                        existIngNumList.add(child.getNodeId());
//                        nodeQueue.add(child);
//                    }
//                }
//            }
//
//        }
//        return Collections.max(existIngNumList)+1;
//    }
//
//    public boolean judgePDA(ArrayList<ArrayList<String>> pathList){
//        int[][] graphMap = generateLinJieGraph(pathList);
//        int graph_length = pathList.size();
//        int[] visit = new int[graph_length];//用于在dfs中记录已访问顶点
//        ArrayList<Integer> path = new ArrayList<>();//存储每一条可能路径
//        ArrayList<String> ans = new ArrayList<>();//存储所有路径
//        for (int x = 0;x<graph_length;x++){
//            for (int y=x+1;y<graph_length;y++){
//                generatePath_dfs(graphMap,visit,path,ans,x,y,graph_length);
//                int firstPathNum = ans.size();
//                System.out.println(ans);
//                path= new ArrayList<>();
//                ans = new ArrayList<>();
//                visit=new int[graph_length];
//                generatePath_dfs(graphMap,visit,path,ans,y,x,graph_length);
//                int secondPathNum = ans.size();
//                System.out.println(ans);
//                System.out.println("----------");
//                path= new ArrayList<>();
//                ans = new ArrayList<>();
//                visit=new int[graph_length];
//                if ((firstPathNum>1 || secondPathNum>1)&&firstPathNum>0&& secondPathNum>0){
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
    public int[][] generateLinJieGraph(ArrayList<ArrayList<String>> pathList){
        int graphSize = pathList.size();
        int [][] graphMap = new int[graphSize][graphSize];
        for (int lineIndex=0;lineIndex<graphSize;lineIndex++){
            String endIndex =pathList.get(lineIndex).get(pathList.get(lineIndex).size()-1);
            for (int lineIndex_j = 0;lineIndex_j<graphSize;lineIndex_j++){
                if (lineIndex_j!=lineIndex &&pathList.get(lineIndex_j).get(0).equals(endIndex)){
                    //递归不考虑
                    graphMap[lineIndex][lineIndex_j] =1;
                }
            }
        }
        return graphMap;
    }

//    /**--------------------------------------------克隆代码检测----------------------------------------------**/
//    //当前循环结构与代码文件中其他循环结构相似度，返回最大相似度
//    public double getMaxCodeSim(Node loopNode,Node rootNode) throws Exception {
//        //原来存在一个问题，嵌套循环中，嵌套的循环必定是1，解决方法，可以修改返回值为一个数组，这边执行一下减法操作,这个解决方法不行。
//        duplicateCode DCobj = new duplicateCode();
//        return DCobj.simCalInterface(loopNode,rootNode);
//    }

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
        String FilePath = "C:\\Users\\57169\\eclipse-workspace\\Library2\\src\\li\\Book.java";
        ThirdFactor obj = new ThirdFactor();
        CompilationUnit compilationUnit = obj.getRoot(FilePath);
//        obj.getFileGlobalVariaAcompilationUnitNDStatic(compilationUnit);
        obj.ParseAST(compilationUnit,compilationUnit);
//        obj.CC_test(FilePath);
    }



}
