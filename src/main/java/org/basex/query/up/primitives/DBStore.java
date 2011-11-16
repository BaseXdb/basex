package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.data.Data;
import org.basex.io.IOFile;
import org.basex.query.QueryException;
import org.basex.query.func.Function;
import org.basex.util.InputInfo;
import org.basex.util.Util;
import org.basex.util.hash.TokenMap;

/**
 * Update primitive for the {@link Function#_DB_STORE} function.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DBStore extends UpdatePrimitive {
  /** Keys. */
  private final TokenMap map = new TokenMap();

  /**
   * Constructor.
   * @param d data
   * @param path target path
   * @param val value to be stored
   * @param info input info
   */
  public DBStore(final Data d, final byte[] path, final byte[] val,
      final InputInfo info) {

    super(PrimitiveType.DBSTORE, -1, d, info);
    map.add(path, val);
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    final DBStore put = (DBStore) p;
    for(final byte[] path : put.map) {
      map.add(path, put.map.get(path));
    }
  }

  @Override
  public void apply() throws QueryException {
    for(final byte[] path : map) {
      try {
        final IOFile file = data.meta.binary(string(path));
        if(file == null) UPDBPUTERR.thrw(input, path);
        new IOFile(file.dir()).md();
        file.write(map.get(path));
      } catch(final IOException ex) {
        Util.debug(ex);
        UPDBPUTERR.thrw(input, path);
      }
    }
  }

  @Override
  public int size() {
    return map.size();
  }
}
