package org.basex.api.xqj;

import java.io.InputStream;
import java.io.Reader;

import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQStaticContext;

import org.basex.core.BaseXException;
import org.basex.core.Command;
import org.basex.core.CommandParser;
import org.basex.core.Context;
import org.basex.query.QueryException;
import org.basex.util.Token;

/**
 * Java XQuery API - Expression.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class BXQExpression extends BXQDynamicContext implements XQExpression {
  /**
   * Constructor.
   * @param s static context
   * @param c closer
   */
  BXQExpression(final BXQStaticContext s, final BXQConnection c) {
    super(null, s, c);
  }

  @Override
  public void cancel() throws XQException {
    opened();
    qp.ctx.stop();
  }

  @Override
  public void executeCommand(final String cmd) throws XQException {
    opened();
    try {
      final Context ctx = BXQDataSource.context();
      for(final Command c : new CommandParser(cmd, ctx).parse()) {
        // process output is suppressed, errors are returned as exception
        try {
          c.execute(ctx);
        } catch(final BaseXException ex) {
          throw new BXQException(ex.getMessage());
        }
      }
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }

  @Override
  public void executeCommand(final Reader cmd) throws XQException {
    executeCommand(Token.string(content(cmd)));
  }

  @Override
  public XQResultSequence executeQuery(final String input) throws XQException {
    qp.query(input);
    return execute();
  }

  @Override
  public XQResultSequence executeQuery(final Reader qu) throws XQException {
    return executeQuery(Token.string(content(qu)));
  }

  @Override
  public XQResultSequence executeQuery(final InputStream qu)
      throws XQException {
    return executeQuery(Token.string(content(qu)));
  }

  @Override
  public XQStaticContext getStaticContext() throws XQException {
    opened();
    return sc;
  }
}
