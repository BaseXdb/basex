package org.basex.query.expr.ft;

import java.io.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Thesaurus nodes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class ThesNodes {
  /** Thesaurus root references. */
  private final TokenObjMap<ThesNode> map = new TokenObjMap<>();
  /** Relationships. */
  private static final TokenMap RSHIPS = new TokenMap();

  static {
    RSHIPS.put("NT", "BT");
    RSHIPS.put("BT", "BT");
    RSHIPS.put("BTG", "NTG");
    RSHIPS.put("NTG", "BTG");
    RSHIPS.put("BTP", "NTP");
    RSHIPS.put("NTP", "BTP");
    RSHIPS.put("USE", "UF");
    RSHIPS.put("UF", "USE");
    RSHIPS.put("RT", "RT");
  }

  /**
   * Returns a map with thesaurus nodes.
   * @param file file reference
   * @param info input info
   * @param ctx database context
   * @throws QueryException query exception
   */
  ThesNodes(final IO file, final InputInfo info, final Context ctx) throws QueryException {
    try {
      final ANode node = new DBNode(file);
      final Value entries = nodes("descendant::*:entry", node, ctx);
      for(final Item entry : entries) build(entry, ctx);
    } catch(final IOException ex) {
      Util.debug(ex);
      throw QueryError.NOTHES_X.get(info, file);
    }
  }

  /**
   * Returns a thesaurus node for the specified term.
   * @param term term
   * @return node or {@code null}
   */
  ThesNode get(final byte[] term) {
    return map.get(term);
  }

  /**
   * Builds the thesaurus.
   * @param nodes input nodes
   * @param ctx database context
   * @throws QueryException query exception
   */
  private void build(final Value nodes, final Context ctx) throws QueryException {
    final Value synonyms = nodes("*:synonym", nodes, ctx);
    if(synonyms.isEmpty()) return;

    final QueryFunction<Value, ThesNode> node = v -> {
      final byte[] term = text("*:term", v, ctx);
      return map.computeIfAbsent(term, () -> new ThesNode(term));
    };

    final ThesNode term = node.apply(nodes);
    for(final Item synonym : synonyms) {
      final ThesNode rnode = node.apply(synonym);
      final byte[] rel = text("*:relationship", synonym, ctx);
      term.add(rnode, rel);

      final byte[] srs = RSHIPS.get(rel);
      if(srs != null) rnode.add(term, srs);
      build(synonyms, ctx);
    }
  }

  /**
   * Performs a query and returns the result as nodes.
   * @param query query string
   * @param nodes nodes
   * @param ctx database context
   * @return resulting nodes
   * @throws QueryException query exception
   */
  private Value nodes(final String query, final Value nodes, final Context ctx)
      throws QueryException {
    try(QueryProcessor qp = new QueryProcessor(query, ctx).context(nodes)) {
      return qp.value();
    }
  }

  /**
   * Performs a query and returns the first result as text.
   * @param query query string
   * @param nodes nodes
   * @param ctx database context
   * @return resulting text
   * @throws QueryException query exception
   */
  private byte[] text(final String query, final Value nodes, final Context ctx)
      throws QueryException {
    try(QueryProcessor qp = new QueryProcessor(query, ctx).context(nodes)) {
      final Item item = qp.iter().next();
      return item != null ? item.string(null) : Token.EMPTY;
    }
  }
}
