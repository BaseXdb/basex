package org.basex.query.ft;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.build.MemBuilder;
import org.basex.build.xml.DirParser;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.io.IO;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.util.Err;
import org.basex.util.Array;
import org.basex.util.Map;
import org.basex.util.Token;
import org.basex.util.TokenList;
import org.basex.util.Tokenizer;

/**
 * Simple Thesaurus for full-text requests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Thesaurus {
  /** Thesaurus root references. */
  private final Map<ThesNode> nodes = new Map<ThesNode>();
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
        nodes = Array.extend(nodes);
        rs = Array.extend(rs);
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
   * @param ft tokenizer
   * @return success flag
   * @throws QueryException query exception
   */
  private boolean init(final Tokenizer ft) throws QueryException {
    try {
      final Data data = new MemBuilder().build(new DirParser(file), "");
      final Nodes result = nodes("//*:entry", new Nodes(0, data));
      for(int n = 0; n < result.size(); n++) {
        build(new Nodes(result.nodes[n], data), ft);
      }
    } catch(final IOException ex) {
      Err.or(NOTHES, file);
    }
    return true;
  }

  /**
   * Builds the thesaurus.
   * @param in input nodes
   * @param ft tokenizer
   * @throws QueryException exception
   */
  private void build(final Nodes in, final Tokenizer ft) throws QueryException {
    final Nodes sub = nodes("*:synonym", in);
    if(sub.size() == 0) return;

    final ThesNode node = getNode(ft.get(text("*:term", in)));
    for(int n = 0; n < sub.size(); n++) {
      final Nodes tmp = new Nodes(sub.nodes[n], sub.data);
      final ThesNode snode = getNode(ft.get(text("*:term", tmp)));
      final byte[] rs = text("*:relationship", tmp);
      node.add(snode, rs);

      final byte[] srs = RSHIPS.get(rs);
      if(srs != null) snode.add(node, srs);
      build(sub, ft);
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
      nodes.add(term, node);
    }
    return node;
  }
  
  /**
   * Performs a query and returns the result as nodes.
   * @param query query string
   * @param in input nodes
   * @return resulting nodes
   * @throws QueryException exception
   */
  private Nodes nodes(final String query, final Nodes in)
      throws QueryException {
    return new QueryProcessor(query, in).queryNodes();
  }

  /**
   * Performs a query and returns the first result as text.
   * @param query query string
   * @param in input nodes
   * @return resulting text
   * @throws QueryException exception
   */
  private byte[] text(final String query, final Nodes in)
      throws QueryException {
    return new QueryProcessor(query, in).iter().next().str();
  }
  
  /**
   * Finds a thesaurus term.
   * @param list result list
   * @param ft tokenizer
   * @throws QueryException query exception
   */
  void find(final TokenList list, final Tokenizer ft) throws QueryException {
    if(nodes.size() == 0) init(ft);
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
