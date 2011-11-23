package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.io.serial.SerializerException;
import org.basex.io.serial.SerializerProp;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Arr;
import org.basex.query.expr.Expr;
import org.basex.query.item.ANode;
import org.basex.query.item.Atm;
import org.basex.query.item.Item;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.query.item.map.Map;
import org.basex.query.iter.AxisIter;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.hash.TokenObjMap;

/**
 * Function call for built-in functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class FuncCall extends Arr {
  /** Output namespace. */
  private static final Uri U_OUTPUT = Uri.uri(OUTPUTURI);
  /** Element: output:serialization-parameter. */
  private static final QNm E_PARAM =
    new QNm(token("serialization-parameters"), U_OUTPUT);
  /** Attribute: value. */
  private static final QNm A_VALUE = new QNm(token("value"));

  /** Function definition. */
  public Function def;

  /**
   * Constructor.
   * @param ii input info
   * @param fd function definition
   * @param args arguments
   */
  protected FuncCall(final InputInfo ii, final Function fd,
      final Expr... args) {
    super(ii, args);
    def = fd;
    type = def.ret;
  }

  @Override
  public final Expr comp(final QueryContext ctx) throws QueryException {
    // compile all arguments
    super.comp(ctx);
    // skip context-based or non-deterministic functions, and non-values
    if(uses(Use.CTX) || uses(Use.NDT) || !allAreValues())
      return optPre(cmp(ctx), ctx);
    // pre-evaluate function
    return optPre(def.ret.zeroOrOne() ? item(ctx, input) : value(ctx), ctx);
  }

  /**
   * Performs function specific compilations.
   * @param ctx query context
   * @return evaluated item
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Expr cmp(final QueryContext ctx) throws QueryException {
    return this;
  }

  /**
   * Atomizes the specified item.
   * @param it input item
   * @return atomized item
   * @throws QueryException query exception
   */
  protected final Item atom(final Item it) throws QueryException {
    final Type ip = it.type;
    return ip.isNode() ? ip == NodeType.PI || ip == NodeType.COM ?
        Str.get(it.string(input)) : new Atm(it.string(input)) : it;
  }

  @Override
  public final boolean isFunction(final Function f) {
    return def == f;
  }

  @Override
  public final String desc() {
    return def.toString();
  }

  @Override
  public final void plan(final Serializer ser) throws IOException {
    ser.openElement(this, NAM, Token.token(def.desc));
    for(final Expr arg : expr) arg.plan(ser);
    ser.closeElement();
  }

  @Override
  public final String toString() {
    final String desc = def.toString();
    return new TokenBuilder().add(desc.substring(0,
        desc.indexOf('(') + 1)).addSep(expr, SEP).add(PAR2).toString();
  }

  /**
   * Creates serializer properties.
   * @param fun calling function
   * @param arg argument with parameters
   * @param ctx query context
   * @return serialization parameters
   * @throws SerializerException serializer exception
   * @throws QueryException query exception
   */
  static SerializerProp serialPar(final FuncCall fun, final int arg,
      final QueryContext ctx) throws SerializerException, QueryException {

    // check if enough arguments are available
    TokenObjMap<Object> tm = new TokenObjMap<Object>();
    if(arg < fun.expr.length) {
      // retrieve parameters
      final Item it = fun.expr[arg].item(ctx, fun.input);
      if(it != null) {
        if(it instanceof Map) {
          tm = ((Map) it).tokenJavaMap(fun.input);
        } else {
          // check root node
          ANode n = (ANode) fun.checkType(it, NodeType.ELM);
          if(!n.qname().eq(E_PARAM)) SERUNKNOWN.thrw(fun.input, n.qname());
          // interpret query parameters
          final AxisIter ai = n.children();
          while((n = ai.next()) != null) {
            final QNm qn = n.qname();
            if(!qn.uri().eq(U_OUTPUT)) SERUNKNOWN.thrw(fun.input, qn);
            final byte[] val = n.attribute(A_VALUE);
            if(val == null) SERNOVAL.thrw(fun.input);
            tm.add(qn.ln(), val);
          }
        }
      }
    }

    // use default parameters if no parameters have been assigned
    final TokenBuilder tb = new TokenBuilder();
    for(final byte[] key : tm) {
      if(tb.size() != 0) tb.add(',');
      tb.add(key).add('=').addExt(tm.get(key));
    }
    return tb.size() == 0 ? ctx.serProp(true) :
      new SerializerProp(tb.toString());
  }
}
