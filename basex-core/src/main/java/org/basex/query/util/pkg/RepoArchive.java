package org.basex.query.util.pkg;

import java.io.*;
import java.util.zip.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.list.*;

/**
 * Contains methods for zipping and unzipping archives.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
final class RepoArchive {
  /** Archive data. */
  private final byte[] data;

  /**
   * Constructor.
   * @param data archive data
   */
  RepoArchive(final byte[] data) {
    this.data = data;
  }

  /**
   * Returns the contents of a zip file entry.
   * @param path file to be read
   * @return resulting byte array
   * @throws IOException I/O exception
   */
  byte[] read(final String path) throws IOException {
    try(ZipInputStream in = new ZipInputStream(new ArrayInput(data))) {
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
  void unzip(final IOFile target) throws IOException {
    try(ZipInputStream in = new ZipInputStream(new ArrayInput(data))) {
      for(ZipEntry ze; (ze = in.getNextEntry()) != null;) {
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
