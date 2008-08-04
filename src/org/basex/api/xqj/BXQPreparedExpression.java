package org.basex.api.xqj;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQSequenceType;
import javax.xml.xquery.XQStaticContext;
import org.basex.BaseX;

/**
 * Java XQuery API - Prepared Expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public final class BXQPreparedExpression extends BXQDynamicContext
    implements XQPreparedExpression {

  /**
   * Constructor.
   * @param input query instance
   * @param sc static context
   * @param c closer
   */
  public BXQPreparedExpression(final String input, final BXQStaticContext sc,
      final BXQConnection c) {
    super(input, sc, c);
  }

  public void cancel() throws XQException {
    check();
  }

  public XQResultSequence executeQuery() throws XQException {
    return execute(ctx);
  }

  public QName[] getAllExternalVariables() throws XQException {
    check();
    BaseX.notimplemented();
    return null;
  }

  public QName[] getAllUnboundExternalVariables() throws XQException {
    check();
    BaseX.notimplemented();
    return null;
  }

  public XQStaticContext getStaticContext() throws XQException {
    check();
    return ctx;
  }

  public XQSequenceType getStaticResultType() throws XQException {
    check();
    BaseX.notimplemented();
    return null;
  }

  public XQSequenceType getStaticVariableType(final QName qn) throws XQException {
    check();
    BaseX.notimplemented();
    return null;
  }
}
