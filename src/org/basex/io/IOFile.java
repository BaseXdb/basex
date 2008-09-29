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
  public long date() {
    return file.lastModified();
  }

  @Override
  public long length() {
    return len;
  }

  @Override
  public boolean more() throws IOException {
    // return cached content
    if(is instanceof ZipInputStream || path.endsWith(ZIPSUFFIX)) {
      // doesn't work yet with several zip entries
      if(is == null) is = new ZipInputStream(new FileInputStream(file));
      else return false;

      while(is != null) {
        final ZipEntry e = ((ZipInputStream) is).getNextEntry();
        if(e == null) {
          is = null;
        } else {
          len = e.getSize();
          path = e.getName();
          if(!e.isDirectory()) break;
        }
      }
      more = true;
    } else if(path.endsWith(GZSUFFIX)) {
      if(is == null) is = new GZIPInputStream(new FileInputStream(file));
      more = false;
    } else {
      more ^= true;
    }
    return more;
  }

  @Override
  public InputSource inputSource() {
    return is == null ? new InputSource(url(path)) :
      new InputSource(is);
  }

  @Override
  public BufferInput buffer() throws IOException {
    // support for zipped files; the first file will be chosen
    if(path.endsWith(ZIPSUFFIX)) {
      final ZipInputStream zip = new ZipInputStream(new FileInputStream(file));
      final ZipEntry entry = zip.getNextEntry();
      final BufferInput in = new BufferInput(zip);
      in.length(entry.getSize());
      return in;
    }
    // support for gzipped files
    if(path.endsWith(GZSUFFIX)) {
      return new BufferInput(new GZIPInputStream(new FileInputStream(file)));
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
