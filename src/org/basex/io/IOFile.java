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
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class IOFile extends IO {
  /** Input stream reference. */
  private InputStream is;
  /** File reference. */
  private final File file;
  /** File length. */
  protected long len;
  /** Zip entry. */
  protected ZipEntry zip;


  /**
   * Constructor.
   * @param f file path
   */
  public IOFile(final String f) {
    file = new File(f);
    path = file.getAbsolutePath().replace('\\', '/');
    len = file.length();
  }

  @Override
  public void cache() throws IOException {
    cont = new byte[(int) file.length()];
    final FileInputStream in = new FileInputStream(file);
    in.read(cont);
    in.close();
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
  public boolean isSymLink() throws IOException {
    return !path.equals(file.getCanonicalPath());
  }

  @Override
  public long date() {
    return file.lastModified();
  }

  @Override
  public long length() {
    return len;
  }

  @Override
  public boolean more() throws IOException {
    // process zip files
    if(is instanceof ZipInputStream || path.endsWith(ZIPSUFFIX)) {
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
        if(!zip.isDirectory()) return true;
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
    final String fn = file.getParent() + "/" + f.name();
    return f instanceof IOUrl ? f : new IOFile(fn);
  }

  @Override
  public IO[] children() {
    final File[] ch = file.listFiles();
    final int l = ch != null ? ch.length : 0;
    final IO[] io = new IO[l];
    for(int i = 0; i < l; i++) io[i] = new IOFile(ch[i].getAbsolutePath());
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
    return file.delete();
  }
}
