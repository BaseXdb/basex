package org.basex.query.func.archive;

import static org.basex.query.func.archive.ArchiveText.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.basex.util.*;

/**
 * ZIP input.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ZIPIn extends ArchiveIn {
  /** ZIP input stream. */
  private final ZipInputStream zis;
  /** Current entry. */
  private ZipEntry ze;

  /**
   * Constructor.
   * @param is input stream
   */
  ZIPIn(final InputStream is) {
    zis = new ZipInputStream(is, Strings.CP437);
  }

  @Override
  public boolean more() throws IOException {
    ze = fix(zis.getNextEntry());
    return ze != null;
  }

  @Override
  public ZipEntry entry() {
    return ze;
  }

  @Override
  public int read(final byte[] d) throws IOException {
    return zis.read(d);
  }

  @Override
  public String format() {
    return ZIP;
  }

  @Override
  public void close() {
    try { zis.close(); } catch(final IOException ex) { Util.debug(ex); }
  }

  /**
   * Looks up an entry by name, falling back to mojibake-fixed name matching so on-disk archives
   * accept the same names that the streaming reader exposes. The returned entry keeps its original
   * name so that {@link ZipFile#getInputStream} can still locate the entry's data.
   * Callers that need the corrected name should use their lookup key.
   * @param zip ZIP file
   * @param name entry name (as seen by callers, after mojibake fix)
   * @return matching entry, or {@code null}
   */
  static ZipEntry lookup(final ZipFile zip, final String name) {
    final ZipEntry entry = zip.getEntry(name);
    if(entry != null) return entry;

    final String canonical = Strings.canonical(name);
    final Enumeration<? extends ZipEntry> en = zip.entries();
    while(en.hasMoreElements()) {
      final ZipEntry ze = en.nextElement();
      if(canonical.equals(Strings.canonical(ze.getName()))) return ze;
    }
    return null;
  }

  /**
   * Returns a copy of the entry with its name corrected or the original if no fix applies.
   * @param entry original entry (can be {@code null})
   * @return entry with corrected name (can be {@code null})
   */
  private static ZipEntry fix(final ZipEntry entry) {
    if(entry == null) return null;
    final String name = entry.getName(), canonical = Strings.canonical(name);
    if(canonical.equals(name)) return entry;

    final ZipEntry ze = new ZipEntry(canonical);
    ze.setCompressedSize(entry.getCompressedSize());
    ze.setTime(entry.getTime());
    ze.setComment(entry.getComment());
    ze.setExtra(entry.getExtra());
    if(entry.getMethod() != -1) ze.setMethod(entry.getMethod());
    if(entry.getSize() != -1) ze.setSize(entry.getSize());
    if(entry.getCrc() != -1) ze.setCrc(entry.getCrc());
    return ze;
  }
}
