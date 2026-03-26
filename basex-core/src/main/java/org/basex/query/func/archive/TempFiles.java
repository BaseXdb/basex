package org.basex.query.func.archive;

import java.util.*;

import org.basex.io.*;
import org.basex.query.*;

/**
 * Temporary files created during query evaluation.
 * 
 * Temporary files that are created during query evaluation and registered in this class will be
 * deleted when the query context is closed, ensuring that no temporary files are left behind after
 * the query execution.
 *
 * @author BaseX Team, BSD License
 */
public final class TempFiles implements QueryResource {
  /** List of temporary files. */
  private final List<IOFile> files = new ArrayList<>();

  /**
   * Adds a temporary file to be deleted on close.
   * @param file temporary file
   */
  synchronized void add(final IOFile file) {
    files.add(file);
  }

  @Override
  public synchronized void close() {
    for(final IOFile file : files) file.delete();
    files.clear();
  }
}
