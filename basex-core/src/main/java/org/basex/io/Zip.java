package org.basex.io;

import java.io.*;
import java.util.zip.*;

import org.basex.core.jobs.*;
import org.basex.io.out.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Contains methods for zipping and unzipping archives.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Zip extends Job {
  /** Archive. */
  private final IO file;
  /** Total files in a zip operation. */
  private int total;
  /** Current file in a zip operation. */
  private int curr;

  /**
   * Constructor.
   * @param file archive file
   */
  public Zip(final IO file) {
    this.file = file;
  }

  /**
   * Returns the number of entries in a zip archive.
   * @return number of entries
   * @throws IOException I/O exception
   */
  private int size() throws IOException {
    try(ZipInputStream in = new ZipInputStream(file.inputStream())) {
      int c = 0;
      while(in.getNextEntry() != null) c++;
      return c;
    }
  }

  /**
   * Returns the contents of a zip file entry.
   * @param path file to be read
   * @return resulting byte array
   * @throws IOException I/O exception
   */
  public byte[] read(final String path) throws IOException {
    try(ZipInputStream in = new ZipInputStream(file.inputStream())) {
      final byte[] cont = getEntry(in, path);
      if(cont == null) throw new FileNotFoundException(path);
      return cont;
    }
  }

  /**
   * Unzips the archive to the specified directory.
   * @param target target path
   * @throws IOException I/O exception
   */
  public void unzip(final IOFile target) throws IOException {
    total = size();
    curr = 0;
    try(ZipInputStream in = new ZipInputStream(file.inputStream())) {
      for(ZipEntry ze; (ze = in.getNextEntry()) != null;) {
        curr++;
        final IOFile trg = new IOFile(target, ze.getName());
        if(ze.isDirectory()) {
          trg.md();
        } else {
          trg.parent().md();
          trg.write(in);
        }
      }
    }
  }

  /**
   * Zips the specified files.
   * @param root root directory
   * @param files files to add
   * @throws IOException I/O exception
   */
  public void zip(final IOFile root, final StringList files) throws IOException {
    if(!(file instanceof IOFile)) throw new FileNotFoundException(file.path());

    curr = 0;
    try(ZipOutputStream out = new ZipOutputStream(new BufferOutput((IOFile) file))) {
      // use simple, fast compression
      out.setLevel(1);
      // loop through all files
      total = files.size();
      final byte[] data = new byte[IO.BLOCKSIZE];
      for(final String f : files) {
        curr++;
        try(FileInputStream in = new FileInputStream(new File(root.file(), f))) {
          final String fl = Prop.WIN ? f.replace('\\', '/') : f;
          out.putNextEntry(new ZipEntry(root.name() + '/' + fl));
          for(int c; (c = in.read(data)) != -1;) out.write(data, 0, c);
          out.closeEntry();
        }
      }
    }
  }

  @Override
  public double progressInfo() {
    return (double) curr / total;
  }

  /**
   * Returns the contents of the specified entry.
   * @param in input stream
   * @param entry entry to be found
   * @return entry, or {@code null} if it is not found
   * @throws IOException I/O exception
   */
  private static byte[] getEntry(final ZipInputStream in, final String entry) throws IOException {
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
      return bl.finish();
    }
    return null;
  }
}
