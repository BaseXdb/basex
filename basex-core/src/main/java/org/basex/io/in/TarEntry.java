package org.basex.io.in;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Representation of a single TAR entry.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class TarEntry {
  /** GNU tar entry with a long name. */
  private static final String LONGNAME = "././@LongLink";

  /** Entry size. */
  private final long size;
  /** File type. */
  private final byte type;
  /** Name of an entry. */
  private String name;

  /**
   * Constructor.
   * @param buffer header buffer
   */
  TarEntry(final byte[] buffer) {
    // file name
    final ByteList result = new ByteList();
    for(int i = 0; i < 100; ++i) {
      if(buffer[i] == 0) break;
      result.add(buffer[i]);
    }
    name = name(result);

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
   * Sets a file name.
   * @param nm name
   */
  public void setName(final String nm) {
    name = nm;
  }

  /**
   * Checks if the the current entry is a directory.
   * @return result of check
   */
  public boolean isDirectory() {
    return type == '5' || Strings.endsWith(name, '/');
  }

  /**
   * Indicate if this entry is a GNU long name block.
   * @return true if this is a long name extension provided by GNU tar
   */
  public boolean isLongName() {
    return type == 'L' && name.equals(LONGNAME);
  }

  /**
   * Converts a byte list to a file name.
   * @param result byte list
   * @return file name
   */
  static String name(final ByteList result) {
    try {
      return new String(result.toArray());
    } catch(final Exception ex) {
      // fallback: UTF8
      Util.debug(ex);
      return result.toString();
    }
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + name + ", " + size + " bytes]";
  }
}
