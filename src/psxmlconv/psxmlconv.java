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
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author tdandrea
 */
public class psxmlconv {

  public static void main(String[] args) {
    /* Process main arguments */
    Boolean verboseFlag = false;
    String path = new String();
    String copyDir = "copy_dir";
    String gitDir = "git_dir";
    String slash = "/";
    if (System.getProperty("os.name").startsWith("Windows")) {
      slash = "\\";
    }
    copyDir = copyDir + slash;
    gitDir = gitDir + slash;
    if (args.length < 2 || args.length > 3) {
      System.err.println("Invalid nuber of arguments provided.");
      show_help();
      System.exit(1);
    }
    if ("-d".equals(args[0])) {
      path = args[1];
    } else {
      show_help();
      System.exit(1);
    }
    if (args.length == 3) {
      if ("-v".equals(args[2])) {
        verboseFlag = true;
      } else {
        System.err.println("Invalid arguments values provided.");
        show_help();
        System.exit(1);
      }
    }

    /* Test for project definition */
    ArrayList<String> ps_filelist = new ArrayList<>();
    if (project_exists(path)) {
      if (verboseFlag) {
        System.out.println("Project definition exists, begin file loop.");
      }
    } else {
      System.err.println("Project XML Definition could not be found in the project root directory.");
      System.exit(1);
    }

    /* Create and/or clear copy and git directories */
    if (!clear_dir(path + copyDir, verboseFlag) || !clear_dir(path + gitDir, verboseFlag)) {
      System.exit(1);
    }

    /* Get directories and files for processing */
    File project_path = new File(path);
    ArrayList<File> directories = get_dir_list(project_path, copyDir);
    ArrayList<File> ps_project_files = get_ps_project_files(directories);
    if (ps_project_files.isEmpty()) {
      System.err.println("There were no project files found for processing.");
      System.exit(1);
    }
    if (verboseFlag) {
      show_project_file_list(ps_project_files);
    }

    /* Copy files for processing */
    copy_for_processing(ps_project_files, path + copyDir);

    /* Start work inside working directory */
    File working_path = new File(path + copyDir);
    ArrayList<File> workingFiles = new ArrayList<>(Arrays.asList(working_path.listFiles()));
    TreeMap<String, Integer> uniqueTypes = new TreeMap<>();
    /* Get unique file names */
    String lastUniqName = "";
    for (File w : workingFiles) {
      String psType = get_pstype(w.getName());
      if (lastUniqName.equals(psType)) {
        uniqueTypes.put(psType, uniqueTypes.get(psType) + 1);
      } else {
        uniqueTypes.put(psType, 1);
        lastUniqName = psType;
      }
    }
    if (verboseFlag && !uniqueTypes.isEmpty()) {
      show_unique_types_list(uniqueTypes);
    }

    /* Loop through unique types. If count is 1, just copy file directly.*/
    for (Map.Entry entry : uniqueTypes.entrySet()){
        try {
        Document finalDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        for (int i = 0; i < Integer.valueOf(entry.getValue().toString()); i++) {
          System.out.println(entry.getKey().toString() + ":" + entry.getValue().toString());
          String sourceDocPath = path + copyDir + entry.getKey() + "_" + get_formatted_file_number(String.valueOf(i+1)) + ".XML";
          Document sourceDoc;
          sourceDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(sourceDocPath);
          if (verboseFlag) {
              System.out.println("Processing file " + entry.getValue());
           }
          if (Integer.valueOf(0).equals(i)){
            Node rundate = sourceDoc.getElementsByTagName("rundate").item(0);
            rundate.setTextContent("DD MON YY : HH MM PM");
            Node object_type = sourceDoc.getElementsByTagName("object_type").item(0);
            NamedNodeMap attr = object_type.getAttributes();
            Node nodeAttr = attr.getNamedItem("firstitem");
            nodeAttr.setTextContent("firstitem_val");
            nodeAttr = attr.getNamedItem("items");
            nodeAttr.setTextContent("items_val");
            finalDoc.appendChild(finalDoc.adoptNode(sourceDoc.getFirstChild().cloneNode(true)));
          }
          else {
            sourceDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(sourceDocPath);
            NodeList finalItemList = finalDoc.getElementsByTagName("item");
            Node finalItem = finalItemList.item(finalItemList.getLength()-1);
            NodeList sourceItemList = sourceDoc.getElementsByTagName("item");
            for (int j=0; j<sourceItemList.getLength(); j++){
              finalItem.appendChild(finalDoc.adoptNode(sourceItemList.item(j).cloneNode(true)));
            }
          }
        }
        String targetDocPath = path + gitDir + entry.getKey() + ".XML";
        DOMSource source = new DOMSource(finalDoc);
        StreamResult result = new StreamResult(new File(targetDocPath));
        TransformerFactory.newInstance().newTransformer().transform(source, result);
      } catch (ParserConfigurationException | SAXException | IOException | DOMException | TransformerFactoryConfigurationError | TransformerException e) {
        System.out.println(e.toString());
      }
  }
   
    System.out.println("The project conversion has completed successfully.");
    System.out.println("The GIT files are located in the git conversion directory.");
    System.out.println(path + gitDir);
    System.exit(0);
  }

  private static boolean clear_dir(String dir, boolean verboseFlag) {
    File filePath = new File(dir);
    if (filePath.exists()) {
      ArrayList<File> files = new ArrayList<>(Arrays.asList(filePath.listFiles()));
      if (verboseFlag) {
        System.out.println("Clearing directory " + dir);
      }
      for (File f : files) {
        if (!f.delete()) {
          System.err.println("Could not clear " + dir + ", check file and directory permissions.");
          return false;
        }
      }
    } else {
      if (filePath.mkdir()) {
        if (verboseFlag) {
          System.out.println("Creating directory " + dir);
        }
      } else {
        System.err.println("Could not create directory " + dir + ", check file and directory permissions.");
        return false;
      }
    }
    return true;
  }

  private static String get_formatted_file_number(String fileNumber) {
    String newFileNumber;
    if (fileNumber.length()>4) {
      newFileNumber = fileNumber;
    } 
    else {
      newFileNumber = "0000".substring(0, 4 - fileNumber.length()) + fileNumber;
    }
    return newFileNumber;
  }
  
  private static void copy_for_processing(ArrayList<File> files, String copy_path) {
    boolean error_flag = false;
    for (File f : files) {
      String newFileName = f.getName().replaceAll(" ", "_").toUpperCase();
      Integer lastUnderscore = newFileName.lastIndexOf("_");
      Integer lastFileSuffix = newFileName.lastIndexOf("XML");
      String newFileNumber = get_formatted_file_number(newFileName.substring(lastUnderscore + 1, lastFileSuffix-1));
      newFileName = newFileName.substring(0, lastUnderscore + 1).replaceAll("[^A-Za-z0-9]", "_") + newFileNumber + ".XML";
      File new_file = new File(copy_path + newFileName);
      try {
        Files.copy(f.toPath(), new_file.toPath());
      } catch (IOException ex) {
        error_flag = true;
        Logger.getLogger(psxmlconv.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    if (error_flag) {
      System.err.println("An error has occurred during file copy processing and the process cannot continue.");
      System.exit(1);
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

  private static ArrayList get_ps_project_files(ArrayList<File> directories) {
    ArrayList<File> xml_files = new ArrayList<>();
    for (File dirs : directories) {
      if (dirs.isDirectory()) {
        File filePath = new File(dirs.getAbsolutePath());
        ArrayList<File> files = new ArrayList<>(Arrays.asList(filePath.listFiles()));
        for (File fls : files) {
          if (!("Index.xml".equalsIgnoreCase(fls.getName()))) {
            xml_files.add(fls);
          }
        }
      }
    }
    return xml_files;
  }

  private static String get_pstype(String filename) {
    String tmpFileName = filename.substring(0, filename.lastIndexOf("_"));
    tmpFileName = tmpFileName.replaceAll("[-\\(\\)\\.]", "_");
    return tmpFileName;
  }

  private static boolean project_exists(String p) {
    File project_file = new File(p + "project.xml");
    return project_file.exists();
  }

  private static void show_help() {
    System.out.println("PeopleSoft XML to GIT Conversion Usage:");
    System.out.println("Required: Specify the path to the project directory to be converted.");
    System.out.println("[-d \\path to project directory\\]");
    System.out.println("Optional: Enable verbose logging.");
    System.out.println("[-v]");
    System.out.println("Example with verbose disabled:");
    System.out.println("java -jar psxmlconv -d \"/temp/mypsproj/\"");
    System.out.println("Example with verbose enabled:");
    System.out.println("java -jar psxmlconv -d \"C:\\temp\\mypsproj\\\" -v");
  }

  private static void show_project_file_list(ArrayList<File> files) {
    System.out.println("= PROJECT FILE LIST BEGIN ====================================================");
    for (File f : files) {
      System.out.println(f.getAbsolutePath());
    }
    System.out.println("= PROJECT FILE LIST END ======================================================");
  }

  private static void show_unique_types_list(TreeMap<String, Integer> map) {
    System.out.println("= PROJECT TYPES LIST BEGIN ====================================================");
    for (Map.Entry entry : map.entrySet()){
      System.out.println(entry.getKey() + " [COUNT:" + entry.getValue().toString() + "]");
    }
    System.out.println("= PROJECT TYPES LIST END ======================================================");
  }
}
