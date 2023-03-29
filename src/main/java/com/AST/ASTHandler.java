package main.java.com.AST;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.printer.YamlPrinter;
import com.zzx.config.Constant;
import com.github.javaparser.ast.CompilationUnit;
import org.eclipse.jdt.core.dom.AST;
//import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ASTHandler {

    private CompilationUnit cu = null;

    public ASTHandler(String FileName) {
        String filePath = Constant.PATH + FileName;

        // 使用JavaParser解析Java文件并生成AST
        try {
            cu = StaticJavaParser.parse(new File(filePath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void ASTPrint() {
        YamlPrinter printer = new YamlPrinter(true);
        System.out.println(printer.output(cu));
    }

    public CompilationUnit getCu() {
        return cu;
    }

    public static void main(String[] args) throws IOException {
        String filePath = Constant.PATH + "HeapSort.java";

        // 使用JavaParser解析Java文件并生成AST
        CompilationUnit cu = StaticJavaParser.parse(new File(filePath));
        String simpleName = cu.getClass().getSimpleName();
        //compilationUnit
        YamlPrinter printer = new YamlPrinter(true);
        System.out.println(printer.output(cu));
    }

//    private void print(ASTNode node) {
//        List properties = node.structuralPropertiesForType();
//        for (Iterator iterator = properties.iterator(); iterator.hasNext();) {
//            Object descriptor = iterator.next();
//            if (descriptor instanceof SimplePropertyDescriptor) {
//                SimplePropertyDescriptor simple = (SimplePropertyDescriptor) descriptor;
//                Object value = node.getStructuralProperty(simple);
//                System.out.println(simple.getId() + " (" + value.toString() + ")");
//            } else if (descriptor instanceof ChildPropertyDescriptor) {
//                ChildPropertyDescriptor child = (ChildPropertyDescriptor) descriptor;
//                ASTNode childNode = (ASTNode) node.getStructuralProperty(child);
//                if (childNode != null) {
//                    System.out.println("Child (" + child.getId() + ") {");
//                    print(childNode);
//                    System.out.println("}");
//                }
//            } else {
//                ChildListPropertyDescriptor list = (ChildListPropertyDescriptor) descriptor;
//                System.out.println("List (" + list.getId() + "){");
//                print((List) node.getStructuralProperty(list));
//                System.out.println("}");
//            }
//        }
//    }
//
//    private void print(List nodes) {
//        for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
//            print((ASTNode) iterator.next());
//        }
//    }

}
