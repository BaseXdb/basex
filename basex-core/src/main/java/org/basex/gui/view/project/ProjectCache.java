package org.basex.gui.view.project;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Project files cache.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class ProjectCache implements Iterable<String> {
  /** Maximum number of paths to be cached. */
  private static final int MAX = 50000;

  /** Cached file paths (all with forward slashes). */
  private final StringList cache = new StringList();
  /** Show hidden files. */
  private final boolean showHidden;
  /** Valid flag. */
  private boolean valid;

  /**
   * Constructor.
   * @param showHidden show hidden files
   */
  ProjectCache(final boolean showHidden) {
    this.showHidden = showHidden;
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

    try {
      // follow symbolic links only once
      if(Files.isSymbolicLink(root) && !links.add(root.toRealPath().toString())) return;

      final ArrayList<Path> dirs = new ArrayList<>();
      final ArrayList<IOFile> files = new ArrayList<>();
      try(DirectoryStream<Path> paths = Files.newDirectoryStream(root)) {
        for(final Path path : paths) {
          // skip hidden files, cancel parsing if directory contains .ignore file
          final IOFile io = new IOFile(path.toFile());
          if(io.ignore()) return;
          if(showHidden || !io.isHidden()) {
            if(Files.isDirectory(path)) {
              dirs.add(path);
            } else {
              files.add(io);
            }
          }
        }
      }

      // traverse directories
      for(final Path dir : dirs) {
        add(dir, stop, links);
      }

      // add files; stop traversal if maximum has been exceeded
      for(final IOFile file : files) {
        if(cache.size() == MAX) return;
        cache.add(file.path());
      }

    } catch(final IOException ex) {
      Util.debug(ex);
    }

  }

  @Override
  public Iterator<String> iterator() {
    return cache.iterator();
  }
}
