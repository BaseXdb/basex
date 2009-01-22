package org.basex.query.util;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.ExprInfo;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
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
   * @throws QueryException xquery exception
   */
  public void comp(final QueryContext ctx) throws QueryException {
    if(expr != null) bind(expr.comp(ctx), ctx);
  }

  /**
   * Binds the specified expression to the variable.
   * @param e expression to be set
   * @param ctx query context
   * @return self reference
   * @throws QueryException evaluation exception
   */
  public Var bind(final Expr e, final QueryContext ctx) throws QueryException {
    expr = e;
    return e.i() ? bind((Item) e, ctx) : this;
  }

  /**
   * Binds the specified item to the variable.
   * @param it item to be set
   * @param ctx query context
   * @return self reference
   * @throws QueryException evaluation exception
   */
  public Var bind(final Item it, final QueryContext ctx) throws QueryException {
    expr = it;
    item = check(it, ctx);
    return this;
  }
  
  /**
   * Evaluates the variable.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  public Iter iter(final QueryContext ctx) throws QueryException {
    return item(ctx).iter();
  }
  
  /**
   * Evaluates the variable.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  public Item item(final QueryContext ctx) throws QueryException {
    if(item == null) {
      if(expr == null) Err.or(VAREMPTY, this);
      final Item it = ctx.item;
      ctx.item = null;
      item = check(ctx.iter(expr).finish(), ctx);
      ctx.item = it;
    }
    return item;
  }
  
  /**
   * Compares the variables for name equality.
   * @param v variable
   * @return result of check
   */
  public boolean eq(final Var v) {
    return v == this || v.name.eq(name);
  }
  
  /**
   * Checks if the variable is not shadowed by the variable.
   * @param v variable
   * @return result of check
   */
  public boolean visible(final Var v) {
    return v == null || !v.name.eq(name);
  }
  
  /**
   * Checks the variable type.
   * @param it input item
   * @param ctx query context
   * @return cast item
   * @throws QueryException query exception
   */
  public Item check(final Item it, final QueryContext ctx)
      throws QueryException {

    if(it.type == Type.STR) ((Str) it).direct = false;
    return type == null ? it : type.cast(it, ctx);
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
  public String color() {
    return "66CC66";
  }

  @Override
  public String toString() {
    final TokenBuilder sb = new TokenBuilder(DOLLAR);
    sb.add(name.str());
    if(type != null) sb.add(" as " + type);
    //if(item != null) sb.add(" = " + item);
    //else if(expr != null) sb.add(" = " + expr);
    return sb.toString();
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, NAM, name.str());
    if(expr != null) expr.plan(ser);
    ser.closeElement();
  }
}
