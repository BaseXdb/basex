package org.basex.io;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.basex.core.Prop;
import org.basex.util.StringList;
import org.basex.util.TokenBuilder;
import org.xml.sax.InputSource;

/**
 * File reference, wrapped into an IO representation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class IOFile extends IO {
  /** File prefix. */
  private static final String FILEPREF = "file:";
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
    this(new File(file(f)));
  }

  /**
   * Constructor.
   * @param f file reference
   */
  public IOFile(final File f) {
    super(new PathList().create(f.getAbsolutePath()));
    file = f;
  }

  @Override
  public void cache() throws IOException {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
      cont = new byte[(int) file.length()];
      new DataInputStream(fis).readFully(cont);
    } finally {
      if(fis != null) try { fis.close(); } catch(final IOException ex) { }
    }
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
    // process zip files
    if(is instanceof ZipInputStream || zip()) {
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
        if(path.toLowerCase().endsWith(XMLSUFFIX) && !zip.isDirectory())
          return true;
      }
      is.close();
      is = null;
      return false;
    }

    // process gzip files
    if(path.endsWith(GZSUFFIX)) {
      if(is == null) {
        is = new GZIPInputStream(new FileInputStream(file));
      } else {
        is.close();
        is = null;
      }
      return is != null;
    }

    // work on normal files
    return more ^= true;
  }

  /**
   * Matches the current file against various file suffixes.
   * @return result of check
   */
  private boolean zip() {
    final int i = path.lastIndexOf('.');
    if(i == -1) return false;
    final String suf = path.substring(i).toLowerCase();
    for(final String z : ZIPSUFFIXES) if(suf.equals(z)) return true;
    return false;
  }

  @Override
  public InputSource inputSource() {
    return is == null ? new InputSource(url()) : new InputSource(is);
  }

  @Override
  public BufferInput buffer() throws IOException {
    // return file stream
    if(is == null) return new BufferInput(path);
    // return input stream
    final BufferInput in = new BufferInput(is);
    if(zip != null && zip.getSize() != -1) in.length(zip.getSize());
    return in;
  }

  @Override
  public IO merge(final String f) {
    return f.contains(":") ? IO.get(f) : new IOFile(new File(dir(), f));
  }

  @Override
  public boolean md() {
    return file.mkdirs();
  }

  @Override
  public IO[] children() {
    final File[] ch = file.listFiles();
    final int l = ch != null ? ch.length : 0;
    final IO[] io = new IO[l];
    for(int i = 0; i < l; ++i) io[i] = new IOFile(ch[i]);
    return io;
  }

  @Override
  public void write(final byte[] c) throws IOException {
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(path);
      out.write(c);
      cont = c;
    } finally {
      if(out != null) try { out.close(); } catch(final IOException ex) { }
    }
  }

  @Override
  public boolean delete() {
    if(isDir()) for(final IO ch : children()) if(!ch.delete()) return false;
    return file.delete();
  }

  @Override
  public boolean rename(final IO trg) {
    return trg instanceof IOFile && file.renameTo(((IOFile) trg).file);
  }

  @Override
  public String url() {
    String pre = FILEPREF;
    if(!path.startsWith("/")) {
      pre += '/';
      if(path.length() < 2 || path.charAt(1) != ':') {
        // [CG] IO paths: check is HOME reference is really needed here
        pre += "/" + Prop.HOME.replace('\\', '/');
        if(!pre.endsWith("/")) pre += '/';
      }
    }
    return pre + path.replace('\\', '/');
  }

  /**
   * Creates a file path from the specified URL.
   * @param url url to be converted
   * @return file path
   */
  public static String file(final String url) {
    String file = url;
    // [CG] IO paths: check if '+' is correctly recognized/handled
    if(file.indexOf("%") != -1) {
      try {
        file = URLDecoder.decode(file, Prop.ENCODING);
      } catch(final Exception ex) { /* ignored. */ }
    }
    // remove file scheme
    String fn = file.startsWith(FILEPREF) ?
        file.substring(FILEPREF.length()) : file;
        // remove leading slashes
    while(fn.startsWith("//")) fn = fn.substring(1);
    // remove slash on Windows systems
    return fn.length() > 2 && fn.charAt(0) == '/' && fn.charAt(2) == ':' ?
        fn.substring(1) : fn;
  }

  /**
   * Converts a file filter (glob) to a regular expression. A filter may
   * contain asterisks (*) and question marks (?); commas (,) are used to
   * separate multiple filters.
   * @param filter filter
   * @return regular expression
   */
  public static String regex(final String filter) {
    final StringBuilder sb = new StringBuilder();
    for(final String g : filter.split(",")) {
      boolean suf = false;
      final String glob = g.trim();
      if(sb.length() != 0) {
        if(!suf) sb.append(".*");
        suf = false;
        sb.append('|');
      }
      for(int f = 0; f < glob.length(); f++) {
        char ch = glob.charAt(f);
        if(ch == '*') {
          sb.append("[^.]");
        } else if(ch == '?') {
          ch = '.';
          suf = true;
        } else if(!Character.isLetterOrDigit(ch)) {
          if(ch == '.') {
            suf = true;
            if(f + 1 == glob.length()) break;
          }
          sb.append('\\');
        }
        sb.append(ch);
      }
      if(!suf) sb.append(".*");
    }
    return Prop.WIN ? sb.toString().toLowerCase() : sb.toString();
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
