package org.basex.query.up.primitives.db;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;

/**
 * Update primitive for the {@link Function#_DB_RENAME} function.
 *
 * @author BaseX Team, BSD License
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
   * @param info input info (can be {@code null})
   */
  public DBRename(final Data data, final String src, final String trg, final InputInfo info) {
    super(UpdateType.DBRENAME, data, info);
    map.put(src, trg);
  }

  @Override
  public void prepare() {
  }

  @Override
  public void apply() throws QueryException {
    for(final Entry<String, String> entry : map.entrySet()) {
      final IOFile src = new IOFile(entry.getKey()), trg = new IOFile(entry.getValue());
      if(src.exists()) {
        trg.delete();
        try {
          src.moveTo(trg, false);
        } catch(final IOException ex) {
          throw UPDBPUT_X.get(info, trg).cause(ex);
        }
      }
    }
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
  public int size() {
    return map.size();
  }
}
