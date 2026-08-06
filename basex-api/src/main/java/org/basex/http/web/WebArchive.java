package org.basex.http.web;

import java.io.*;
import java.net.*;
import java.util.*;

import org.basex.io.*;
import org.basex.query.util.pkg.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This class caches the contents of an installed web application archive.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WebArchive {
  /** Archive file. */
  private final IOFile file;
  /** Virtual root path of the archive contents. */
  private final String root;
  /** Time stamp of the archive. */
  private final long time;
  /** Cached archive entries. */
  private final HashMap<String, byte[]> entries;

  /**
   * Constructor.
   * @param file archive file
   * @throws IOException I/O exception
   */
  WebArchive(final IOFile file) throws IOException {
    this.file = file;
    root = file.path() + '/';
    time = file.timeStamp();
    entries = RepoArchive.entries(file);
  }

  /**
   * Indicates if the archive has been modified since it was cached.
   * @return result of check
   */
  boolean outdated() {
    return file.timeStamp() != time;
  }

  /**
   * Returns the time stamp of the archive.
   * @return time stamp
   */
  long time() {
    return time;
  }

  /**
   * Returns references to all XQuery modules of the archive.
   * @return module references
   */
  ArrayList<IO> modules() {
    final ArrayList<IO> list = new ArrayList<>();
    for(final String entry : entries.keySet()) {
      if(IO.checkSuffix(entry, IO.XQSUFFIXES)) list.add(io(entry));
    }
    return list;
  }

  /**
   * Resolves a path against the base URI of an archived resource.
   * @param path path to be resolved
   * @param base base URI (can be {@code null})
   * @return IO reference, or {@code null} if the path is not part of this archive
   */
  IO resolve(final String path, final Uri base) {
    if(base == null) return null;
    final String uri = IO.get(Token.string(base.string())).path();
    if(!uri.startsWith(root)) return null;

    // resolve the path against the directory of the current entry
    final String rel = uri.substring(root.length());
    final String dir = rel.substring(0, rel.lastIndexOf('/') + 1);
    final String entry;
    try {
      entry = URI.create(dir).resolve(path).normalize().toString();
    } catch(final IllegalArgumentException ex) {
      Util.debug(ex);
      return null;
    }
    return entries.containsKey(entry) ? io(entry) : null;
  }

  /**
   * Returns an IO reference for an archive entry.
   * @param entry entry path
   * @return IO reference
   */
  private IO io(final String entry) {
    return new IOContent(entries.get(entry), root + entry);
  }
}
