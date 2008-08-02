package org.basex.api.xqj;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQMetaData;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQStaticContext;
import org.basex.core.Context;
import org.basex.query.xquery.XQueryProcessor;

/**
 * BaseX XQuery connection.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public final class BXQConnection extends BXQDataFactory
    implements XQConnection {

  /** BaseX context. */
  final Context ctx;
  /** Boolean Value if connection is closed. */
  private boolean closed;
  /** Boolean Value if autoCommit is enabled. Default disabled */
  private boolean autoCommit;

  /**
   * Constructor.
   * @param c context instance
   */
  public BXQConnection(final Context c) {
    ctx = c;
  }

  public void close() {
    closed = true;
  }

  public void commit() {
  }

  public XQExpression createExpression() {
    return new BXQExpression(this);
  }

  public XQExpression createExpression(final XQStaticContext arg0) {
    return new BXQExpression(this);
  }

  public boolean getAutoCommit() {
    return autoCommit;
  }

  public XQMetaData getMetaData() {
    return null;
  }

  public XQStaticContext getStaticContext() {
    return null;
  }

  public boolean isClosed() {
    return closed;
  }

  public XQPreparedExpression prepareExpression(final InputStream is,
      final XQStaticContext arg1) throws XQException {
    try {
      return prepareExpression(read(is));
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
  }

  public XQPreparedExpression prepareExpression(final InputStream is)
      throws XQException {
    return prepareExpression(is, null);
  }

  public XQPreparedExpression prepareExpression(final Reader r,
      final XQStaticContext sc) throws XQException {
    try {
      return prepareExpression(read(r));
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
  }

  public XQPreparedExpression prepareExpression(final Reader r)
      throws XQException {
    return prepareExpression(r, null);
  }

  public XQPreparedExpression prepareExpression(final String query,
      final XQStaticContext sc) {
    final XQueryProcessor xquery = new XQueryProcessor(query);
    return new BXQPreparedExpression(xquery, ctx.current());
  }

  public XQPreparedExpression prepareExpression(final String query) {
    return prepareExpression(query, null);
  }

  public void rollback() throws XQException {
    if(closed) throw new BXQException("Connection has been closed");
  }

  public void setAutoCommit(final boolean arg0) {
    autoCommit = arg0;
  }

  public void setStaticContext(final XQStaticContext arg0) {
  }
  
  /**
   * Return the contents of the specified reader as a string.
   * @param r reader 
   * @return string
   * @throws IOException exception
   */
  public static final String read(final Reader r) throws IOException {
    final StringBuilder sb = new StringBuilder();
    final BufferedReader br = new BufferedReader(r);
    int i = 0;
    while((i = br.read()) != -1) sb.append((char) i);
    br.close();
    return sb.toString();
    
  }
  
  /**
   * Return the contents of the specified input stream as a string.
   * @param is input stream
   * @return string
   * @throws IOException exception
   */
  public static final String read(final InputStream is) throws IOException {
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    final BufferedInputStream bis = new BufferedInputStream(is);
    int i = 0;
    while((i = bis.read()) != -1) bos.write(i);
    bis.close();
    return bos.toString();
  }
}
