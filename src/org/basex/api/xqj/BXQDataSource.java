package org.basex.api.xqj;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Properties;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import org.basex.core.Context;

/**
 * BaseX  XQuery data source.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public final class BXQDataSource implements XQDataSource {
  /** BaseX context. */
  private final Context ctx;
  /** Log output (currently ignored). */
  private PrintWriter log;

  /**
   * Constructor.
   */
  public BXQDataSource() {
    ctx = new Context();
  }

  public BXQConnection getConnection() {
    return new BXQConnection(ctx);
  }

  public XQConnection getConnection(final Connection c) throws XQException {
    throw new BXQException("SQL sources not supported.");
  }

  public XQConnection getConnection(final String name, final String pw) {
    return getConnection();
  }

  public int getLoginTimeout() {
    return 0;
  }

  public PrintWriter getLogWriter() {
    return log;
  }

  public String getProperty(final String key) throws XQException {
    throw new BXQException("No property support.");
  }

  public String[] getSupportedPropertyNames() {
    return new String[] {};
  }

  public void setLoginTimeout(final int to) {
  }

  public void setLogWriter(final PrintWriter out) {
    log = out;
  }

  public void setProperties(final Properties prop) throws XQException {
    throw new BXQException("No property support.");
  }

  public void setProperty(final String key, final String val) throws XQException {
    throw new BXQException("No property support.");
  }
}
