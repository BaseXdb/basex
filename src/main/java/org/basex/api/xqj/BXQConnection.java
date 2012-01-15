package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class BXQConnection extends BXQDataFactory implements XQConnection {
  /** Database meta data. */
  private final BXQMetaData meta = new BXQMetaData(this);

  /**
   * Default constructor.
   * @param name user name
   * @param pw password
   * @throws XQException if authentication fails
   */
  BXQConnection(final String name, final String pw) throws XQException {
    super(name, pw);
  }

  @Override
  public void commit() throws XQException {
    opened();
    throw new BXQException(TRANS);
  }

  @Override
  public XQExpression createExpression() throws XQException {
    return createExpression(context);
  }

  @Override
  public XQExpression createExpression(final XQStaticContext sc)
      throws XQException {
    opened();
    valid(sc, XQStaticContext.class);
    return new BXQExpression((BXQStaticContext) sc, this);
  }

  @Override
  public boolean getAutoCommit() {
    return true;
  }

  @Override
  public XQMetaData getMetaData() throws XQException {
    opened();
    return meta;
  }

  @Override
  public XQStaticContext getStaticContext() throws XQException {
    opened();
    return context;
  }

  @Override
  public XQPreparedExpression prepareExpression(final InputStream is,
      final XQStaticContext sc) throws XQException {
    return prepareExpression(Token.string(content(is)), sc);
  }

  @Override
  public XQPreparedExpression prepareExpression(final InputStream is)
      throws XQException {
    return prepareExpression(is, context);
  }

  @Override
  public XQPreparedExpression prepareExpression(final Reader r,
      final XQStaticContext sc) throws XQException {
    return prepareExpression(Token.string(content(r)), sc);
  }

  @Override
  public XQPreparedExpression prepareExpression(final Reader r)
      throws XQException {
    return prepareExpression(r, context);
  }

  @Override
  public XQPreparedExpression prepareExpression(final String query,
      final XQStaticContext sc) throws XQException {
    opened();
    valid(sc, XQStaticContext.class);
    valid(query, String.class);
    return new BXQPreparedExpression(query, (BXQStaticContext) sc, this);
  }

  @Override
  public XQPreparedExpression prepareExpression(final String query)
      throws XQException {
    return prepareExpression(query, context);
  }

  @Override
  public void rollback() throws XQException {
    opened();
    throw new BXQException(TRANS);
  }

  @Override
  public void setAutoCommit(final boolean ac) throws XQException {
    opened();
    if(!ac) throw new BXQException(TRANS);
  }

  @Override
  public void setStaticContext(final XQStaticContext sc) throws XQException {
    opened();
    valid(sc, XQStaticContext.class);
    context = (BXQStaticContext) sc;
  }
}
