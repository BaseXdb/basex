package org.basex.query.func.fn;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnPath extends ContextFn {
  /** Root function string. */
  private static final byte[] ROOT = QNm.eqName(QueryText.FN_URI, Token.token("root()"));
  /** Path cache. Caches the 1000 last accessed elements. */
  private final Map<ANode, byte[]> paths = Collections.synchronizedMap(
    new LinkedHashMap<ANode, byte[]>(16, 0.75f, true) {
      @Override
      protected boolean removeEldestEntry(final Map.Entry<ANode, byte[]> eldest) {
        return size() > 1000;
      }
    }
  );

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ANode node = toNodeOrNull(ctxArg(0, qc), qc);
    if(node == null) return Empty.VALUE;

    final TokenBuilder tb = new TokenBuilder();
    final TokenList steps = new TokenList();
    final ANodeList nodes = new ANodeList();

    while(true) {
      // skip ancestor traversal if cached path is found
      final byte[] path = paths.get(node);
      if(path != null) {
        tb.add(path);
        break;
      }
      // root node: finalize traversal
      final ANode parent = node.parent();
      if(parent == null) {
        if(node.type != NodeType.DOCUMENT_NODE) tb.add(ROOT);
        break;
      }

      final QNm qname = node.qname();
      final NodeType type = (NodeType) node.type;
      if(type == NodeType.ATTRIBUTE) {
        tb.add('@').add(qname.id());
      } else if(type == NodeType.ELEMENT) {
        tb.add(qname.eqName()).add('[').addInt(element(node, qname, qc)).add(']');
      } else if(type == NodeType.COMMENT || type == NodeType.TEXT) {
        tb.add(type.toString()).add('[').addInt(textComment(node, qc)).add(']');
      } else if(type == NodeType.PROCESSING_INSTRUCTION) {
        final String name = type.toString(Token.string(qname.local()));
        tb.add(name).add('[').addInt(pi(node, qname, qc)).add(']');
      }
      steps.add(tb.next());
      nodes.add(node);
      node = parent;
    }

    // add all steps in reverse order; cache element paths
    for(int s = steps.size() - 1; s >= 0; --s) {
      tb.add('/').add(steps.get(s));
      node = nodes.get(s);
      if(node.type == NodeType.ELEMENT) paths.put(node, tb.toArray());
    }
    return Str.get(tb.isEmpty() ? Token.SLASH : tb.finish());
  }

  /**
   * Returns the child index of an element.
   * @param node node
   * @param qname QName
   * @param qc query context
   * @return index
   */
  private static int element(final ANode node, final QNm qname, final QueryContext qc) {
    int p = 1;
    final BasicNodeIter iter = node.precedingSiblingIter();
    for(ANode fs; (fs = iter.next()) != null;) {
      qc.checkStop();
      final QNm qnm = fs.qname();
      if(qnm != null && qnm.eq(qname)) p++;
    }
    return p;
  }

  /**
   * Returns the child index of an text or comment.
   * @param node node
   * @param qc query context
   * @return index
   */
  private static int textComment(final ANode node, final QueryContext qc) {
    int p = 1;
    final BasicNodeIter iter = node.precedingSiblingIter();
    for(ANode fs; (fs = iter.next()) != null;) {
      qc.checkStop();
      if(fs.type == node.type) p++;
    }
    return p;
  }

  /**
   * Returns the child index of a processing instruction.
   * @param node node
   * @param qname QName
   * @param qc query context
   * @return index
   */
  private static int pi(final ANode node, final QNm qname, final QueryContext qc) {
    final BasicNodeIter iter = node.precedingSiblingIter();
    int p = 1;
    for(ANode fs; (fs = iter.next()) != null;) {
      qc.checkStop();
      if(fs.type == node.type && fs.qname().eq(qname)) p++;
    }
    return p;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst(true, false, cc.qc.focus.value);
  }
}
