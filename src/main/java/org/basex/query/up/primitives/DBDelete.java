package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import org.basex.core.cmd.Delete;
import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.func.Function;
import org.basex.util.InputInfo;
import org.basex.util.list.TokenList;

/**
 * Update primitive for the {@link Function#_DB_DELETE} function.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DBDelete extends UpdatePrimitive {
  /** Keys. */
  private final TokenList keys;

  /**
   * Constructor.
   * @param d data
   * @param k entries to be deleted
   * @param info input info
   */
  public DBDelete(final Data d, final TokenList k, final InputInfo info) {
    super(PrimitiveType.DBDELETE, -1, d, info);
    keys = k;
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    for(final byte[] key : ((DBDelete) p).keys) keys.add(key);
  }

  @Override
  public void apply() throws QueryException {
    final byte[] key = Delete.delete(data,  keys);
    if(key != null) UPDBDELERR.thrw(input, key);
  }
}
