package main.java.com.API;

import com.github.javaparser.ast.CompilationUnit;
import com.zzx.config.Constant;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.FileNotFoundException;


public class zzx {
    public static void main(String[] args) {
        Api api = new ApiImpl();
        String FilePath = Constant.FilePath;

//        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(DBPATH);

        CompilationUnit compilationUnit = null;
        try {
            compilationUnit = api.getRoot(FilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //api.CountcalIn(compilationUnit,compilationUnit);
       // System.out.println( api.getDataBaseModifyNum(compilationUnit));
//        System.out.println( api.getDataBaseRelation(compilationUnit));
        System.out.println(api.getMovelineNum(compilationUnit));

        //2System.out.println(api.getCommentNum(compilationUnit));
       //3 System.out.println(api.getAPINum(compilationUnit,compilationUnit));

        //api.getFileGlobalVariaANDStatic(compilationUnit);
//        System.out.println( ((ApiImpl) api).globalVariaNumList.size());
//        System.out.println( ((ApiImpl) api).staticVariaNumList.size());
        //10 System.out.println(api.getCallFuncNum(compilationUnit));
    }
}
