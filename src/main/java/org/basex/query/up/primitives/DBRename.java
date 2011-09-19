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
 * Update primitive for the {@link Function#DBRENAME} function.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DBRename extends UpdatePrimitive {
  /** Keys. */
  private final HashMap<IOFile, IOFile> map = new HashMap<IOFile, IOFile>();

  /**
   * Constructor.
   * @param d data
   * @param src source path
   * @param trg target path
   * @param info input info
   */
  public DBRename(final Data d, final IOFile src, final IOFile trg,
      final InputInfo info) {

    super(PrimitiveType.DBRENAME, -1, d, info);
    map.put(src, trg);
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    map.putAll(((DBRename) p).map);
  }

  @Override
  public void apply() throws QueryException {
    for(final Entry<IOFile, IOFile> op : map.entrySet()) {
      final IOFile src = op.getKey();
      if(src.exists() && !src.rename(op.getValue()))
        UPDBRENAMEERR.thrw(input, src);
    }
  }
}
