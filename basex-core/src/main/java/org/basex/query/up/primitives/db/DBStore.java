package org.basex.query.up.primitives.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Update primitive for the {@link Function#_DB_STORE} function.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class DBStore extends DBUpdate {
  /** Keys. */
  private final TokenObjMap<Item> map = new TokenObjMap<>();

  /**
   * Constructor.
   * @param data data
   * @param path target path
   * @param item item to be stored
   * @param info input info
   */
  public DBStore(final Data data, final String path, final Item item, final InputInfo info) {
    super(UpdateType.DBSTORE, data, info);
    map.put(token(path), item);
  }

  @Override
  public void prepare() {
  }

  @Override
  public void apply() throws QueryException {
    for(final byte[] path : map) {
      final IOFile bin = data.meta.binary(string(path));
      if(bin.isDir()) bin.delete();
      bin.parent().md();
      try {
        bin.write(map.get(path).input(info));
      } catch(final IOException ex) {
        Util.debug(ex);
        throw UPDBPUT_X.get(info, path);
      }
    }
  }

  @Override
  public void merge(final Update update) throws QueryException {
    final TokenObjMap<Item> store = ((DBStore) update).map;
    for(final byte[] path : store) {
      if(map.contains(path)) throw DB_CONFLICT5_X.get(info, path);
      map.put(path, store.get(path));
    }
  }

  @Override
  public int size() {
    return map.size();
  }
}
