package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Bln;
import org.basex.query.item.FNode;
import org.basex.query.item.Item;
import org.basex.query.item.ANode;
import org.basex.query.item.NodeType;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeCache;
import org.basex.query.iter.AxisIter;
import org.basex.util.InputInfo;
import org.basex.util.list.TokenList;

/**
 * ID functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNId extends FuncCall {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNId(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    // functions have 1 or 2 arguments...
    final Item it = checkEmpty((expr.length == 2 ? expr[1] :
      checkCtx(ctx)).item(ctx, input));

    final ANode node = checkNode(it);
    switch(def) {
      case ID:              return id(ctx.iter(expr[0]), node);
      case IDREF:           return idref(ctx.iter(expr[0]), node);
      case ELEMENT_WITH_ID: return elid(ctx.iter(expr[0]), node);
      default:              return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    // functions have 1 or 2 arguments...
    final Item it = checkEmpty((expr.length == 2 ? expr[1] :
      checkCtx(ctx)).item(ctx, input));

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
  private Iter elid(final Iter it, final ANode node) throws QueryException {
    final NodeCache nc = id(it, node);
    final NodeCache res = new NodeCache().random();
    for(ANode n; (n = nc.next()) != null;) res.add(n.parent());
    return res;
  }

  /**
   * Returns the result of the ID function.
   * @param it item ids to be found
   * @param node attribute
   * @return resulting node list
   * @throws QueryException query exception
   */
  private NodeCache id(final Iter it, final ANode node) throws QueryException {
    final NodeCache nc = new NodeCache().random();
    add(ids(it), nc, checkRoot(node));
    return nc;
  }

  /**
   * Returns the result of the IDREF function.
   * @param it item ids to be found
   * @param node attribute
   * @return resulting node list
   * @throws QueryException query exception
   */
  private Iter idref(final Iter it, final ANode node) throws QueryException {
    final NodeCache nb = new NodeCache().random();
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
  private Bln lang(final byte[] lang, final ANode node) throws QueryException {
    for(ANode n = node; n != null; n = n.parent()) {
      final AxisIter atts = n.attributes();
      for(ANode at; (at = atts.next()) != null;) {
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
    for(Item id; (id = iter.next()) != null;) {
      for(final byte[] i : split(norm(checkEStr(id)), ' ')) tl.add(i);
    }
    return tl.toArray();
  }

  /**
   * Adds nodes with the specified id.
   * @param ids ids to be found
   * @param nc node cache
   * @param node node
   * @throws QueryException query exception
   */
  private void add(final byte[][] ids, final NodeCache nc,
      final ANode node) throws QueryException {

    AxisIter ai = node.attributes();
    for(ANode att; (att = ai.next()) != null;) {
      // [CG] XQuery: ID-IDREF Parsing
      for(final byte[] id : ids) {
        if(!eq(checkEStr(att), id)) continue;
        final byte[] nm = lc(att.qname().atom());
        if(contains(nm, ID) && !contains(nm, IDREF)) nc.add(node);
      }
    }
    ai = node.children();
    for(ANode att; (att = ai.next()) != null;) add(ids, nc, att.finish());
  }

  /**
   * Adds nodes with the specified id.
   * @param ids ids to be found
   * @param nc node cache
   * @param node node
   * @throws QueryException query exception
   */
  private void addRef(final byte[][] ids, final NodeCache nc,
      final ANode node) throws QueryException {

    AxisIter ai = node.attributes();
    for(ANode att; (att = ai.next()) != null;) {
      // [CG] XQuery: ID-IDREF Parsing
      for(final byte[] id : ids) {
        if(!eq(checkEStr(att), id)) continue;
        final byte[] nm = lc(att.qname().atom());
        if(contains(nm, IDREF)) nc.add(att.finish());
      }
    }
    ai = node.children();
    for(ANode att; (att = ai.next()) != null;) addRef(ids, nc, att.finish());
  }

  /**
   * Checks if the specified node has a document node as root.
   * @param node input node
   * @return specified node
   * @throws QueryException query exception
   */
  private ANode checkRoot(final ANode node) throws QueryException {
    if(node instanceof FNode) {
      ANode n = node;
      while(n.type != NodeType.DOC) {
        n = n.parent();
        if(n == null) throw IDDOC.thrw(input);
      }
    }
    return node;
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X30 && def == Function.ELEMENT_WITH_ID ||
      u == Use.CTX && expr.length == 1 || super.uses(u);
  }
}
