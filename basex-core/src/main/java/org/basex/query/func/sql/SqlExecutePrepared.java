package org.basex.query.func.sql;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.sql.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Functions on relational databases.
 *
 * @author BaseX Team, BSD License
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
    final StatementOptions options = toOptions(arg(2), new StatementOptions(), qc);

    final boolean keys = jdbc(qc).generatedKeys(ps);
    final boolean nulls = options.get(StatementOptions.NULL);
    try {
      ps.setQueryTimeout(options.get(StatementOptions.TIMEOUT));
      if(params instanceof final XQArray array) {
        // parameters supplied as array: one positional value per member
        setParameters(array, ps, qc);
      } else if(!params.isEmpty()) {
        // parameters supplied as <sql:parameters/> element
        final XNode prms = toElem(params, qc);
        if(!prms.qname().eq(Q_PARAMETERS)) throw UNKNOWNOPTION_X.get(info, prms.qname().local());
        setParameters(prms.childIter(), ps);
      }
      // If execute returns false, statement was updating: return keys or number of updated rows
      return iter(ps, false, ps.execute(), keys, nulls);
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
    for(GNode next; (next = params.next()) != null; i++) {
      // Check name
      if(!next.qname().eq(Q_PARAMETER)) throw UNKNOWNOPTION_X.get(info, next.qname().local());
      final BasicNodeIter attrs = next.attributeIter();
      String type = null;
      boolean isNull = false;
      for(GNode attr; (attr = attrs.next()) != null;) {
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

  /**
   * Sets the parameters of a prepared statement from an array. Each array member supplies one
   * positional parameter; an empty sequence is bound to {@code NULL}, and the SQL type is derived
   * from the XDM type of the value.
   * @param array array with one value per positional parameter
   * @param ps prepared statement
   * @param qc query context
   * @throws QueryException query exception
   * @throws SQLException SQL exception
   */
  private void setParameters(final XQArray array, final PreparedStatement ps, final QueryContext qc)
      throws QueryException, SQLException {
    int i = 1;
    for(final Value member : array.members()) {
      final Item item = member.atomItem(qc, info);
      final Type type = item.type;
      if(item.isEmpty()) {
        ps.setNull(i, java.sql.Types.NULL);
      } else if(type == BasicType.DATE) {
        ps.setDate(i, new java.sql.Date(ms(item)));
      } else if(type == BasicType.TIME) {
        ps.setTime(i, new Time(ms(item)));
      } else if(type == BasicType.DATE_TIME) {
        ps.setTimestamp(i, new Timestamp(ms(item)));
      } else {
        ps.setObject(i, item.toJava());
      }
      i++;
    }
  }

  /**
   * Returns the milliseconds since the epoch of a date/time item.
   * @param item date, time or dateTime item
   * @return milliseconds
   */
  private static long ms(final Item item) {
    return ((ADate) item).toJava().toGregorianCalendar().getTimeInMillis();
  }
}
