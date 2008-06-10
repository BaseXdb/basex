package org.basex.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.basex.BaseX;
import org.basex.io.IO;

/**
 * This class assembles properties which are used all around the
 * project. They are initially read from and finally written to disk.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Prop {
  
  // STATIC PROPERTIES ========================================================

  /** Returns the system's default encoding. */
  public static final String ENCODING = System.getProperty("file.encoding");
  /** Returns the current working directory. */
  public static final String WORK = System.getProperty("user.dir") + "/";
  /** User's home directory. */
  public static final String HOME = System.getProperty("user.home");
  /** Flag denoting if OS belongs to UNIX or Windows family. */
  public static final boolean UNIX =
    System.getProperty("os.name").charAt(0) != 'W';
  /** New line string. */
  public static final String NL = System.getProperty("line.separator");

  /** Available languages (should be retrieved dynamically, but leads to
   * problems with JAR file. Someone knows what to do?) */
  public static final String[] LANGUAGES = { "English", "German" };

  // DATABASE & PROGRAM PATHS =================================================
  
  /** Database path. */
  public static String dbpath = HOME + "/BaseXData";
  /** Web Server path. */
  public static String webpath = WORK + "/web";
  /** Path to dotty. */
  public static String dotty = "dotty";  
  /** Language Name (currently: English or German). */
  public static String language = "English";
  /** Port for client/server communication. */
  public static int port = 1984;
  /** Port for web server. */
  public static int webport = 8080;

  // TRANSIENT OPTIONS ========================================================
  
  /** The following options are not saved/read; don't remove this flag. */
  public static final boolean SKIP = true;

  /** Debug mode. */
  public static boolean debug = false;
  /** Short query info. */
  public static boolean info = false;
  /** Detailed query info. */
  public static boolean allInfo = false;
  /** Flag for serializing query results. */
  public static boolean serialize = true;
  /** Flag for serialization as XML. */
  public static boolean xmloutput = false;
  /** Dots the Query plan. */
  public static boolean dotplan = false;
  /** Prints a XML plan. */
  public static boolean xmlplan = false;
  /** Dots the Result. */
  public static boolean dotresult = false;
  /** Format XQuery output. */
  public static boolean xqformat = true;
  /** Use internal XML parser. */
  public static boolean intparse = false;
  
  /** Number of query executions. */
  public static int runs = 1;
  /** Flag for whitespace chopping. */
  public static boolean chop = false;
  /** Flag for entity parsing. */
  public static boolean entity = true;
  /** Flag for creating a fulltext index. */
  public static boolean ftindex = true;
  /** Flag for creating a fuzzy index. */
  public static boolean fzindex = false;
  /** Flag for compressing ft-values on disk. */
  public static boolean fcompress = false;
  /** Flag for creating a text index. */
  public static boolean textindex = true;
  /** Flag for creating an attribute value index. */
  public static boolean attrindex = true;
  /** Flag for loading database table into main memory. */
  public static boolean mainmem = false;
  /** Flag for creating databases on-the-fly (in memory). */
  public static boolean onthefly = false;

  /** Flag for creating a fuzzy index. */
  public static boolean ftfuzzy = true;
  /** Flag for compressing ft-values on disk. */
  public static boolean ftcompress = true;
  /** Flag for fulltext stemming. */
  public static boolean ftstem = false;
  /** Flag for fulltext case sensitivity. */
  public static boolean ftcs = false;
  /** Flag for fulltext diacritics. */
  public static boolean ftdc = false;

  /** Flow for showing the XQuery error code. */
  public static boolean xqerrcode = true;
  /** Fulltext details. */
  public static boolean ftdetails = true;
  /** Last XQuery file. */
  public static IO xquery;

  // FILE SYSTEM OPTIONS ======================================================
  
  /** Flag for importing file contents. */
  public static boolean fscont = false;
  /** Flag for importing file metadata. */
  public static boolean fsmeta = false;
  /** Maximum size for textual imports. */
  public static int fstextmax = 10240;
  /** Levenshtein default error. */
  public static int lserr = 0;

  // WEBSERVER OPTIONS ========================================================

  /** PHP Path. */
  public static String phppath = "php.exe";

  // CONFIG OPTIONS ===========================================================

  /** Property information. */
  private static final String PROPHEADER =  "# BaseX Property File." + NL +
    "# This here will be overwritten every time, but" + NL +
    "# you can set fix options at the end of the file." + NL;
  /** Property information. */
  private static final String PROPUSER = "# User defined section";

  /** Default path to the BaseX configuration file. */
  private static final String CONFIGFILE = HOME + "/" + IO.BASEXSUFFIX;
  /** Remembers if the config file has already been read. */
  private static boolean read;

  /** Private constructor, preventing class instantiation. */
  private Prop() { }

  /**
   * Reads the configuration file and initializes the project properties.
   * The file is located in the user's home directory.
   */
  public static void read() {
    if(read) return;
    read(CONFIGFILE, Prop.class.getFields());
    read = true;
  }
  
  /**
   * Assigns the properties from the specified file to the field array.
   * @param filename file to be read
   * @param fields fields to be assigned
   */
  public static void read(final String filename, final Field[] fields) {
    final File file = new File(filename);
    if(!file.exists()) return;
    
    try {
      final BufferedReader br = new BufferedReader(new FileReader(file));
      String line = null;

      while((line = br.readLine()) != null) {
        line = line.trim();
        if(line.length() == 0 || line.charAt(0) == '#') continue;
        final int d = line.indexOf('=');
        if(d < 0) {
          BaseX.debug("Can't guess what \"%\" means in \"%\"", line, filename);
          continue;
        }
        final String key = line.substring(0, d).trim().toLowerCase();
        final String val = line.substring(d + 1).trim();
        
        if(!assign(fields, key, val)) {
          BaseX.debug("\"%\" ignored in \"%\"", line, filename);
        }
      }
      br.close();
    } catch(final Exception ex) {
      BaseX.debug("% could not be read.", filename);
      ex.printStackTrace();
    }
  }
  
  /**
   * Assigns the specified keys and values to one the specified fields.
   * @param fields field array
   * @param key key
   * @param value value
   * @throws IllegalAccessException exception
   * @return true if field could be assigned
   */
  private static boolean assign(final Field[] fields, final String key,
      final String value) throws IllegalAccessException {

    // extract numeric value in key
    String k = key;
    int num = 0;
    for(int s = 0; s < k.length(); s++) {
      if(Character.isDigit(k.charAt(s))) {
        num = Integer.parseInt(k.substring(s));
        k = k.substring(0, s);
        break;
      }
    }
    
    // parse all fields
    for(final Field f : fields) {
      final String name = f.getName();
      
      // field found...
      if(name.equals(k.toLowerCase())) {
        final String t = f.getType().getSimpleName();
        // assign value
        if(t.equals("boolean")) {
          f.setBoolean(null, Boolean.parseBoolean(value));
        } else if(t.equals("String")) {
          f.set(null, value);
        } else if(t.equals("int")) {
          f.setInt(null, Integer.parseInt(value));
        } else if(t.equals("String[]")) {
          if(num == 0) {
            f.set(null, new String[Integer.parseInt(value)]);
          } else {
            ((String[]) f.get(null))[num - 1] = value;
          }
        } else if(t.equals("int[]")) {
          ((int[]) f.get(null))[num] = Integer.parseInt(value);
        } else {
          BaseX.debug("Can't write property \"%\".", f);
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Writes the configuration file.
   */
  public static void write() {
    write(CONFIGFILE, Prop.class.getFields());
  }
  
  /**
   * Writes the properties from field array to the specified file.
   * @param filename file to be read
   * @param fields fields to be assigned
   */
  public static void write(final String filename, final Field[] fields) {
    final File file = new File(filename);
    
    try {
      // caches options specified by the user
      final StringBuilder user = new StringBuilder();
      if(file.exists()) {
        final BufferedReader br = new BufferedReader(new FileReader(file));
        String line = null;
        while((line = br.readLine()) != null) {
          if(line.equals(PROPUSER)) break;
        }
        while((line = br.readLine()) != null) {
          user.append(line);
          user.append(NL);
        }
        br.close();
      }
      
      final BufferedWriter bw = new BufferedWriter(new FileWriter(file));
      bw.write(PROPHEADER + NL);
  
      for(final Field f : fields) {
        final String name = f.getName();
        if(name.equals("SKIP")) break;
        if(Modifier.isFinal(f.getModifiers())) continue;

        if(f.get(null) instanceof String[]) {
          final String[] str = (String[]) f.get(null);
          bw.write(name + " = " + str.length + NL);
          for(int i = 0; i < str.length; i++) {
            if(str[i] != null) bw.write(name + (i + 1) + " = " + str[i] + NL);
          }
        } else if(f.get(null) instanceof int[]) {
          final int[] num = (int[]) f.get(null);
          for(int i = 0; i < num.length; i++) {
            bw.write(name + i + " = " + num[i] + NL);
          }
        } else {
          bw.write(name + " = " + f.get(null).toString() + NL);
        }
      }
      
      bw.write(NL + PROPUSER + NL + user);
      bw.close();
    } catch(final Exception ex) {
      BaseX.debug("% could not be written.", filename);
      ex.printStackTrace();
    }
  }
}
