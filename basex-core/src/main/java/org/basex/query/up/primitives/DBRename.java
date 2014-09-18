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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DBRename extends DBUpdate {
  /** Source and target paths. */
  private final HashMap<String, String> map = new HashMap<>();

  /**
   * Constructor.
   * @param data target data
   * @param src source path
   * @param trg target path
   * @param info input info
   */
  public DBRename(final Data data, final String src, final String trg, final InputInfo info) {
    super(UpdateType.DBRENAME, data, info);
    map.put(src, trg);
  }

  @Override
  public void merge(final Update update) throws QueryException {
    for(final Entry<String, String> e : ((DBRename) update).map.entrySet()) {
      final String src = e.getKey();
      if(map.containsKey(src)) throw UPPATHREN_X.get(info, src);
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
  public void prepare(final MemData tmp) { }
}
