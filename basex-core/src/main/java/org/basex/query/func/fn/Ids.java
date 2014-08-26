package org.basex.query.func.fn;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.list.*;

/**
 * Id functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class Ids extends StandardFunc {
  /**
   * Extracts the ids from the specified iterator.
   * @param iter iterator
   * @return ids
   * @throws QueryException query exception
   */
  final byte[][] ids(final Iter iter) throws QueryException {
    final TokenList tl = new TokenList();
    for(Item id; (id = iter.next()) != null;) {
      for(final byte[] i : split(normalize(toToken(id)), ' ')) tl.add(i);
    }
    return tl.finish();
  }

  /**
   * Adds nodes with the specified id.
   * @param ids ids to be found
   * @param idref idref flag
   * @param nc node cache
   * @param node node
   */
  static void add(final byte[][] ids, final NodeSeqBuilder nc, final ANode node,
                  final boolean idref) {
    AxisIter ai = node.attributes();
    for(ANode at; (at = ai.next()) != null;) {
      final byte[][] val = split(at.string(), ' ');
      // [CG] XQuery: ID-IDREF Parsing
      for(final byte[] id : ids) {
        if(!eq(id, val)) continue;
        final byte[] nm = lc(at.qname().string());
        final boolean ii = contains(nm, ID), ir = contains(nm, IDREF);
        if(idref ? ir : ii && !ir) nc.add(idref ? at.finish() : node);
      }
    }
    ai = node.children();
    for(ANode att; (att = ai.next()) != null;) add(ids, nc, att.finish(), idref);
  }

  /**
   * Checks if the specified node has a document node as root.
   * @param node input node
   * @return specified node
   * @throws QueryException query exception
   */
  ANode checkRoot(final ANode node) throws QueryException {
    if(node instanceof FNode) {
      ANode n = node;
      while(n.type != NodeType.DOC) {
        n = n.parent();
        if(n == null) throw IDDOC.get(info);
      }
    }
    return node;
  }

  @Override
  public final boolean has(final Flag flag) {
    return flag == Flag.CTX && exprs.length == 1 || super.has(flag);
  }
}
