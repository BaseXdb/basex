package org.basex.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.basex.core.Progress;
import org.basex.util.ByteList;


/**
 * Contains utility classes for zip files.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Zip extends Progress {
  /** Archive. */
  private final File archive;
  /** Total files in a zip operation. */
  private int total;
  /** Current file in a zip operation. */
  private int curr;

  /**
   * Constructor.
   * @param file archive file
   */
  public Zip(final File file) {
    archive = file;
  }

  /**
   * Returns the number of entries in a zip archive.
   * @return number of entries
   * @throws IOException I/O exception
   */
  public int size() throws IOException {
    final ZipFile zf = new ZipFile(archive);
    final int c = zf.size();
    zf.close();
    return c;
  }

  /**
   * Returns the contents of a zip file entry.
   * @param path file to be read
   * @return resulting byte array
   * @throws IOException I/O exception
   */
  public byte[] read(final String path) throws IOException {
    final ZipFile zf = new ZipFile(archive);
    final ZipEntry ze = zf.getEntry(path);
    if(ze == null) throw new FileNotFoundException(path);

    try {
      final InputStream in = zf.getInputStream(ze);
      final int s = (int) ze.getSize();
      if(s >= 0) {
        // known size: pre-allocate and fill array
        final byte[] data = new byte[s];
        int c, o = 0;
        while(s - o != 0 && (c = in.read(data, o, s - o)) != -1) o += c;
        return data;
      }
      // unknown size: use byte list
      final byte[] data = new byte[IO.BLOCKSIZE];
      final ByteList bl = new ByteList();
      for(int c; (c = in.read(data)) != -1;) bl.add(data, 0, c);
      return bl.toArray();
    } finally {
      zf.close();
    }
  }

  /**
   * Unzips the archive to the specified directory.
   * @param target target path
   * @throws IOException I/O exception
   */
  public void unzip(final IOFile target) throws IOException {
    final byte[] data = new byte[IO.BLOCKSIZE];
    ZipInputStream in = null;
    total = size();
    curr = 0;
    try {
      in = new ZipInputStream(new BufferedInputStream(
           new FileInputStream(archive)));

      for(ZipEntry ze; (ze = in.getNextEntry()) != null;) {
        curr++;
        final IOFile trg = new IOFile(target, ze.getName());
        if(ze.isDirectory()) {
          trg.md();
        } else {
          trg.parent().md();
          OutputStream out = null;
          try {
            out = new FileOutputStream(trg.path());
            for(int c; (c = in.read(data)) != -1;) out.write(data, 0, c);
          } finally {
            if(out != null) try { out.close(); } catch(final IOException e) { }
          }
        }
      }
    } finally {
      if(in != null) try { in.close(); } catch(final IOException e) { }
    }
  }

  /**
   * Zips the specified source.
   * @param source source to be zipped
   * @throws IOException I/O exception
   */
  public void zip(final File source) throws IOException {
    final byte[] data = new byte[IO.BLOCKSIZE];
    ZipOutputStream out = null;
    curr = 0;

    try {
      // create output stream for zipping; use fast compression
      out = new ZipOutputStream(new BufferedOutputStream(
          new FileOutputStream(archive)));
      out.setLevel(1);
      out.putNextEntry(new ZipEntry(source.getName() + '/'));
      out.closeEntry();

      // loop through all files
      final IO[] files = new IOFile(source).children();
      total = files.length;
      for(final IO io : files) {
        curr++;
        FileInputStream in = null;
        try {
          in = new FileInputStream(io.path());
          out.putNextEntry(new ZipEntry(source.getName() + '/' + io.name()));
          for(int c; (c = in.read(data)) != -1;) out.write(data, 0, c);
          out.closeEntry();
        } finally {
          if(in != null) try { in.close(); } catch(final IOException e) { }
        }
      }
    } finally {
      if(out != null) try { out.close(); } catch(final IOException e) { }
    }
  }

  @Override
  protected double prog() {
    return (double) curr / total;
  }
}
