package org.basex.query.fs;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import java.util.regex.Pattern;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.util.Array;
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
  /** # of attributes. */
  public static final int NUMATT = 5;

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
   * Returns the path of a file.
   * @param data data reference
   * @param pre pre value
   * @return file path.
   */
  public static byte[] getPath(final Data data, final int pre) {
    int p = pre;
    final IntList il = new IntList();
    int k = data.kind(p);
    while(k != Data.DOC) {
      il.add(p);
      p = data.parent(p, k);
      k = data.kind(p);
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
   * @param par pre value of last dir to print
   * @return file path.
   */
  public static byte[] getRelativePath(final Data data,
      final int pre, final int par) {

    int p = pre;
    int k = data.kind(p);
    if(isFile(data, p)) {
      p = data.parent(p, k);
      k = data.kind(p);
    }

    final IntList il = new IntList();
    while(p >= par && k != Data.DOC) {
      il.add(p);
      p = data.parent(p, k);
      k = data.kind(p);
    }

    final TokenBuilder tb = new TokenBuilder();
    tb.add('.');
    final int s = il.size;
    boolean keepSlash = pre > par;
    if(keepSlash) tb.add('/');

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
    return getAttr(data, pre, data.nameID);
  }
  
  /**
   * Sets the name of a file.
   * @param data data reference
   * @param pre pre value
   * @param name to set
   */
  public static void setName(final Data data, final int pre,
      final byte[] name) {
    data.update(pre + 1, NAME, name);
    data.flush();
  }
  
  
  /**
   * Returns the size of a file.
   * @param data data reference
   * @param pre pre value
   * @return file size
   */
  public static long getSize(final Data data, final int pre) {
    return toLong(getAttr(data, pre, data.sizeID));
  }

  /**
   * Sets the size of a file.
   * @param data data reference
   * @param pre pre value
   * @param size to set
   */
  public static void setSize(final Data data, final int pre,
      final byte[] size) {
    data.update(pre + 3, SIZE, size);
    data.flush();
  }
  

  /**
   * Returns the Mtime of a file.
   * @param data data reference
   * @param pre pre value
   * @return file name.
   */
  public static byte[] getMtime(final Data data, final int pre) {
    return getAttr(data, pre, data.timeID);
  }

  /**
   * Sets the Mtime of a file.
   * @param data data reference
   * @param pre pre value
   * @param mtime to set
   */
  public static void setMtime(final Data data, final int pre,
      final byte[] mtime) {
    data.update(pre + 4, MTIME, mtime);
    data.flush();
  }
  
  /**
   * Returns the suffix of a file.
   * @param data data reference
   * @param pre pre value
   * @return file name.
   */
  public static byte[] getSuffix(final Data data, final int pre) {
    return getAttr(data, pre, data.suffixID);
  }
  
  /**
   * Sets the suffix of a file.
   * @param data data reference
   * @param pre pre value
   * @param suffix to set
   */
  public static void setSuffix(final Data data, final int pre,
      final byte[] suffix) {
    data.update(pre + 2, SUFFIX, suffix);
    data.flush();
  }

  /**
   * Returns a file attribute.
   * @param data data reference
   * @param pre pre value
   * @param at the attribute to be found
   * @return attribute or empty token.
   */
  private static byte[] getAttr(final Data data, final int pre, final int at) {
    final byte[] att = data.attValue(at, pre);
    return att != null ? att : EMPTY;
  }

  /**
   * Returns the current time in the FS format.
   * @return current time
   */
  public static byte[] currTime() {
    return Token.token(System.currentTimeMillis() / 60000);
  }

  /**
   * Returns all directories and files of a directory.
   * @param data - the data table
   * @param pre - pre value of the "parent" directory
   * @return -  all pre values of the dirs and files
   */
  public static int[] getChildren(final Data data, final int pre) {
    return new DirIterator(data, pre).all();
  }

  /**
   * Returns the pre values of files or dir. The names are described
   * by a pattern.
   * @param data - the data table
   * @param pre - pre value of the "parent" directory
   * @param path - directory name
   * @return -  all pre values of all files
   */
  public static int[] getChildren(final Data data, final int pre,
      final String path) {

    String file = path;
    int curDirPre = pre;
    final int lastSlash = path.lastIndexOf('/');
    if(lastSlash != -1) {
      curDirPre = FSUtils.goToDir(data, pre, path.substring(0, lastSlash));
      // No such directory.
      if(curDirPre == -1) return Array.NOINTS;
      file = path.substring(lastSlash + 1);
    }

    file = FSUtils.transformToRegex(file);
    
    final IntList res = new IntList();
    final DirIterator it = new DirIterator(data, curDirPre);
    while(it.more()) {
      final int n = it.next();
      if(Pattern.matches(file, Token.string(getName(data, n)))) res.add(n);
    }
    return res.finish();
  }

  /**
   * Returns the pre value of a dir.
   * @param data - the data table
   * @param pre - pre value of the "parent" directory
   * @param path - path name
   * @return -  all pre values of all files
   */
  public static int getDir(final Data data, final int pre, final String path) {
    final DirIterator it = new DirIterator(data, pre);
    final String p = FSUtils.transformToRegex(path);
    
    while(it.more()) {
      final int n = it.next();
      if(isDir(data, n) && Pattern.matches(p, Token.string(getName(data, n))))
        return n;
    }
    return -1;
  }

  /**
   * Returns the pre value of the resulting directory.
   * @param data - data table
   * @param pre - pre value
   * @param path - path expression
   * @return pre value of the result dir
   */
  public static int goToDir(final Data data, final int pre, final String path) {
    int n = pre;
    final int kind = data.kind(pre);

    // No path -> return the same dir
    if(path.length() < 1) return pre;

    // Separate path expression
    for(final String p : path.split("/")) {
      // Parent directory
      if(p.equals("..")) {
        n = data.parent(n, kind);
        // / was first char of the path - after split it's ""
      } else if(p.equals("")) {
        n = 3;
        // if path equals "." do nothing else getDir
      } else if(!p.equals(".")) {
        n = getDir(data, n, p);
        // if there is no such dir return -1
        if(n == -1) return -1;
      }
    }
    return n;
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
        run.exec(new String[] { "open", path }); // xdg-open
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
    final StringBuilder result = new StringBuilder();
    for(int i = 0; i < expr.length(); i++) {
      final char c = expr.charAt(i);
      if(c == '*') {
        result.append(".*");
      } else if(c == '?') {
        result.append(".");
      } else if(c == '.') {
        result.append("\\.");
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }

  /**
   * Returns if the file/dir expression is valid.
   *
   * @param file file/directory
   * @return result of check
   */
  public static boolean validFileName(final String file) {
    return file.indexOf('?') < 0 && file.indexOf('*') < 0 &&
      file.indexOf('[') < 0 && file.indexOf(']') < 0;
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
   * Inserts a new entry into the table.
   *
   * @param data - the data table
   * @param isDir - insert dir or file
   * @param name - filename
   * @param suffix - suffix of the file
   * @param size - size of the file
   * @param mtime - make time
   * @param parent - pre value of the parent
   * @param pre - position to insert
   */
  public static void insert(final Data data, final boolean isDir,
      final byte[] name, final byte[] suffix, final byte[] size,
      final byte[] mtime, final int parent, final int pre) {

    data.insert(pre, parent, isDir ? DIR : FILE, Data.ELEM);
    data.insert(pre + 1, pre, NAME, name);
    data.insert(pre + 2, pre, SUFFIX, suffix);
    data.insert(pre + 3, pre, SIZE, size);
    data.insert(pre + 4, pre, MTIME, mtime);
    data.flush();
  }
  
  /**
   * Deletes an entry of the table.
   *
   * @param data - the data table
   * @param del - pre value of file to delete
   */
  public static void delete(final Data data, final int del) {
    data.delete(del);
    data.flush();
  }
}
