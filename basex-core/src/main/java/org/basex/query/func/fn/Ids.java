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
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Id functions.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
abstract class Ids extends StandardFunc {
  /** Hash map for data references and id flags. */
  final IdentityHashMap<Data, Boolean> indexed = new IdentityHashMap<>();

  /**
   * Returns referenced nodes.
   * @param qc query context
   * @param idref follow idref
   * @return referenced nodes
   * @throws QueryException query exception
   */
  protected BasicNodeIter ids(final QueryContext qc, final boolean idref) throws QueryException {
    final TokenSet idSet = ids(exprs[0].atomIter(qc, info));
    final ANode root = checkRoot(toNode(ctxArg(1, qc), qc));

    if(index(root, idref)) {
      // create index iterator
      final TokenList idList = new TokenList(idSet.size());
      for(final byte[] id : idSet) idList.add(id);
      final Value ids = StrSeq.get(idList);
      final ValueAccess va = new ValueAccess(info, ids, idref ? IndexType.TOKEN :
        IndexType.ATTRIBUTE, null, new IndexContext(root.data(), false));

      // collect and return index results, filtered by id/idref attributes
      final ANodeList results = new ANodeList();
      for(final ANode attr : va.iter(qc)) {
        if(XMLToken.isId(attr.name(), idref) && attr.root().is(root))
          results.add(idref ? attr : attr.parent());
      }
      return results.iter();
    }

    // otherwise, do sequential scan: parse node and its descendants
    final ANodeList list = new ANodeList().check();
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
    if(data == null || (idref ? data.tokenIndex : data.attrIndex) == null) return false;
    // check if index names contain id attributes
    if(!indexed.containsKey(data)) {
      indexed.put(data, new IndexNames(IndexType.ATTRIBUTE, data).containsIds(idref));
    }
    return indexed.get(data);
  }

  /**
   * Adds nodes with the specified id.
   * @param idSet ids to be found
   * @param results node cache
   * @param node current node
   * @param idref idref flag
   */
  private static void add(final TokenSet idSet, final ANodeList results, final ANode node,
      final boolean idref) {

    for(final ANode attr : node.attributes()) {
      if(XMLToken.isId(attr.name(), idref)) {
        // id/idref found
        for(final byte[] val : distinctTokens(attr.string())) {
          // correct value: add to results
          if(idSet.contains(val)) {
            results.add(idref ? attr.finish() : node);
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
   * @return id set
   * @throws QueryException query exception
   */
  private TokenSet ids(final Iter iter) throws QueryException {
    final TokenSet ts = new TokenSet();
    for(Item ids; (ids = iter.next()) != null;) {
      for(final byte[] id : distinctTokens(toToken(ids))) ts.put(id);
    }
    return ts;
  }

  @Override
  public final boolean has(final Flag flag) {
    return flag == Flag.CTX && exprs.length == 1 || super.has(flag);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return (exprs.length != 1 || visitor.lock(DBLocking.CONTEXT)) && super.accept(visitor);
  }
}
