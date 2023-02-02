package org.basex.query.func.sql;

import static org.basex.query.QueryError.*;

import java.sql.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Functions on relational databases.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Rositsa Shadura
 */
public final class SqlConnect extends SqlFn {
  /** Auto-commit mode. */
  private static final String AUTOCOMMIT = "autocommit";
  /** User. */
  private static final String USER = "user";
  /** Password. */
  private static final String PASS = "password";

  @Override
  public Uri item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final int el = exprs.length;
    final String url = toString(exprs[0], qc);
    final String username = el > 1 ? toStringOrNull(exprs[1], qc) : null;
    final String password = el > 2 ? toStringOrNull(exprs[2], qc) : null;
    final HashMap<String, String> options = toOptions(3, new Options(), qc).free();

    // parse options; overwrite with user and password (if supplied); treat autocommit independently
    final Properties props = new Properties();
    props.putAll(options);
    if(username != null) props.setProperty(USER, username);
    if(password != null) props.setProperty(PASS, password);
    final Object auto = props.remove(AUTOCOMMIT);

    // open connection and set auto-commit mode
    final Connection conn;
    try {
      conn = DriverManager.getConnection(url, props);
      if(auto != null) conn.setAutoCommit(Strings.toBoolean(auto.toString()));
    } catch(final SQLException ex) {
      throw SQL_ERROR_X.get(info, ex);
    }
    return jdbc(qc).add(conn, url);
  }
}
