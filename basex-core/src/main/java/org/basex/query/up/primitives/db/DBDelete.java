package org.basex.query.up.primitives.db;

import java.util.*;

import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;

/**
 * Update primitive for the {@link Function#_DB_DELETE} function.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DBDelete extends DBUpdate {
  /** Paths to binaries to be deleted, and their resource types. */
  private final HashMap<IOFile, ResourceType> paths = new HashMap<>();
  /** Number of keys. */
  private int size;

  /**
   * Constructor.
   * @param data data
   * @param path path to binaries
   * @param type resource type
   * @param info input info (can be {@code null})
   */
  public DBDelete(final Data data, final IOFile path, final ResourceType type,
      final InputInfo info) {
    super(UpdateType.DBDELETE, data, info);
    size = path.isDir() ? path.descendants().size() : 1;
    paths.put(path, type);
  }

  @Override
  public void prepare() {
  }

  @Override
  public void apply() {
    paths.forEach((path, tp) -> {
      path.delete();
      path.parent().deleteEmpty(data.meta.dir(tp));
    });
  }

  @Override
  public void merge(final Update update) {
    paths.putAll(((DBDelete) update).paths);
    size += update.size();
  }

  @Override
  public int size() {
    return size;
  }
}
