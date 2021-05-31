package org.basex.query.expr.ft;

import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Thesaurus structure for full-text requests.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Thesaurus {
  /** Input Info (can be {@code null}). */
  private final InputInfo info;
  /** File reference. */
  private final IO file;
  /** Relationship. */
  private final byte[] rel;
  /** Minimum level. */
  private final long min;
  /** Maximum level. */
  private final long max;

  /** Thesaurus nodes. */
  private ThesNodes nodes;

  /**
   * Constructor.
   * @param file file reference
   */
  public Thesaurus(final IO file) {
    this(file, EMPTY, 0, Long.MAX_VALUE, null);
  }

  /**
   * Constructor.
   * @param file file reference
   * @param rel relationship
   * @param min minimum level
   * @param max maximum level
   * @param info input info
   */
  public Thesaurus(final IO file, final byte[] rel, final long min, final long max,
      final InputInfo info) {
    this.file = file;
    this.rel = rel;
    this.min = min;
    this.max = Math.min(max, min + 100);
    this.info = info;
  }

  /**
   * Finds a thesaurus term.
   * @param list result list
   * @param token token
   * @param ctx database context
   * @throws QueryException query exception
   */
  void find(final TokenList list, final byte[] token, final Context ctx) throws QueryException {
    if(nodes == null) nodes = new ThesNodes(file, info, ctx);
    final ThesNode node = nodes.get(token);
    if(node != null) find(list, node, 1);
  }

  /**
   * Recursively collects relevant thesaurus terms.
   * @param list result list
   * @param node thesaurus node
   * @param level current level
   */
  private void find(final TokenList list, final ThesNode node, final long level) {
    final int ns = node.size;
    for(int n = 0; n < ns; n++) {
      if(rel.length == 0 || eq(node.relation[n], rel)) {
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
    return file.equals(th.file) && min == th.min && max == th.max && eq(rel, th.rel);
  }

  @Override
  public String toString() {
    return "\"" + file + '"';
  }
}
