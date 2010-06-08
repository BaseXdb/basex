package org.basex.query.ft;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import java.util.Arrays;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.io.IO;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.util.Err;
import org.basex.util.ObjectMap;
import org.basex.util.Token;
import org.basex.util.TokenList;
import org.basex.util.Tokenizer;

/**
 * Simple Thesaurus for full-text requests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Thesaurus {
  /** Thesaurus root references. */
  private final ObjectMap<ThesNode> nodes = new ObjectMap<ThesNode>();
  /** Relationships. */
  private static final ObjectMap<byte[]> RSHIPS = new ObjectMap<byte[]>();
  /** Database properties. */
  private final Context ctx;

  static {
    RSHIPS.put(token("NT"), token("BT"));
    RSHIPS.put(token("BT"), token("BT"));
    RSHIPS.put(token("BTG"), token("NTG"));
    RSHIPS.put(token("NTG"), token("BTG"));
    RSHIPS.put(token("BTP"), token("NTP"));
    RSHIPS.put(token("NTP"), token("BTP"));
    RSHIPS.put(token("USE"), token("UF"));
    RSHIPS.put(token("UF"), token("USE"));
    RSHIPS.put(token("RT"), token("RT"));
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
   * @return success flag
   * @throws QueryException query exception
   */
  private boolean init() throws QueryException {
    try {
      final Data data = new MemBuilder(
          Parser.xmlParser(file, ctx.prop, ""), ctx.prop).build();
      final Nodes result = nodes("//*:entry", new Nodes(0, data));
      for(int n = 0; n < result.size(); n++) {
        build(new Nodes(result.nodes[n], data));
      }
    } catch(final IOException ex) {
      Err.or(NOTHES, file);
    }
    return true;
  }

  /**
   * Builds the thesaurus.
   * @param in input nodes
   * @throws QueryException query exception
   */
  private void build(final Nodes in) throws QueryException {
    final Nodes sub = nodes("*:synonym", in);
    if(sub.size() == 0) return;

    final ThesNode node = getNode(text("*:term", in));
    for(int n = 0; n < sub.size(); n++) {
      final Nodes tmp = new Nodes(sub.nodes[n], sub.data);
      final ThesNode snode = getNode(text("*:term", tmp));
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
  private ThesNode getNode(final byte[] term) {
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
  private Nodes nodes(final String query, final Nodes in)
      throws QueryException {
    return new QueryProcessor(query, in, ctx).queryNodes();
  }

  /**
   * Performs a query and returns the first result as text.
   * @param query query string
   * @param in input nodes
   * @return resulting text
   * @throws QueryException query exception
   */
  private byte[] text(final String query, final Nodes in)
      throws QueryException {
    return new QueryProcessor(query, in, ctx).iter().next().str();
  }

  /**
   * Finds a thesaurus term.
   * @param list result list
   * @param ft tokenizer
   * @throws QueryException query exception
   */
  void find(final TokenList list, final Tokenizer ft) throws QueryException {
    if(nodes.size() == 0) init();
    find(list, nodes.get(ft.text), 1);
  }

  /**
   * Recursively collects relevant thesaurus terms.
   * @param list result list
   * @param node input node
   * @param lev current level
   */
  private void find(final TokenList list, final ThesNode node, final long lev) {
    if(lev > max || node == null) return;

    for(int n = 0; n < node.size; n++) {
      if(rel.length == 0 || Token.eq(node.rs[n], rel)) {
        final byte[] term = node.nodes[n].term;
        if(!list.contains(term)) {
          //if(lev >= min) list.add(node.term);
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
  boolean eq(final Thesaurus th) {
    return file.eq(th.file) && min == th.min && max == th.max &&
      Token.eq(rel, th.rel);
  }
}
