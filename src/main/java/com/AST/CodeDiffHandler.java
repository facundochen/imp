package main.java.com.AST;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.zzx.config.Constant;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;


public class CodeDiffHandler {

    private Patch<String> patch = null;
    List<String> original = null;
    List<String> revised = null;

    public CodeDiffHandler(String FileName1, String FileName2) {
        //对比文件
        try {
            original = Files.readAllLines(new File(Constant.PATH + FileName1).toPath());
            revised = Files.readAllLines(new File(Constant.PATH + FileName2).toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //两文件的不同点
        patch = DiffUtils.diff(original, revised);
    }

    public void CodeDiffPrint() throws IOException {
        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff("HeapSort.java", "HeapSort1.java", original, patch, 0);
        unifiedDiff.forEach(System.out::println);
    }

    public Patch<String> getPatch() {
        return patch;
    }

    public static void main(String[] args) {
     CodeDiffHandler zzx =  new CodeDiffHandler("HeapSort.java","HeapSort1.java");
//        try {
//            zzx.CodeDiffPrint();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        for (int i= 0; i< zzx.getPatch().getDeltas().size();i++)
        System.out.println(zzx.getPatch().getDeltas().get(i));

    }

}
