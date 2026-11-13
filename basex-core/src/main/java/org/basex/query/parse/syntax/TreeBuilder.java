package org.basex.query.parse.syntax;

import java.util.*;

import org.basex.query.parse.*;
import org.basex.query.parse.gen.*;
import org.basex.query.parse.gen.XQuerySyntaxParser.*;

/**
 * Builds the syntax tree from the bottom-up events of the generated parser. Each stack entry
 * stands for one reported symbol, or for several: adjacent content characters are merged into the
 * entry of the first one, which then has a weight greater than one. The child counts of the events
 * refer to reported symbols, so they are resolved by adding up the weights of the entries. The
 * reductions of merged characters leave the tree unchanged and are skipped. The nodes of the
 * nonterminals are created by {@link NodeFactory}. The tree is shaped as defined by
 * {@link FoldPolicy}:
 * <ul>
 * <li>Whitespace and the end marker are dropped, and delimiters are skipped. Their entries are
 *   placeholders, so that the child counts of the events remain valid.</li>
 * <li>A nonterminal whose children are all dropped vanishes. If some were skipped delimiters,
 *   the node stays without children, as for {@code ()}, unless the nonterminal is listed in
 *   {@link FoldPolicy#VANISH}.</li>
 * <li>A nonterminal that is listed in {@link NodeFactory#FOLDS} is replaced by its only child.
 *   This applies to the nonterminals of {@link FoldPolicy#NAMES}, and to all that always have
 *   exactly one child.</li>
 * <li>NCName, QName and UnreservedFunctionQName are wrappers around a single token. They are
 *   collapsed into a terminal named NCName or QName.</li>
 * </ul>
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
final class TreeBuilder implements BottomUpEventHandler {
  /** Terminals that are merged if they are adjacent. */
  private static final BitSet MERGE = Sym.set(
      Sym.ELEMENT_CONTENT_CHAR, Sym.QUOT_ATTR_CONTENT_CHAR, Sym.APOS_ATTR_CONTENT_CHAR);
  /** Nonterminals that are removed if only removed delimiters are left. */
  private static final BitSet VANISH = nonterminals(FoldPolicy.VANISH);
  /** Left-recursive lists. */
  private static final BitSet LISTS = nonterminals(FoldPolicy.LISTS);
  /** Delimiters that are skipped. */
  private static final BitSet DELIMITERS = terminals(FoldPolicy.DELIMITERS);
  /** Delimiters that are removed below some nonterminals, by nonterminal. */
  private static final BitSet[] PARENT_DELIMITERS = parentDelimiters();

  /** Flag of terminals: whitespace and the end marker are dropped. */
  private static final byte DROP = 1;
  /** Flag of terminals: delimiters are skipped. */
  private static final byte DELIMITER = 2;
  /** Flag of terminals: adjacent characters are merged. */
  private static final byte CONTENT = 4;
  /** Flags of terminals, by symbol. */
  private static final byte[] TERMINALS = new byte[Sym.SYMBOL_COUNT];
  /** Flag of nonterminals: replaced by the only child. */
  private static final byte FOLD = 1;
  /** Flag of nonterminals: left-recursive list. */
  private static final byte LIST = 2;
  /** Flag of nonterminals: removed if only removed delimiters are left. */
  private static final byte REMOVE = 4;
  /** Flags of nonterminals, by symbol. */
  private static final byte[] NONTERMINALS = new byte[Sym.SYMBOL_COUNT];

  static {
    TERMINALS[Sym.S] = DROP;
    TERMINALS[Sym.EOF] = DROP;
    for(int sym = DELIMITERS.nextSetBit(0); sym >= 0; sym = DELIMITERS.nextSetBit(sym + 1)) {
      TERMINALS[sym] = DELIMITER;
    }
    for(int sym = MERGE.nextSetBit(0); sym >= 0; sym = MERGE.nextSetBit(sym + 1)) {
      TERMINALS[sym] = CONTENT;
    }
    for(int sym = NodeFactory.FOLDS.nextSetBit(0); sym >= 0;
        sym = NodeFactory.FOLDS.nextSetBit(sym + 1)) {
      NONTERMINALS[sym] |= FOLD;
    }
    for(int sym = LISTS.nextSetBit(0); sym >= 0; sym = LISTS.nextSetBit(sym + 1)) {
      NONTERMINALS[sym] |= LIST;
    }
    for(int sym = VANISH.nextSetBit(0); sym >= 0; sym = VANISH.nextSetBit(sym + 1)) {
      NONTERMINALS[sym] |= REMOVE;
    }
  }

  /** Input. */
  private final int[] input;
  /** Nodes and placeholders. */
  private Node[] nodes = new Node[64];
  /** Symbols. */
  private int[] syms = new int[64];
  /** Stack size. */
  private int top;
  /** Gaps between tokens: pairs of begin and end. */
  private int[] gaps = new int[16];
  /** Number of entries in the gaps. */
  private int gapSize;
  /** End of the last token. */
  private int lastEnd;
  /** Last terminal that may be extended by adjacent content characters (can be {@code null}). */
  private Terminal merging;
  /** Number of reported symbols that each stack entry stands for. */
  private int[] weights = new int[64];
  /** Number of merged characters on top of the stack, whose reductions are still to come. */
  private int pending;

  /**
   * Constructor.
   * @param input input that is passed to the parser
   */
  TreeBuilder(final int[] input) {
    this.input = input;
  }

  @Override
  public void reset(final int[] string) {
    top = 0;
    gapSize = 0;
    lastEnd = 0;
    merging = null;
    pending = 0;
  }

  @Override
  public void ambiguity(final int begin, final int end) {
    // the parser has resolved the ambiguity: it selects the alternative with the fewest nodes
  }

  @Override
  public void terminal(final int id, final int begin, final int end) {
    // tokens of the grammar are adjacent, except for whitespace and comments
    if(begin > lastEnd) {
      if(gapSize == gaps.length) gaps = Arrays.copyOf(gaps, gapSize << 1);
      gaps[gapSize++] = lastEnd;
      gaps[gapSize++] = begin;
    }
    lastEnd = end;
    final int kind = Sym.ofTerminal(id);
    final byte flags = TERMINALS[kind];
    if(flags == DROP) {
      push(Marker.DROPPED, kind);
    } else if(flags == DELIMITER) {
      push(Marker.SKIPPED, kind);
    } else {
      // merge with the directly preceding symbol: its entry stands for both symbols
      final boolean merge = flags == CONTENT;
      if(merge && merging != null && nodes[top - 1] == merging && merging.sym == kind &&
          merging.end == begin) {
        merging.end = end;
        weights[top - 1]++;
        pending++;
        return;
      }
      final Terminal terminal = new Terminal(kind, begin, end);
      push(terminal, kind);
      if(merge) merging = terminal;
    }
  }

  @Override
  public void nonterminal(final int id, final int begin, final int end, final int count) {
    // the reduction of a merged character does not change the tree
    if(pending > 0 && count == 1) {
      assert NodeFactory.FOLDS.get(Sym.ofNonterminal(id)) :
        "Merged character in " + Sym.symbol(Sym.ofNonterminal(id));
      pending--;
      return;
    }
    pending = 0;
    // fast path: a node that is replaced by its only child, which is retained as is
    if(count == 1 && top > 0 && weights[top - 1] == 1) {
      final int s = Sym.ofNonterminal(id);
      final int k = s == Sym.UnreservedFunctionEQName ? Sym.EQName : s;
      if(NONTERMINALS[k] == FOLD && PARENT_DELIMITERS[k] == null && collapsed(k) == -1) {
        final Node node = nodes[top - 1];
        if(!(node instanceof Marker || node instanceof ListAcc)) {
          syms[top - 1] = k;
          return;
        }
      }
    }
    int from = top;
    for(int remaining = count; remaining > 0;) {
      if(from == 0) {
        throw new IllegalStateException("Too few symbols for " +
            Sym.symbol(Sym.ofNonterminal(id)) + ".");
      }
      remaining -= weights[--from];
    }
    final int entries = top - from;
    // UnreservedFunctionEQName is a technical variant of EQName
    final int sym = Sym.ofNonterminal(id);
    final int kind = sym == Sym.UnreservedFunctionEQName ? Sym.EQName : sym;
    final byte flags = NONTERMINALS[kind];
    final BitSet delimiters = PARENT_DELIMITERS[kind];
    final boolean list = (flags & LIST) != 0;
    int retained = 0;
    Node only = null;
    boolean skipped = false;
    for(int i = from; i < top; i++) {
      Node node = nodes[i];
      if(delimiters != null && delimiters.get(syms[i])) node = nodes[i] = Marker.SKIPPED;
      if(!list && node instanceof final ListAcc acc) node = nodes[i] = acc.finish();
      if(node == Marker.SKIPPED) {
        skipped = true;
      } else if(node != Marker.DROPPED) {
        retained++;
        only = node;
      }
    }
    top = from;
    if(entries > 0 && retained == 0 && (!skipped || (flags & REMOVE) != 0)) {
      push(skipped ? Marker.SKIPPED : Marker.DROPPED, kind);
      return;
    }
    if(list) {
      // a list is extended by each reduction
      ListAcc acc = null;
      for(int i = from; i < from + entries; i++) {
        final Node node = nodes[i];
        if(node instanceof final ListAcc previous) {
          acc = previous;
          acc.end(end);
        }
      }
      if(acc == null) acc = new ListAcc(begin);
      for(int i = from; i < from + entries; i++) {
        final Node node = nodes[i];
        if(node != acc && !(node instanceof Marker)) acc.add(node, end);
      }
      push(acc, kind);
      return;
    }
    final int terminal = collapsed(kind);
    if(retained == 1 && terminal != -1 && only instanceof final Terminal t &&
        t.begin == begin && t.end == end) {
      // the child becomes a terminal named after its parent
      push(new Terminal(terminal, begin, end), kind);
    } else if(retained == 1 && (flags & FOLD) != 0) {
      push(only, kind);
    } else {
      push(NodeFactory.create(kind, begin, end, nodes, syms, from, from + entries), kind);
    }
  }

  /**
   * Pushes a node.
   * @param node node or placeholder
   * @param sym symbol
   */
  private void push(final Node node, final int sym) {
    if(top == nodes.length) {
      nodes = Arrays.copyOf(nodes, top << 1);
      syms = Arrays.copyOf(syms, top << 1);
      weights = Arrays.copyOf(weights, top << 1);
    }
    weights[top] = 1;
    pending = 0;
    nodes[top] = node;
    syms[top++] = sym;
  }

  /**
   * Returns the delimiters that are removed below some nonterminals.
   * @return delimiters by nonterminal
   */
  private static BitSet[] parentDelimiters() {
    final BitSet[] delimiters = new BitSet[Sym.SYMBOL_COUNT];
    FoldPolicy.PARENT_DELIMITERS.forEach((parent, texts) ->
      delimiters[Sym.nonterminal(parent)] = terminals(texts));
    return delimiters;
  }

  /**
   * Returns the nonterminals with the given names.
   * @param names names
   * @return nonterminals
   */
  private static BitSet nonterminals(final Set<String> names) {
    final BitSet kinds = new BitSet(Sym.SYMBOL_COUNT);
    for(final String name : names) kinds.set(Sym.nonterminal(name));
    return kinds;
  }

  /**
   * Returns the terminals with the given texts. The grammar names of literal tokens are quoted,
   * with quotes in the text doubled.
   * @param texts texts
   * @return terminals
   */
  private static BitSet terminals(final Set<String> texts) {
    final BitSet kinds = new BitSet(Sym.SYMBOL_COUNT);
    for(final String text : texts) {
      kinds.set(Sym.terminal("'" + text.replace("'", "''") + "'"));
    }
    return kinds;
  }

  /**
   * Returns the terminal that a name wrapper collapses to.
   * @param kind nonterminal
   * @return terminal, or {@code -1} if the nonterminal is no name wrapper
   */
  private static int collapsed(final int kind) {
    return switch(kind) {
      case Sym.NCName -> Sym.NC_NAME;
      case Sym.QName, Sym.UnreservedFunctionQName -> Sym.Q_NAME;
      default -> -1;
    };
  }

  /**
   * Returns the tree.
   * @return tree
   * @throws IllegalStateException if the events do not form a single tree
   */
  SyntaxTree finish() {
    if(top != 1 || nodes[0] instanceof Marker) {
      throw new IllegalStateException("Events do not form a single tree.");
    }
    return new SyntaxTree(input, nodes[0], Arrays.copyOf(gaps, gapSize));
  }
}
