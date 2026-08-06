package org.basex.query.util.pkg;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.basex.io.*;
import org.basex.io.in.*;

/**
 * Contains methods for zipping and unzipping archives.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RepoArchive {
  /** Archive data. */
  private final byte[] data;

  /**
   * Constructor.
   * @param data archive data
   */
  public RepoArchive(final byte[] data) {
    this.data = data;
  }

  /**
   * Returns the contents of a zip file entry.
   * @param path file to be read
   * @return resulting byte array
   * @throws IOException I/O exception
   */
  byte[] read(final String path) throws IOException {
    final byte[] cont = entry(path);
    if(cont == null) throw new FileNotFoundException(path);
    return cont;
  }

  /**
   * Returns the contents of an optional zip file entry.
   * @param path file to be read
   * @return resulting byte array, or {@code null} if the entry does not exist
   * @throws IOException I/O exception
   */
  public byte[] entry(final String path) throws IOException {
    try(ZipInputStream in = new ZipInputStream(new ArrayInput(data))) {
      return entry(in, path);
    }
  }

  /**
   * Returns the contents of an optional entry of an archive file. The file is streamed, i.e.
   * only the requested entry is read.
   * @param file archive file
   * @param path file to be read
   * @return resulting byte array, or {@code null} if the entry does not exist
   * @throws IOException I/O exception
   */
  public static byte[] entry(final IOFile file, final String path) throws IOException {
    try(ZipInputStream in = new ZipInputStream(file.inputStream())) {
      return entry(in, path);
    }
  }

  /**
   * Returns the contents of an optional entry of an input stream.
   * @param in input stream
   * @param path file to be read
   * @return resulting byte array, or {@code null} if the entry does not exist
   * @throws IOException I/O exception
   */
  private static byte[] entry(final ZipInputStream in, final String path) throws IOException {
    for(ZipEntry ze; (ze = in.getNextEntry()) != null;) {
      if(path.equals(ze.getName())) return content(in, ze);
    }
    return null;
  }

  /**
   * Returns the contents of all entries of an archive file.
   * @param file archive file
   * @return map with entry paths and contents
   * @throws IOException I/O exception
   */
  public static HashMap<String, byte[]> entries(final IOFile file) throws IOException {
    final HashMap<String, byte[]> map = new HashMap<>();
    try(ZipInputStream in = new ZipInputStream(file.inputStream())) {
      for(ZipEntry ze; (ze = in.getNextEntry()) != null;) {
        if(!ze.isDirectory()) map.put(ze.getName(), content(in, ze));
      }
    }
    return map;
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
   * Returns the contents of the current entry.
   * @param in input stream
   * @param ze zip entry
   * @return contents
   * @throws IOException I/O exception
   */
  private static byte[] content(final ZipInputStream in, final ZipEntry ze) throws IOException {
    // pre-allocate if the size is known, otherwise read to the end of the entry
    final int s = (int) ze.getSize();
    return s >= 0 ? in.readNBytes(s) : in.readAllBytes();
  }
}
