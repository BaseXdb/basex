package org.basex.query.fs;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.util.IntList;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Preliminary collection of file system methods.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen & Hannes Schwarz
 */
public final class FSUtils {
  
  /** Root dir. */
  private static final int ROOTDIR = 2;
  
  /** Private constructor, preventing class instances. */
  private FSUtils() { }

  /**
   * Checks if the specified node is a file.
   * @param data data reference
   * @param pre pre value
   * @return result of comparison
   */
  public static boolean isFile(final Data data, final int pre) {
    return data.kind(pre) == Data.ELEM && data.tagID(pre) == data.fileID;
  }

  /**
   * Checks if the specified node is a directory.
   * @param data data reference
   * @param pre pre value
   * @return result of comparison
   */
  public static boolean isDir(final Data data, final int pre) {
    return data.kind(pre) == Data.ELEM && data.tagID(pre) == data.dirID;
  }

  /**
   * Returns the size of a file.
   * @param data data reference
   * @param pre pre value
   * @return file size
   */
  public static long getSize(final Data data, final int pre) {
    final byte[] att = data.attValue(data.sizeID, pre);
    return att != null ? toLong(att) : 0;
  }

  /**
   * Returns the path of a file.
   * @param data data reference
   * @param pre pre value
   * @return file path.
   */
  public static byte[] getPath(final Data data, final int pre) {
    int p = pre;
    final IntList il = new IntList();
    while(p != 0) {
      il.add(p);
      final int kind = data.kind(p);
      p = data.parent(p, kind);
    }

    final TokenBuilder tb = new TokenBuilder();
    final int s = il.size;
    for(int i = s - 2; i >= 0; i--) {
      final byte[] node = replace(getName(data, il.get(i)), '\\', '/');
      tb.add(node);
      if(!endsWith(node, '/')) tb.add('/');
    }
    byte[] node = tb.finish();
    while(endsWith(node, '/')) node = substring(node, 0, node.length - 1);
    return node;
  }

  /**
   * Returns the name of a file.
   * @param data data reference
   * @param pre pre value
   * @return file name.
   */
  public static byte[] getName(final Data data, final int pre) {
    final byte[] att = data.attValue(data.nameID, pre);
    return att != null ? att : EMPTY;
  }
  
  /**
   * Returns the name of a file.
   * @param data data reference
   * @param pre pre value
   * @return file name.
   */
  public static String getFileName(final Data data, final int pre) {
    final byte[] att = data.attValue(data.nameID, pre);    
    if(att == null)
      return "";    
    String filename = "";
    for(byte i : att) {
      filename += (char) i;
    }
    return filename;
  }

  /**
   * Returns the suffix of a file.
   * @param data data reference
   * @param pre pre value
   * @return file name.
   */
  public static byte[] getSuffix(final Data data, final int pre) {
    final byte[] att = data.attValue(data.suffixID, pre);
    return att != null ? att : EMPTY;
  }

  /**
   * Returns all directories and files of a directory.
   *  
   * @param data - the data table
   * @param pre - pre value of the "parent" directory
   * @return -  all pre values of the dirs and files
   */
  public static int[] getAllOfDir(final Data data, final int pre) {

    // Den Elementtyp einmal speichern
    int kind = data.kind(pre);    
    int n = pre;    
    // Wie weit das Verzeichnis reicht
    int size = data.size(n, kind) + n;
    // Zu erstem file/dir springen
    n += data.attSize(n, kind);
    // Ergebnisarray 
    // <HS> ..calculation size/5 is dubious as XML structure might change,
    //    (e.g. if file contents are included)..
    IntList res = new IntList();
    while(n < size) {
      // pre speichern
      res.add(n);
      n += data.size(n, data.kind(n));
    }    
    return res.finish();
  }

  /**
   * Returns all files of a directory.
   *  
   * @param data - the data table
   * @param pre - pre value of the "parent" directory
   * @return -  all pre values of all files
   */
  public static int[] getAllFiles(final Data data, final int pre) {
    // Den Elementtyp einmal speichern
    int kind = data.kind(pre);    
    int n = pre;    
    // Wie weit das Verzeichnis reicht
    int size = data.size(n, kind) + n;
    // Zu erstem file/dir springen
    n += data.attSize(n, kind);
    // Ergebnisarray 
    IntList res = new IntList();
    while(n < size) {
      if(isFile(data , n)) {
        // pre speichern
        res.add(n);
      }
      n += data.size(n, data.kind(n));
    }    
    return res.finish();
  }

  /**
   * Returns all directories of a directory.
   *  
   * @param data - the data table
   * @param pre - pre value of the "parent" directory
   * @return -  all pre values of all files
   */
  public static int[] getAllDir(final Data data, final int pre) {
    // Den Elementtyp einmal speichern
    int kind = data.kind(pre);    
    int n = pre;    
    // Wie weit das Verzeichnis reicht
    int size = data.size(n, kind) + n;
    // Zu erstem file/dir springen    
    n += data.attSize(n, kind);
    // Ergebnisarray 
    IntList res = new IntList();
    while(n < size) {
      if(isDir(data , n)) {
        // pre speichern
        res.add(n);
      }
      n += data.size(n, kind);
    }    
    return res.finish();
  }
  
  /**
   * Returns the pre value of a dir.
   *  
   * @param data - the data table
   * @param pre - pre value of the "parent" directory
   * @param dir - directory name
   * @param kind - kind value of dir (elem)
   * @return -  all pre values of all files
   */
  public static int getSpecificFile(final Data data, final int pre,
      final byte[] dir, final int kind) {
    int n = pre;        
    // Wie weit das Verzeichnis reicht
    int size = data.size(n, kind) + n;
    // Zu erstem file/dir springen
    n += data.attSize(n, data.kind(n));

    while(n < size) {
      if(isFile(data , n)) {
        // pre speichern
        if(Token.eq(getName(data, n), dir)) {
         return n; 
        }
      }
      n += data.size(n, kind);
    }    
    return -1;
  }
  
  /**
   * Returns the pre value of a dir.
   *  
   * @param data - the data table
   * @param pre - pre value of the "parent" directory
   * @param dir - directory name
   * @param kind - kind value of dir (elem)
   * @return -  all pre values of all files
   */
  public static int getSpecificDir(final Data data, final int pre,
      final byte[] dir, final int kind) {
    int n = pre;        
    // Wie weit das Verzeichnis reicht
    int size = data.size(n, kind) + n;
    // Zu erstem file/dir springen
    n += data.attSize(n, data.kind(n));

    while(n < size) {
      if(isDir(data , n)) {
        // pre speichern
        // <CG> using byte comparison to avoid byte/string conversion
        if(Token.eq(getName(data, n), dir)) {
          return n;
        }
      }
      n += data.size(n, kind);
    }    
    return -1;
  }
  
  /**
   *  Test pathexpression.
   * 
   * @param data - data table 
   * @param pre - pre value
   * @param path - path expression
   * @return pre value of the result dir 
   */
  public static int goToDir(final Data data, final int pre, final String path) {

    int n = pre;
    int kind = data.kind(pre);

    // No path -> return the same dir
    if(path.length() < 1) {
      return pre;
    }

    // Seperate path expression 
    String[] paths = path.split("/");

    for(String p : paths) {
      // Parent directory
      if(p.equals("..")) {
        n = data.parent(n, kind);
      // / was first char of the path - after split it's ""  
      } else if(p.equals("")) {
        n = 3;
      // if path equals "." do nothing else getDir  
      } else if(!p.equals(".")) {
        n = getSpecificDir(data, n, Token.token(p), kind);
      }
      // if there is no such dir return -1
      if(n == -1) {
        return -1;
      }      
    }
    return n;
  }
  
  /**
   * Splits the Options.
   * 
   * @param options - Options
   * @return String[] - all options stored in an array
   */
  public static String[] readOptions(final String options) {

      String[] opt = options.split(" ");
      if(opt.length < 2) {
        return null;
      }
      String[] res = new String[opt.length - 1];             
      for(int i = 0; i < res.length; i++) {
        res[i] = opt[i + 1];
      }
      return res;      
    } 
  
  /**
   * Opens the file which is defined by the specified pre value.
   * @param data data reference
   * @param pre pre value
   */
  public static void launch(final Data data, final int pre) {
    if(!data.deepfs || pre == -1 || !isFile(data, pre)) return;

    final String path = Token.string(getPath(data, pre));
    try {
      final Runtime run = Runtime.getRuntime();
      if(Prop.UNIX) {
        run.exec(new String[] { "open", path });
      } else {
        run.exec("rundll32.exe url.dll,FileProtocolHandler " + path);
      }
    } catch(final IOException ex) {
      BaseX.debug("Could not open \"%\"", path);
      ex.printStackTrace();
    }
  }

  /**
   * Returns int value of the root dir.
   * 
   * @return the rOOTDIR
   */
  public static int getROOTDIR() {
    return ROOTDIR;
  }
}
