package org.basex.gui.view.project;

import java.util.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Prepares a content replacement: collects all matching files (other than the interactive
 * filter, the result is not truncated) and counts the strings that will be replaced.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class FilterJob extends Job implements Runnable {
  /** Project files ({@code null} if the files to be changed are already known). */
  private final ProjectFiles files;
  /** Files filter. */
  private final String query;
  /** Contents filter. */
  private final ProjectFiles.ContentFilter content;
  /** Project root directory. */
  private final IOFile root;
  /** Search pattern. */
  private final Pattern pattern;

  /** Files to be changed. */
  private List<IOFile> result = new ArrayList<>();
  /** Number of strings to be replaced. */
  private int strings;
  /** Number of counted files (read by the progress dialog). */
  private volatile int counted;
  /** Indicates that all files to be changed are known. */
  private volatile boolean collected;

  /**
   * Constructor for counting the matches in the specified files.
   * @param targets files to be changed
   * @param pattern search pattern
   */
  FilterJob(final List<IOFile> targets, final Pattern pattern) {
    this(null, null, null, null, pattern);
    result = targets;
    collected = true;
  }

  /**
   * Constructor for collecting all matching files and counting their matches.
   * @param files project files
   * @param query files filter
   * @param content contents filter
   * @param root project root directory
   * @param pattern search pattern
   */
  FilterJob(final ProjectFiles files, final String query, final ProjectFiles.ContentFilter content,
      final IOFile root, final Pattern pattern) {
    this.files = files;
    this.query = query;
    this.content = content;
    this.root = root;
    this.pattern = pattern;
  }

  @Override
  public void run() {
    try {
      if(!collected) {
        for(final String path : files.filter(query, content, root, Integer.MAX_VALUE, this)) {
          result.add(new IOFile(path));
        }
        collected = true;
      }
      int count = 0;
      for(final IOFile file : result) {
        checkStop();
        // unreadable files are reported as skipped by the replacement itself
        count += Math.max(0, ProjectFiles.count(file, pattern));
        counted++;
      }
      strings = count;
    } catch(final InterruptedException | JobException ex) {
      // canceled or superseded search: no files are returned
      Util.debug(ex);
      result = new ArrayList<>();
    }
  }

  /**
   * Returns the files to be changed.
   * @return files
   */
  List<IOFile> result() {
    return result;
  }

  /**
   * Returns the number of strings to be replaced.
   * @return number of strings
   */
  int strings() {
    return strings;
  }

  @Override
  public String shortInfo() {
    return Text.FIND_CONTENTS;
  }

  @Override
  public double progressInfo() {
    if(collected) {
      final int size = result.size();
      return size == 0 ? 1 : (double) counted / size;
    }
    final int size = files.cacheSize();
    return size == 0 ? 0 : Math.min(1, (double) content.searched() / size);
  }

  @Override
  public boolean supportsProg() {
    return true;
  }

  @Override
  public boolean stoppable() {
    return true;
  }
}
