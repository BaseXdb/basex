package org.basex.query.func.sql;

import static org.basex.query.QueryError.*;

import java.sql.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SqlExecuteBatch extends SqlExecutePrepared {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final PreparedStatement ps = prepared(qc);
    final StatementOptions options = toOptions(arg(2), new StatementOptions(), qc);

    try {
      ps.setQueryTimeout(options.get(StatementOptions.TIMEOUT));
      // add one batch per parameter set (array or <sql:parameters/> element)
      for(final Item params : arg(1).value(qc)) {
        // clear previous values so an under-specified set cannot inherit stale parameters
        ps.clearParameters();
        bind(params, ps, qc);
        ps.addBatch();
      }
      // return the update counts of all batches
      return IntSeq.get(ps.executeBatch()).iter();
    } catch(final QueryException ex) {
      // already handled
      throw ex;
    } catch(final Exception ex) {
      // catch all kinds of exceptions
      throw SQL_ERROR_X.get(info, ex);
    }
  }
}
