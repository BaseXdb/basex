package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Properties;
import javax.xml.xquery.XQConnection;
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

  public BXQConnection getConnection() {
    return new BXQConnection();
  }

  public XQConnection getConnection(final Connection c) throws XQException {
    throw new BXQException(SQL);
  }

  public XQConnection getConnection(final String name, final String pw) {
    return getConnection();
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
    throw new BXQException(PROPS);
  }

  public void setProperty(final String key, final String val)
      throws XQException {
    throw new BXQException(PROPS);
  }
}
