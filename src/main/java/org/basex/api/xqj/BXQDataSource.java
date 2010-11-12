package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Map;
import java.util.Properties;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;

import org.basex.core.Text;
import org.basex.util.Util;

/**
 * Java XQuery API - Data Source.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class BXQDataSource implements XQDataSource {
  /** User key. */
  private static final String USER = "user";
  /** Password key. */
  private static final String PASSWORD = "password";
  /** Log output (currently ignored). */
  private PrintWriter log;
  /** User. */
  private String user = Text.ADMIN;
  /** Password. */
  private String password = Text.ADMIN;
  /** Timeout. */
  private int timeout;

  @Override
  public BXQConnection getConnection() throws XQException {
    return getConnection(user, password);
  }

  @Override
  public BXQConnection getConnection(final Connection c) throws XQException {
    throw new BXQException(SQL);
  }

  @Override
  public BXQConnection getConnection(final String name, final String pw)
      throws XQException {
    return new BXQConnection(name, pw);
  }

  @Override
  public int getLoginTimeout() {
    return timeout;
  }

  @Override
  public PrintWriter getLogWriter() {
    return log;
  }

  @Override
  public String getProperty(final String key) throws XQException {
    if(USER.equals(key)) return user;
    if(PASSWORD.equals(key)) return password;
    throw new BXQException(PROPS);
  }

  @Override
  public String[] getSupportedPropertyNames() {
    return new String[] { USER, PASSWORD };
  }

  @Override
  public void setLoginTimeout(final int to) {
    timeout = to;
  }

  @Override
  public void setLogWriter(final PrintWriter out) {
    log = out;
  }

  @Override
  public void setProperties(final Properties prop) throws XQException {
    if(prop == null) throw new BXQException(NULL, Util.name(Properties.class));
    for(final Map.Entry<?, ?> o : prop.entrySet()) {
      setProperty(o.getKey().toString(), o.getValue().toString());
    }
  }

  @Override
  public void setProperty(final String key, final String val)
      throws XQException {
    if(USER.equals(key)) user = val;
    else if(PASSWORD.equals(key)) password = val;
    else throw new BXQException(PROPS, key);
  }
}
