package org.basex.query.func.sql;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.sql.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Functions on relational databases.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 */
public final class SqlConnect extends SqlFn {
  /** Auto-commit mode. */
  private static final String AUTOCOMMIT = "autocommit";
  /** User. */
  private static final String USER = "user";
  /** Password. */
  private static final String PASS = "password";

  @SuppressWarnings("resource")
  @Override
  public Uri item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);
    // URL to relational database
    final String url = string(toToken(exprs[0], qc));
    final JDBCConnections jdbc = jdbc(qc);
    try {
      final Connection conn;
      if(exprs.length > 2) {
        // credentials
        final String user = string(toToken(exprs[1], qc));
        final String pass = string(toToken(exprs[2], qc));
        if(exprs.length == 4) {
          // parse connection options
          final HashMap<String, String> options = toOptions(3, new Options(), qc).free();

          // prepares connection properties
          final Properties props = new Properties();
          options.forEach((key, value) -> {
            if(!key.equals(AUTOCOMMIT)) props.setProperty(key, value);
          });
          props.setProperty(USER, user);
          props.setProperty(PASS, pass);

          // open connection and set auto-commit mode
          conn = DriverManager.getConnection(url, props);
          if(options.containsKey(AUTOCOMMIT)) {
            conn.setAutoCommit(Strings.toBoolean(options.get(AUTOCOMMIT)));
          }
        } else {
          conn = DriverManager.getConnection(url, user, pass);
        }
      } else {
        conn = DriverManager.getConnection(url);
      }
      return jdbc.add(conn, url);
    } catch(final SQLException ex) {
      throw SQL_ERROR_X.get(info, ex);
    }
  }
}
