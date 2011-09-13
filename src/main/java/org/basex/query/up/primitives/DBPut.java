package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.data.Data;
import org.basex.io.IOFile;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;
import org.basex.util.Util;
import org.basex.util.hash.TokenMap;

/**
 * Update primitive for the db:store() function.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DBPut extends UpdatePrimitive {
  /** Keys. */
  private final TokenMap map = new TokenMap();

  /**
   * Constructor.
   * @param d data
   * @param key key to be stored
   * @param val value to be stored
   * @param info input info
   */
  public DBPut(final Data d, final byte[] key, final byte[] val,
      final InputInfo info) {

    super(PrimitiveType.DBPUT, -1, d, info);
    map.add(key, val);
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    final DBPut put = (DBPut) p;
    for(final byte[] key : put.map) {
      map.add(key, put.map.get(key));
    }
  }

  @Override
  public void apply() throws QueryException {
    for(final byte[] key : map) {
      try {
        final IOFile file = data.meta.binary(string(key));
        new IOFile(file.dir()).md();
        file.write(map.get(key));
      } catch(final IOException ex) {
        Util.debug(ex);
        UPDBPUTERR.thrw(input, key);
      }
    }
  }
}
