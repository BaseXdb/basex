package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
import javax.xml.namespace.QName;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQQueryException;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQSequenceType;
import javax.xml.xquery.XQStaticContext;
import org.basex.query.QueryException;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.util.Var;
import org.basex.query.xquery.util.Vars;
import org.basex.util.Array;

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
   * @throws XQException exception
   */
  public BXQPreparedExpression(final String input, final BXQStaticContext sc,
      final BXQConnection c) throws XQException {
    super(input, sc, c);
    
    try {
      query.create();
    } catch(final QueryException ex) {
      throw new XQQueryException(ex.getMessage());
    }
  }

  public void cancel() throws XQException {
    check();
    query.ctx.stop();
  }

  public XQResultSequence executeQuery() throws XQException {
    return execute();
  }

  public QName[] getAllExternalVariables() throws XQException {
    check();
    final Var[] vars = getVariables();
    final QName[] names = new QName[vars.length];
    for(int v = 0; v < vars.length; v++) names[v] = vars[v].name.java();
    return names;
  }

  public QName[] getAllUnboundExternalVariables() throws XQException {
    check();
    QName[] names = new QName[0];
    for(final Var v : getVariables()) {
      if(v.expr == null) names = Array.add(names, v.name.java());
    }
    return names;
  }

  private Var[] getVariables() throws XQException {
    check();
    final Vars vars = query.ctx.vars.getGlobal();
    return Array.finish(vars.vars, vars.size);
  }

  public XQStaticContext getStaticContext() throws XQException {
    check();
    return sc;
  }

  public XQSequenceType getStaticResultType() throws XQException {
    check();
    return BXQItemType.DEFAULT;
  }

  public XQSequenceType getStaticVariableType(final QName qn) throws XQException {
    check();
    check(qn, String.class);
    String name = qn.getLocalPart();
    final String pre = qn.getPrefix();
    if(pre.length() != 0) name = pre + ":" + name; 
    final QNm nm = new QNm(name, qn.getNamespaceURI());
    final Var var = query.ctx.vars.get(new Var(nm));
    if(var == null) throw new BXQException(VAR, nm);
    return var.type != null ? new BXQItemType(var.type.type) :
      BXQItemType.DEFAULT;
  }
}
