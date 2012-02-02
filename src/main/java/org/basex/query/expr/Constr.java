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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Constr {
  /** Node array. */
  public final NodeCache children = new NodeCache();
  /** Attribute array. */
  public final NodeCache atts = new NodeCache();
  /** Namespace array. */
  public Atts nspaces = new Atts();
  /** Error: attribute position. */
  public boolean errAtt;
  /** Error: namespace position. */
  public boolean errNS;
  /** Error: duplicate attribute. */
  public byte[] duplAtt;
  /** Error: duplicate namespace. */
  public byte[] duplNS;

  /** Query context. */
  private final QueryContext ctx;
  /** Input information. */
  private final InputInfo input;
  /** Text cache. */
  private final TokenBuilder text = new TokenBuilder();
  /** Space separator flag. */
  private boolean more;

  /**
   * Creates the children of the constructor.
   * @param ii input info
   * @param qc query context
   */
  public Constr(final InputInfo ii, final QueryContext qc) {
    input = ii;
    ctx = qc;
  }

  /**
   * Constructs child and attribute nodes.
   * @param expr input expressions
   * @return self reference
   * @throws QueryException query exception
   */
  public Constr add(final Expr... expr) throws QueryException {
    final int s = ctx.sc.ns.size();
    try {
      for(final Expr e : expr) {
        more = false;
        final Iter iter = ctx.iter(e);
        for(Item ch; (ch = iter.next()) != null && add(ch););
      }
      if(text.size() != 0) children.add(new FTxt(text.finish()));
      return this;
    } finally {
      ctx.sc.ns.size(s);
    }
  }

  /**
   * Recursively adds nodes to the element arrays. Recursion is necessary
   * as documents are resolved to their child nodes.
   * @param it current item
   * @return true if item was added
   * @throws QueryException query exception
   */
  private boolean add(final Item it) throws QueryException {
    final Type ip = it.type;
    if(ip.isFunction()) CONSFUNC.thrw(input, it);

    if(!ip.isNode()) {
      // type: atomic value
      if(more) text.add(' ');
      text.add(it.string(input));
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
        final QNm name = node.qname();
        for(int a = 0; a < atts.size(); ++a) {
          if(name.eq(atts.get(a).qname())) {
            duplAtt = name.string();
            return false;
          }
        }

        // add attribute
        atts.add(new FAttr(name, node.string()));

        if(name.hasURI()) {
          ctx.sc.ns.add(name.prefix(), name.uri());
        }

      } else if(ip == NodeType.NSP) {
        // type: namespace node

        // no attribute allowed after texts or child nodes
        if(text.size() != 0 || children.size() != 0) {
          errNS = true;
          return false;
        }

        // add namespace
        final byte[] name = node.name();
        final byte[] uri = node.string();
        final byte[] u = nspaces.string(name);
        if(u == null) {
          nspaces.add(name, uri);
        } else if(!Token.eq(uri, u)) {
          // duplicate namespace (ignore duplicates with same uri)
          duplNS = name;
          return false;
        }

      } else if(ip == NodeType.DOC) {
        // type: document node

        final AxisIter ai = node.children();
        for(ANode ch; (ch = ai.next()) != null && add(ch););

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
      }
      more = false;
    }
    return true;
  }
}
