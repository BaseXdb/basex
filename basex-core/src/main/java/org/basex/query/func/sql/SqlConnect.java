package org.basex.query.func.sql;

import static java.sql.DriverManager.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.sql.*;
import java.util.*;
import java.util.Map.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Functions on relational databases.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 */
public final class SqlConnect extends SqlFn {
  /** QName. */
  private static final QNm Q_OPTIONS = QNm.get(SQL_PREFIX, "options", SQL_URI);

  /** Auto-commit mode. */
  private static final String AUTO_COMM = "autocommit";
  /** User. */
  private static final String USER = "user";
  /** Password. */
  private static final String PASS = "password";

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);
    // URL to relational database
    final String url = string(toToken(exprs[0], qc));
    final JDBCConnections jdbc = jdbc(qc);
    try {
      if(exprs.length > 2) {
        // credentials
        final String user = string(toToken(exprs[1], qc));
        final String pass = string(toToken(exprs[2], qc));
        if(exprs.length == 4) {
          // connection options
          final Options opts = toOptions(3, Q_OPTIONS, new Options(), qc);
          // extract auto-commit mode from options
          boolean ac = true;
          final HashMap<String, String> options = opts.free();
          final String commit = options.get(AUTO_COMM);
          if(commit != null) {
            ac = Util.yes(commit);
            options.remove(AUTO_COMM);
          }
          // connection properties
          final Properties props = connProps(options);
          props.setProperty(USER, user);
          props.setProperty(PASS, pass);

          // open connection
          final Connection conn = getConnection(url, props);
          // set auto/commit mode
          conn.setAutoCommit(ac);
          return Int.get(jdbc.add(conn));
        }
        return Int.get(jdbc.add(getConnection(url, user, pass)));
      }
      return Int.get(jdbc.add(getConnection(url)));
    } catch(final SQLException ex) {
      throw BXSQ_ERROR_X.get(info, ex);
    }
  }

  /**
   * Parses connection options.
   * @param options options
   * @return connection properties
   */
  private static Properties connProps(final HashMap<String, String> options) {
    final Properties props = new Properties();
    for(final Entry<String, String> entry : options.entrySet()) {
      props.setProperty(entry.getKey(), entry.getValue());
    }
    return props;
  }
}
