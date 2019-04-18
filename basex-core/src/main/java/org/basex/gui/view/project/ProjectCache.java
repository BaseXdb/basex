package org.basex.gui.view.project;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Project files cache.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
final class ProjectCache implements Iterable<String> {
  /** Maximum number of paths to be cached. */
  private static final int MAX = 50000;

  /** Cached file paths (all with forward slashes). */
  private final StringList files = new StringList();
  /** Hide files. */
  private final boolean hide;
  /** Valid flag. */
  private boolean valid;

  /**
   * Constructor.
   * @param hide hide files
   */
  ProjectCache(final boolean hide) {
    this.hide = hide;
  }

  /**
   * Indicates if the cache is valid.
   * @return flag
   */
  boolean valid() {
    return valid;
  }

  /**
   * Recursively populates the cache.
   * @param root root directory
   * @param stop stop function
   * @throws InterruptedException interruption
   */
  void scan(final Path root, final Predicate<ProjectCache> stop) throws InterruptedException {
    add(root, stop, new HashSet<>());
    valid = true;
  }

  /**
   * Recursively populates the cache.
   * @param root root directory
   * @param stop stop function
   * @param links symbolic links
   * @throws InterruptedException interruption
   */
  private void add(final Path root, final Predicate<ProjectCache> stop,
      final HashSet<String> links) throws InterruptedException {

    // check if file cache was replaced or invalidated
    if(stop.test(this)) throw new InterruptedException();

    final ArrayList<Path> dirs = new ArrayList<>();
    try {
      // follow symbolic links only once
      if(Files.isSymbolicLink(root) && !links.add(root.toRealPath().toString())) return;

      try(DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
        for(final Path file : stream) {
          // stop traversal if maximum has been exceeded
          if(files.size() == MAX) return;

          // skip hidden files
          if(hide && (file.getFileName().toString().startsWith(".") || Files.isHidden(file)))
            continue;

          if(Files.isDirectory(file)) {
            dirs.add(file);
          } else {
            files.add(file.toString());
          }
        }
      }

    } catch(final IOException ex) {
      Util.debug(ex);
    }

    // traverse directory in second step (reduces number of opened files)
    for(final Path dir : dirs) add(dir, stop, links);
  }

  @Override
  public Iterator<String> iterator() {
    return files.iterator();
  }
}
