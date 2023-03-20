import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;

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
import  org.neo4j.graphdb.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;


//import com.iseu.nanhang.util.MyCFGNode;
//import com.iseu.nanhang.util.GraphViz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
public class test {
    static  String DB_PATH = "C:\\Users\\57169\\Desktop\\毕设用\\新建文件夹\\Neo4J\\JavaSlicer7.1\\Repository\\Graphs\\Library2.db";
    public static void main(String[] args) throws Exception {
        String FilePath = "C:\\Users\\57169\\eclipse-workspace\\Library2\\src\\li\\Book.java";
        ApiImpl obj = new ApiImpl();
        CompilationUnit compilationUnit = obj.getRoot(FilePath);
        System.out.println("码中局部变量数量:"+obj.getLocalVariableNum(compilationUnit));
        System.out.println("码中所有变量数量:"+obj.getVariableNum(compilationUnit));
        System.out.println("码中hash数量:"+obj.getLoopHashNum(compilationUnit));
        obj.getFileGlobalVariaANDStatic(compilationUnit);
        System.out.println("全局变量有"+obj.globalVariaNumList.toString()+"静态变量有"+obj.staticVariaNumList.toString());
        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
        new test().ParseAST(compilationUnit,compilationUnit,db);

    }

    public void ParseAST(Node root,Node rootNode, GraphDatabaseService db) throws Exception {

        if (root.getChildNodes()!=null){
            for (Node child: root.getChildNodes()){
                if (child instanceof MethodDeclaration){
                    System.out.println("入度为"+new ApiImpl().calIn(child,db));
                    System.out.println("出度为"+new ApiImpl().calOut(child,db));
                }
                ParseAST(child,rootNode,db);
            }
        }
    }

}
