package org.basex.query.func;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Bln;
import org.basex.query.item.FNode;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
import org.basex.util.InputInfo;
import org.basex.util.TokenList;

/**
 * ID functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class FNId extends Fun {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNId(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    // functions have 1 or 2 arguments...
    final Item it = checkEmptyType((expr.length == 2 ? expr[1] :
      checkCtx(ctx)).item(ctx, input), Type.NOD);

    final Nod node = checkNode(it);
    switch(def) {
      case ID:    return id(ctx.iter(expr[0]), node);
      case IDREF: return idref(ctx.iter(expr[0]), node);
      case ELID:  return elid(ctx.iter(expr[0]), node);
      default:    return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    // functions have 1 or 2 arguments...
    final Item it = checkEmptyType((expr.length == 2 ? expr[1] :
      checkCtx(ctx)).item(ctx, input), Type.NOD);

    switch(def) {
      case LANG:  return lang(lc(checkEStr(expr[0], ctx)), checkNode(it));
      default:    return super.item(ctx, ii);
    }
  }

  /**
   * Returns the parent result of the ID function.
   * @param it item ids to be found
   * @param node attribute
   * @return resulting node list
   * @throws QueryException query exception
   */
  private Iter elid(final Iter it, final Nod node) throws QueryException {
    final NodIter nb = id(it, node);
    final NodIter par = new NodIter().random();
    Nod n;
    while((n = nb.next()) != null) par.add(n.parent());
    return par;
  }

  /**
   * Returns the result of the ID function.
   * @param it item ids to be found
   * @param node attribute
   * @return resulting node list
   * @throws QueryException query exception
   */
  private NodIter id(final Iter it, final Nod node) throws QueryException {
    final NodIter nb = new NodIter().random();
    add(ids(it), nb, checkRoot(node));
    return nb;
  }

  /**
   * Returns the result of the IDREF function.
   * @param it item ids to be found
   * @param node attribute
   * @return resulting node list
   * @throws QueryException query exception
   */
  private Iter idref(final Iter it, final Nod node) throws QueryException {
    final NodIter nb = new NodIter().random();
    addRef(ids(it), nb, checkRoot(node));
    return nb;
  }

  /**
   * Returns the result of the language function.
   * @param lang language to be found
   * @param node attribute
   * @return resulting node list
   * @throws QueryException query exception
   */
  private Bln lang(final byte[] lang, final Nod node) throws QueryException {
    for(Nod n = node; n != null; n = n.parent()) {
      final NodeIter atts = n.attr();
      Nod at;
      while((at = atts.next()) != null) {
        if(eq(at.qname().atom(), LANG)) {
          final byte[] ln = lc(norm(checkEStr(at)));
          return Bln.get(startsWith(ln, lang)
              && (lang.length == ln.length || ln[lang.length] == '-'));
        }
      }
    }
    return Bln.FALSE;
  }

  /**
   * Extracts the ids from the specified item.
   * @param iter iterator
   * @return ids
   * @throws QueryException query exception
   */
  private byte[][] ids(final Iter iter) throws QueryException {
    final TokenList tl = new TokenList();
    Item id;
    while((id = iter.next()) != null) {
      for(final byte[] i : split(norm(checkEStr(id)), ' ')) tl.add(i);
    }
    return tl.toArray();
  }

  /**
   * Adds nodes with the specified id.
   * @param ids ids to be found
   * @param nb node builder
   * @param nod node
   * @throws QueryException query exception
   */
  private void add(final byte[][] ids, final NodIter nb,
      final Nod nod) throws QueryException {

    final NodeIter ni = nod.attr();
    Nod att;
    while((att = ni.next()) != null) {
      // [CG] XQuery: ID-IDREF Parsing
      for(final byte[] id : ids) {
        if(!eq(checkEStr(att), id)) continue;
        final byte[] nm = lc(att.qname().atom());
        if(contains(nm, ID) && !contains(nm, IDREF)) nb.add(nod);
      }
    }
    final NodeIter ch = nod.child();
    while((att = ch.next()) != null) add(ids, nb, att.finish());
  }

  /**
   * Adds nodes with the specified id.
   * @param ids ids to be found
   * @param nb node builder
   * @param nod node
   * @throws QueryException query exception
   */
  private void addRef(final byte[][] ids, final NodIter nb,
      final Nod nod) throws QueryException {

    final NodeIter ni = nod.attr();
    Nod att;
    while((att = ni.next()) != null) {
      // [CG] XQuery: ID-IDREF Parsing
      for(final byte[] id : ids) {
        if(!eq(checkEStr(att), id)) continue;
        final byte[] nm = lc(att.qname().atom());
        if(contains(nm, IDREF)) nb.add(att.finish());
      }
    }
    final NodeIter ch = nod.child();
    while((att = ch.next()) != null) addRef(ids, nb, att.finish());
  }

  /**
   * Checks if the specified node has a document node as root.
   * @param nod input node
   * @return specified node
   * @throws QueryException query exception
   */
  private Nod checkRoot(final Nod nod) throws QueryException {
    if(nod instanceof FNode) {
      Nod n = nod;
      while(n.type != Type.DOC) {
        n = n.parent();
        if(n == null) IDDOC.thrw(input);
      }
    }
    return nod;
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X11 && def == FunDef.ELID ||
      u == Use.CTX && expr.length == 1 || super.uses(u);
  }
}
