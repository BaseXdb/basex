package org.basex.api.xqj;

import java.io.InputStream;
import java.io.Reader;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQMetaData;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQStaticContext;
import org.basex.util.Token;

/**
 * Java XQuery API - Connection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class BXQConnection extends BXQDataFactory
    implements XQConnection {

  /** Database meta data. */
  private final BXQMetaData meta = new BXQMetaData(this);
  /** Boolean Value if autoCommit is enabled. */
  private boolean autoCommit = true;

  public void commit() throws XQException {
    opened();
  }

  public XQExpression createExpression() throws XQException {
    return createExpression(ctx);
  }

  public XQExpression createExpression(final XQStaticContext sc)
      throws XQException {
    opened();
    valid(sc, XQStaticContext.class);
    final BXQStaticContext bsc = (BXQStaticContext) sc;
    return new BXQExpression(bsc, this);
  }

  public boolean getAutoCommit() {
    return autoCommit;
  }

  public XQMetaData getMetaData() throws XQException {
    opened();
    return meta;
  }

  public XQStaticContext getStaticContext() throws XQException {
    opened();
    return ctx;
  }

  public XQPreparedExpression prepareExpression(final InputStream is,
      final XQStaticContext sc) throws XQException {
    return prepareExpression(Token.string(content(is)), sc);
  }

  public XQPreparedExpression prepareExpression(final InputStream is)
      throws XQException {
    return prepareExpression(is, ctx);
  }

  public XQPreparedExpression prepareExpression(final Reader r,
      final XQStaticContext sc) throws XQException {
    return prepareExpression(Token.string(content(r)), sc);
  }

  public XQPreparedExpression prepareExpression(final Reader r)
      throws XQException {
    return prepareExpression(r, ctx);
  }

  public XQPreparedExpression prepareExpression(final String query,
      final XQStaticContext sc) throws XQException {
    opened();
    valid(sc, XQStaticContext.class);
    valid(query, String.class);
    final BXQStaticContext bsc = (BXQStaticContext) sc;
    return new BXQPreparedExpression(query, bsc, this);
  }

  public XQPreparedExpression prepareExpression(final String query)
      throws XQException {
    return prepareExpression(query, ctx);
  }

  public void rollback() throws XQException {
    opened();
  }

  public void setAutoCommit(final boolean ac) throws XQException {
    opened();
    autoCommit = ac;
  }

  public void setStaticContext(final XQStaticContext sc) throws XQException {
    opened();
    valid(sc, XQStaticContext.class);
    ctx = (BXQStaticContext) sc;
  }
}
