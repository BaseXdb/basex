package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
import java.util.Arrays;
import javax.xml.namespace.QName;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQQueryException;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQSequenceType;
import javax.xml.xquery.XQStaticContext;
import org.basex.query.QueryException;
import org.basex.query.item.QNm;
import org.basex.query.util.Var;
import org.basex.query.util.VarList;
import org.basex.util.Array;
import org.basex.util.Token;

/**
 * Java XQuery API - Prepared Expression.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class BXQPreparedExpression extends BXQDynamicContext
    implements XQPreparedExpression {

  /**
   * Constructor.
   * @param input query instance
   * @param s static context
   * @param c closer
   * @throws XQQueryException exception
   */
  BXQPreparedExpression(final String input, final BXQStaticContext s,
      final BXQConnection c) throws XQQueryException {

    super(input, s, c);
    try {
      qp.parse();
    } catch(final QueryException ex) {
      final QNm qnm = ex.qname();
      throw new XQQueryException(ex.getMessage(),
        new QName(Token.string(qnm.uri()), Token.string(qnm.local())),
        ex.line(), ex.col(), -1);
    }
  }

  @Override
  public void cancel() throws XQException {
    opened();
    qp.ctx.stop();
  }

  @Override
  public XQResultSequence executeQuery() throws XQException {
    //qp.reset();
    return execute();
  }

  @Override
  public QName[] getAllExternalVariables() throws XQException {
    return getVariables(true);
  }

  @Override
  public QName[] getAllUnboundExternalVariables() throws XQException {
    return getVariables(false);
  }

  /**
   * Returns the names of all global variables.
   * @param all return all/unbound variables
   * @throws XQException query exception
   * @return variables
   */
  private QName[] getVariables(final boolean all) throws XQException {
    opened();
    QName[] names = { };
    final VarList vars = qp.ctx.vars.global();
    for(final Var v : Arrays.copyOf(vars.vars, vars.size)) {
      if(all || v.expr() == null) names = Array.add(names, v.name.toJava());
    }
    return names;
  }

  @Override
  public XQStaticContext getStaticContext() throws XQException {
    opened();
    return sc;
  }

  @Override
  public XQSequenceType getStaticResultType() throws XQException {
    opened();
    return BXQItemType.DEFAULT;
  }

  @Override
  public XQSequenceType getStaticVariableType(final QName qn)
      throws XQException {
    opened();
    valid(qn, String.class);
    final QNm nm = new QNm(qn);
    final Var var = qp.ctx.vars.get(nm);
    if(var == null) throw new BXQException(VAR, nm);
    return var.type != null ? new BXQItemType(var.type.type) :
      BXQItemType.DEFAULT;
  }
}
