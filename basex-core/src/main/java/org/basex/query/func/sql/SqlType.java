package org.basex.query.func.sql;

import java.math.*;
import java.sql.*;

/**
 * Supported SQL types.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
enum SqlType {
  /** Big decimal. */
  BIGDECIMAL(Types.NUMERIC) {
    @Override
    void set(final int i, final String v, final PreparedStatement ps) throws SQLException {
      ps.setBigDecimal(i, new BigDecimal(v));
    }
  },
  /** Boolean. */
  BOOLEAN(Types.BIT) {
    @Override
    void set(final int i, final String v, final PreparedStatement ps) throws SQLException {
      ps.setBoolean(i, Boolean.parseBoolean(v));
    }
  },
  /** Byte. */
  BYTE(Types.TINYINT) {
    @Override
    void set(final int i, final String v, final PreparedStatement ps) throws SQLException {
      ps.setByte(i, Byte.parseByte(v));
    }
  },
  /** Date. */
  DATE(Types.DATE) {
    @Override
    void set(final int i, final String v, final PreparedStatement ps) throws SQLException {
      ps.setDate(i, Date.valueOf(v));
    }
  },
  /** Double. */
  DOUBLE(Types.DOUBLE) {
    @Override
    void set(final int i, final String v, final PreparedStatement ps) throws SQLException {
      ps.setDouble(i, Double.parseDouble(v));
    }
  },
  /** Float. */
  FLOAT(Types.REAL) {
    @Override
    void set(final int i, final String v, final PreparedStatement ps) throws SQLException {
      ps.setFloat(i, Float.parseFloat(v));
    }
  },
  /** Int. */
  INT(Types.INTEGER) {
    @Override
    void set(final int i, final String v, final PreparedStatement ps) throws SQLException {
      ps.setInt(i, Integer.parseInt(v));
    }
  },
  /** Long. */
  LONG(Types.BIGINT) {
    @Override
    void set(final int i, final String v, final PreparedStatement ps) throws SQLException {
      ps.setLong(i, Long.parseLong(v));
    }
  },
  /** Short. */
  SHORT(Types.SMALLINT) {
    @Override
    void set(final int i, final String v, final PreparedStatement ps) throws SQLException {
      ps.setShort(i, Short.parseShort(v));
    }
  },
  /** SQL XML. */
  SQLXML(Types.SQLXML) {
    @Override
    void set(final int i, final String v, final PreparedStatement ps) throws SQLException {
      final SQLXML xml = ps.getConnection().createSQLXML();
      xml.setString(v);
      ps.setSQLXML(i, xml);
    }
  },
  /** String. */
  STRING(Types.VARCHAR) {
    @Override
    void set(final int i, final String v, final PreparedStatement ps) throws SQLException {
      ps.setString(i, v);
    }
  },
  /** Time. */
  TIME(Types.TIME) {
    @Override
    void set(final int i, final String v, final PreparedStatement ps) throws SQLException {
      ps.setTime(i, Time.valueOf(v));
    }
  },
  /** Timestamp. */
  TIMESTAMP(Types.TIMESTAMP) {
    @Override
    void set(final int i, final String v, final PreparedStatement ps) throws SQLException {
      ps.setTimestamp(i, Timestamp.valueOf(v));
    }
  };

  /** Numeric type. */
  private final int type;

  /**
   * Constructor.
   * @param type numeric type
   */
  SqlType(final int type) {
    this.type = type;
  }

  /**
   * Sets a parameter to {@code NULL}.
   * @param index index of parameter (starting with {@code 1})
   * @param ps prepared statement
   * @throws SQLException SQL exception
   */
  final void setNull(final int index, final PreparedStatement ps) throws SQLException {
    ps.setNull(index, type);
  }

  /**
   * Assigns a value.
   * @param i index of parameter (starting with {@code 1})
   * @param v value to assign
   * @param ps prepared statement
   * @throws SQLException SQL exception
   */
  abstract void set(int i, String v, PreparedStatement ps) throws SQLException;
}
