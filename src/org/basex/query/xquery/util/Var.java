package org.basex.query.xquery.util;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import org.basex.data.Serializer;
import org.basex.query.ExprInfo;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.SeqType;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.TokenBuilder;

/**
 * Variable.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Var extends ExprInfo implements Cloneable {
  /** Variable name. */
  public QNm name;
  /** Data type. */
  public SeqType type;
  /** Variable expressions. */
  public Expr expr;
  /** Variable results. */
  public Item item;

  /**
   * Constructor.
   * @param n variable name
   */
  public Var(final QNm n) {
    name = n;
  }

  /**
   * Constructor.
   * @param n variable name
   * @param t data type
   */
  public Var(final QNm n, final SeqType t) {
    name = n;
    type = t;
  }
  
  /**
   * Compiles the variable.
   * @param ctx xquery context
   * @throws XQException xquery exception
   */
  public void comp(final XQContext ctx) throws XQException {
    if(expr != null) expr = expr.comp(ctx);
  }

  /**
   * Sets the specified expression.
   * @param e expression to be set
   * @return self reference
   * @throws XQException evaluation exception
   */
  public Var expr(final Expr e) throws XQException {
    expr = e;
    return e.i() ? item((Item) e) : this;
  }

  /**
   * Sets the specified item.
   * @param it item to be set
   * @return self reference
   * @throws XQException evaluation exception
   */
  public Var item(final Item it) throws XQException {
    expr = it;
    item = it;
    check();
    return this;
  }
  
  /**
   * Evaluates the variable.
   * @param ctx query context
   * @return iterator
   * @throws XQException query exception
   */
  public Iter iter(final XQContext ctx) throws XQException {
    if(item == null) {
      if(expr == null) Err.or(VAREMPTY, this);
      
      final Item it = ctx.item;
      ctx.item = null;
      item = ctx.iter(expr).finish();
      ctx.item = it;
      check();
    }
    return item.iter();
  }
  
  /**
   * Checks the variable type.
   * @throws XQException query exception
   */
  public void check() throws XQException {
    if(type != null) {
      // [CG] check untyped/node types..
      if(!type.instance(item.iter(), true)) Err.cast(type.type, item);
    }
  }

  @Override
  public Var clone() {
    try {
      return (Var) super.clone();
    } catch(final CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public String toString() {
    final TokenBuilder sb = new TokenBuilder("$");
    sb.add(name.str());
    if(type != null) sb.add(" as " + type);
    if(item != null) sb.add(" = " + item);
    else if(expr != null) sb.add(" = " + expr);
    return sb.toString();
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this, NAM, name.str());
    expr.plan(ser);
    ser.closeElement(this);
  }
}
