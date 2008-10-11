package org.basex.fs;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import java.util.regex.Pattern;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.DataText;
import org.basex.query.ChildIterator;
import org.basex.util.Array;
import org.basex.util.IntList;
import org.basex.util.TokenBuilder;

/**
 * Preliminary collection of file system methods.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen & Hannes Schwarz
 */
public final class DataFS {
  /** Root dir. */
  public static final int ROOTDIR = 2;
  /** # of attributes. */
  public static final int NUMATT = 5;
  /** Data reference. */
  public final Data data;

  /**
   * Constructor.
   * @param d data reference
   */
  public DataFS(final Data d) {
    data = d;
  }

  /**
   * Checks if the specified node is a file.
   * @param pre pre value
   * @return result of comparison
   */
  public boolean isFile(final int pre) {
    return data.kind(pre) == Data.ELEM &&
      data.tagID(pre) == data.tags.id(DataText.FILE);
  }

  /**
   * Checks if the specified node is a directory.
   * @param pre pre value
   * @return result of comparison
   */
  public boolean isDir(final int pre) {
    return data.kind(pre) == Data.ELEM &&
      data.tagID(pre) == data.tags.id(DataText.DIR);
  }

  /**
   * Returns the path of a file.
   * @param pre pre value
   * @return file path.
   */
  public byte[] path(final int pre) {
    return path(pre, 0, true);
  }

  /**
   * Returns the relative path of a file.
   * @param pre pre value of current node
   * @param par pre value of last dir to print
   * @param abs absolute flag
   * @return file path.
   */
  public byte[] path(final int pre, final int par, final boolean abs) {
    int p = pre;
    int k = data.kind(p);
    /*
    if(isFile(p)) {
      p = data.parent(p, k);
      k = data.kind(p);
    }*/

    final IntList il = new IntList();
    while(p >= par && k != Data.DOC) {
      il.add(p);
      p = data.parent(p, k);
      k = data.kind(p);
    }

    final TokenBuilder tb = new TokenBuilder();
    if(!abs) tb.add('.');
    final int s = il.size;
    final boolean keepSlash = pre > par;
    if(!abs && keepSlash) tb.add('/');

    for(int i = s - 2; i >= 0; i--) {
      final byte[] node = replace(name(il.get(i)), '\\', '/');
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
   * @param pre pre value
   * @return file name.
   */
  public byte[] name(final int pre) {
    return attr(pre, data.nameID);
  }
  
  /**
   * Returns the size of a file.
   * @param pre pre value
   * @return file size
   */
  public byte[] size(final int pre) {
    return attr(pre, data.atts.id(DataText.SIZE));
  }

  /**
   * Sets the size of a file.
   * @param pre pre value
   * @param size to set
   */
  public void size(final int pre, final byte[] size) {
    data.update(pre + 3, SIZE, size);
    data.flush();
  }

  /**
   * Returns the Mtime of a file.
   * @param pre pre value
   * @return file name.
   */
  public byte[] time(final int pre) {
    return attr(pre, data.atts.id(DataText.MTIME));
  }

  /**
   * Sets the Mtime of a file.
   * @param pre pre value
   * @param mtime to set
   */
  public void time(final int pre, final byte[] mtime) {
    data.update(pre + 4, MTIME, mtime);
    data.flush();
  }
  
  /**
   * Returns the suffix of a file.
   * @param pre pre value
   * @return file name.
   */
  public byte[] suffix(final int pre) {
    return attr(pre, data.atts.id(DataText.SUFFIX));
  }

  /**
   * Returns a file attribute.
   * @param pre pre value
   * @param at the attribute to be found
   * @return attribute or empty token.
   */
  private byte[] attr(final int pre, final int at) {
    final byte[] att = data.attValue(at, pre);
    return att != null ? att : EMPTY;
  }

  /**
   * Returns the current time in the FS format.
   * @return current time
   */
  public byte[] currTime() {
    return token(System.currentTimeMillis() / 60000);
  }

  /**
   * Returns all directories and files of a directory.
   * @param pre - pre value of the "parent" directory
   * @return -  all pre values of the dirs and files
   */
  public int[] children(final int pre) {
    return new ChildIterator(data, pre).all();
  }

  /**
   * Returns the pre values of files or dir. The names are described
   * by a pattern.
   * @param pre - pre value of the "parent" directory
   * @param path - directory name
   * @return -  all pre values of all files
   */
  public int[] children(final int pre, final String path) {
    String f = path;
    int p = pre;

    final int sl = path.lastIndexOf('/');
    if(sl != -1) {
      f = path.substring(sl + 1);
      p = goTo(pre, path.substring(0, sl));
      if(p == -1) return Array.NOINTS;
    }
    f = regex(f);
    
    final IntList res = new IntList();
    final ChildIterator it = new ChildIterator(data, p);
    while(it.more()) {
      final int n = it.next();
      if(Pattern.matches(f, string(name(n)))) res.add(n);
    }
    return res.finish();
  }

  /**
   * Returns the pre value of a dir.
   * @param pre - pre value of the "parent" directory
   * @param path - path name
   * @return -  all pre values of all files
   */
  public int dir(final int pre, final String path) {
    final ChildIterator it = new ChildIterator(data, pre);
    final String p = regex(path);
    
    while(it.more()) {
      final int n = it.next();
      if(isDir(n) && Pattern.matches(p, string(name(n)))) {
        return n;
      }
    }
    return -1;
  }

  /**
   * Returns the pre value of the resulting directory.
   * @param pre - pre value
   * @param path - path expression
   * @return pre value of the result dir
   */
  public int goTo(final int pre, final String path) {
    // No path -> return the same dir
    if(path.length() < 1) return pre;

    // Separate path expression
    int n = pre;
    for(final String p : path.split("/")) {
      // Parent directory
      if(p.equals("..")) {
        n = data.parent(n, data.kind(pre));
        // / was first char of the path - after split it's ""
      } else if(p.equals("")) {
        n = DataFS.ROOTDIR;
        // if path equals "." do nothing else getDir
      } else if(!p.equals(".")) {
        n = dir(n, p);
        // if there is no such dir return -1
        if(n == -1) return -1;
      }
    }
    return n;
  }

  /**
   * Tests if wildcards are used. If true it returns
   * the regular expression. If not null will be returned.
   * @param expr - the expression of the user
   * @return If a wildcard is used a regular expression is
   *         returned otherwise null
   */
  public String regex(final String expr) {
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
   * @param file file/directory
   * @return result of check
   */
  public boolean valid(final String file) {
    return file.indexOf('?') < 0 && file.indexOf('*') < 0 &&
      file.indexOf('[') < 0 && file.indexOf(']') < 0;
  }

  /**
   * Inserts a new entry into the table.
   * @param isDir - insert dir or file
   * @param name - filename
   * @param suffix - suffix of the file
   * @param size - size of the file
   * @param mtime - make time
   * @param parent - pre value of the parent
   * @param pre - position to insert
   */
  public void insert(final boolean isDir,
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
   * Opens the file which is defined by the specified pre value.
   * @param pre pre value
   */
  public void launch(final int pre) {
    if(pre == -1 || !isFile(pre)) return;

    final String path = string(path(pre));
    try {
      final Runtime run = Runtime.getRuntime();
      if(Prop.UNIX) {
        run.exec(new String[] { "open", path }); // xdg-open
      } else {
        run.exec("rundll32.exe url.dll,FileProtocolHandler " + path);
      }
    } catch(final IOException ex) {
      BaseX.debug(ex);
    }
  }
}
