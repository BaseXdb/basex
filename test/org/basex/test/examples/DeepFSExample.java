package org.basex.test.examples;

import java.io.File;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.Optimize;
import org.basex.core.proc.XQuery;
import org.deepfs.fsml.DeepFile;
import org.deepfs.fsml.ParserRegistry;
import org.deepfs.fsml.ser.FSMLSerializer;
import org.deepfs.util.FSImporter;
import org.deepfs.util.FSWalker;

/**
 * This class presents the usage of the DeepFS package.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author BaseX Team
 */
public final class DeepFSExample {
  
  /** The current database Context. */
  static final Context CONTEXT = new Context();
  /** Name of test database. */
  private static final String DB = "testdb";

  /** Test file. */
  private static final File FILE = new File(".").listFiles()[0];
  /** Test directory. */
  private static final File DIRECTORY = new File(CONTEXT.prop.get(Prop.DBPATH));
  
  /** Example queries. */
  private static final String[][] QUERIES = new String[][] {
    {"All directories which contain more than 10 mp3 files",
      "//dir[count(file[type=\"audio\"]) > 10]"
    }
  };
  
  /** Private constructor. */
  private DeepFSExample() { }

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    String xml;
    
    // -------------------------------------------------------------------------
    
    System.out.println(
        "=== Serialize a single file (without metadata and contents)");
    xml = FSMLSerializer.serialize(FILE, false);
    System.out.println(xml);
    
    // -------------------------------------------------------------------------
    
    System.out.println(
        "\n=== Serialize a single file (with metadata and text/xml contents)");
    DeepFile deepFile = new DeepFile(FILE);
    // Extracts metadata and text/xml contents
    deepFile.extract();    
    // Serializes the deep file
    xml = FSMLSerializer.serialize(deepFile);
    System.out.println(xml);
    
    // -------------------------------------------------------------------------
    
    System.out.println("\n=== Traverse a file system hierarchy");
    // Initializes the file system importer
    FSImporter importer = new FSImporter(CONTEXT);
    // Creates the database
    importer.createDB(DB);
    // Traverses the directory, extracts the metadata and text/xml contents and
    // inserts everything into the database
    new FSWalker(importer).traverse(DIRECTORY);
    // Creates indexes
    new Optimize().exec(CONTEXT, System.out);
    // Serializes the database
    new XQuery("/").exec(CONTEXT, System.out);
    System.out.flush();
    
    // -------------------------------------------------------------------------
    
    System.out.println("\n\n=== List available parsers");
    System.out.println("file suffix\t| \t java class");
    System.out.println("-----------------------------------------------------");
    ParserRegistry registry = new ParserRegistry();
    for(String[] parser : registry.availableParsers()) {
      System.out.println(parser[0] + (parser[0].length() > 7 ? "" : "\t") +
          "\t  " + parser[1]);
    }
    
    // -------------------------------------------------------------------------
    
    System.out.println("\n=== Example queries ====================");
    for(String[] query : QUERIES) {
      System.out.println("\n=== " + query[0]);
      new XQuery(query[1]).exec(CONTEXT, System.out);
    }
  }
}
