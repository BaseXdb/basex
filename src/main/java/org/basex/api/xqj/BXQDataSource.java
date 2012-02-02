package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Map;
import java.util.Properties;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;

import org.basex.core.Context;
import org.basex.core.MainProp;
import org.basex.core.Text;
import org.basex.util.Util;

/**
 * Java XQuery API - Data Source.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BXQDataSource implements XQDataSource {
  /** Static database context. */
  private static Context context;

  /** Log output (currently ignored). */
  private PrintWriter log;
  /** User. */
  private final Properties props = new Properties();
  /** Timeout. */
  private int timeout;

  /**
   * Default constructor.
   */
  public BXQDataSource() {
    props.setProperty(USER, Text.ADMIN);
    props.setProperty(PASSWORD, Text.ADMIN);
    props.setProperty(SERVERNAME, MainProp.HOST[1].toString());
    props.setProperty(PORT, MainProp.PORT[1].toString());
  }

  /**
   * Returns the static database context reference.
   * @return database context
   */
  static Context context() {
    if(context == null) context = new Context();
    return context;
  }

  @Override
  public BXQConnection getConnection() throws XQException {
    return getConnection(props.getProperty(USER), props.getProperty(PASSWORD));
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
    final String val = key != null ? props.getProperty(key) : null;
    if(val == null) throw new BXQException(PROPS);
    return val;
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

    getProperty(key);
    props.setProperty(key, val);
  }
}
