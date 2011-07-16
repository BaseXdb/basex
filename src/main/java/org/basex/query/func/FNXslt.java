package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.lang.reflect.InvocationTargetException;

import org.basex.data.XMLSerializer;
import org.basex.io.ArrayOutput;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.Uri;
import org.basex.query.item.map.Map;
import org.basex.query.iter.AxisIter;
import org.basex.util.InputInfo;
import org.basex.util.TokenObjMap;
import org.basex.util.Util;
import org.basex.util.Xslt;

/**
 * Project specific functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNXslt extends FuncCall {
  /** Util namespace. */
  private static final Uri U_XSLT = Uri.uri(XSLTURI);
  /** Element: parameters. */
  private static final QNm E_PARAM = new QNm(token("parameters"), U_XSLT);

  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNXslt(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case TRANSFORM: return xslt(ctx);
      default: return super.item(ctx, ii);
    }
  }

  /**
   * Performs the xslt function.
   * @param ctx query context
   * @return item
   * @throws QueryException query exception
   */
  private ANode xslt(final QueryContext ctx) throws QueryException {
    try {
      final IO in = read(expr[0], ctx);
      final IO xsl = read(expr[1], ctx);
      final TokenObjMap<Object> map = xsltParams(2, E_PARAM, ctx);

      final byte[] result = new Xslt().transform(in, xsl, map);
      return new DBNode(new IOContent(result), ctx.context.prop);
    } catch(final Exception ex) {
      Util.debug(ex);
      // return cause of reflection error, or error itself
      throw NODOC.thrw(input, ex instanceof InvocationTargetException ?
          ex.getCause() : ex);
    }
  }

  /**
   * Creates serializer properties.
   * @param arg argument with parameters
   * @param root expected root element
   * @param ctx query context
   * @return serialization parameters
   * @throws QueryException query exception
   */
  private TokenObjMap<Object> xsltParams(final int arg, final QNm root,
      final QueryContext ctx) throws QueryException {

    // initialize token map
    final TokenObjMap<Object> tm = new TokenObjMap<Object>();
    // argument does not exist...
    if(arg >= expr.length) return tm;

    // empty sequence...
    final Item it = expr[arg].item(ctx, input);
    if(it == null) return tm;

    // XQuery map: convert to internal map
    if(it instanceof Map) return ((Map) it).tokenJavaMap(input);
    // no element: convert XQuery map to internal map
    if(!it.type().eq(SeqType.ELM)) throw NODFUNTYPE.thrw(input, this, it.type);

    // parse nodes
    ANode node = (ANode) it;
    if(!node.qname().eq(root)) PARWHICH.thrw(input, node.qname());

    // interpret query parameters
    final AxisIter ai = node.children();
    while((node = ai.next()) != null) {
      final QNm qn = node.qname();
      if(!qn.uri().eq(U_XSLT)) PARWHICH.thrw(input, qn);
      tm.add(qn.ln(), node.children().next());
    }
    return tm;
  }

  /**
   * Returns the input reference of the specified input.
   * @param e expression to be evaluated
   * @param ctx query context
   * @return item
   * @throws QueryException query exception
   * @throws Exception exception
   */
  private IO read(final Expr e, final QueryContext ctx) throws Exception {
    final Item in = checkEmpty(e.item(ctx, input));
    if(in.node()) {
      final ArrayOutput ao = new ArrayOutput();
      final XMLSerializer xml = new XMLSerializer(ao);
      in.serialize(xml);
      xml.close();
      return new IOContent(ao.toArray());
    }
    if(in.str()) return IO.get(string(in.atom(input)));
    throw STRNODTYPE.thrw(input, this, in.type);
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.CTX && def == Function.TRANSFORM || super.uses(u);
  }
}
