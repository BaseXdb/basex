package org.basex.gui.view.project;

import java.util.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Replaces the contents of a list of files.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ReplaceJob extends Job implements Runnable {
  /** Files to be changed. */
  private final List<IOFile> targets;
  /** Search pattern. */
  private final Pattern pattern;
  /** Java replacement string. */
  private final String replacement;
  /** Backup directory ({@code null} if no backups are created). */
  private final IOFile backupDir;
  /** Project root directory. */
  private final IOFile root;
  /** Changed files. */
  private final List<IOFile> changed = new ArrayList<>();

  /** Number of processed files (read by the progress dialog). */
  private volatile int processed;
  /** Number of replaced strings. */
  private int count;
  /** Number of files that could not be processed. */
  private int skipped;
  /** Error raised by an invalid replacement ({@code null} otherwise). */
  private RuntimeException error;

  /**
   * Constructor.
   * @param targets files to be changed
   * @param root project root directory
   * @param pattern search pattern
   * @param replacement Java replacement string
   * @param backupDir backup directory (can be {@code null})
   */
  ReplaceJob(final List<IOFile> targets, final IOFile root, final Pattern pattern,
      final String replacement, final IOFile backupDir) {
    this.targets = targets;
    this.root = root;
    this.pattern = pattern;
    this.replacement = replacement;
    this.backupDir = backupDir;
  }

  @Override
  public void run() {
    try {
      for(final IOFile file : targets) {
        checkStop();
        final String rel = relative(root, file);
        final IOFile backup = rel == null || backupDir == null ? null :
          new IOFile(backupDir, rel);
        final int c = rel == null ? -1 :
          ProjectFiles.replace(file, pattern, replacement, backup);
        if(c > 0) {
          changed.add(file);
          count += c;
        } else if(c < 0) {
          skipped++;
        }
        processed++;
      }
    } catch(final JobException ex) {
      // replacement was canceled: the files changed so far are kept
      Util.debug(ex);
    } catch(final RuntimeException ex) {
      Util.debug(ex);
      error = ex;
    }
  }

  /**
   * Returns the path of a file relative to the project root.
   * @param root project root directory
   * @param file file
   * @return relative path, or {@code null} if the file is not inside the root
   */
  private static String relative(final IOFile root, final IOFile file) {
    final String base = Strings.endsWith(root.path(), '/') ? root.path() : root.path() + '/';
    final String path = file.path();
    return path.startsWith(base) ? path.substring(base.length()) : null;
  }

  /**
   * Returns the files whose contents were replaced.
   * @return changed files
   */
  List<IOFile> changed() {
    return changed;
  }

  /**
   * Returns the number of replaced strings.
   * @return number of replacements
   */
  int count() {
    return count;
  }

  /**
   * Returns the number of files that could not be processed.
   * @return number of skipped files
   */
  int skipped() {
    return skipped;
  }

  /**
   * Returns the error raised by an invalid replacement.
   * @return error (can be {@code null})
   */
  RuntimeException error() {
    return error;
  }

  @Override
  public String shortInfo() {
    return Text.REPLACE_ALL;
  }

  @Override
  public String detailedInfo() {
    final int t = processed;
    return t < targets.size() ? targets.get(t).path() : Text.PLEASE_WAIT_D;
  }

  @Override
  public double progressInfo() {
    return (double) processed / targets.size();
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
