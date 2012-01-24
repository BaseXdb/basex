package org.basex.query.up.primitives;

import org.basex.core.cmd.Delete;
import org.basex.data.Data;
import org.basex.query.func.Function;
import org.basex.util.InputInfo;
import org.basex.util.list.StringList;

/**
 * Update primitive for the {@link Function#_DB_DELETE} function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DBDelete extends UpdatePrimitive {
  /** Keys. */
  private final StringList paths = new StringList(1);
  /** Number of keys. */
  private int size;

  /**
   * Constructor.
   * @param d data
   * @param k entries to be deleted
   * @param info input info
   */
  public DBDelete(final Data d, final String k, final InputInfo info) {
    super(PrimitiveType.DBDELETE, -1, d, info);
    paths.add(k);
    size = d.resources.binaries(k).size();
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    for(final String path : ((DBDelete) p).paths) paths.add(path);
    size += p.size();
  }

  @Override
  public void apply() {
    for(final String path : paths) Delete.delete(data,  path);
  }

  @Override
  public int size() {
    return size;
  }
}
