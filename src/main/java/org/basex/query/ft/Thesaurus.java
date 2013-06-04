package org.basex.query.ft;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Simple Thesaurus for full-text requests.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Thesaurus {
  /** Thesaurus root references. */
  private final TokenObjMap<ThesNode> nodes = new TokenObjMap<ThesNode>();
  /** Relationships. */
  private static final TokenMap RSHIPS = new TokenMap();
  /** Database properties. */
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

  /** Thesaurus node. */
  static class ThesNode {
    /** Related nodes. */
    ThesNode[] nodes = new ThesNode[1];
    /** Relationships. */
    byte[][] rs = new byte[1][];
    /** Term. */
    byte[] term;
    /** Entries. */
    int size;

    /**
     * Adds a relationship to the node.
     * @param n target node
     * @param r relationship
     */
    void add(final ThesNode n, final byte[] r) {
      if(size == nodes.length) {
        final int s = size << 1;
        nodes = Arrays.copyOf(nodes, s);
        rs = Arrays.copyOf(rs, s);
      }
      nodes[size] = n;
      rs[size++] = r;
    }
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
   * @param fl file reference
   * @param c database context
   */
  public Thesaurus(final IO fl, final Context c) {
    this(fl, EMPTY, 0, Long.MAX_VALUE, c);
  }

  /**
   * Reads a thesaurus file.
   * @param fl file reference
   * @param rs relationship
   * @param mn minimum level
   * @param mx maximum level
   * @param c database context
   */
  public Thesaurus(final IO fl, final byte[] rs, final long mn, final long mx,
      final Context c) {
    file = fl;
    rel = rs;
    min = mn;
    max = mx;
    ctx = c;
  }

  /**
   * Initializes the thesaurus.
   * @param ii input info
   * @throws QueryException query exception
   */
  private void init(final InputInfo ii) throws QueryException {
    try {
      final Data data = MemBuilder.build(Parser.xmlParser(file, ctx.prop));
      final Nodes result = nodes("//*:entry", new Nodes(0, data));
      for(int n = 0; n < result.size(); ++n) {
        build(new Nodes(result.pres[n], data));
      }
    } catch(final IOException ex) {
      NOTHES.thrw(ii, file);
    }
  }

  /**
   * Builds the thesaurus.
   * @param in input nodes
   * @throws QueryException query exception
   */
  private void build(final Nodes in) throws QueryException {
    final Nodes sub = nodes("*:synonym", in);
    if(sub.size() == 0) return;

    final ThesNode node = node(text("*:term", in));
    for(int n = 0; n < sub.size(); ++n) {
      final Nodes tmp = new Nodes(sub.pres[n], sub.data);
      final ThesNode snode = node(text("*:term", tmp));
      final byte[] rs = text("*:relationship", tmp);
      node.add(snode, rs);

      final byte[] srs = RSHIPS.get(rs);
      if(srs != null) snode.add(node, srs);
      build(sub);
    }
  }

  /**
   * Returns a node for the specified term.
   * @param term term
   * @return node
   */
  private ThesNode node(final byte[] term) {
    ThesNode node = nodes.get(term);
    if(node == null) {
      node = new ThesNode();
      node.term = term;
      nodes.put(term, node);
    }
    return node;
  }

  /**
   * Performs a query and returns the result as nodes.
   * @param query query string
   * @param in input nodes
   * @return resulting nodes
   * @throws QueryException query exception
   */
  private Nodes nodes(final String query, final Nodes in) throws QueryException {
    return new QueryProcessor(query, ctx).context(in).queryNodes();
  }

  /**
   * Performs a query and returns the first result as text.
   * @param query query string
   * @param in input nodes
   * @return resulting text
   * @throws QueryException query exception
   */
  private byte[] text(final String query, final Nodes in) throws QueryException {
    return new QueryProcessor(query, ctx).context(in).iter().next().string(null);
  }

  /**
   * Finds a thesaurus term.
   * @param ii input info
   * @param list result list
   * @param ft token
   * @throws QueryException query exception
   */
  void find(final InputInfo ii, final TokenList list, final byte[] ft)
      throws QueryException {
    if(nodes.size() == 0) init(ii);
    find(list, nodes.get(ft), 1);
  }

  /**
   * Recursively collects relevant thesaurus terms.
   * @param list result list
   * @param node input node
   * @param lev current level
   */
  private void find(final TokenList list, final ThesNode node, final long lev) {
    if(lev > max || node == null) return;

    for(int n = 0; n < node.size; ++n) {
      if(rel.length == 0 || eq(node.rs[n], rel)) {
        final byte[] term = node.nodes[n].term;
        if(!list.contains(term)) {
          list.add(term);
          find(list, node.nodes[n], lev + 1);
        }
      }
    }
  }

  /**
   * Compares two thesaurus instances.
   * @param th instance to be compared
   * @return result of check
   */
  boolean sameAs(final Thesaurus th) {
    return file.eq(th.file) && min == th.min && max == th.max &&
      eq(rel, th.rel);
  }
}
