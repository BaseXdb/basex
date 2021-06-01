package org.basex.query.expr.ft;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
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
  /** Relationship. */
  private final byte[] rel;
  /** Minimum level. */
  private final long min;
  /** Maximum level. */
  private final long max;

  /** File reference. */
  private IO file;
  /** Thesaurus root node. */
  private ANode root;

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
    this(rel, min, max, info);
    this.file = file;
  }

  /**
   * Constructor.
   * @param root thesaurus root node
   * @param rel relationship
   * @param min minimum level
   * @param max maximum level
   * @param info input info
   */
  public Thesaurus(final ANode root, final byte[] rel, final long min, final long max,
      final InputInfo info) {
    this(rel, min, max, info);
    this.root = root;
  }

  /**
   * Constructor.
   * @param rel relationship
   * @param min minimum level
   * @param max maximum level
   * @param info input info
   */
  private Thesaurus(final byte[] rel, final long min, final long max, final InputInfo info) {
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
  public void find(final TokenList list, final byte[] token, final Context ctx)
      throws QueryException {
    final ThesNode node = nodes(ctx).get(token);
    if(node != null) find(list, node, 0);
  }

  /**
   * Initializes the thesaurus map.
   * @param ctx database context
   * @return thesaurus map
   * @throws QueryException query exception
   */
  private ThesNodes nodes(final Context ctx) throws QueryException {
    if(nodes == null) {
      if(root == null) {
        try {
          root = new DBNode(file);
        } catch(final IOException ex) {
          Util.debug(ex);
          throw QueryError.NOTHES_X.get(info, file);
        }
      }
      nodes = new ThesNodes(root, ctx);
    }
    return nodes;
  }

  /**
   * Recursively collects relevant thesaurus terms.
   * @param list result list
   * @param node thesaurus node
   * @param level current level
   */
  private void find(final TokenList list, final ThesNode node, final long level) {
    if(level >= max) return;

    final int ns = node.size;
    for(int n = 0; n < ns; n++) {
      if(rel.length == 0 || eq(node.relation[n], rel)) {
        final ThesNode t = node.nodes[n];
        final byte[] term = t.term;
        if(!list.contains(term)) {
          list.add(term);
          find(list, t, level + 1);
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
