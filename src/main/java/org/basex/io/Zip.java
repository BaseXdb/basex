package org.basex.io;

import java.io.*;
import java.util.regex.*;
import java.util.zip.*;

import org.basex.core.*;
import org.basex.util.list.*;

/**
 * Contains methods for zipping and unzipping archives.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Zip extends Progress {
  /** Archive. */
  private final IO archive;
  /** Total files in a zip operation. */
  private int total;
  /** Current file in a zip operation. */
  private int curr;

  /**
   * Constructor.
   * @param file archive file
   */
  public Zip(final IO file) {
    archive = file;
  }

  /**
   * Returns the number of entries in a zip archive.
   * @return number of entries
   * @throws IOException I/O exception
   */
  private int size() throws IOException {
    int c = 0;
    ZipInputStream in = null;
    try {
      in = new ZipInputStream(archive.inputStream());
      while(in.getNextEntry() != null) c++;
      return c;
    } finally {
      if(in != null) try { in.close(); } catch(final IOException e) { }
    }
  }

  /**
   * Returns the contents of a zip file entry.
   * @param path file to be read
   * @return resulting byte array
   * @throws IOException I/O exception
   */
  public byte[] read(final String path) throws IOException {
    final ZipInputStream in = new ZipInputStream(archive.inputStream());
    try {
      final byte[] cont = getEntry(in, path);
      if(cont == null) throw new FileNotFoundException(path);
      return cont;
    } finally {
      try { in.close(); } catch(final IOException e) { }
    }
  }

  /**
   * Unzips the archive to the specified directory.
   * @param target target path
   * @throws IOException I/O exception
   */
  public void unzip(final IOFile target) throws IOException {
    final byte[] data = new byte[IO.BLOCKSIZE];
    final ZipInputStream in = new ZipInputStream(archive.inputStream());
    total = size();
    curr = 0;
    try {
      for(ZipEntry ze; (ze = in.getNextEntry()) != null;) {
        curr++;
        final IOFile trg = new IOFile(target, ze.getName());
        if(ze.isDirectory()) {
          trg.md();
        } else {
          new IOFile(trg.dir()).md();
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
      try { in.close(); } catch(final IOException e) { }
    }
  }

  /**
   * Zips the specified directory.
   * @param source directory to be zipped
   * @param pattern regular expression pattern
   * @throws IOException I/O exception
   */
  public void zip(final IOFile source, final Pattern pattern) throws IOException {
    if(!(archive instanceof IOFile)) throw new FileNotFoundException(archive.path());

    final byte[] data = new byte[IO.BLOCKSIZE];
    ZipOutputStream out = null;
    curr = 0;

    try {
      // create output stream for zipping; use fast compression
      out = new ZipOutputStream(new BufferedOutputStream(
          new FileOutputStream(archive.path())));
      out.setLevel(1);
      out.putNextEntry(new ZipEntry(source.name() + '/'));
      out.closeEntry();

      // loop through all files
      final StringList files = source.descendants();
      total = files.size();
      for(final String io : files) {
        curr++;
        if(pattern != null && !pattern.matcher(io).matches()) continue;

        FileInputStream in = null;
        try {
          in = new FileInputStream(new File(source.file(), io));
          out.putNextEntry(new ZipEntry(source.name() + '/' + io));
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

  /**
   * Returns the contents of the specified entry, or {@code null}.
   * @param in input stream
   * @param entry entry to be found
   * @return entry
   * @throws IOException I/O exception
   */
  public static byte[] getEntry(final ZipInputStream in, final String entry)
      throws IOException {

    for(ZipEntry ze; (ze = in.getNextEntry()) != null;) {
      if(!entry.equals(ze.getName())) continue;
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
    }
    return null;
  }
}
