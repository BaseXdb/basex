package org.basex.query.expr.constr;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Element constructor.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class Constr {
  /** Node array. */
  public final ANodeList children = new ANodeList();
  /** Attribute array. */
  public final ANodeList atts = new ANodeList();
  /** Namespace array. */
  final Atts nspaces = new Atts();
  /** Error: attribute position. */
  public boolean errAtt;
  /** Error: namespace position. */
  boolean errNS;
  /** Error: duplicate attribute. */
  public byte[] duplAtt;
  /** Error: duplicate namespace. */
  byte[] duplNS;

  /** Static context. */
  private final StaticContext sc;
  /** Input information. */
  private final InputInfo info;
  /** Text cache. */
  private final TokenBuilder text = new TokenBuilder();
  /** Space separator flag. */
  private boolean more;

  /**
   * Creates the children of the constructor.
   * @param info input info
   * @param sc static context
   */
  public Constr(final InputInfo info, final StaticContext sc) {
    this.info = info;
    this.sc = sc;
  }

  /**
   * Constructs child and attribute nodes.
   * @param qc query context
   * @param expr input expressions
   * @return self reference
   * @throws QueryException query exception
   */
  public Constr add(final QueryContext qc, final Expr... expr) throws QueryException {
    final int s = sc.ns.size();
    try {
      for(final Expr e : expr) {
        more = false;
        final Iter iter = qc.iter(e);
        for(Item it; (it = iter.next()) != null && add(qc, it););
      }
      if(!text.isEmpty()) children.add(new FTxt(text.toArray()));
      return this;
    } finally {
      sc.ns.size(s);
    }
  }

  /**
   * Recursively adds nodes to the element arrays. Recursion is necessary
   * as documents are resolved to their child nodes.
   * @param qc query context
   * @param it current item
   * @return true if item was added
   * @throws QueryException query exception
   */
  private boolean add(final QueryContext qc, final Item it) throws QueryException {
    if(it instanceof Array) {
      for(final Value val : ((Array) it).members()) {
        for(final Item i : val) {
          if(!add(qc, i)) return false;
        }
      }
      return true;
    }

    if(it instanceof FItem) throw CONSFUNC_X.get(info, it);

    if(it instanceof ANode) {
      // type: nodes
      ANode node = (ANode) it;

      final Type ip = it.type;
      if(ip == NodeType.TXT) {
        // type: text node
        text.add(node.string());

      } else if(ip == NodeType.ATT) {
        // type: attribute node

        // no attribute allowed after texts or child nodes
        if(!text.isEmpty() || !children.isEmpty()) {
          errAtt = true;
          return false;
        }
        // check for duplicate attribute names
        final QNm name = node.qname();
        for(final ANode att : atts) {
          if(name.eq(att.qname())) {
            duplAtt = name.string();
            return false;
          }
        }
        // add attribute
        atts.add(new FAttr(name, node.string()));
        // add new namespace
        if(name.hasURI()) sc.ns.add(name.prefix(), name.uri());

      } else if(ip == NodeType.NSP) {
        // type: namespace node

        // no attribute allowed after texts or child nodes
        if(!text.isEmpty() || !children.isEmpty()) {
          errNS = true;
          return false;
        }

        // add namespace
        final byte[] name = node.name();
        final byte[] uri = node.string();
        final byte[] u = nspaces.value(name);
        if(u == null) {
          nspaces.add(name, uri);
        } else if(!Token.eq(uri, u)) {
          // duplicate namespace (ignore duplicates with same uri)
          duplNS = name;
          return false;
        }

      } else if(ip == NodeType.DOC) {
        // type: document node

        final BasicNodeIter iter = node.children();
        for(ANode ch; (ch = iter.next()) != null && add(qc, ch););

      } else {
        // type: element/comment/processing instruction node

        // add text node
        if(!text.isEmpty()) children.add(new FTxt(text.next()));

        // [CG] XQuery, element construction: avoid full copy of sub tree if not needed
        node = node.deepCopy(qc.context.options);
        children.add(node);
      }
      more = false;
    } else {
      // type: atomic value
      if(more) text.add(' ');
      text.add(it.string(info));
      more = true;

    }
    return true;
  }
}
