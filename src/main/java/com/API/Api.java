package main.java.com.API;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import org.neo4j.graphdb.GraphDatabaseService;

import java.io.FileNotFoundException;

public interface Api {
    /**--------------------------------------------1 返回代码行数----------------------------------------------**/
    public int getLoopLineNum(Node root,Node rootNode);

    /**--------------------------------------------2 返回代码注释行数----------------------------------------------**/
    public int getCommentNum(Node root);
    /**--------------------------------------------3 返回代码API数量----------------------------------------------**/

    public int getAPINum(Node loopNode,Node rootNode);

    /**--------------------------------------------4 5 获取循环中全局变量数量----------------------------------------------**/
    //用数组存储局部变量,循环结构体所属函数包含的局部变量
    public void getFileGlobalVariaANDStatic(Node rootNode);

    /**--------------------------------------------6 返回代码中局部变量数量----------------------------------------------**/
    //包括静态局部变量的数量！！！是否要上升到循环---所属函数---中局部变量。
    public int getLocalVariableNum(Node loopNode);

    /**--------------------------------------------7 获取代码中全部变量数量----------------------------------------------**/
    //用数组存储局部变量,循环结构体所属函数包含的局部变量
    public int getVariableNum(Node loopNode);
    /**--------------------------------------------8 返回中对象数量----------------------------------------------**/
//    public int getObjNum(Node loopNode);

    /**--------------------------------------------9获取代码中HashMap数量----------------------------------------------**/
    public int getLoopHashNum(Node loopNode);
    /**--------------------------------------------10 返回代码调用函数数量----------------------------------------------**/
    public int getCallFuncNum(Node root);

    /**--------------------------------------------12 最大等级计算----------------------------------------------**/
    public int getMaxDegree(Node loopNode);
    /**--------------------------------------------13 中心性计算----------------------------------------------**/
    public int get_degree_centrality(Node loopNode,GraphDatabaseService db,int diGuiFlag);

    //紧密中心性：到其他节点的最短距离，越少越重要，无法到达的节点距离暂时定为10
    public int get_closeness_centrality(Node loopNode,GraphDatabaseService db);

    /**--------------------------------------------14 15扇入扇出计算----------------------------------------------**/
    public int calIn(Node loopNode, GraphDatabaseService db, int diGuiFlag);
    public int calOut(Node loopNode,GraphDatabaseService db,int diFuiFlag);
    public void CountcalIn(Node root,Node rootNode);

    /**-------------------------------------------- 21 22 数据库修改统计----------------------------------------------**/
    public int[] getDataBaseModifyNum(Node loopNode);

    /**--------------------------------------------16 数据库相关性计算----------------------------------------------**/
    public int getDataBaseRelation(Node loopNode);

    /**--------------------------------------------17 循环结构体中无效代码行数----------------------------------------------**/
    //无效代码包括：打印语句，和未被使用的声明语句
    public int getWuxiaoCode(Node loopNode);
    /**--------------------------------------------18 catch语句统计----------------------------------------------**/
    public int getCatchNum(Node loopNode);
    /**--------------------------------------------24 可移动代码检测----------------------------------------------**/
    public int getMovelineNum(Node loopNode);


    public CompilationUnit getRoot(String filePath) throws FileNotFoundException;
}
