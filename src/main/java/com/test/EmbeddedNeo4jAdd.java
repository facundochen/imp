package main.java.com.test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import java.io.File;
public class EmbeddedNeo4jAdd {
//    private static final File databaseDirectory = new File( "target/zzx.db" );
    private static final File databaseDirectory = new File( "D:\\JavaSlicer7.1\\Repository\\Graphs\\wiki.db" );
    public static void main(String[] args) {
        int i=0;
        int n=0;
        for(i=0;i<10;i++)
        {
            n++;
        }
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(String.valueOf(databaseDirectory));
        System.out.println("Database Load!");
        Transaction tx = graphDb.beginTx();
//        Node n1 = graphDb.createNode();
//        n1.setProperty("name", "张三");
//        n1.setProperty("character", "A");
//        n1.setProperty("gender",1);
//        n1.setProperty("money", 1101);
//        n1.addLabel(new Label() {
//            @Override
//            public String name() {
//                return "Person";
//            }
//        });
//        String cql = "CREATE (p:Person{name:'李四',character:'B',gender:1,money:21000})";
        String cql = "MATCH (n:ClassNode) RETURN n LIMIT 25";
        Result execute = graphDb.execute(cql);
//        execute.resultAsString();
        System.out.println(execute.resultAsString());
        tx.success();
        tx.close();
        System.out.println("Database Shutdown!");
        graphDb.shutdown();
    }
}
