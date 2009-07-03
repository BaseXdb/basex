package org.basex.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.xml.sax.InputSource;

/**
 * BaseX file representation, pointing to a local or remote file or
 * byte array contents.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class IOFile extends IO {
  /** Input stream reference. */
  private InputStream is;
  /** File reference. */
  private final File file;
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
    file = f;
    path = file.getAbsolutePath().replace('\\', '/');
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
    final String suf = path.toLowerCase().replaceAll(".*\\.", ".");
    return suf.equals(ZIPSUFFIX) || suf.equals(".docx") ||
      suf.equals(".xslx") || suf.equals(".pptx") ||
      suf.equals(".odt") || suf.equals(".ods") || suf.equals(".odp");
  }

  @Override
  public InputSource inputSource() {
    return is == null ? new InputSource(url(path)) : new InputSource(is);
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
  public IO merge(final IO f) {
    return f instanceof IOUrl ? f : new IOFile(dir() + "/" + f.name());
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
}
