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
import org.basex.util.*;

/**
 * Functions on relational databases.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Rositsa Shadura
 */
public final class SqlExecutePrepared extends SqlExecute {
  /** QName. */
  private static final QNm Q_PARAMETERS = new QNm(SQL_PREFIX, "parameters", SQL_URI);
  /** QName. */
  private static final QNm Q_PARAMETER = new QNm(SQL_PREFIX, "parameter", SQL_URI);

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
  /** Type xml. */
  private static final byte[] SQLXML = token("sqlxml");

  /** Attribute "type" of <sql:parameter/>. */
  private static final byte[] TYPE = token("type");
  /** Attribute "null" of <sql:parameter/>. */
  private static final byte[] NULL = token("null");

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    checkCreate(qc);

    final PreparedStatement stmt = prepared(qc);
    long n = 0;
    ANode params = null;
    if(exprs.length > 1) {
      params = toElem(exprs[1], qc);
      if(!params.qname().eq(Q_PARAMETERS)) throw INVALIDOPTION_X.get(info, params.qname().local());
      n = countParams(params);
    }
    final StatementOptions options = toOptions(2, new StatementOptions(), qc);

    try {
      stmt.setQueryTimeout(options.get(StatementOptions.TIMEOUT));
      try {
        // Check if number of parameters equals number of place holders
        final int ph = stmt.getParameterMetaData().getParameterCount();
        if(n != ph) throw SQL_PARAMETERS_X_X.get(info, n, ph);
      } catch(final SQLSyntaxErrorException ex) {
        // Oracle will fail receiving ParameterMetaData on update statement ()
        // stackoverflow.com/questions/30666622/apache-dbutils-changing-column-name-in-update-sql
        // disable all oracle exceptions (found beside ORA-00904 ORA-00936 also)
        if(!ex.getMessage().startsWith("ORA-")) throw ex;
      }
      if(params != null) setParameters(params.children(), stmt);
      // If execute returns false, statement was updating: return number of updated rows
      return iter(stmt, false, stmt.execute());
    } catch(final SQLException ex) {
      throw SQL_ERROR_X.get(info, ex);
    }
  }

  /**
   * Counts the numbers of <sql:parameter/> elements.
   * @param params element <sql:parameter/>
   * @return number of parameters
   */
  private static long countParams(final ANode params) {
    final BasicNodeIter iter = params.children();
    long size = iter.size();
    if(size == -1) do ++size;
    while(iter.next() != null);
    return size;
  }

  /**
   * Sets the parameters of a prepared statement.
   * @param params parameters
   * @param stmt prepared statement
   * @throws QueryException query exception
   */
  private void setParameters(final BasicNodeIter params, final PreparedStatement stmt)
      throws QueryException {

    int i = 0;
    for(ANode next; (next = params.next()) != null;) {
      // Check name
      if(!next.qname().eq(Q_PARAMETER)) throw INVALIDOPTION_X.get(info, next.qname().local());
      final BasicNodeIter attrs = next.attributes();
      byte[] paramType = null;
      boolean isNull = false;
      for(ANode attr; (attr = attrs.next()) != null;) {
        // attribute "type"
        if(eq(attr.name(), TYPE)) paramType = attr.string();
        // attribute "null"
        else if(eq(attr.name(), NULL)) isNull = attr.string() != null && Bln.parse(attr, info);
        // attribute not expected
        else throw SQL_ATTRIBUTE_X.get(info, attr.name());
      }
      if(paramType == null) throw SQL_PARAMETERS.get(info);
      final byte[] v = next.string();
      setParam(++i, stmt, paramType, isNull ? null : string(v), isNull);
    }
  }

  /**
   * Sets the parameter with the given index in a prepared statement.
   * @param index parameter index
   * @param stmt prepared statement
   * @param type parameter type
   * @param value parameter value
   * @param isNull indicator if the parameter is null or not
   * @throws QueryException query exception
   */
  private void setParam(final int index, final PreparedStatement stmt, final byte[] type,
      final String value, final boolean isNull) throws QueryException {
    try {
      if(eq(BOOL, type)) {
        if(isNull) stmt.setNull(index, Types.BOOLEAN);
        else stmt.setBoolean(index, Boolean.parseBoolean(value));
      } else if(eq(DATE, type)) {
        if(isNull) stmt.setNull(index, Types.DATE);
        else stmt.setDate(index, Date.valueOf(value));
      } else if(eq(DOUBLE, type)) {
        if(isNull) stmt.setNull(index, Types.DOUBLE);
        else stmt.setDouble(index, Double.parseDouble(value));
      } else if(eq(FLOAT, type)) {
        if(isNull) stmt.setNull(index, Types.FLOAT);
        else stmt.setFloat(index, Float.parseFloat(value));
      } else if(eq(INT, type)) {
        if(isNull) stmt.setNull(index, Types.INTEGER);
        else stmt.setInt(index, Integer.parseInt(value));
      } else if(eq(SHORT, type)) {
        if(isNull) stmt.setNull(index, Types.SMALLINT);
        else stmt.setShort(index, Short.parseShort(value));
      } else if(eq(STRING, type)) {
        if(isNull) stmt.setNull(index, Types.VARCHAR);
        else stmt.setString(index, value);
      } else if(eq(TIME, type)) {
        if(isNull) stmt.setNull(index, Types.TIME);
        else stmt.setTime(index, Time.valueOf(value));
      } else if(eq(TIMESTAMP, type)) {
        if(isNull) stmt.setNull(index, Types.TIMESTAMP);
        else stmt.setTimestamp(index, Timestamp.valueOf(value));
      } else if(eq(SQLXML, type)) {
        if(isNull) stmt.setNull(index, Types.SQLXML);
        else {
          final SQLXML xml = stmt.getConnection().createSQLXML();
          xml.setString(value);
          stmt.setSQLXML(index, xml);
        }
      } else {
        throw SQL_ERROR_X.get(info, "unsupported type: " + string(type));
      }
    } catch(final SQLException ex) {
      throw SQL_ERROR_X.get(info, ex);
    } catch(final IllegalArgumentException ex) {
      Util.debug(ex);
      throw SQL_TYPE_X_X.get(info, type, value);
    }
  }
}
