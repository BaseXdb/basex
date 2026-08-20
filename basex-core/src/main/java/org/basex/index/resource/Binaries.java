package org.basex.index.resource;

import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This index organizes file resources (binaries, values) in a database.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class Binaries {
  /** Private constructor. */
  private Binaries() { }

  /**
   * Returns the database paths to all file resources that match the specified path.
   * @param data data reference
   * @param type resource type
   * @param path input path
   * @return paths
   */
  static StringList paths(final Data data, final String path, final ResourceType type) {
    final StringList paths = new StringList();
    String norm = MetaData.normPath(path);
    if(norm != null && !data.inMemory()) {
      final IOFile bin = data.meta.dir(type), root = new IOFile(bin, norm);
      if(root.isDir()) {
        if(!(norm.isEmpty() || norm.endsWith("/"))) norm += '/';
        for(final String relative : root.descendants()) {
          paths.add(type.dbPath(norm + relative));
        }
      } else if(type.filePath(bin, norm).exists()) {
        paths.add(norm);
      }
    }
    return paths.sort(Prop.CASE);
  }

  /**
   * Adds the paths of file resources to a map.
   * @param data data reference
   * @param path path
   * @param dir returns directories instead of files
   * @param map paths and resource types
   */
  static void children(final Data data, final String path, final boolean dir,
      final TokenObjectMap<ResourceType> map) {

    for(final ResourceType type : Resources.BINARIES) {
      final IOFile bin = data.meta.file(path, type);
      if(bin != null) {
        final boolean value = type == ResourceType.VALUE;
        for(final IOFile child : bin.children()) {
          if(dir == child.isDir()) {
            map.put(token(value && !dir ? type.dbPath(child.name()) : child.name()), type);
          }
        }
      }
    }
  }

  /**
   * Determines whether the given path is the path to a directory with files.
   * @param data data reference
   * @param path path
   * @param type resource type
   * @return result of check
   */
  static boolean isDir(final Data data, final String path, final ResourceType type) {
    final IOFile bin = data.meta.file(path, type);
    return bin != null && bin.isDir();
  }
}
