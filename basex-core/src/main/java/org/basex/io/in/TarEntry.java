package org.basex.io.in;

import org.basex.util.list.*;

/**
 * Representation of a single TAR entry.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class TarEntry {
  /** Name of an entry. */
  private final String name;
  /** Entry size. */
  private final long size;
  /** File type. */
  private final byte type;

  /**
   * Constructor.
   * @param buffer header buffer
   */
  public TarEntry(final byte[] buffer) {
    // file name
    final ByteList result = new ByteList();
    for(int i = 0; i < 100; ++i) {
      if(buffer[i] == 0) break;
      result.add(buffer[i]);
    }
    String n;
    try {
      n = new String(result.toArray());
    } catch(final Exception ex) {
      // fallback: UTF8
      n = result.toString();
    }
    name = n;

    // file size
    long s = 0;
    boolean p = true;
    for(int i = 124; i < 136; ++i) {
      final byte b = buffer[i];
      if(p && (b == ' ' || b == '0')) continue;
      if(b == 0 || b == ' ') break;
      s = (s << 3) + (b - '0');
      p = false;
    }

    size = s;
    type = buffer[156];
  }

  /**
   * Returns the byte size of the entry.
   * @return size
   */
  public long getSize() {
    return size;
  }

  /**
   * Returns the name of the entry.
   * @return size
   */
  public String getName() {
    return name;
  }

  /**
   * Checks if the the current entry is a directory.
   * @return result of check
   */
  public boolean isDirectory() {
    return type == '5' || name.endsWith("/");
  }
}
