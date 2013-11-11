/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package psxmlconv;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
/**
 *
 * @author tdandrea
 */
public class psxmlconv {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    Boolean verbose_flag = true;
    String path = System.getProperty("user.dir"); 
    path = path + "/LXDMOCOMPARE9_04_13";
    String copyToDir = "/copy_dir/";
    ArrayList<String> ps_filelist = new ArrayList<>();

    if (true == project_exists(path)) {
      System.out.println("Project definition exists, begin file loop.");
    }
    else {
      System.out.println("Project XML Definition could not be found in the project root directory.");
      System.exit(1);
    }
    clear_copy_dir(path + copyToDir);
    File project_path = new File(path);
    ArrayList<File> directories = get_dir_list(project_path, copyToDir);
    ArrayList<File> ps_project_files = get_ps_project_files(directories, verbose_flag);
    if(verbose_flag) {
      show_project_file_list(ps_project_files);
    }
    copy_for_processing(ps_project_files, path + copyToDir);
    System.exit(0);
  }
  
  private static void show_project_file_list(ArrayList<File> files) {
    System.out.println("=============================================");
    for (File f : files) {
      System.out.println(f.getAbsolutePath());
    }
    System.out.println("=============================================");
  }
  
  private static void copy_for_processing(ArrayList<File> files, String copy_path) {
    boolean error_flag = false;
    for (File f : files) {
      String newFileName = f.getName().replaceAll(" ","_").toUpperCase();
      Integer lastUnderscore = newFileName.lastIndexOf("_");
      Integer lastFileSuffix = newFileName.lastIndexOf("XML");
      String fileNumber = newFileName.substring(lastUnderscore + 1, lastFileSuffix);
      String newFileNumber = "0000".substring(0,5 - fileNumber.length()) + fileNumber;
      newFileName = newFileName.substring(0, lastUnderscore + 1) + newFileNumber + newFileName.substring(lastFileSuffix);
      File new_file = new File(copy_path + newFileName);
      try {
        Files.copy(f.toPath(), new_file.toPath());
      } catch (IOException ex) {
        error_flag = true;
        Logger.getLogger(psxmlconv.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    if (true == error_flag) {
      System.out.println("=============================================");
      System.out.println("An error has occurred during file copy processing.");
      System.exit(1);
    }
  }
  
  private static boolean project_exists(String p) {
    File project_file = new File(p + "/project.xml");
    return project_file.exists();
  }
  
  private static ArrayList get_ps_project_files(ArrayList<File> directories, Boolean verbose) {
    ArrayList<File> xml_files = new ArrayList<>();
    for (File dirs : directories) {
      if (verbose) {
        System.out.println(dirs.getAbsolutePath());
        System.out.println("    " + dirs.getName());
      }
      File filePath = new File(dirs.getAbsolutePath());
      ArrayList<File> files = new ArrayList<>(Arrays.asList(filePath.listFiles()));
      for (File fls : files) {
        if (!("Index.xml".equalsIgnoreCase(fls.getName()))) {
          xml_files.add(fls);
        }
      }
    }
    return xml_files;
  }
  
  private static void clear_copy_dir(String copy_dir) {
    File filePath = new File(copy_dir);
    ArrayList<File> files = new ArrayList<>(Arrays.asList(filePath.listFiles()));
    for (File f : files) {
      boolean delete = f.delete();
    }
  }
  
  private static ArrayList get_dir_list(File directory_lookup, String copy_dir) {
    ArrayList<File> files = new ArrayList<>(Arrays.asList(directory_lookup.listFiles()));
    ArrayList<File> directories = new ArrayList<>();
    for (File f : files) {
      if (f.isDirectory()) {
        if (!f.getName().toUpperCase().contains(copy_dir.replace("/", "").toUpperCase())) {
          directories.add(f);
        }
      }
    }
    return directories;
  }
  
  protected Node getNode(String tagName, NodeList nodes) {
      for ( int x = 0; x < nodes.getLength(); x++ ) {
          Node node = nodes.item(x);
          if (node.getNodeName().equalsIgnoreCase(tagName)) {
              return node;
          }
      }
      return null;
  }

  protected String getNodeValue( Node node ) {
      NodeList childNodes = node.getChildNodes();
      for (int x = 0; x < childNodes.getLength(); x++ ) {
          Node data = childNodes.item(x);
          if ( data.getNodeType() == Node.TEXT_NODE )
              return data.getNodeValue();
      }
      return "";
  }

  protected String getNodeValue(String tagName, NodeList nodes ) {
      for ( int x = 0; x < nodes.getLength(); x++ ) {
          Node node = nodes.item(x);
          if (node.getNodeName().equalsIgnoreCase(tagName)) {
              NodeList childNodes = node.getChildNodes();
              for (int y = 0; y < childNodes.getLength(); y++ ) {
                  Node data = childNodes.item(y);
                  if ( data.getNodeType() == Node.TEXT_NODE )
                      return data.getNodeValue();
              }
          }
      }
      return "";
  }

  protected String getNodeAttr(String attrName, Node node ) {
      NamedNodeMap attrs = node.getAttributes();
      for (int y = 0; y < attrs.getLength(); y++ ) {
          Node attr = attrs.item(y);
          if (attr.getNodeName().equalsIgnoreCase(attrName)) {
              return attr.getNodeValue();
          }
      }
      return "";
  }

  protected String getNodeAttr(String tagName, String attrName, NodeList nodes ) {
      for ( int x = 0; x < nodes.getLength(); x++ ) {
          Node node = nodes.item(x);
          if (node.getNodeName().equalsIgnoreCase(tagName)) {
              NodeList childNodes = node.getChildNodes();
              for (int y = 0; y < childNodes.getLength(); y++ ) {
                  Node data = childNodes.item(y);
                  if ( data.getNodeType() == Node.ATTRIBUTE_NODE ) {
                      if ( data.getNodeName().equalsIgnoreCase(attrName) )
                          return data.getNodeValue();
                  }
              }
          }
      }

      return "";
  }

  protected void setNodeValue(String tagName, String value, NodeList nodes) {
      Node node = getNode(tagName, nodes);
      if ( node == null )
          return;

      // Locate the child text node and change its value
      NodeList childNodes = node.getChildNodes();
      for (int y = 0; y < childNodes.getLength(); y++ ) {
          Node data = childNodes.item(y);
          if ( data.getNodeType() == Node.TEXT_NODE ) {
              data.setNodeValue(value);
              return;
          }
      }
  }

  protected void addNode(String tagName, String value, Node parent) {
      Document dom = parent.getOwnerDocument();

      // Create a new Node with the given tag name
      Node node = dom.createElement(tagName);

      // Add the node value as a child text node
      Node nodeVal = dom.createTextNode(value);
      Node c = node.appendChild(nodeVal);

      // Add the new node structure to the parent node
      parent.appendChild(node);
  }
}
