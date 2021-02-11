package org.basex.index.resource;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * <p>This index organizes binary files in a database.</p>
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class Binaries {
  /** Data reference. */
  private final Data data;

  /**
   * Constructor.
   * @param data data reference
   */
  Binaries(final Data data) {
    this.data = data;
  }

  /**
   * Returns the database paths to all binary files that match the
   * specified path. All paths are relative to the filesystem.
   * @param path input path
   * @return root nodes
   */
  synchronized TokenList bins(final String path) {
    final TokenList tl = new TokenList();
    final String np = MetaData.normPath(path);
    if(np == null || data.inMemory()) return tl;

    final String exct = Prop.CASE ? np : np.toLowerCase(Locale.ENGLISH);
    final String pref = Strings.endsWith(exct, '/') ? exct : exct + '/';
    for(final String f : data.meta.binaryDir().descendants()) {
      final String lc = Prop.CASE ? f : f.toLowerCase(Locale.ENGLISH);
      if(exct.isEmpty() || lc.equals(exct) || lc.startsWith(pref)) tl.add(f);
    }
    return tl.sort(Prop.CASE);
  }

  /**
   * Adds the database paths for the binaries of the given path to
   * the given map.
   * @param path path
   * @param dir returns directories instead of files
   * @param tbm map; values will be {@code true} to indicate raw files
   */
  synchronized void children(final byte[] path, final boolean dir, final TokenBoolMap tbm) {
    if(data.inMemory()) return;
    final IOFile file = data.meta.binary(string(path));
    if(file == null) return;

    for(final IOFile f : file.children()) {
      if(!dir ^ f.isDir()) tbm.put(token(f.name()), true);
    }
  }

  /**
   * Determines whether the given path is the path to a directory.
   * @param path given path
   * @return result of check
   */
  synchronized boolean isDir(final String path) {
    if(data.inMemory()) return false;
    final IOFile bin = data.meta.binary(path);
    return bin != null && bin.isDir();
  }
}
