package org.basex.query.up.primitives.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Update primitive for the {@link Function#_DB_PUT_BINARY} function.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DBPutBinary extends DBUpdate {
  /** Sources, keyed by target path. Each value is either a {@link Bin} or an {@link IO}. */
  private final TokenObjectMap<Object> paths = new TokenObjectMap<>();

  /**
   * Constructor.
   * @param data data
   * @param source source to be stored (a {@link Bin} item or an {@link IO} file reference)
   * @param path target path
   * @param info input info (can be {@code null})
   */
  public DBPutBinary(final Data data, final Object source, final String path,
      final InputInfo info) {
    super(UpdateType.DBPUTBINARY, data, info);
    paths.put(token(path), source);
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
      final Object source = paths.get(path);
      try(InputStream is = source instanceof final Bin b ? b.input(info) :
          ((IO) source).inputStream()) {
        bin.write(is);
      } catch(final IOException ex) {
        Util.debug(ex);
        throw UPDBPUT_X.get(info, path);
      }
    }
  }

  @Override
  public void merge(final Update update) throws QueryException {
    final TokenObjectMap<Object> store = ((DBPutBinary) update).paths;
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
