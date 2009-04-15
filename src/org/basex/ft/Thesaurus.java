package org.basex.ft;

import static org.basex.util.Token.*;
import org.basex.build.MemBuilder;
import org.basex.build.xml.DirParser;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.io.IO;
import org.basex.query.QueryProcessor;
import org.basex.util.Array;
import org.basex.util.Map;
import org.basex.util.Token;
import org.basex.util.TokenList;

/**
 * Simple Thesaurus for fulltext requests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Thesaurus {
  /** Thesaurus root references. */
  private final Map<Node> nodes = new Map<Node>();
  /** Relationships. */
  private static final Map<byte[]> RSHIPS = new Map<byte[]>();

  static {
    RSHIPS.add(token("NT"), token("BT"));
    RSHIPS.add(token("BT"), token("BT"));
    RSHIPS.add(token("BTG"), token("NTG"));
    RSHIPS.add(token("NTG"), token("BTG"));
    RSHIPS.add(token("BTP"), token("NTP"));
    RSHIPS.add(token("NTP"), token("BTP"));
    RSHIPS.add(token("USE"), token("UF"));
    RSHIPS.add(token("UF"), token("USE"));
    RSHIPS.add(token("RT"), token("RT"));
  }
  
  /** Thesaurus node. */
  static class Node {
    /** Related nodes. */
    Node[] nodes = {};
    /** Relationships. */
    byte[][] rs = {};
    /** Term. */
    byte[] term;

    /**
     * Adds a relationship to the node.
     * @param n target node
     * @param r relationship
     */
    void add(final Node n, final byte[] r) {
      nodes = Array.add(nodes, n);
      rs = Array.add(rs, r);
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
   */
  public Thesaurus(final IO fl) {
    this(fl, EMPTY, 0, Long.MAX_VALUE);
  }

  /**
   * Reads a thesaurus file.
   * @param fl file reference
   * @param rs relationship
   * @param mn minimum level
   * @param mx maximum level
   */
  public Thesaurus(final IO fl, final byte[] rs, final long mn, final long mx) {
    file = fl;
    rel = rs;
    min = mn;
    max = mx;
  }
  
  /**
   * Initializes the thesaurus.
   * @return success flag
   */
  public boolean init() {
    try {
      final Data data = new MemBuilder().build(new DirParser(file), "");
      final Nodes result = nodes("//*:entry", new Nodes(0, data));
      for(int n = 0; n < result.size(); n++) {
        build(new Nodes(result.nodes[n], data));
      }
      return true;
    } catch(final Exception ex) {
      return false;
    }
  }

  /**
   * Builds the thesaurus.
   * @param in input nodes
   * @throws Exception exception
   */
  private void build(final Nodes in) throws Exception {
    final byte[] term = text("*:term", in);
    Node node = getNode(term);

    final Nodes sub = nodes("*:synonym", in);
    for(int n = 0; n < sub.size(); n++) {
      final Nodes tmp = new Nodes(sub.nodes[n], sub.data);
      final Node snode = getNode(text("*:term", tmp));
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
  private Node getNode(final byte[] term) {
    Node node = nodes.get(term);
    if(node == null) {
      node = new Node();
      node.term = term;
      nodes.add(term, node);
    }
    return node;
  }
  
  /**
   * Performs a query and returns the result as nodes.
   * @param query query string
   * @param in input nodes
   * @return resulting nodes
   * @throws Exception exception
   */
  private Nodes nodes(final String query, final Nodes in) throws Exception {
    return new QueryProcessor(query, in).queryNodes();
  }

  /**
   * Performs a query and returns the first result as text.
   * @param query query string
   * @param in input nodes
   * @return resulting text
   * @throws Exception exception
   */
  private byte[] text(final String query, final Nodes in) throws Exception {
    return new QueryProcessor(query, in).iter().next().str();
  }
  
  /**
   * Finds a thesaurus term.
   * @param list result list
   * @param term query term
   */
  void find(final TokenList list, final byte[] term) {
    find(list, nodes.get(term), 1);
  }

  /**
   * Recursively collects relevant thesaurus terms.
   * @param list result list
   * @param node input node
   * @param lev current level
   */
  private void find(final TokenList list, final Node node, final long lev) {
    if(lev > max || node == null) return;

    for(int n = 0; n < node.nodes.length; n++) {
      if(rel.length == 0 || node.rs[n].equals(rel)) {
        if(!list.contains(node.term)) {
          if(lev >= min) list.add(node.term);
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
