package org.basex.io;

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
import org.xml.sax.InputSource;

/**
 * File reference, wrapped into an IO representation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class IOFile extends IO {
  /** Zip entry. */
  ZipEntry zip;

  /** File prefix. */
  private static final String PREFILE = "file:";
  /** Input stream reference. */
  private InputStream is;
  /** File reference. */
  private final File file;
  /** File length. */
  private long len = -1;

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
    file = f;
    path = file.getAbsolutePath().replace('\\', '/');
    /*try {
      path = file.getCanonicalPath().replace('\\', '/');
    } catch(final IOException ex) {
    }*/
  }

  @Override
  public void cache() throws IOException {
    cont = new byte[(int) file.length()];
    BufferInput.read(file, cont);
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
        path = zip.getName();
        if(len > 0 && path.toLowerCase().endsWith(XMLSUFFIX) &&
            !zip.isDirectory()) return true;
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
    final String[] zips = {
        "zip", "docx", "xslx", "pptx", "odt", "ods", "odp", "thmx"
    };
    final String suf = path.toLowerCase().replaceAll(".*\\.", "");
    for(final String z : zips) if(suf.equals(z)) return true;
    return false;
  }

  @Override
  public InputSource inputSource() {
    return is == null ? new InputSource(url()) : new InputSource(is);
  }

  @Override
  public BufferInput buffer() throws IOException {
    if(is != null) {
      if(zip == null) return new BufferInput(is);
      final BufferInput in = new BufferInput(is);
      in.length(zip.getSize());
      return in;
    }
    // return file content
    return new BufferInput(path);
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
  public String dir() {
    return file.isDirectory() ? file.getPath() : file.getParent();
  }

  @Override
  public IO[] children() {
    final File[] ch = file.listFiles();
    final int l = ch != null ? ch.length : 0;
    final IO[] io = new IO[l];
    for(int i = 0; i < l; i++) io[i] = new IOFile(ch[i]);
    return io;
  }

  @Override
  public void write(final byte[] c) throws IOException {
    final FileOutputStream out = new FileOutputStream(path);
    out.write(c);
    out.close();
    cont = c;
  }

  @Override
  public boolean delete() {
    if(isDir()) for(final IO ch : children()) if(!ch.delete()) return false;
    return file.delete();
  }

  @Override
  public String url() {
    String pre = PREFILE;
    if(!path.startsWith("/")) {
      pre += "/";
      if(path.length() < 2 || path.charAt(1) != ':') {
        pre += "/" + Prop.WORK.replace('\\', '/');
        if(!pre.endsWith("/")) pre += "/";
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
    if(file.indexOf("%") != -1) {
      try {
        file = URLDecoder.decode(file, Prop.ENCODING);
      } catch(final Exception ex) { /* ignored. */ }
    }
    final String fn = file.startsWith(PREFILE) ?
        file.substring(PREFILE.length()) : file;
    return fn.length() > 2 && fn.charAt(0) == '/' && fn.charAt(2) == ':' ?
        fn.substring(1) : fn;
  }
}
