/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkapplicationrmi;


import java.awt.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
//import org.apache.tools.ant.DirectoryScanner;

/**
 *
 * @author adnkh
 */
public class SearchHelper {
    
//    public static String [] SearchFileInPath(String fileName, String PATH) {                
//        
//        DirectoryScanner scanner = new DirectoryScanner();
//        String searchString  = "**\\*" + fileName;
//        System.out.println(searchString);
//        scanner.setIncludes(new String[]{searchString});
//        scanner.setBasedir(PATH);
//        scanner.setCaseSensitive(false);
//        scanner.scan();
//        String[] files = scanner.getIncludedFiles();
//        
//        for(int i = 0;i<files.length;i++){
//            files[i] = PATH + files [i];            
//        }
//        
//        return files ;
//    }
    
    public static ArrayList<FileProperty> searchForPattern(String pattern, String PATH, String serverName,boolean recursively){
        
        File folder = new File(PATH);
        
        ArrayList<FileProperty> files = new ArrayList<FileProperty>();
        
        try {
           files = searchInFiles(folder, pattern, files, serverName,recursively);
        } catch (FileNotFoundException e1) {
           // you should tell the user here that something went wrong
        }
        
        return files;
    }
    
    public static ArrayList <FileProperty> searchInFiles(File file, String pattern, ArrayList <FileProperty> result, String serverName,boolean recursively) throws FileNotFoundException {
        
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("file has to be a directory");
        }
        
        if (result == null) {
            result = new ArrayList<FileProperty>();
        }

        File[] files = file.listFiles();

        if (files != null) {
            for (File currentFile : files) {
                if (currentFile.isDirectory()&&recursively) {
                    searchInFiles(currentFile, pattern, result, serverName,recursively);
                } else {
                    try{
                        String filePath = currentFile.getAbsolutePath();
                        String extention = filePath.substring(filePath.length()-3);
                        
                        if(!extention.equals("txt") || !currentFile.canRead())
                            continue;
                        
                        System.out.println(filePath);
                        
                        Scanner scanner = new Scanner(currentFile);
                        
                        if(scanner != null){
                            if (scanner.findWithinHorizon(pattern, 0) != null) {
                                FileProperty fp = new FileProperty(currentFile.getPath(), currentFile.getName(), serverName);
                                result.add(fp);
                            }
                            scanner.close();
                        }
                    }catch (Error e){
                        e.printStackTrace();
                    }
                    
                }
            }
        }
        return result;
}
    
    public static String readFile(String path, Charset encoding) throws IOException 
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

}
