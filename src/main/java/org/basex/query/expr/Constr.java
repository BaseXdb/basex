package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTxt;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
import org.basex.util.Atts;
import org.basex.util.TokenBuilder;

/**
 * Element constructor.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
  /** Base URI. */
  byte[] base = EMPTY;

  /**
   * Creates the children of the constructor.
   * @param ctx query context
   * @param expr input expressions
   * @throws QueryException query exception
   */
  public Constr(final QueryContext ctx, final Expr... expr)
      throws QueryException {

    for(final Expr e : expr) {
      more = false;
      final Iter iter = ctx.iter(e);
      while(add(ctx, iter.next()));
    }
    if(text.size() != 0) children.add(new FTxt(text.finish(), null));
  }

  /**
   * Recursively adds nodes to the element arrays. Recursion is necessary
   * as documents are resolved to their child nodes.
   * @param ctx query context
   * @param it current item
   * @return true if item was added
   * @throws QueryException query exception
   */
  private boolean add(final QueryContext ctx, final Item it)
      throws QueryException {

    if(it == null) return false;

    if(it.node() && it.type != Type.TXT) {
      Nod node = (Nod) it;

      if(it.type == Type.ATT) {
        // text has already been added - no attribute allowed anymore
        if(text.size() != 0 || children.size() != 0) {
          errAtt = true;
          return false;
        }

        // split attribute name
        final QNm name = node.qname();
        final byte[] ln = name.ln();
        final byte[] pre = name.pref();
        if(eq(pre, XML) && eq(ln, BASE)) base = it.str();

        // check for duplicate attribute names
        final QNm qname = node.qname();
        for(int a = 0; a < ats.size(); a++) {
          if(qname.eq(ats.get(a).qname())) {
            duplAtt = qname.str();
            return false;
          }
        }
        // add attribute
        ats.add(node.copy());
      } else if(it.type == Type.DOC) {
        final NodeIter iter = node.child();
        Nod ch;
        while((ch = iter.next()) != null) add(ctx, ch);
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
          node = node.parent();
          while(node != null && node.type == Type.ELM) {
            final Atts ns = node.ns();
            for(int a = 0; a < ns.size; a++) {
              if(!atts.contains(ns.key[a])) atts.add(ns.key[a], ns.val[a]);
            }
            node = node.parent();
          }
        }
      }
      more = false;
    } else {
      if(more && it.type != Type.TXT) text.add(' ');
      text.add(it.str());
      more = it.type != Type.TXT;
    }
    return true;
  }
}
