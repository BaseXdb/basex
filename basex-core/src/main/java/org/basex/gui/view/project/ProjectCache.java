package org.basex.gui.view.project;

import java.util.*;

import org.basex.util.list.*;

/**
 * Project files cache.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
final class ProjectCache implements Iterable<String> {
  /** Cached file paths (all with forward slashes). */
  private final StringList files = new StringList();
  /** Valid flag. */
  private boolean valid;

  /**
   * Indicates if the cache is valid.
   * @return flag
   */
  boolean valid() {
    return valid;
  }

  /**
   * Adds a file path.
   * @param path file path
   */
  void add(final String path) {
    files.add(path);
  }

  /**
   * Finishes the cache.
   */
  void finish() {
    valid = true;
  }

  @Override
  public Iterator<String> iterator() {
    return files.iterator();
  }
}
