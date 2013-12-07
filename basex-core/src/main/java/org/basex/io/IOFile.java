package org.basex.io;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import javax.xml.transform.stream.*;

import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.xml.sax.*;

/**
 * {@link IO} reference, representing a local file or directory path.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class IOFile extends IO {
  /** Pattern for valid file names. */
  private static final Pattern VALIDNAME =
      Pattern.compile("^[^\\\\/" + (Prop.WIN ? ":*?\"<>\\|" : "") + "]+$");
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
   * @param fl file reference
   */
  public IOFile(final File fl) {
    super(new PathList().create(fl.getAbsolutePath()));
    file = fl.isAbsolute() ? fl : fl.getAbsoluteFile();
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
        Performance.sleep(i * 10L);
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
  public StreamSource streamSource() {
    return new StreamSource(path);
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
   * Checks if the file is hidden.
   * @return result of check
   */
  public boolean hidden() {
    return file.isHidden();
  }

  /**
   * Recursively creates the directory.
   * @return success flag
   */
  public boolean md() {
    return !file.exists() && file.mkdirs();
  }

  @Override
  public String dirPath() {
    return isDir() ? path : path.substring(0, path.lastIndexOf('/') + 1);
  }

  /**
   * Returns a directory reference. If the file points to a directory, a self reference
   * is returned
   * @return directory
   */
  public IOFile dir() {
    return isDir() ? this : new IOFile(path.substring(0, path.lastIndexOf('/') + 1));
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
    final Pattern p = Pattern.compile(regex, Prop.CASE ? 0 : Pattern.CASE_INSENSITIVE);
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
    try {
      final BufferOutput out = new BufferOutput(path);
      try {
        for(int i; (i = in.read()) != -1;) out.write(i);
      } finally {
        out.close();
      }
    } finally {
      in.close();
    }
  }

  /**
   * Deletes the IO reference.
   * @return success flag
   */
  public boolean delete() {
    if(file.exists()) {
      boolean ok = true;
      if(isDir()) for(final IOFile ch : children()) ok &= ch.delete();
      // some file systems require several runs
      for(int i = 0; i < 5; i++) {
        if(file.delete() && !file.exists()) return ok;
        Performance.sleep(i * 10L);
      }
    }
    return false;
  }

  /**
   * Renames a file to the specified path. The path must not exist yet.
   * @param target target reference
   * @return success flag
   */
  public boolean rename(final IOFile target) {
    return file.renameTo(target.file);
  }

  /**
   * Copies a file to another target.
   * @param target target
   * @throws IOException I/O exception
   */
  public void copyTo(final IOFile target) throws IOException {
    // optimize buffer size
    final int bsize = (int) Math.max(1, Math.min(length(), 1 << 22));
    final byte[] buf = new byte[bsize];

    // create parent directory of target file
    target.dir().md();
    final FileInputStream fis = new FileInputStream(file);
    try {
      final FileOutputStream fos = new FileOutputStream(target.file);
      try {
        // copy file buffer by buffer
        for(int i; (i = fis.read(buf)) != -1;) fos.write(buf, 0, i);
      } finally {
        fos.close();
      }
    } finally {
      fis.close();
    }
  }

  @Override
  public boolean eq(final IO io) {
    return io instanceof IOFile && (Prop.CASE ? path.equals(io.path) :
      path.equalsIgnoreCase(io.path));
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
    if(isDir()) tb.add('/');
    return tb.toString();
  }

  /**
   * Opens the file externally.
   * @throws IOException I/O exception
   */
  public void open() throws IOException {
    final String[] args;
    if(Prop.WIN) {
      args = new String[] { "rundll32", "url.dll,FileProtocolHandler", path };
    } else if(Prop.MAC) {
      args = new String[] { "/usr/bin/open", path };
    } else {
      args = new String[] { "xdg-open", path };
    }
    new ProcessBuilder(args).directory(dir().file).start();
  }

  // STATIC METHODS ===============================================================================

  /**
   * Checks if the specified sting is a valid file name.
   * @param name file name
   * @return result of check
   */
  public static boolean isValidName(final String name) {
    return VALIDNAME.matcher(name).matches();
  }

  /**
   * Checks if the specified string is a valid file reference.
   * @param s source
   * @return result of check
   */
  public static boolean isValid(final String s) {
    // accept short strings, string without colons and strings with Windows drive letters
    return s.length() < 3 || s.indexOf(':') == -1 ||
        Token.letter(s.charAt(0)) && s.charAt(1) == ':';
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
      final String gl = g.trim();
      if(sb.length() != 0) sb.append('|');
      // loop through single pattern
      boolean suf = false;
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
    return Prop.CASE ? sb.toString() : sb.toString().toLowerCase(Locale.ENGLISH);
  }

  /**
   * Normalizes the specified URL and creates a new instance of this class.
   * @param url url to be converted
   * @return file path
   */
  public static IOFile get(final String url) {
    String file = url;
    try {
      if(file.indexOf('%') != -1) file = URLDecoder.decode(file, Prop.ENCODING);
    } catch(final Exception ex) { /* ignored. */ }
    // remove file scheme
    if(file.startsWith(FILEPREF)) file = file.substring(FILEPREF.length());
    // remove duplicate slashes
    file = normSlashes(file);
    // remove leading slash from Windows paths
    if(file.length() > 2 && file.charAt(0) == '/' && file.charAt(2) == ':' &&
        Token.letter(file.charAt(1))) file = file.substring(1);

    return new IOFile(file);
  }

  /**
   * Normalize slashes in the specified path.
   * @param path path to be normalized
   * @return normalized path
   */
  private static String normSlashes(final String path) {
    boolean a = true;
    final StringBuilder sb = new StringBuilder(path.length());
    final int pl = path.length();
    for(int p = 0; p < pl; p++) {
      final char c = path.charAt(p);
      final boolean b = c != '/';
      if(a || b) sb.append(c);
      a = b;
    }
    return sb.toString();
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
      if("..".equals(s) && size > 0) {
        // parent step
        if(list[size - 1].indexOf(':') == -1) deleteAt(size - 1);
      } else if(!".".equals(s) && !s.isEmpty()) {
        // skip self and empty steps
        add(s);
      }
      tb.reset();
    }
  }
}
