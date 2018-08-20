package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.locks.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.expr.index.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Id functions.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
abstract class Ids extends StandardFunc {
  /** Hash map for data references and id flags. */
  private final IdentityHashMap<Data, Boolean> indexed = new IdentityHashMap<>();

  /**
   * Returns referenced nodes.
   * @param qc query context
   * @param idref resolve id reference
   * @return referenced nodes
   * @throws QueryException query exception
   */
  protected BasicNodeIter ids(final QueryContext qc, final boolean idref) throws QueryException {
    final TokenSet idSet = ids(exprs[0].atomIter(qc, info), qc);
    final ANode root = checkRoot(toNode(ctxArg(1, qc), qc));

    if(index(root, idref)) {
      // create index iterator
      final Data data = root.data();
      final ValueAccess va = new ValueAccess(info, idSet, idref ? IndexType.TOKEN :
        IndexType.ATTRIBUTE, null, new IndexStaticDb(info, data));

      // collect and return index results, filtered by id/idref attributes
      final ANodeList results = new ANodeList();
      for(final ANode attr : va.iter(qc)) {
        // check attribute name; check root if database has more than one document
        if(XMLToken.isId(attr.name(), idref) && (data.meta.ndocs == 1 || attr.root().is(root)))
          results.add(idref ? attr : attr.parent());
      }
      return results.iter();
    }

    // otherwise, do sequential scan: parse node and its descendants
    final ANodeBuilder list = new ANodeBuilder();
    add(idSet, list, root, idref);
    return list.iter();
  }

  /**
   * Checks if the ids can to be found in the index.
   * @param root root node
   * @param idref follow idref
   * @return result of check
   */
  private boolean index(final ANode root, final boolean idref) {
    // check if index exists
    final Data data = root.data();
    if(data == null || !(idref ? data.meta.tokenindex : data.meta.attrindex)) return false;
    // check if index names contain id attributes
    synchronized(indexed) {
      return indexed.computeIfAbsent(data, d -> new IndexNames(IndexType.ATTRIBUTE, d).
          containsIds(idref));
    }
  }

  /**
   * Adds nodes with the specified id.
   * @param idSet ids to be found
   * @param results node cache
   * @param node current node
   * @param idref idref flag
   */
  private static void add(final TokenSet idSet, final ANodeBuilder results, final ANode node,
      final boolean idref) {

    for(final ANode attr : node.attributes()) {
      if(XMLToken.isId(attr.name(), idref)) {
        // id/idref found
        for(final byte[] token : distinctTokens(attr.string())) {
          // correct value: add to results
          if(idSet.contains(token)) {
            results.add(idref ? attr.finish() : node.finish());
            break;
          }
        }
      }
    }
    for(final ANode child : node.children()) add(idSet, results, child, idref);
  }

  /**
   * Checks if the specified node has a document node as root.
   * @param node input node
   * @return root node
   * @throws QueryException query exception
   */
  private ANode checkRoot(final ANode node) throws QueryException {
    final ANode root = node.root();
    if(root.type != NodeType.DOC) throw IDDOC.get(info);
    return root;
  }

  /**
   * Extracts and returns all unique ids from the iterated strings.
   * @param iter iterator
   * @param qc query context
   * @return id set
   * @throws QueryException query exception
   */
  private TokenSet ids(final Iter iter, final QueryContext qc) throws QueryException {
    final TokenSet ts = new TokenSet();
    for(Item ids; (ids = qc.next(iter)) != null;) {
      for(final byte[] id : distinctTokens(toToken(ids))) ts.put(id);
    }
    return ts;
  }

  @Override
  public final boolean has(final Flag... flags) {
    return Flag.CTX.in(flags) && exprs.length == 1 || super.has(flags);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return (exprs.length != 1 || visitor.lock(Locking.CONTEXT, false)) && super.accept(visitor);
  }
}
