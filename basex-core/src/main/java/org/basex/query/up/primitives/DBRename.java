package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import java.util.*;
import java.util.Map.Entry;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;

/**
 * Update primitive for the {@link Function#_DB_RENAME} function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DBRename extends BasicOperation {
  /** Source and target paths. */
  private final HashMap<String, String> map = new HashMap<String, String>();

  /**
   * Constructor.
   * @param d target data
   * @param src source path
   * @param trg target path
   * @param ii input info
   */
  public DBRename(final Data d, final String src, final String trg, final InputInfo ii) {
    super(TYPE.DBRENAME, d, ii);
    map.put(src, trg);
  }

  @Override
  public void merge(final BasicOperation o) throws QueryException {
    for(final Entry<String, String> e : ((DBRename) o).map.entrySet()) {
      final String src = e.getKey();
      if(map.containsKey(src)) UPPATHREN.thrw(info, src);
      map.put(src, e.getValue());
    }
  }

  @Override
  public void apply() {
    for(final Entry<String, String> op : map.entrySet()) {
      final IOFile src = new IOFile(op.getKey());
      final IOFile trg = new IOFile(op.getValue());
      if(src.exists()) src.rename(trg);
    }
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public void prepare(final MemData tmp) throws QueryException { }
}
