package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import java.util.HashMap;
import java.util.Map.Entry;

import org.basex.data.Data;
import org.basex.io.IOFile;
import org.basex.query.QueryException;
import org.basex.query.func.Function;
import org.basex.util.InputInfo;

/**
 * Update primitive for the {@link Function#_DB_RENAME} function.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DBRename extends UpdatePrimitive {
  /** Source and target paths. */
  private final HashMap<String, String> map = new HashMap<String, String>();

  /**
   * Constructor.
   * @param d data
   * @param src source path
   * @param trg target path
   * @param info input info
   */
  public DBRename(final Data d, final String src, final String trg,
      final InputInfo info) {

    super(PrimitiveType.DBRENAME, -1, d, info);
    map.put(src, trg);
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    for(final Entry<String, String> e : ((DBRename) p).map.entrySet()) {
      final String src = e.getKey();
      if(map.containsKey(src)) UPPATHREN.thrw(input, src);
      map.put(src, e.getValue());
    }
  }

  @Override
  public void apply() throws QueryException {
    for(final Entry<String, String> op : map.entrySet()) {
      final IOFile src = data.meta.binary(op.getKey());
      if(src.exists() && !src.rename(data.meta.binary(op.getValue())))
        UPDBRENAMEERR.thrw(input, src);
    }
  }
}
