/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package psxmlconv;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
/**
 *
 * @author tdandrea
 */
public class psxmlconv {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    String path = "."; 
    ArrayList<String> ps_filelist = new ArrayList<>();

    if (project_exists(path)!= true) {
      System.out.println("Project XML Definition could not be found in the project root directory.");
      System.exit(1);
    }
    
    File project_path = new File(path);
    ArrayList<File> ps_dirlist;
    ps_dirlist = new ArrayList<File>(Arrays.asList(project_path.listFiles()));
    for (File psd : ps_dirlist) {
      if (psd.isDirectory()) {
        System.out.println(psd.toString());
    }
    System.exit(0);
    }
  }
  
  public static boolean project_exists(String p) {
    File project_file = new File(p + "\\project.xml");
    return project_file.exists();
  }
  
  public static void temp_method() {
    String path = "";
    String files = "";
    File folder = new File(path);
    File[] listOfFiles = folder.listFiles(); 
    for (int i = 0; i < listOfFiles.length; i++) {
      if (listOfFiles[i].isFile()) {
        files = listOfFiles[i].getName();
        if (files.endsWith(".sed") || files.endsWith(".SED")) {
          System.out.println(files);
        }
      }
    }
  }
}
