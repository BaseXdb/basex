package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;

/**
 * Id functions.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
abstract class Ids extends StandardFunc {
  /**
   * Returns referenced nodes.
   * @param qc query context
   * @param idref follow idref
   * @return referenced nodes
   * @throws QueryException query exception
   */
  protected BasicNodeIter ids(final QueryContext qc, final boolean idref) throws QueryException {
    // [CG] XQuery: ID-IDREF Parsing: take advantage of index structure; consider schema information
    final ANodeList list = new ANodeList().check();
    add(ids(exprs[0].atomIter(qc, info)), list, checkRoot(toNode(arg(1, qc), qc)), idref);
    return list.iter();
  }


  /**
   * Adds nodes with the specified id.
   * @param ids ids to be found
   * @param idref idref flag
   * @param list node cache
   * @param node current node
   */
  private static void add(final TokenSet ids, final ANodeList list, final ANode node,
      final boolean idref) {

    for(final ANode item : node.attributes()) {
      final byte[] name = lc(item.name());
      if(idref ? contains(name, IDREF) : contains(name, ID) && !contains(name, IDREF)) {
        // id/idref found
        for(final byte[] val : split(normalize(item.string()), ' ')) {
          // correct value: add to results
          if(ids.contains(val)) {
            list.add(idref ? item.finish() : node);
            break;
          }
        }
      }
    }
    for(final ANode item : node.children()) {
      add(ids, list, item.finish(), idref);
    }
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
   * Extracts and returns all ids from the iterated strings.
   * @param iter iterator
   * @return id set
   * @throws QueryException query exception
   */
  private TokenSet ids(final Iter iter) throws QueryException {
    final TokenSet ts = new TokenSet();
    for(Item ids; (ids = iter.next()) != null;) {
      for(final byte[] id : split(normalize(toToken(ids)), ' ')) ts.put(id);
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
