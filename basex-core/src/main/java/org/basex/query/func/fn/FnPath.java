package org.basex.query.func.fn;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class FnPath extends ContextFn {
  /** Root function string. */
  private static final byte[] ROOT = Token.token("root()");
  /** Node position cache. Caches the 1000 last accessed nodes. */
  private final Map<ANode, Integer> cache = Collections.synchronizedMap(
    new LinkedHashMap<ANode, Integer>(16, 0.75f, true) {
      @Override
      protected boolean removeEldestEntry(final Map.Entry<ANode, Integer> eldest) {
        return size() > 1000;
      }
    }
  );

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ANode node = toNodeOrNull(ctxArg(0, qc), qc);
    if(node == null) return Empty.VALUE;

    final TokenList tl = new TokenList();
    final TokenBuilder tb = new TokenBuilder();
    while(true) {
      final ANode parent = node.parent();
      if(parent == null) break;

      final QNm qname = node.qname();
      final Type type = node.type;
      if(type == NodeType.ATT) {
        tb.add('@').add(qname.id());
      } else if(type == NodeType.ELM) {
        tb.add(qname.eqName()).add('[').addInt(element(node, qname, qc)).add(']');
      } else if(type == NodeType.COM || type == NodeType.TXT) {
        tb.add(node.seqType().toString()).add('[').addInt(textComment(node, qc)).add(']');
      } else if(type == NodeType.PI) {
        tb.add(type.string()).add('(').add(qname.local());
        tb.add(")[").addInt(pi(node, qname, qc)).add(']');
      }
      tl.add(tb.next());
      node = parent;
    }

    // add root function
    if(node.type != NodeType.DOC) tb.add(QNm.eqName(QueryText.FN_URI, ROOT));
    // add all steps in reverse order
    for(int i = tl.size() - 1; i >= 0; --i) tb.add('/').add(tl.get(i));
    return Str.get(tb.isEmpty() ? Token.SLASH : tb.finish());
  }

  /**
   * Returns the child index of an element.
   * @param node node
   * @param qname QName
   * @param qc query context
   * @return index
   */
  private int element(final ANode node, final QNm qname, final QueryContext qc) {
    final Integer pos = cache.get(node);
    if(pos != null) return pos;

    int p = 1;
    final BasicNodeIter iter = node.precedingSiblingIter();
    for(ANode fs; (fs = iter.next()) != null;) {
      qc.checkStop();
      final QNm qnm = fs.qname();
      if(qnm != null && qnm.eq(qname)) p++;
    }
    cache.put(node, p);
    return p;
  }

  /**
   * Returns the child index of an text or comment.
   * @param node node
   * @param qc query context
   * @return index
   */
  private int textComment(final ANode node, final QueryContext qc) {
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
  private int pi(final ANode node, final QNm qname, final QueryContext qc) {
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
