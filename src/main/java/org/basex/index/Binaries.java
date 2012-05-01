package org.basex.index;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * <p>This index organizes binary files in a database.</p>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class Binaries {
  /** Data reference. */
  private final Data data;

  /**
   * Constructor.
   * @param d data reference
   */
  Binaries(final Data d) {
    data = d;
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

    final String exct = Prop.WIN ? np.toLowerCase(Locale.ENGLISH) : np;
    final String pref = exct + '/';
    for(final String f : data.meta.binaries().descendants()) {
      final String lc = Prop.WIN ? f.toLowerCase(Locale.ENGLISH) : f;
      if(exct.isEmpty() || lc.equals(exct) || lc.startsWith(pref)) tl.add(f);
    }
    return tl.sort(!Prop.WIN);
  }

  /**
   * Adds the database paths for the binaries of the given path to
   * the given map.
   * @param path path
   * @param dir returns directories instead of files
   * @param tbm map; values will be {@code true} to indicate raw files
   */
  synchronized void children(final byte[] path, final boolean dir,
      final TokenBoolMap tbm) {

    if(data.inMemory()) return;
    final IOFile file = data.meta.binary(string(path));
    if(file == null) return;

    for(final IOFile f : file.children()) {
      if(!dir ^ f.isDir()) tbm.add(token(f.name()), true);
    }
  }

  /**
   * Determines whether the given path is the path to a directory.
   * @param path given path (must be normalized, means one leading but
   * no trailing slash.
   * @return path to a directory or not
   */
  synchronized boolean isDir(final String path) {
    if(data.inMemory()) return false;
    final IOFile bin = data.meta.binary(path);
    return bin != null && bin.isDir();
  }
}
