package org.basex.query.expr;

import static org.basex.query.util.Err.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.FAttr;
import org.basex.query.item.FTxt;
import org.basex.query.item.Item;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeCache;
import org.basex.util.Atts;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Element constructor.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Constr {
  /** Node array. */
  public final NodeCache children = new NodeCache();
  /** Attribute array. */
  public final NodeCache atts = new NodeCache();
  /** Namespace array. */
  public final Atts ns = new Atts();
  /** Error: attribute position. */
  public boolean errAtt;
  /** Error: namespace position. */
  public boolean errNS;
  /** Error: duplicate attribute. */
  public byte[] duplAtt;
  /** Error: duplicate namespace. */
  public byte[] duplNS;

  /** Text cache. */
  private final TokenBuilder text = new TokenBuilder();
  /** Space separator flag. */
  private boolean more;

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
      for(Item ch; (ch = iter.next()) != null;) {
        if(!add(ctx, ch, ii)) break;
      }
    }
    if(text.size() != 0) children.add(new FTxt(text.finish()));
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

    final Type ip = it.type;
    if(ip.isFunction()) CONSFUNC.thrw(ii, it);

    if(!ip.isNode()) {
      // type: atomic value
      if(more) text.add(' ');
      text.add(it.string(ii));
      more = true;

    } else {
      // type: nodes
      ANode node = (ANode) it;

      if(ip == NodeType.TXT) {
        // type: text node
        text.add(node.string());
      } else if(ip == NodeType.ATT) {
        // type: attribute node

        // no attribute allowed after texts or child nodes
        if(text.size() != 0 || children.size() != 0) {
          errAtt = true;
          return false;
        }

        // check for duplicate attribute names
        final QNm qname = node.qname();
        for(int a = 0; a < atts.size(); ++a) {
          if(qname.eq(atts.get(a).qname())) {
            duplAtt = qname.string();
            return false;
          }
        }
        // add attribute
        atts.add(new FAttr(node.qname(), node.string()));
        //atts.add(node.copy());

      } else if(ip == NodeType.NSP) {
        // type: namespace node

        // no attribute allowed after texts or child nodes
        if(text.size() != 0 || children.size() != 0) {
          errNS = true;
          return false;
        }

        // check for duplicate attribute names
        final byte[] name = node.name();
        final byte[] val = node.string();
        int a = -1;
        while(++a < ns.size()) {
          if(Token.eq(name, ns.key(a))) {
            if(Token.eq(val, ns.value(a))) break;
            duplNS = name;
            return false;
          }
        }
        // add namespace
        if(a == ns.size()) ns.add(name, val);

      } else if(ip == NodeType.DOC) {
        // type: document node

        final AxisIter ai = node.children();
        for(ANode ch; (ch = ai.next()) != null;) {
          if(!add(ctx, ch, ii)) return false;
        }
      } else {
        // type: element/comment/processing instruction node

        // add text node
        if(text.size() != 0) {
          children.add(new FTxt(text.finish()));
          text.reset();
        }

        // [CG] Element construction: avoid full copy of sub tree if not needed
        node = node.copy(ctx);
        children.add(node);

        // add namespaces to new node
        if(ip == NodeType.ELM) {
          final Atts ats = node.namespaces();

          // add inherited namespaces
          if(ctx.nsInherit) {
            final Atts nsp = ctx.ns.stack();
            for(int a = 0; a < nsp.size(); ++a) {
              final byte[] key = nsp.key(a);
              if(!ats.contains(key)) ats.add(key, nsp.value(a));
            }
          }
          // add namespaces from ancestors
          while(true) {
            node = node.parent();
            if(node == null || node.type != NodeType.ELM) break;
            final Atts nsp = node.namespaces();
            for(int a = 0; a < nsp.size(); ++a) {
              final byte[] key = nsp.key(a);
              if(!ats.contains(key)) ats.add(key, nsp.value(a));
            }
          }
        }
      }
      more = false;
    }
    return true;
  }
}
