package org.basex.io;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.basex.core.Prop;
import org.basex.io.in.BufferInput;
import org.basex.io.out.BufferOutput;
import org.basex.util.TokenBuilder;
import org.basex.util.list.ObjList;
import org.basex.util.list.StringList;
import org.xml.sax.InputSource;

/**
 * {@link IO} reference, representing a local file or directory path.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class IOFile extends IO {
  /** File reference. */
  private final File file;

  /** Input stream reference. */
  private InputStream is;
  /** File length. */
  private long len = -1;
  /** Zip entry. */
  ZipEntry zip;

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
  public IOFile(final File dir, final String n) {
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

  @Override
  public byte[] read() throws IOException {
    final DataInputStream dis = new DataInputStream(new FileInputStream(file));
    final byte[] cont = new byte[(int) file.length()];
    try {
      dis.readFully(cont);
    } finally {
      dis.close();
    }
    return cont;
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
  public long date() {
    return file.lastModified();
  }

  @Override
  public long length() {
    if(len == -1) len = file.length();
    return len;
  }

  @Override
  public boolean more() throws IOException {
    // process gzip files
    if(path.toLowerCase(Locale.ENGLISH).endsWith(GZSUFFIX)) {
      if(is == null) {
        is = new GZIPInputStream(new FileInputStream(file));
      } else {
        is.close();
        is = null;
      }
      return is != null;
    }

    // process zip files
    if(is instanceof ZipInputStream || isArchive()) {
      if(is == null) {
        // keep stream open until last file was parsed...
        is = new ZipInputStream(new FileInputStream(file)) {
          @Override
          public void close() throws IOException {
            if(zip == null) super.close();
          }
        };
      }
      while(true) {
        zip = ((ZipInputStream) is).getNextEntry();
        if(zip == null) break;
        len = zip.getSize();
        init(zip.getName());
        if(path.toLowerCase(Locale.ENGLISH).endsWith(XMLSUFFIX) &&
            !zip.isDirectory()) return true;
      }
      is.close();
      is = null;
      return false;
    }

    // work on normal files
    return more ^= true;
  }

  @Override
  public boolean isArchive() {
    return isSuffix(ZIPSUFFIXES);
  }

  @Override
  public boolean isXML() {
    return isSuffix(XMLSUFFIXES);
  }

  /**
   * Tests if the file suffix matches the specified suffixed.
   * @param suffixes suffixes to compare with
   * @return result of check
   */
  private boolean isSuffix(final String[] suffixes) {
    final int i = path.lastIndexOf('.');
    if(i == -1) return false;
    final String suf = path.substring(i).toLowerCase(Locale.ENGLISH);
    for(final String z : suffixes) if(suf.equals(z)) return true;
    return false;
  }

  @Override
  public InputSource inputSource() {
    return is == null ? new InputSource(path) : new InputSource(is);
  }

  @Override
  public BufferInput buffer() throws IOException {
    // return file stream
    if(is == null) return new BufferInput(file);
    // return input stream
    final BufferInput in = new BufferInput(is);
    if(zip != null && zip.getSize() != -1) in.length(zip.getSize());
    return in;
  }

  @Override
  public IOFile merge(final String f) {
    return f.contains(":") ? new IOFile(f) : new IOFile(dir(), f);
  }

  /**
   * Recursively creates the directory.
   * @return contents
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
   * @param pattern pattern
   * @return children
   */
  public IOFile[] children(final String pattern) {
    final File[] ch = file.listFiles();
    if(ch == null) return new IOFile[] {};

    final ObjList<IOFile> io = new ObjList<IOFile>(ch.length);
    final Pattern p = Pattern.compile(pattern,
        Prop.WIN ? Pattern.CASE_INSENSITIVE : 0);
    for(final File f : ch) {
      if(p.matcher(f.getName()).matches()) io.add(new IOFile(f));
    }
    return io.toArray(new IOFile[io.size()]);
  }

  /**
   * Returns all descendants of the path.
   * @return descendants
   */
  public synchronized StringList descendants() {
    final StringList files = new StringList();
    final File[] ch = file.listFiles();
    if(ch == null) return files;
    if(exists()) add(this, files, path().length() + 1);
    return files;
  }

  /**
   * Adds binary files to the specified list.
   * @param io current file
   * @param files file list
   * @param off root prefix
   */
  private void add(final IOFile io, final StringList files, final int off) {
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
   * Writes the specified input.
   * @param in input stream
   * @throws IOException I/O exception
   */
  public void write(final InputStream in) throws IOException {
    final BufferOutput out = new BufferOutput(path);
    try {
      for(int i; (i = in.read()) != -1;) out.write(i);
    } finally {
      try { in.close(); } catch(final IOException ex) { }
      out.close();
    }
  }

  /**
   * Deletes the IO reference.
   * @return success flag
   */
  public boolean delete() {
    boolean ok = true;
    if(isDir()) for(final IOFile ch : children()) ok &= ch.delete();
    return file.delete() && ok;
  }

  /**
   * Renames the specified IO reference.
   * @param trg target reference
   * @return success flag
   */
  public boolean rename(final IOFile trg) {
    return file.renameTo(trg.file);
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
   * Checks if the specified string is a valid file reference.
   * @param s source
   * @return result of check
   */
  static boolean valid(final String s) {
    if(s.length() < 3 || s.indexOf(':') == -1) return true;
    final char c = Character.toLowerCase(s.charAt(0));
    return c >= 'a' && c <= 'z' && s.charAt(1) == ':';
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
        if(list[size - 1].indexOf(':') == -1) delete(size - 1);
      } else if(!s.equals(".") && !s.isEmpty()) {
        // skip self and empty steps
        add(s);
      }
      tb.reset();
    }
  }
}
