package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.core.locks.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

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
    // [CG] XQuery: ID-IDREF Parsing: consider schema information

    final TokenSet idSet = ids(exprs[0].atomIter(qc, info));
    final ANode root = checkRoot(toNode(ctxArg(1, qc), qc));

    final Data data = root.data();
    if(idref || data == null) {
      final ANodeList list = new ANodeList().check();
      add(idSet, list, root, idref);
      return list.iter();
    }

    // database value index can be utilized. create index iterator
    final TokenList idList = new TokenList(idSet.size());
    for(final byte[] id : idSet) idList.add(id);
    final Value ids = StrSeq.get(idList);
    final ValueAccess va = new ValueAccess(info, ids, false, null, new IndexContext(data, false));

    // collect and return index results, filtered by id/idref attributes
    final ANodeList results = new ANodeList();
    for(final ANode attr : va.iter(qc)) {
      if(isId(attr, idref)) results.add(attr.parent());
    }
    return results.iter();
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
      if(isId(attr, idref)) {
        // id/idref found
        for(final byte[] val : split(normalize(attr.string()), ' ')) {
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
   * Checks if an attribute is an id/idref attribute.
   * @param attr attribute
   * @param idref id/idref flag
   * @return result of check
   */
  private static boolean isId(final ANode attr, final boolean idref) {
    final byte[] name = lc(attr.name());
    return idref ? contains(name, IDREF) : contains(name, ID) && !contains(name, IDREF);
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
