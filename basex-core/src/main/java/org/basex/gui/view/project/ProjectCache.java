package org.basex.gui.view.project;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.function.*;

import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Project files cache.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ProjectCache implements Iterable<String> {
  /** Cached file paths (all with forward-slashes). */
  private final StringList cache = new StringList();
  /** Show hidden files. */
  private final boolean showHidden;
  /** Maximum number of paths to be cached. */
  private final int max;
  /** Valid flag. */
  private boolean valid;

  /**
   * Constructor.
   * @param showHidden show hidden files
   * @param max maximum number of paths to be cached
   */
  ProjectCache(final boolean showHidden, final int max) {
    this.showHidden = showHidden;
    this.max = max;
  }

  /**
   * Indicates if the cache is valid.
   * @return flag
   */
  boolean valid() {
    return valid;
  }

  /**
   * Returns the number of cached files.
   * @return number of files
   */
  int size() {
    return cache.size();
  }

  /**
   * Recursively populates the cache.
   * @param root root directory
   * @param stop stop function
   * @throws InterruptedException interrupted exception
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
   * @throws InterruptedException interrupted exception
   */
  private void add(final Path root, final Predicate<ProjectCache> stop,
      final HashSet<String> links) throws InterruptedException {

    // check if file cache was replaced or invalidated; stop if maximum has been reached
    if(stop.test(this)) throw new InterruptedException();
    if(cache.size() == max) return;

    try {
      // follow symbolic links and junctions only once
      final BasicFileAttributes attrs = Files.readAttributes(root, BasicFileAttributes.class,
          LinkOption.NOFOLLOW_LINKS);
      if(IOFile.isLink(attrs) && !links.add(root.toRealPath().toString())) return;
    } catch(final IOException ex) {
      Util.debug(ex);
      return;
    }

    // skip hidden files, cancel parsing if directory contains .ignore file
    final ArrayList<Path> dirs = new ArrayList<>(), files = new ArrayList<>();
    final boolean[] ignore = { false };
    new IOFile(root).children((name, attrs) -> {
      if(IOFile.ignore(name)) {
        ignore[0] = true;
      } else if(showHidden || !IOFile.isHidden(name, attrs)) {
        (attrs.isDirectory() ? dirs : files).add(root.resolve(name));
      }
    });
    if(ignore[0]) return;

    // traverse directories
    for(final Path dir : dirs) {
      add(dir, stop, links);
    }

    // add files; stop traversal if maximum has been exceeded
    for(final Path file : files) {
      if(cache.size() == max) return;
      cache.add(new IOFile(file).path());
    }
  }

  @Override
  public Iterator<String> iterator() {
    return cache.iterator();
  }
}
