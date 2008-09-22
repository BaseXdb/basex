package org.basex.api.xqj;

import java.io.InputStream;
import java.io.Reader;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQStaticContext;
import org.basex.core.CommandParser;
import org.basex.query.QueryException;
import org.basex.util.Token;

/**
 * Java XQuery API - Expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BXQExpression extends BXQDynamicContext 
    implements XQExpression {

  /**
   * Constructor.
   * @param sc static context
   * @param c closer
   */
  BXQExpression(final BXQStaticContext sc, final BXQConnection c) {
    super(null, sc, c);
  }

  public void cancel() throws XQException {
    opened();
  }

  public void executeCommand(final String cmd) throws XQException {
    opened();
    try {
      new CommandParser(cmd).parse();
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }

  public void executeCommand(final Reader cmd) throws XQException {
    executeCommand(Token.string(content(cmd)));
  }

  public XQResultSequence executeQuery(final String input) throws XQException {
    query.setQuery(input);
    return execute();
  }

  public XQResultSequence executeQuery(final Reader query) throws XQException {
    return executeQuery(Token.string(content(query)));
  }

  public XQResultSequence executeQuery(final InputStream query)
      throws XQException {
    return executeQuery(Token.string(content(query)));
  }

  public XQStaticContext getStaticContext() throws XQException {
    opened();
    return sc;
  }
}
