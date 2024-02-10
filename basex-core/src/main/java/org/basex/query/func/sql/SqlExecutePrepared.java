package org.basex.query.func.sql;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.sql.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Functions on relational databases.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Rositsa Shadura
 */
public final class SqlExecutePrepared extends SqlExecute {
  /** Attribute "type" of <sql:parameter/>. */
  private static final byte[] TYPE = token("type");
  /** Attribute "null" of <sql:parameter/>. */
  private static final byte[] NULL = token("null");

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final PreparedStatement ps = prepared(qc);
    final Item params = arg(1).item(qc, info);
    final StatementOptions options = toOptions(arg(2), new StatementOptions(), true, qc);

    final ANode prms = params.isEmpty() ? null : toElem(params, qc);
    if(prms != null && !prms.qname().eq(Q_PARAMETERS)) {
      throw INVALIDOPTION_X.get(info, prms.qname().local());
    }

    try {
      ps.setQueryTimeout(options.get(StatementOptions.TIMEOUT));
      if(prms != null) setParameters(prms.childIter(), ps);
      // If execute returns false, statement was updating: return number of updated rows
      return iter(ps, false, ps.execute());
    } catch(final QueryException ex) {
      // already handled
      throw ex;
    } catch(final Exception ex) {
      // catch all kinds of exceptions
      // e.g., java.lang.ArrayIndexOutOfBoundsException in case of SQLite
      throw SQL_ERROR_X.get(info, ex);
    }
  }

  /**
   * Sets the parameters of a prepared statement.
   * @param params parameters
   * @param ps prepared statement
   * @throws QueryException query exception
   */
  private void setParameters(final BasicNodeIter params, final PreparedStatement ps)
      throws QueryException {

    int i = 1;
    for(ANode next; (next = params.next()) != null; i++) {
      // Check name
      if(!next.qname().eq(Q_PARAMETER)) throw INVALIDOPTION_X.get(info, next.qname().local());
      final BasicNodeIter attrs = next.attributeIter();
      String type = null;
      boolean isNull = false;
      for(ANode attr; (attr = attrs.next()) != null;) {
        // attribute "type"
        if(eq(attr.name(), TYPE)) type = string(attr.string());
        // attribute "null"
        else if(eq(attr.name(), NULL)) isNull = attr.string() != null && Bln.parse(attr, info);
        // attribute not expected
        else throw SQL_ATTRIBUTE_X.get(info, attr.name());
      }
      if(type == null) throw SQL_PARAMETERS.get(info);

      final String value = string(next.string());
      try {
        final SqlType st = SqlType.valueOf(type.toUpperCase(Locale.ENGLISH));
        if(isNull) st.setNull(i, ps);
        else st.set(i, value, ps);
      } catch(final SQLException ex) {
        throw SQL_ERROR_X.get(info, ex);
      } catch(final IllegalArgumentException ex) {
        Util.debug(ex);
        throw SQL_TYPE_X_X.get(info, type, value);
      }
    }
  }
}
