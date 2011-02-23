package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTxt;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
import org.basex.util.Atts;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Element constructor.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Constr {
  /** Node array. */
  public final NodIter children = new NodIter();
  /** Attribute array. */
  public final NodIter ats = new NodIter();
  /** Error: attribute position. */
  public boolean errAtt;
  /** Error: duplicate attribute. */
  public byte[] duplAtt;

  /** Text cache. */
  private final TokenBuilder text = new TokenBuilder();
  /** Space separator flag. */
  private boolean more;
  /** Base uri. */
  byte[] base = EMPTY;

  /**
   * Creates the children of the constructor.
   * @param ii input info
   * @param ctx query context
   * @param expr input expressions
   * @throws QueryException query exception
   */
  public Constr(final InputInfo ii, final QueryContext ctx, final Expr... expr)
      throws QueryException {

    for(final Expr e : expr) {
      more = false;
      final Iter iter = ctx.iter(e);
      while(add(ctx, iter.next(), ii));
    }
    if(text.size() != 0) children.add(new FTxt(text.finish(), null));
  }

  /**
   * Recursively adds nodes to the element arrays. Recursion is necessary
   * as documents are resolved to their child nodes.
   * @param ctx query context
   * @param it current item
   * @param ii input info
   * @return true if item was added
   * @throws QueryException query exception
   */
  private boolean add(final QueryContext ctx, final Item it, final InputInfo ii)
      throws QueryException {

    if(it == null) return false;

    if(it.node() && it.type != NodeType.TXT) {
      Nod node = (Nod) it;

      if(it.type == NodeType.ATT) {
        // text has already been added - no attribute allowed anymore
        if(text.size() != 0 || children.size() != 0) {
          errAtt = true;
          return false;
        }

        // split attribute name
        final QNm name = node.qname();
        final byte[] ln = name.ln();
        final byte[] pre = name.pref();
        if(eq(pre, XML) && eq(ln, BASE)) base = it.atom(ii);

        // check for duplicate attribute names
        final QNm qname = node.qname();
        for(int a = 0; a < ats.size(); ++a) {
          if(qname.eq(ats.get(a).qname())) {
            duplAtt = qname.atom();
            return false;
          }
        }
        // add attribute
        ats.add(node.copy());
      } else if(it.type == NodeType.DOC) {
        final NodeIter iter = node.child();
        Nod ch;
        while((ch = iter.next()) != null) add(ctx, ch, ii);
      } else {
        // add text node
        if(text.size() != 0) {
          children.add(new FTxt(text.finish(), null));
          text.reset();
        }
        node = node.copy();
        children.add(node);

        // add namespaces from ancestors
        final Atts atts = node.ns();
        if(atts != null && atts.size != 0) {
          // [LK][LW] why only if there are already namespaces?
          node = node.parent();
          while(node != null && node.type == NodeType.ELM) {
            final Atts ns = node.ns();
            for(int a = 0; a < ns.size; ++a) {
              if(!atts.contains(ns.key[a])) atts.add(ns.key[a], ns.val[a]);
            }
            node = node.parent();
          }
        }
      }
      more = false;
    } else {
      if(more && it.type != NodeType.TXT) text.add(' ');
      text.add(it.atom(ii));
      more = it.type != NodeType.TXT;
    }
    return true;
  }
}
