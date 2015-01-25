package org.basex.query.up.primitives.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Update primitive for the {@link Function#_DB_STORE} function.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DBStore extends DBUpdate {
  /** Keys. */
  private final TokenObjMap<Item> map = new TokenObjMap<>();

  /**
   * Constructor.
   * @param data data
   * @param path target path
   * @param it item to be stored
   * @param inf input info
   */
  public DBStore(final Data data, final String path, final Item it, final InputInfo inf) {
    super(UpdateType.DBSTORE, data, inf);
    map.put(token(path), it);
  }

  @Override
  public void merge(final Update update) {
    final DBStore put = (DBStore) update;
    for(final byte[] path : put.map) map.put(path, put.map.get(path));
  }

  @Override
  public void apply() throws QueryException {
    for(final byte[] path : map) {
      try {
        final IOFile file = data.meta.binary(string(path));
        if(file.isDir()) file.delete();
        file.parent().md();
        try(final BufferInput bi = map.get(path).input(info)) {
          file.write(bi);
        }
      } catch(final IOException ex) {
        Util.debug(ex);
        throw UPDBPUT_X.get(info, path);
      }
    }
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public void prepare(final MemData tmp) { }
}
