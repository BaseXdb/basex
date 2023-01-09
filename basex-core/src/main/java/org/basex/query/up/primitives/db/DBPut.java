package org.basex.query.up.primitives.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Update primitive for the {@link Function#_DB_PUT_VALUE} function.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class DBPut extends DBUpdate {
  /** Values to be stored. */
  private final TokenObjMap<Value> paths = new TokenObjMap<>();

  /**
   * Constructor.
   * @param data data
   * @param value value to be stored
   * @param path target path
   * @param info input info
   */
  public DBPut(final Data data, final Value value, final String path, final InputInfo info) {
    super(UpdateType.DBPUT, data, info);
    paths.put(token(path), value);
  }

  @Override
  public void prepare() {
  }

  @Override
  public void apply() throws QueryException {
    for(final byte[] path : paths) {
      final IOFile bin = data.meta.file(string(path), ResourceType.VALUE);
      bin.parent().md();
      try(DataOutput out = new DataOutput(bin)) {
        Store.write(out, paths.get(path));
      } catch(final IOException ex) {
        Util.debug(ex);
        throw UPDBPUT_X.get(info, path);
      }
    }
  }

  @Override
  public void merge(final Update update) throws QueryException {
    final TokenObjMap<Value> put = ((DBPut) update).paths;
    for(final byte[] path : put) {
      if(paths.contains(path)) throw DB_CONFLICT5_X.get(info, path);
      paths.put(path, put.get(path));
    }
  }

  @Override
  public int size() {
    return paths.size();
  }
}
