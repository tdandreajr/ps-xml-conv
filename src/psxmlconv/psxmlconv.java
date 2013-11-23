/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package psxmlconv;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;


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
      usage();
      System.exit(1);
    }
    if ("-d".equals(args[0])) {
      path = args[1];
    } else {
      usage();
      System.exit(1);
    }
    if (args.length == 3) {
      if ("-v".equals(args[2])) {
        verboseFlag = true;
      } else {
        System.err.println("Invalid arguments values provided.");
        usage();
        System.exit(1);
      }
    }

    /* Test for project definition */
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
    TreeMap<String, Integer> uniqueTypes = get_unique_file_types(workingFiles);
    if (verboseFlag && !uniqueTypes.isEmpty()) {
      show_unique_types_list(uniqueTypes);
    }

    /* Loop through unique types. If iterator is 1, get header and footer.
    *  If iterator = 1, write out header.
    *  If iterator <= counter write out all details.
    *  If iterator = counter, writ out footer.
    */
    int fileCount = 0;
    for (Map.Entry entry : uniqueTypes.entrySet()){
      if (fileCount > 2 || Integer.valueOf(entry.getValue().toString()) > 1){
        System.exit(1);
      }
      Integer fileNumber = 0;
      fileCount++;
      System.out.println("File Key: >>>>" + entry.getKey());
      XMLInputFactory xmlif = XMLInputFactory.newInstance();
      XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
      try {
      fileNumber++;
      XMLStreamReader reader = xmlif.createXMLStreamReader(new FileReader(path + copyDir + entry.getKey() + "_0001.XML"));
      XMLStreamWriter xwriter = xmlof.createXMLStreamWriter(new FileWriter(path + copyDir + entry.getKey() + ".XML"));
      IndentingXMLStreamWriter writer = new IndentingXMLStreamWriter(xwriter);
      writer.setIndentStep("  ");
      if (fileNumber.equals(1)) {
        System.out.println("Starting document.");
        writer.writeStartDocument();
      }
      String currElement = "";
      while (reader.hasNext()) {
        int eventType = reader.next();
        switch(eventType) {
          case XMLEvent.START_ELEMENT:
            currElement = reader.getLocalName();
            writer.writeStartElement(reader.getLocalName());
            for (int i=0; i<reader.getAttributeCount(); i++) {
              if("object_type".equals(reader.getLocalName())){
                String attrName = reader.getAttributeLocalName(i);
                if("firstitem".equals(attrName) || "items".equals(attrName)) {
                  writer.writeAttribute(reader.getAttributeLocalName(i), "0");
                } else {
                  writer.writeAttribute(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
                }
              } else {
                writer.writeAttribute(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
              }
            }
            break;
          case XMLEvent.END_ELEMENT:
            currElement = "";
            writer.writeEndElement();
            break;
          case XMLEvent.CHARACTERS:
            if (!reader.isWhiteSpace()) {
              if (!"rundate".equals(currElement)){
                writer.writeCharacters(reader.getText());
              }
            }
            break;
        }
      }
      writer.flush();
      if (fileNumber.equals(Integer.valueOf(entry.getValue().toString()))){
        writer.close();
        System.out.println("Cloing file.");
      }
      reader.close();
      } catch (Exception e) {
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
  
  private static TreeMap<String,Integer> get_unique_file_types(ArrayList<File> files){
    /* Get unique file names */
    String lastUniqName = "";
    TreeMap<String,Integer> types = new TreeMap<>();
    for (File f : files) {
      String psType = get_pstype(f.getName());
      if (lastUniqName.equals(psType)) {
        types.put(psType, types.get(psType) + 1);
      } else {
        types.put(psType, 1);
        lastUniqName = psType;
      }
    }
    return types;
  }
  
  private static boolean project_exists(String p) {
    File project_file = new File(p + "project.xml");
    return project_file.exists();
  }

  private static void usage() {
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
