package org.basex.query.up.primitives;

import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.query.func.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Update primitive for the {@link Function#_DB_DELETE} function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DBDelete extends BasicOperation {
  /** Keys. */
  private final StringList paths = new StringList(1);
  /** Number of keys. */
  private int size;

  /**
   * Constructor.
   * @param d data
   * @param p entries to be deleted
   * @param ii input info
   */
  public DBDelete(final Data d, final String p, final InputInfo ii) {
    super(TYPE.DBDELETE, d, ii);
    paths.add(p);
    size = d.resources.binaries(p).size();
  }

  @Override
  public void merge(final BasicOperation o) {
    for(final String path : ((DBDelete) o).paths) paths.add(path);
    size += o.size();
  }

  @Override
  public void apply() {
    for(final String path : paths) Delete.delete(data,  path);
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public void prepare(final MemData tmp) { }
}
