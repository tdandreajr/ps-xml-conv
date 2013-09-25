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
    String path = System.getProperty("user.dir"); 
    ArrayList<String> ps_filelist = new ArrayList<>();

    if (project_exists(path)!= true) {
      System.out.println("Project XML Definition could not be found in the project root directory.");
      System.exit(1);
    }
    System.out.println("Project definition exists, begin file loop.");
    File project_path = new File(path);
    ArrayList<File> files = new ArrayList<File>(Arrays.asList(project_path.listFiles()));
    ArrayList<File> directories = new ArrayList<File>();
    int counter = 0;
    for (File f : files) {
      if (f.isDirectory()) {
        directories.add(f);
        counter++;
      }
    }
    
    int dcounter = 0;
    for (File f : directories) {
      System.out.println(f.getAbsolutePath());
      dcounter++;
    }
    
    System.out.println("------------------------------");
    System.out.println("File counter:" + String.valueOf(counter));
    System.out.println("Dir counter :" + String.valueOf(dcounter));
    System.exit(0);
  }
  
  public static boolean project_exists(String p) {
    File project_file = new File(p + "/project.xml");
    return project_file.exists();
  }
}
