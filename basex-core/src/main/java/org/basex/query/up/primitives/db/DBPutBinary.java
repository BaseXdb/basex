package org.basex.query.up.primitives.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Update primitive for the {@link Function#_DB_PUT_BINARY} function.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class DBPutBinary extends DBUpdate {
  /** Paths. */
  private final TokenObjMap<Item> paths = new TokenObjMap<>();

  /**
   * Constructor.
   * @param data data
   * @param item item to be stored
   * @param path target path
   * @param info input info
   */
  public DBPutBinary(final Data data, final Item item, final String path, final InputInfo info) {
    super(UpdateType.DBPUTBINARY, data, info);
    paths.put(token(path), item);
  }

  @Override
  public void prepare() {
  }

  @Override
  public void apply() throws QueryException {
    for(final byte[] path : paths) {
      final IOFile bin = data.meta.file(string(path), ResourceType.BINARY);
      if(bin.isDir()) bin.delete();
      bin.parent().md();
      try(BufferInput bi = paths.get(path).input(info)) {
        bin.write(bi);
      } catch(final IOException ex) {
        Util.debug(ex);
        throw UPDBPUT_X.get(info, path);
      }
    }
  }

  @Override
  public void merge(final Update update) throws QueryException {
    final TokenObjMap<Item> store = ((DBPutBinary) update).paths;
    for(final byte[] path : store) {
      if(paths.contains(path)) throw DB_CONFLICT5_X.get(info, path);
      paths.put(path, store.get(path));
    }
  }

  @Override
  public int size() {
    return paths.size();
  }
}
