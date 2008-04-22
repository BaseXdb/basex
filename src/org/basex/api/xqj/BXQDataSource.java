package org.basex.api.xqj;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Properties;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import org.basex.core.Context;
import org.basex.core.Prop;

/**
 * BaseX  XQuery data source.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXQDataSource implements XQDataSource {
  /** BaseX context. */
  private Context ctx;
  /** Log output (currently ignored). */
  private PrintWriter log;

  /**
   * Constructor.
   */
  public BXQDataSource() {
    // read properties (database path, language, ...)
    Prop.read();
    // create new context instance, containing the data reference
    // and initial node set
    ctx = new Context();
    
    /* example for creating a data reference..
    Proc proc = Proc.get(ctx, Commands.CHECK, input);
    // launch process..
    if(!proc.execute()) {
      // execution failed: throw exception
      throw new XQException(proc.info());
    }
    */
  }

  public BXQConnection getConnection() {
    return new BXQConnection(ctx);
  }

  public XQConnection getConnection(Connection c) throws XQException {
    throw new XQException("SQL sources not supported.");
  }

  public XQConnection getConnection(String name, String pw) {
    return getConnection();
  }

  public int getLoginTimeout() {
    return 0;
  }

  public PrintWriter getLogWriter() {
    return log;
  }

  public String getProperty(String key) throws XQException {
    throw new XQException("No property support.");
  }

  public String[] getSupportedPropertyNames() {
    return new String[] {};
  }

  public void setLoginTimeout(int to) {
  }

  public void setLogWriter(PrintWriter out) {
    log = out;
    
  }

  public void setProperties(Properties prop) throws XQException {
    throw new XQException("No property support.");
  }

  public void setProperty(String key, String val) throws XQException {
    throw new XQException("No property support.");
  }
}
