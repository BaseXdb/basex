package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.expr.index.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Id functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class Ids extends ContextFn {
  /** Map for data references and ID flags. */
  private final Map<Data, Boolean> indexed = Collections.synchronizedMap(new IdentityHashMap<>());

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    return ids(qc);
  }

  /**
   * Returns referenced nodes.
   * @param qc query context
   * @return referenced nodes
   * @throws QueryException query exception
   */
  private Value ids(final QueryContext qc) throws QueryException {
    final TokenSet idSet = ids(arg(0).atomIter(qc, info), qc);
    final XNode node = toNodeOrNull(arg(1), qc);

    final XNode root = (node != null ? node : toNode(context(qc), qc)).root();
    if(root.type != NodeType.DOCUMENT_NODE) throw IDDOC.get(info);

    final ANodeBuilder results = new ANodeBuilder();
    final boolean idref = idref();
    if(index(root, idref)) {
      // create index iterator
      final Data data = root.data();
      final ValueAccess va = new ValueAccess(info, idSet, idref ? IndexType.TOKEN :
        IndexType.ATTRIBUTE, null, new IndexStaticDb(data, info));

      // collect and return index results, filtered by ID/IDREF attributes
      final Iter iter = va.iter(qc);
      for(Item item; (item = iter.next()) != null;) {
        // check attribute name; check root if database has more than one document
        final XNode attr = (XNode) item;
        if(XMLToken.isId(attr.name(), idref) && (idref || idSet.remove(attr.string()) != 0) &&
            (data.meta.ndocs == 1 || attr.root().is(root))) {
          results.add(idref ? attr : attr.parent());
        }
      }
    } else {
      // otherwise, do sequential scan: parse node and its descendants
      add(idSet, results, root);
    }
    return results.value(this);
  }

  /**
   * Checks if the IDs can to be found in the index.
   * @param root root node
   * @param idref follow IDREF
   * @return result of check
   */
  private boolean index(final XNode root, final boolean idref) {
    // check if index exists
    final Data data = root.data();
    if(data == null || (idref ? !data.meta.tokenindex : !data.meta.attrindex)) return false;
    // check if index names contain ID attributes
    return indexed.computeIfAbsent(data, d -> new IndexNames(IndexType.ATTRIBUTE, d).
        containsIds(idref));
  }

  /**
   * Adds nodes with the specified ID.
   * @param idSet IDs to be found
   * @param results node cache
   * @param node current node
   */
  private void add(final TokenSet idSet, final ANodeBuilder results, final XNode node) {
    final boolean idref = idref();
    for(final XNode attr : node.attributeIter()) {
      if(XMLToken.isId(attr.name(), idref)) {
        // ID/IDREF found
        boolean found = false;
        for(final byte[] id : distinctTokens(attr.string())) {
          if((idref ? idSet.contains(id) : idSet.remove(id) != 0) && !found) {
            results.add(idref ? attr.finish() : node.finish());
            found = true;
          }
        }
      }
    }
    for(final XNode child : node.childIter()) {
      add(idSet, results, child);
    }
  }

  /**
   * Extracts and returns all unique IDs from the iterated strings.
   * @param iter iterator
   * @param qc query context
   * @return ID set
   * @throws QueryException query exception
   */
  private TokenSet ids(final Iter iter, final QueryContext qc) throws QueryException {
    final TokenSet ts = new TokenSet();
    for(Item ids; (ids = qc.next(iter)) != null;) {
      for(final byte[] id : distinctTokens(toToken(ids))) {
        if(XMLToken.isNCName(id)) ts.put(id);
      }
    }
    return ts;
  }

  /**
   * Return IDREF flag.
   * @return result of check
   */
  boolean idref() {
    return false;
  }

  @Override
  public final int contextIndex() {
    return 1;
  }

  @Override
  public final boolean ddo() {
    return true;
  }
}
