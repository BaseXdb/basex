package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Update primitive for the {@link Function#_DB_STORE} function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DBStore extends DBUpdate {
  /** Keys. */
  private final TokenObjMap<Object> map = new TokenObjMap<>();

  /**
   * Constructor.
   * @param data data
   * @param path target path
   * @param it item to be stored
   * @param inf input info
   */
  public DBStore(final Data data, final String path, final Object it, final InputInfo inf) {
    super(UpdateType.DBSTORE, data, inf);
    map.put(token(path), it);
  }

  @Override
  public void merge(final Update up) {
    final DBStore put = (DBStore) up;
    for(final byte[] path : put.map) map.put(path, put.map.get(path));
  }

  @Override
  public void apply() throws QueryException {
    for(final byte[] path : map) {
      try {
        final IOFile file = data.meta.binary(string(path));
        if(file == null) throw UPDBPUTERR.get(info, path);
        file.dir().md();
        final Object item = map.get(path);
        file.write(item instanceof Item ? ((Item) item).input(info) :
          ((QueryInput) item).input.inputStream());
      } catch(final IOException ex) {
        Util.debug(ex);
        throw UPDBPUTERR.get(info, path);
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
