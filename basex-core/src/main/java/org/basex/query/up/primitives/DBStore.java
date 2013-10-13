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
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class DBStore extends BasicOperation {
  /** Keys. */
  private final TokenObjMap<Object> map = new TokenObjMap<Object>();

  /**
   * Constructor.
   * @param d data
   * @param path target path
   * @param it item to be stored
   * @param ii input info
   */
  public DBStore(final Data d, final String path, final Object it, final InputInfo ii) {
    super(TYPE.DBSTORE, d, ii);
    map.put(token(path), it);
  }

  @Override
  public void merge(final BasicOperation o) {
    final DBStore put = (DBStore) o;
    for(final byte[] path : put.map) {
      map.put(path, put.map.get(path));
    }
  }

  @Override
  public void apply() throws QueryException {
    for(final byte[] path : map) {
      try {
        final IOFile file = data.meta.binary(string(path));
        if(file == null) UPDBPUTERR.thrw(info, path);
        file.dir().md();
        final Object item = map.get(path);
        file.write(item instanceof Item ? ((Item) item).input(info) :
          ((QueryInput) item).input.inputStream());
      } catch(final IOException ex) {
        Util.debug(ex);
        UPDBPUTERR.thrw(info, path);
      }
    }
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public void prepare(final MemData tmp) throws QueryException { }
}
