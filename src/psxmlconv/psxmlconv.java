/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package psxmlconv;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    ArrayList<File> files = new ArrayList<>(Arrays.asList(project_path.listFiles()));
    ArrayList<File> directories = new ArrayList<>();
    for (File f : files) {
      if (f.isDirectory()) {
        directories.add(f);
      }
    }
    
    ArrayList<File> ps_project_files = new ArrayList<>();
    for (File f : directories) {
      System.out.println(f.getAbsolutePath());
      System.out.println("    " + f.getName());
      File filePath = new File(f.getAbsolutePath());
      ArrayList<File> xmlfiles = new ArrayList<>(Arrays.asList(filePath.listFiles()));
      for (File x : xmlfiles) {
        if (!("Index.xml".equalsIgnoreCase(x.getName()))) {
          ps_project_files.add(x);
        }
      }
    }
    
    System.out.println("=============================================");
    for (File f : ps_project_files) {
      System.out.println(f.getAbsolutePath());
    }
    System.out.println("=============================================");
    System.exit(0);
  }
  
  public static boolean project_exists(String p) {
    File project_file = new File(p + "/project.xml");
    return project_file.exists();
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
