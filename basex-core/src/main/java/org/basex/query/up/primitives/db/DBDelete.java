package org.basex.query.up.primitives.db;

import java.util.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;

/**
 * Update primitive for the {@link Function#_DB_DELETE} function.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class DBDelete extends DBUpdate {
  /** Path to binaries to be deleted. */
  private final ArrayList<IOFile> paths = new ArrayList<>(1);
  /** Number of keys. */
  private int size;

  /**
   * Constructor.
   * @param data data
   * @param path path to binaries
   * @param info input info
   */
  public DBDelete(final Data data, final IOFile path, final InputInfo info) {
    super(UpdateType.DBDELETE, data, info);
    this.size = path.isDir() ? path.descendants().size() : 1;
    paths.add(path);
  }

  @Override
  public void prepare() {
  }

  @Override
  public void apply() {
    for(final IOFile path : paths) path.delete();
  }

  @Override
  public void merge(final Update update) {
    paths.addAll(((DBDelete) update).paths);
    size += update.size();
  }

  @Override
  public int size() {
    return size;
  }
}
