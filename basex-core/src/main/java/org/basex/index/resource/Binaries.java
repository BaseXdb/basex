package org.basex.index.resource;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This index organizes file resources in a database.
 *
 * @author BaseX Team 2005-22, BSD License
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
   * Returns the database paths to all file resources that match the specified path.
   * All paths are relative to the filesystem.
   * @param path input path
   * @return paths
   */
  synchronized TokenList paths(final String path) {
    final TokenList paths = new TokenList();
    final String norm = MetaData.normPath(path);
    if(norm != null && !data.inMemory()) {
      final IOFile bin = data.meta.dir(), root = new IOFile(bin, norm);
      if(root.exists()) {
        final String exact = Prop.CASE ? norm : norm.toLowerCase(Locale.ENGLISH);
        final String dir = root.isDir() ? (
            exact.isEmpty() || Strings.endsWith(exact, '/') ? exact : exact + '/') : null;
        for(final String relative : bin.descendants()) {
          final String rel = Prop.CASE ? relative : relative.toLowerCase(Locale.ENGLISH);
          if(dir != null ? rel.startsWith(dir) : rel.equals(exact)) paths.add(relative);
        }
      }
    }
    return paths.sort(Prop.CASE);
  }

  /**
   * Adds the paths of file resources to a map.
   * @param path path
   * @param dir returns directories instead of files
   * @param map map paths and booleans, indicating binary resources
   */
  synchronized void children(final String path, final boolean dir, final TokenBoolMap map) {
    final IOFile bin = data.meta.file(path);
    if(bin != null) {
      for(final IOFile child : bin.children()) {
        if(dir == child.isDir()) {
          map.put(token(child.name()), true);
        }
      }
    }
  }

  /**
   * Determines whether the given path is the path to a files directory.
   * @param path path
   * @return result of check
   */
  synchronized boolean isDir(final String path) {
    final IOFile bin = data.meta.file(path);
    return bin != null && bin.isDir();
  }
}
