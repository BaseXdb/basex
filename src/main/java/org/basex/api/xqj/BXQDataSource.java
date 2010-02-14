package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Map;
import java.util.Properties;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;

/**
 * Java XQuery API - Data Source.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class BXQDataSource implements XQDataSource {
  /** Log output (currently ignored). */
  private PrintWriter log;
  /** Timeout. */
  private int timeout;

  public BXQConnection getConnection() throws XQException {
    return getConnection(null, null);
  }

  public BXQConnection getConnection(final Connection c) throws XQException {
    throw new BXQException(SQL);
  }

  public BXQConnection getConnection(final String name, final String pw)
      throws XQException {
    return new BXQConnection(name, pw);
  }

  public int getLoginTimeout() {
    return timeout;
  }

  public PrintWriter getLogWriter() {
    return log;
  }

  public String getProperty(final String key) throws XQException {
    throw new BXQException(PROPS);
  }

  public String[] getSupportedPropertyNames() {
    return new String[] {};
  }

  public void setLoginTimeout(final int to) {
    timeout = to;
  }

  public void setLogWriter(final PrintWriter out) {
    log = out;
  }

  public void setProperties(final Properties prop) throws XQException {
    if(prop == null) throw new BXQException(NULL,
        Properties.class.getSimpleName());
    for(final Map.Entry<?, ?> o : prop.entrySet()) {
      setProperty(o.getKey().toString(), o.getValue().toString());
    }
  }

  public void setProperty(final String key, final String val)
      throws XQException {
    throw new BXQException(PROPS, key);
  }
}
