package org.basex.io;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.xml.sax.*;

/**
 * {@link IO} reference, representing a local file or directory path.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class IOFile extends IO {
  /** File reference. */
  private final File file;

  /**
   * Constructor.
   * @param f file path
   */
  public IOFile(final String f) {
    this(new File(f));
  }

  /**
   * Constructor.
   * @param f file reference
   */
  public IOFile(final File f) {
    super(new PathList().create(f.getAbsolutePath()));
    file = f;
  }

  /**
   * Constructor.
   * @param dir directory
   * @param n file name
   */
  public IOFile(final String dir, final String n) {
    this(new File(dir, n));
  }

  /**
   * Constructor.
   * @param dir directory
   * @param n file name
   */
  public IOFile(final IOFile dir, final String n) {
    this(new File(dir.file, n));
  }

  /**
   * Returns the file reference.
   * @return file reference
   */
  public File file() {
    return file;
  }

  /**
   * Creates a new instance of this file.
   * @return success flag
   */
  public boolean touch() {
    // some file systems require several runs
    for(int i = 0; i < 5; i++) {
      try {
        if(file.createNewFile()) return true;
      } catch(final IOException ex) {
        Performance.sleep(i * 10);
        Util.debug(ex);
      }
    }
    return false;
  }

  @Override
  public byte[] read() throws IOException {
    return new BufferInput(this).content();
  }

  @Override
  public boolean exists() {
    return file.exists();
  }

  @Override
  public boolean isDir() {
    return file.isDirectory();
  }

  @Override
  public long timeStamp() {
    return file.lastModified();
  }

  @Override
  public long length() {
    return file.length();
  }

  @Override
  public InputSource inputSource() {
    return new InputSource(path);
  }

  @Override
  public InputStream inputStream() throws IOException {
    return new FileInputStream(file);
  }

  @Override
  public IO merge(final String f) {
    final IO io = IO.get(f);
    if(!(io instanceof IOFile) || f.contains(":") || f.startsWith("/")) return io;
    return new IOFile(dir(), f);
  }

  /**
   * Recursively creates the directory.
   * @return success flag
   */
  public boolean md() {
    return !file.exists() && file.mkdirs();
  }

  @Override
  public String dir() {
    return isDir() ? path : path.substring(0, path.lastIndexOf('/') + 1);
  }

  /**
   * Returns the children of the path.
   * @return children
   */
  public IOFile[] children() {
    return children(".*");
  }

  /**
   * Returns the children of the path that match the specified regular
   * expression.
   * @param regex regular expression pattern
   * @return children
   */
  public IOFile[] children(final String regex) {
    final File[] ch = file.listFiles();
    if(ch == null) return new IOFile[] {};

    final ArrayList<IOFile> io = new ArrayList<IOFile>(ch.length);
    final Pattern p = Pattern.compile(regex, Prop.WIN ? Pattern.CASE_INSENSITIVE : 0);
    for(final File f : ch) {
      if(p.matcher(f.getName()).matches()) io.add(new IOFile(f));
    }
    return io.toArray(new IOFile[io.size()]);
  }

  /**
   * Returns the relative paths of all descendant files.
   * @return relative paths
   */
  public synchronized StringList descendants() {
    final StringList files = new StringList();
    final File[] ch = file.listFiles();
    if(ch == null) return files;
    if(exists()) add(this, files, path().length() + 1);
    return files;
  }

  /**
   * Adds the relative paths of all descendant files to the specified list.
   * @param io current file
   * @param files file list
   * @param off string length of root path
   */
  private static void add(final IOFile io, final StringList files, final int off) {
    if(io.isDir()) {
      for(final IOFile f : io.children()) add(f, files, off);
    } else {
      files.add(io.path().substring(off).replace('\\', '/'));
    }
  }

  /**
   * Writes the specified byte array.
   * @param c contents
   * @throws IOException I/O exception
   */
  public void write(final byte[] c) throws IOException {
    final FileOutputStream out = new FileOutputStream(path);
    try {
      out.write(c);
    } finally {
      out.close();
    }
  }

  /**
   * Writes the specified input. The specified stream is eventually closed.
   * @param in input stream
   * @throws IOException I/O exception
   */
  public void write(final InputStream in) throws IOException {
    BufferOutput out = null;
    try {
      out = new BufferOutput(path);
      for(int i; (i = in.read()) != -1;) out.write(i);
    } finally {
      try { in.close(); } catch(final IOException ex) { }
      if(out != null) out.close();
    }
  }

  /**
   * Deletes the IO reference.
   * @return success flag
   */
  public boolean delete() {
    boolean ok = true;
    if(file.exists()) {
      if(isDir()) for(final IOFile ch : children()) ok &= ch.delete();
      // some file systems require several runs
      for(int i = 0; i < 5; i++) {
        if(file.delete() && !file.exists()) return ok;
        Performance.sleep(i * 10);
      }
    }
    return false;
  }

  /**
   * Renames the file to the specified name.
   * @param trg target reference
   * @return success flag
   */
  public boolean rename(final IOFile trg) {
    return file.renameTo(trg.file);
  }

  /**
   * Copies a file to another destination.
   * @param trg target
   * @throws IOException I/O exception
   */
  public void copyTo(final IOFile trg) throws IOException {
    // optimize buffer size
    final int bsize = (int) Math.max(1, Math.min(length(), 1 << 22));
    final byte[] buf = new byte[bsize];

    FileInputStream fis = null;
    FileOutputStream fos = null;
    try {
      // create parent directory of target file
      new IOFile(trg.dir()).md();
      fis = new FileInputStream(file);
      fos = new FileOutputStream(trg.file);
      // copy file buffer by buffer
      for(int i; (i = fis.read(buf)) != -1;) fos.write(buf, 0, i);
    } finally {
      // close file references
      if(fis != null) try { fis.close(); } catch(final IOException ex) { }
      if(fos != null) try { fos.close(); } catch(final IOException ex) { }
    }
  }

  @Override
  public String url() {
    final TokenBuilder tb = new TokenBuilder(FILEPREF);
    // add leading slash for Windows paths
    if(!path.startsWith("/")) tb.add("///");
    for(int p = 0; p < path.length(); p++) {
      // replace spaces with %20
      final char ch = path.charAt(p);
      if(ch == ' ') tb.add("%20");
      else tb.add(ch);
    }
    return tb.toString();
  }

  /**
   * Converts a file filter (glob) to a regular expression.
   * @param glob filter
   * @return regular expression
   */
  public static String regex(final String glob) {
    return regex(glob, true);
  }

  /**
   * Converts a file filter (glob) to a regular expression. A filter may
   * contain asterisks (*) and question marks (?); commas (,) are used to
   * separate multiple filters.
   * @param glob filter
   * @param sub accept substring in the result
   * @return regular expression
   */
  public static String regex(final String glob, final boolean sub) {
    final StringBuilder sb = new StringBuilder();
    for(final String g : glob.split(",")) {
      boolean suf = false;
      final String gl = g.trim();
      if(sb.length() != 0) {
        if(!suf) sb.append(".*");
        suf = false;
        sb.append('|');
      }
      // loop through single pattern
      for(int f = 0; f < gl.length(); f++) {
        char ch = gl.charAt(f);
        if(ch == '*') {
          // don't allow other dots if pattern ends with a dot
          suf = true;
          sb.append(gl.endsWith(".") ? "[^.]" : ".");
        } else if(ch == '?') {
          ch = '.';
          suf = true;
        } else if(ch == '.') {
          suf = true;
          // last character is dot: disallow file suffix
          if(f + 1 == gl.length()) break;
          sb.append('\\');
        } else if(!Character.isLetterOrDigit(ch)) {
          sb.append('\\');
        }
        sb.append(ch);
      }
      if(!suf && sub) sb.append(".*");
    }
    return Prop.WIN ? sb.toString().toLowerCase(Locale.ENGLISH) : sb.toString();
  }

  /**
   * Path constructor. Resolves parent and self references and normalizes the
   * path.
   */
  static class PathList extends StringList {
    /**
     * Creates a path.
     * @param path input path
     * @return path
     */
    String create(final String path) {
      final TokenBuilder tb = new TokenBuilder();
      final int l = path.length();
      for(int i = 0; i < l; ++i) {
        final char ch = path.charAt(i);
        if(ch == '\\' || ch == '/') add(tb);
        else tb.add(ch);
      }
      add(tb);
      if(path.startsWith("\\\\") || path.startsWith("//")) tb.add("//");
      for(int s = 0; s < size; ++s) {
        if(s != 0 || path.startsWith("/")) tb.add('/');
        tb.add(list[s]);
      }
      return tb.toString();
    }

    /**
     * Adds a directory/file to the path list.
     * @param tb entry to be added
     */
    private void add(final TokenBuilder tb) {
      String s = tb.toString();
      // switch first Windows letter to upper case
      if(s.length() > 1 && s.charAt(1) == ':' && size == 0) {
        s = Character.toUpperCase(s.charAt(0)) + s.substring(1);
      }
      if(s.equals("..") && size > 0) {
        // parent step
        if(list[size - 1].indexOf(':') == -1) deleteAt(size - 1);
      } else if(!s.equals(".") && !s.isEmpty()) {
        // skip self and empty steps
        add(s);
      }
      tb.reset();
    }
  }
}
