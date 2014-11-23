package org.basex.query.func.sql;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.sql.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

/**
 * Functions on relational databases.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 */
public final class SqlExecutePrepared extends SqlExecute {
  /** QName. */
  private static final QNm Q_PARAMETERS = QNm.get(SQL_PREFIX, "parameters", SQL_URI);
  /** QName. */
  private static final QNm Q_PARAMETER = QNm.get(SQL_PREFIX, "parameter", SQL_URI);

  /** Type int. */
  private static final byte[] INT = AtomType.INT.string();
  /** Type string. */
  private static final byte[] STRING = AtomType.STR.string();
  /** Type boolean. */
  private static final byte[] BOOL = AtomType.BLN.string();
  /** Type date. */
  private static final byte[] DATE = AtomType.DAT.string();
  /** Type double. */
  private static final byte[] DOUBLE = AtomType.DBL.string();
  /** Type float. */
  private static final byte[] FLOAT = AtomType.FLT.string();
  /** Type short. */
  private static final byte[] SHORT = AtomType.SHR.string();
  /** Type time. */
  private static final byte[] TIME = AtomType.TIM.string();
  /** Type timestamp. */
  private static final byte[] TIMESTAMP = token("timestamp");

  /** Attribute "type" of <sql:parameter/>. */
  private static final byte[] TYPE = token("type");

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    final int id = (int) toLong(exprs[0], qc);
    final Object obj = jdbc(qc).get(id);
    if(!(obj instanceof PreparedStatement)) throw BXSQ_STATE_X.get(info, id);

    // Get parameters for prepared statement
    long c = 0;
    ANode params = null;
    if(exprs.length > 1) {
      params = toElem(exprs[1], qc);
      if(!params.qname().eq(Q_PARAMETERS)) throw INVALIDOPTION_X.get(info, params.qname().local());
      c = countParams(params);
    }

    try {
      final PreparedStatement stmt = (PreparedStatement) obj;
      // Check if number of parameters equals number of place holders
      if(c != stmt.getParameterMetaData().getParameterCount()) throw BXSQ_PARAMS.get(info);
      if(params != null) setParameters(params.children(), stmt);
      return stmt.execute() ? buildResult(stmt.getResultSet()) : new NodeSeqBuilder();
    } catch(final SQLException ex) {
      throw BXSQ_ERROR_X.get(info, ex);
    }
  }

  /**
   * Counts the numbers of <sql:parameter/> elements.
   * @param params element <sql:parameter/>
   * @return number of parameters
   */
  private static long countParams(final ANode params) {
    final AxisIter ch = params.children();
    long c = ch.size();
    if(c == -1) do ++c;
    while(ch.next() != null);
    return c;
  }

  /**
   * Sets the parameters of a prepared statement.
   * @param params parameters
   * @param stmt prepared statement
   * @throws QueryException query exception
   */
  private void setParameters(final AxisMoreIter params, final PreparedStatement stmt)
      throws QueryException {

    int i = 0;
    for(ANode next; (next = params.next()) != null;) {
      // Check name
      if(!next.qname().eq(Q_PARAMETER)) throw INVALIDOPTION_X.get(info, next.qname().local());
      final AxisIter attrs = next.attributes();
      byte[] paramType = null;
      boolean isNull = false;
      for(ANode attr; (attr = attrs.next()) != null;) {
        // Attribute "type"
        if(eq(attr.name(), TYPE)) paramType = attr.string();
        // Attribute "null"
        else if(eq(attr.name(), NULL))
          isNull = attr.string() != null && Bln.parse(attr.string(), info);
        // Not expected attribute
        else throw BXSQ_ATTR_X.get(info, string(attr.name()));
      }
      if(paramType == null) throw BXSQ_TYPE.get(info);
      final byte[] v = next.string();
      setParam(++i, stmt, paramType, isNull ? null : string(v), isNull);
    }
  }

  /**
   * Sets the parameter with the given index in a prepared statement.
   * @param index parameter index
   * @param stmt prepared statement
   * @param paramType parameter type
   * @param value parameter value
   * @param isNull indicator if the parameter is null or not
   * @throws QueryException query exception
   */
  private void setParam(final int index, final PreparedStatement stmt,
      final byte[] paramType, final String value, final boolean isNull) throws QueryException {
    try {
      if(eq(BOOL, paramType)) {
        if(isNull) stmt.setNull(index, Types.BOOLEAN);
        else stmt.setBoolean(index, Boolean.parseBoolean(value));
      } else if(eq(DATE, paramType)) {
        if(isNull) stmt.setNull(index, Types.DATE);
        else stmt.setDate(index, Date.valueOf(value));
      } else if(eq(DOUBLE, paramType)) {
        if(isNull) stmt.setNull(index, Types.DOUBLE);
        else stmt.setDouble(index, Double.parseDouble(value));
      } else if(eq(FLOAT, paramType)) {
        if(isNull) stmt.setNull(index, Types.FLOAT);
        else stmt.setFloat(index, Float.parseFloat(value));
      } else if(eq(INT, paramType)) {
        if(isNull) stmt.setNull(index, Types.INTEGER);
        else stmt.setInt(index, Integer.parseInt(value));
      } else if(eq(SHORT, paramType)) {
        if(isNull) stmt.setNull(index, Types.SMALLINT);
        else stmt.setShort(index, Short.parseShort(value));
      } else if(eq(STRING, paramType)) {
        if(isNull) stmt.setNull(index, Types.VARCHAR);
        else stmt.setString(index, value);
      } else if(eq(TIME, paramType)) {
        if(isNull) stmt.setNull(index, Types.TIME);
        else stmt.setTime(index, Time.valueOf(value));
      } else if(eq(TIMESTAMP, paramType)) {
        if(isNull) stmt.setNull(index, Types.TIMESTAMP);
        else stmt.setTimestamp(index, Timestamp.valueOf(value));
      } else {
        throw BXSQ_ERROR_X.get(info, "unsupported type: " + string(paramType));
      }
    } catch(final SQLException ex) {
      throw BXSQ_ERROR_X.get(info, ex);
    } catch(final IllegalArgumentException ex) {
      throw BXSQ_FORMAT_X.get(info, string(paramType));
    }
  }
}
