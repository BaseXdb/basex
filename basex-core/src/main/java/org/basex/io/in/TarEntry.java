package org.basex.io.in;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.util.*;

/**
 * Representation of a single TAR entry.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TarEntry {
  /** Block size. */
  public static final int BLOCK = 512;
  /** GNU tar entry with a long name. */
  public static final String LONGNAME = "././@LongLink";
  /** Magic and version of the ustar format. */
  public static final byte[] MAGIC = { 'u', 's', 't', 'a', 'r', 0, '0', '0' };
  /** Length of the magic string (without version). */
  private static final int MAGIC_LENGTH = 5;

  /** File type. */
  private final byte type;
  /** Name of an entry. */
  private String name;
  /** Entry size. */
  private long size;
  /** Modification time in milliseconds ({@code -1} if unknown). */
  private long time;

  /**
   * Constructor for a new entry.
   * @param name name (directory names end with a slash)
   * @param size size
   * @param time modification time in milliseconds ({@code -1} if unknown)
   */
  public TarEntry(final String name, final long size, final long time) {
    this.name = name;
    this.size = size;
    this.time = time;
    type = (byte) (Strings.endsWith(name, '/') ? '5' : '0');
  }

  /**
   * Constructor for a parsed header block.
   * @param header header block
   * @throws IOException I/O exception
   */
  TarEntry(final byte[] header) throws IOException {
    // verify checksum (sum of all header bytes, the checksum field counted as spaces)
    int unsigned = 0, signed = 0;
    for(int i = 0; i < BLOCK; i++) {
      final int b = i >= 148 && i < 156 ? ' ' : header[i];
      unsigned += b & 0xFF;
      signed += b;
    }
    final long checksum = octal(header, 148, 8);
    if(checksum != unsigned && checksum != signed) throw new IOException("Invalid TAR header.");

    final String nm = field(header, 0, 100);
    final String prefix = magic(header) ? field(header, 345, 155) : "";
    name = prefix.isEmpty() ? nm : prefix + '/' + nm;
    size = octal(header, 124, 12);
    time = octal(header, 136, 12) * 1000;
    type = header[156];
  }

  /**
   * Returns the byte size of the entry.
   * @return size
   */
  public long getSize() {
    return size;
  }

  /**
   * Sets the byte size of the entry.
   * @param sz size
   */
  public void setSize(final long sz) {
    size = sz;
  }

  /**
   * Returns the modification time of the entry.
   * @return time in milliseconds ({@code -1} if unknown)
   */
  public long getTime() {
    return time;
  }

  /**
   * Sets the modification time of the entry.
   * @param tm time in milliseconds
   */
  public void setTime(final long tm) {
    time = tm;
  }

  /**
   * Returns the name of the entry.
   * @return name
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
   * Checks if the current entry is a directory.
   * @return result of check
   */
  public boolean isDirectory() {
    return type == '5' || Strings.endsWith(name, '/');
  }

  /**
   * Checks if the current entry is a regular file.
   * @return result of check
   */
  public boolean isFile() {
    return (type == '0' || type == 0 || type == '7') && !isDirectory();
  }

  /**
   * Indicates if this entry is a GNU long name block.
   * @return result of check
   */
  public boolean isLongName() {
    return type == 'L' && name.equals(LONGNAME);
  }

  /**
   * Indicates if this entry is a pax extended header.
   * @return result of check
   */
  public boolean isPax() {
    return type == 'x';
  }

  /**
   * Indicates if this entry is a pax global header.
   * @return result of check
   */
  public boolean isGlobalPax() {
    return type == 'g';
  }

  /**
   * Checks if the specified block starts a TAR archive: ustar magic, or an end-of-archive block
   * (empty archive).
   * @param header header block (may be shorter than a full block)
   * @return result of check
   */
  public static boolean isTar(final byte[] header) {
    return header.length == BLOCK && (magic(header) || isEmpty(header));
  }

  /**
   * Checks if the specified header block carries the ustar magic.
   * @param header header block
   * @return result of check
   */
  private static boolean magic(final byte[] header) {
    for(int i = 0; i < MAGIC_LENGTH; i++) {
      if(header[257 + i] != MAGIC[i]) return false;
    }
    return true;
  }

  /**
   * Checks if the specified header block consists of zero bytes.
   * @param header header block
   * @return result of check
   */
  static boolean isEmpty(final byte[] header) {
    for(final byte b : header) {
      if(b != 0) return false;
    }
    return true;
  }

  /**
   * Returns a NUL-terminated string from a header field.
   * @param header header block
   * @param off field offset
   * @param len field length
   * @return string
   */
  private static String field(final byte[] header, final int off, final int len) {
    int l = 0;
    while(l < len && header[off + l] != 0) l++;
    return string(header, off, l);
  }

  /**
   * Returns an octal number from a header field.
   * @param header header block
   * @param off field offset
   * @param len field length
   * @return number
   */
  private static long octal(final byte[] header, final int off, final int len) {
    long value = 0;
    for(int i = off, e = off + len; i < e; i++) {
      final byte b = header[i];
      if(b >= '0' && b <= '7') value = (value << 3) + b - '0';
      else if(b != ' ') break;
    }
    return value;
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + name + ", " + size + " bytes]";
  }
}
