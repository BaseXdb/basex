package org.basex.query.expr.ft;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Simple Thesaurus for full-text requests.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Thesaurus {
  /** Thesaurus root references. */
  private final TokenObjMap<ThesNode> nodes = new TokenObjMap<>();
  /** Relationships. */
  private static final TokenMap RSHIPS = new TokenMap();
  /** Database context. */
  private final Context ctx;

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

  /** Input file. */
  private final IO file;
  /** Relationship. */
  private final byte[] rel;
  /** Minimum level. */
  private final long min;
  /** Maximum level. */
  private final long max;

  /**
   * Constructor.
   * @param file file reference
   * @param ctx database context
   */
  public Thesaurus(final IO file, final Context ctx) {
    this(file, EMPTY, 0, Long.MAX_VALUE, ctx);
  }

  /**
   * Reads a thesaurus file.
   * @param file file reference
   * @param res relationship
   * @param min minimum level
   * @param max maximum level
   * @param ctx database context
   */
  public Thesaurus(final IO file, final byte[] res, final long min, final long max,
      final Context ctx) {
    this.file = file;
    rel = res;
    this.min = min;
    this.max = Math.min(max, min + 100);
    this.ctx = ctx;
  }

  /**
   * Initializes the thesaurus.
   * @param ii input info
   * @throws QueryException query exception
   */
  private void init(final InputInfo ii) throws QueryException {
    try {
      final Value entries = nodes("//*:entry", new DBNode(file));
      for(final Item entry : entries) build(entry);
    } catch(final IOException ex) {
      Util.debug(ex);
      throw NOTHES_X.get(ii, file);
    }
  }

  /**
   * Builds the thesaurus.
   * @param value input nodes
   * @throws QueryException query exception
   */
  private void build(final Value value) throws QueryException {
    final Value synonyms = nodes("*:synonym", value);
    if(synonyms.isEmpty()) return;

    final ThesNode term = node(text("*:term", value));
    for(final Item synonym : synonyms) {
      final ThesNode sterm = node(text("*:term", synonym));
      final byte[] rs = text("*:relationship", synonym);
      term.add(sterm, rs);

      final byte[] srs = RSHIPS.get(rs);
      if(srs != null) sterm.add(term, srs);
      build(synonyms);
    }
  }

  /**
   * Returns a node for the specified term.
   * @param term term
   * @return node
   */
  private ThesNode node(final byte[] term) {
    return nodes.computeIfAbsent(term, () -> new ThesNode(term));
  }

  /**
   * Performs a query and returns the result as nodes.
   * @param query query string
   * @param value value
   * @return resulting nodes
   * @throws QueryException query exception
   */
  private Value nodes(final String query, final Value value) throws QueryException {
    try(QueryProcessor qp = new QueryProcessor(query, ctx).context(value)) {
      return qp.value();
    }
  }

  /**
   * Performs a query and returns the first result as text.
   * @param query query string
   * @param value value
   * @return resulting text
   * @throws QueryException query exception
   */
  private byte[] text(final String query, final Value value) throws QueryException {
    try(QueryProcessor qp = new QueryProcessor(query, ctx).context(value)) {
      return qp.iter().next().string(null);
    }
  }

  /**
   * Finds a thesaurus term.
   * @param ii input info
   * @param list result list
   * @param token token
   * @throws QueryException query exception
   */
  void find(final InputInfo ii, final TokenList list, final byte[] token) throws QueryException {
    if(nodes.isEmpty()) init(ii);

    final ThesNode tn = nodes.get(token);
    if(tn != null) find(list, tn, 1);
  }

  /**
   * Recursively collects relevant thesaurus terms.
   * @param list result list
   * @param node input node
   * @param level current level
   */
  private void find(final TokenList list, final ThesNode node, final long level) {
    for(int n = 0; n < node.size; ++n) {
      if(rel.length == 0 || eq(node.rs[n], rel)) {
        final ThesNode tn = node.nodes[n];
        final byte[] term = tn.term;
        if(!list.contains(term)) {
          list.add(term);
          if(level < max) find(list, tn, level + 1);
        }
      }
    }
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof Thesaurus)) return false;
    final Thesaurus th = (Thesaurus) obj;
    return file.eq(th.file) && min == th.min && max == th.max && eq(rel, th.rel);
  }

  /** Thesaurus node. */
  private static final class ThesNode {
    /** Related nodes. */
    private ThesNode[] nodes = new ThesNode[1];
    /** Relationships. */
    private byte[][] rs = new byte[1][];
    /** Term. */
    private final byte[] term;
    /** Entries. */
    private int size;

    /**
     * Constructor.
     * @param term term
     */
    private ThesNode(final byte[] term) {
      this.term = term;
    }

    /**
     * Adds a relationship to the node.
     * @param n target node
     * @param r relationship
     */
    private void add(final ThesNode n, final byte[] r) {
      if(size == nodes.length) {
        final int s = Array.newCapacity(size);
        nodes = Array.copy(nodes, new ThesNode[s]);
        rs = Array.copyOf(rs, s);
      }
      nodes[size] = n;
      rs[size++] = r;
    }
  }
}
