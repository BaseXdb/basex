package org.basex.api.xqj;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Properties;

import org.basex.api.xqj.javax.XQConnection;
import org.basex.api.xqj.javax.XQDataSource;
import org.basex.api.xqj.javax.XQException;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.core.proc.Check;


public class BXQDataSource implements XQDataSource {
  
  Data data;

  public BXQConnection getConnection() throws XQException {
    // TODO Auto-generated method stub
    // read properties (database path, language, ...)
    Prop.read();
    return new BXQConnection();
  }

  public XQConnection getConnection(Connection arg0) throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQConnection getConnection(String arg0, String arg1)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public int getLoginTimeout() throws XQException {
    // TODO Auto-generated method stub
    return 0;
  }

  public PrintWriter getLogWriter() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public String getProperty(String arg0) throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public String[] getSupportedPropertyNames() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setLoginTimeout(int arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void setLogWriter(PrintWriter arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void setProperties(Properties arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void setProperty(String arg0, String arg1) throws XQException {
    // TODO Auto-generated method stub
    
  }
  
  public Data getData(String str) {
    return data = Check.check(str);  
  }
  
}
