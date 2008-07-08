package org.basex.query.fs;

import static org.basex.data.DataText.*;
import static org.basex.query.fs.FSText.*;
import static org.basex.Text.NL;
import static org.basex.util.Token.*;
import java.io.IOException;
import java.util.regex.Pattern;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.util.IntList;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.io.PrintOutput;

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
   * Returns the pre value of the parrent.
   * @param data data reference
   * @param pre pre value of child
   * @return pre value of the parrent.
   */
  public static int getParrent(final Data data, final int pre) {
    return data.parent(pre, data.kind(pre));
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
   * Returns the path of a file.
   * @param data data reference
   * @param pre pre value of current node
   * @param parrent pre value of last dir to print 
   * @return file path.
   */
  public static byte[] getRelativePath(final Data data, 
      final int pre, final int parrent) {
    int p = pre;
    final int kind = data.kind(p);
    if(isFile(data, p))      
      p = data.parent(p, kind);
    final IntList il = new IntList();
    while(p >= parrent && p != 0) {
      il.add(p);      
      p = data.parent(p, kind);
    }

    final TokenBuilder tb = new TokenBuilder();
    tb.add('.');
    final int s = il.size;
    boolean keepSlash = false;
    if(pre > parrent) {
      tb.add('/');
      keepSlash = true;
    }
    for(int i = s - 2; i >= 0; i--) {      
      final byte[] node = replace(getName(data, il.get(i)), '\\', '/');
      tb.add(node);
      if(!endsWith(node, '/')) tb.add('/');
    }
    byte[] node = tb.finish();
    while(endsWith(node, '/') && !keepSlash) 
      node = substring(node, 0, node.length - 1);
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
  public static byte[] getMtime(final Data data, final int pre) {
    final byte[] att = data.attValue(data.timeID, pre);
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
   * Returns the pre values of files or dir. The names are described
   * by a pattern.
   *  
   * @param data - the data table
   * @param pre - pre value of the "parent" directory
   * @param path - directory name
   * @return -  all pre values of all files
   */
  public static int[] getSpecificFilesOrDirs(final Data data, final int pre,
      final String path) {   

    String file = path;  
    int curDirPre = pre;
    int lastSlash = path.lastIndexOf('/');
    if(lastSlash != -1) {
      curDirPre = FSUtils.goToDir(data, pre, 
          path.substring(0, lastSlash));   
      if(curDirPre == -1) {
        // No such directory.
        return new int[] {-1};
      } else {
        file = path.substring(lastSlash + 1);
      }
    }

    final IntList res = new IntList();
    final DirIterator it = new DirIterator(data, curDirPre);

    String fileToFind = FSUtils.transformToRegex(file);
    while(it.more()) {
      final int n = it.next();
      if(Pattern.matches(fileToFind, Token.string(getName(data, n)))) 
        res.add(n);
    }
    return res.finish();
  }

  /**
   * Returns the pre value of a dir.
   *  
   * @param data - the data table
   * @param pre - pre value of the "parent" directory
   * @param path - path name
   * @return -  all pre values of all files
   */
  public static int[] getOneSpecificFileOrDir(final Data data, final int pre,
      final String path) {

    final IntList res = new IntList();
    final DirIterator it = new DirIterator(data, pre);

    String fileToFind = FSUtils.transformToRegex(path);
    while(it.more()) {
      final int n = it.next();
      if(Pattern.matches(fileToFind, Token.string(getName(data, n)))) 
        res.add(n);
    }
    return res.finish();
  }

  /**
   * Returns all directories and files of a directory.
   *  
   * @param data - the data table
   * @param pre - pre value of the "parent" directory
   * @return -  all pre values of the dirs and files
   */
  public static int[] getAllOfDir(final Data data, final int pre) {
    final IntList res = new IntList();
    final DirIterator it = new DirIterator(data, pre);
    while(it.more()) res.add(it.next());
    return res.finish();
  }

  /**
   *  Test path expression.
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
        //n = getSpecificDir(data, n, Token.token(p));
        p = transformToRegex(p);      
        final DirIterator it = new DirIterator(data, n);
        n = -1;
        while(it.more()) {
          int j = it.next();
          if(isDir(data, j) &&
              Pattern.matches(p, Token.string(getName(data, j)))) {          
            n = j;
            break;
          }        
        }
        // if there is no such dir return -1
        if(n == -1) {
          return -1;
        }      
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
   * Tests if wildcards are used. If true it returns
   * the regular expression. If not null will be returned.
   * 
   * @param expr - the expression of the user 
   * @return If a wildcard is used a regular expression is
   *         returned otherwise null
   */
  public static String transformToRegex(final String expr) {

    String result = "";
    for(int i = 0; i < expr.length(); i++) {
      switch (expr.charAt(i)) {
        case '*':
          result += ".*";
          break;
        case '?':
          result += ".";
          break;
        case '.':
          result += "\\.";
          break;
        default:
          result += expr.charAt(i);
        break;
      }
    }
    return result;
  }



  /**
   * Returns int value of the root dir.
   * 
   * @return the rOOTDIR
   */
  public static int getROOTDIR() {
    return ROOTDIR;
  }


  /**
   * Returns the pre value of a dir.
   *  
   * @param out - the Outputstream
   * @param programm - name of the programm
   * @param arg - passed by the console
   * @param error - error code
   * @throws IOException - in case of problems with the PrintOutput 
   */
  public static void printError(final PrintOutput out, final String programm, 
      final String arg, final int error) throws IOException {
    switch(error) {
      case 1:
        out.print(programm + ": " + arg + ": " + EPERM);
        break;
      case 2:
        out.print(programm + ": " + arg + ": " + ENOENT);
        break;
      case 5:
        out.print(programm + ": " + arg + ": " + EIO);
        break;
      case 13:
        out.print(programm + ": " + arg + ": " + EACCES);
        break;
      case 17:
        out.print(programm + ": " + arg + ": " + EEXIST);
        break;
      case 20:
        out.print(programm + ": " + arg + ": " + ENOTDIR);
        break;
      case 21:
        out.print(programm + ": " + arg + ": " + EISDIR);
        break;
      case 22:
        out.print(programm + ": " + arg + ": " + EACCES);
        break;
      case 30:
        out.print(programm + ": " + arg + ": " + EROFS);
        break;
      case 34:
        out.print(programm + ": " + arg + ": " + ERANGE);
        break;
      case 63:
        out.print(programm + ": " + arg + ": " + ENAMETOOLONG);
        break;
      case 66:
        out.print(programm + ": " + arg + ": " + ENOTEMPTY);
        break;
      case 79:
        out.print(programm + ": " + arg + ": " + EFTYPE);
        break;
      case 99:
        out.print(programm + ": " + arg + ": " + EMISSARG);
        break;
      case 100:
        out.print(programm + ": " + arg + ": " + EOMDIR);
        break;        
      case 101:
        out.print(programm + ": " + arg + ": " + ENAMENOALLOW);
        break;     
      case 102:
        out.print(programm + ": " + arg + ": " + EINVOPT);
        break;  
      default:
        out.print(programm + ": " + arg + ": " + EUND);
      break;
    }
    out.print(NL);
  }

  /**
   * Inserts a new entry into the table.
   * 
   * @param data - the data table
   * @param isDir - insert dir or file 
   * @param name - filename
   * @param suffix - suffix of the file
   * @param size - size of the file
   * @param mtime - make time
   * @param parrent - pre value of the parrent
   * @param pre - position to insert
   * 
   */
  public static void insert(final Data data, final boolean isDir,
      final byte[] name, final byte[] suffix, final byte[] size,
      final byte[] mtime, final int parrent, final int pre) {

    if(isDir)
      data.insert(pre, parrent, DIR, Data.ELEM);
    else
      data.insert(pre, parrent, FILE, Data.ELEM);
    
    data.insert(pre + 1, pre, NAME, name);
    data.insert(pre + 2, pre, SUFFIX, suffix);
    data.insert(pre + 3, pre, SIZE, size);
    data.insert(pre + 4, pre, MTIME, mtime);  
    data.flush();
  }
  /**
   * Updates a entry of the table.
   * 
   * @param data - the data table
   * @param name - filename
   * @param suffix - suffix of the file
   * @param size - size of the file
   * @param mtime - make time
   * @param pre - position to insert
   * 
   */
  public static void update(final Data data, final byte[] name, 
      final byte[] suffix, final byte[] size, final byte[] mtime, 
      final int pre) {
    if(name != null)
      data.update(pre + 1, NAME, name);
    if(suffix != null)
      data.update(pre + 2, SUFFIX, suffix);
    if(size != null)
      data.update(pre + 3, SIZE, size);
    if(mtime != null)
      data.update(pre + 4, MTIME, mtime);  
    data.flush();
  }
}
