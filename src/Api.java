import com.github.javaparser.ast.Node;
import org.neo4j.graphdb.GraphDatabaseService;

public interface Api {
    /**--------------------------------------------1 返回代码行数----------------------------------------------**/
    public int getLoopLineNum(Node root,Node rootNode);

    /**--------------------------------------------2 返回代码注释行数----------------------------------------------**/
    public int getCommentNum(Node root,Node rootNode);

    /**--------------------------------------------3 返回代码调用函数数量----------------------------------------------**/
    public int getCallFuncNum(Node root,Node rootNode);

    /**--------------------------------------------4 返回代码API数量----------------------------------------------**/
    public int getAPINum(Node loopNode,Node rootNode);

    /**--------------------------------------------5 返回中对象数量----------------------------------------------**/
    public int getObjNum(Node loopNode);

    /**--------------------------------------------6 返回代码中局部变量数量----------------------------------------------**/
    //包括静态局部变量的数量！！！是否要上升到循环---所属函数---中局部变量。
    public int getLocalVariableNum(Node loopNode);

    //返回循环结构所属函数节点
    public Node getLoopMethodNode(Node indexNode);

    /**--------------------------------------------7 获取编译单元全局变量 以及 静态变量----------------------------------------------**/
    public void getFileGlobalVariaANDStatic(Node rootNode);
    /**--------------------------------------------8 获取代码中全部变量数量----------------------------------------------**/
    //用数组存储局部变量,循环结构体所属函数包含的局部变量
    public int getVariableNum(Node loopNode);

    /**--------------------------------------------9获取代码中HashMap数量----------------------------------------------**/
    public int getLoopHashNum(Node loopNode);
    /**--------------------------------------------扇入扇出计算----------------------------------------------**/
    public int calIn(Node loopNode, GraphDatabaseService db);
}
