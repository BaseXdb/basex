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
import org.basex.query.util.Vars;
import org.basex.util.Array;

/**
 * Java XQuery API - Prepared Expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
      query.parse();
    } catch(final QueryException ex) {
      throw new XQQueryException(ex.getMessage(), new QName(ex.code()),
          ex.line(), ex.col(), -1);
    }
  }

  public void cancel() throws XQException {
    opened();
    query.ctx.stop();
  }

  public XQResultSequence executeQuery() throws XQException {
    query.reset();
    return execute();
  }

  public QName[] getAllExternalVariables() throws XQException {
    opened();
    final Var[] vars = getVariables();
    final QName[] names = new QName[vars.length];
    for(int v = 0; v < vars.length; v++) names[v] = vars[v].name.java();
    return names;
  }

  public QName[] getAllUnboundExternalVariables() throws XQException {
    opened();
    QName[] names = new QName[0];
    for(final Var v : getVariables()) {
      if(v.expr == null) names = Array.add(names, v.name.java());
    }
    return names;
  }

  public XQStaticContext getStaticContext() throws XQException {
    opened();
    return sc;
  }

  public XQSequenceType getStaticResultType() throws XQException {
    opened();
    return BXQItemType.DEFAULT;
  }

  public XQSequenceType getStaticVariableType(final QName qn)
      throws XQException {
    opened();
    valid(qn, String.class);
    final QNm nm = new QNm(qn);
    final Var var = query.ctx.vars.get(new Var(nm, true));
    if(var == null) throw new BXQException(VAR, nm);
    return var.type != null ? new BXQItemType(var.type.type) :
      BXQItemType.DEFAULT;
  }

  /**
   * Returns the variable array.
   * @return variables
   * @throws XQException exception
   */
  private Var[] getVariables() throws XQException {
    opened();
    final Vars vars = query.ctx.vars.getGlobal();
    return Arrays.copyOf(vars.vars, vars.size);
  }
}
