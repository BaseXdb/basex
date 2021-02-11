package org.basex.query.expr.constr;

import static org.basex.query.QueryError.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Element constructor.
 *
 * @author BaseX Team 2005-21, BSD License
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
  public QNm errAtt;
  /** Error: duplicate attribute. */
  public QNm duplAtt;
  /** Error: namespace position. */
  QNm errNS;
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
   * @param exprs input expressions
   * @return self reference
   * @throws QueryException query exception
   */
  public Constr add(final QueryContext qc, final Expr... exprs) throws QueryException {
    final int size = sc.ns.size();
    try {
      final QNmSet qnames = new QNmSet();
      for(final Expr expr : exprs) {
        more = false;
        final Iter iter = expr.iter(qc);
        for(Item item; (item = qc.next(iter)) != null && add(qc, item, qnames););
      }
      if(!text.isEmpty()) children.add(new FTxt(text.toArray()));
      return this;
    } finally {
      sc.ns.size(size);
    }
  }

  /**
   * Recursively adds nodes to the element arrays.
   * @param qc query context
   * @param item current item
   * @param qnames assigned attributes (required for duplicate check)
   * @return true if item was added
   * @throws QueryException query exception
   */
  private boolean add(final QueryContext qc, final Item item, final QNmSet qnames)
      throws QueryException {

    if(item instanceof XQArray) {
      for(final Value value : ((XQArray) item).members()) {
        for(final Item it : value) {
          if(!add(qc, it, qnames)) return false;
        }
      }
      return true;
    }

    if(item instanceof FItem) throw CONSFUNC_X.get(info, item);

    if(item instanceof ANode) {
      // type: nodes
      final ANode node = (ANode) item;

      final Type type = item.type;
      if(type == NodeType.TEXT) {
        // type: text node
        text.add(node.string());

      } else if(type == NodeType.ATTRIBUTE) {
        // type: attribute node

        // check if attribute is specified after texts or child nodes
        final QNm name = node.qname();
        if(!text.isEmpty() || !children.isEmpty()) {
          errAtt = name;
          return false;
        }
        // check for duplicate attribute names
        if(!qnames.add(name)) {
          duplAtt = name;
          return false;
        }
        // add attribute
        atts.add(new FAttr(name, node.string()));
        // add new namespace
        if(name.hasURI()) sc.ns.add(name.prefix(), name.uri());

      } else if(type == NodeType.NAMESPACE_NODE) {
        // type: namespace node

        // no attribute allowed after texts or child nodes
        if(!text.isEmpty() || !children.isEmpty()) {
          errNS = node.qname();
          return false;
        }

        // add namespace
        final byte[] name = node.name(), uri = node.string(), knownUri = nspaces.value(name);
        if(knownUri == null) {
          nspaces.add(name, uri);
        } else if(!Token.eq(uri, knownUri)) {
          // duplicate namespace (ignore duplicates with same uri)
          duplNS = name;
          return false;
        }

      } else if(type == NodeType.DOCUMENT_NODE) {
        // type: document node

        final BasicNodeIter iter = node.childIter();
        for(Item it; (it = qc.next(iter)) != null && add(qc, it, qnames););

      } else {
        // type: element/comment/processing instruction node

        // add text node
        if(!text.isEmpty()) children.add(new FTxt(text.next()));
        children.add(node.materialize(qc, qc.context.options.get(MainOptions.COPYNODE)));
      }
      more = false;
    } else {
      // type: atomic value
      if(more) text.add(' ');
      text.add(item.string(info));
      more = true;

    }
    return true;
  }
}
