package org.basex.query.util.ft;

import java.util.*;

import org.basex.data.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class provides a container for query full-text positions,
 * which is evaluated by the visualizations.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class FTPosData {
  /** Position references of constructed nodes. */
  private final IdentityHashMap<XNode, FTPos> fragments = new IdentityHashMap<>();
  /** Position references. */
  private FTPos[] pos = new FTPos[1];
  /** Data reference (can be {@code null}). */
  private Data dt;
  /** Language of the tokenized input (can be {@code null}). */
  private Language lang;
  /** Number of values. */
  private int size;

  /**
   * Constructor.
   */
  public FTPosData() {
  }

  /**
   * Creates a new instance of this class.
   * @param data data reference
   */
  public FTPosData(final Data data) {
    dt = data;
  }

  /**
   * Adds position data.
   * @param data data reference
   * @param pre PRE value
   * @param all full-text matches
   * @param language language that was used for tokenizing the input (can be {@code null})
   */
  public void add(final Data data, final int pre, final FTMatches all, final Language language) {
    if(dt == null) dt = data;
    else if(dt != data) return;
    lang = language;

    final IntList il = positions(all);
    int c = find(pre);
    if(c < 0) {
      c = -c - 1;
      final int sz = size;
      if(sz == pos.length) pos = Arrays.copyOf(pos, Array.newCapacity(sz));
      Array.insert(pos, c, 1, sz, null);
      pos[c] = new FTPos(pre, il);
      size++;
    } else {
      pos[c].union(il);
    }
  }

  /**
   * Adds position data for a constructed node.
   * @param node node
   * @param all full-text matches
   * @param language language that was used for tokenizing the input (can be {@code null})
   */
  public void add(final XNode node, final FTMatches all, final Language language) {
    lang = language;
    final IntList il = positions(all);
    final FTPos ftpos = fragments.get(node);
    if(ftpos != null) ftpos.union(il);
    else fragments.put(node, new FTPos(-1, il));
  }

  /**
   * Returns the language that was used for tokenizing the input.
   * @return language (can be {@code null})
   */
  public Language language() {
    return lang;
  }

  /**
   * Returns the sorted positions of the specified matches.
   * @param all full-text matches
   * @return positions
   */
  private static IntList positions(final FTMatches all) {
    final IntSet set = new IntSet();
    for(final FTMatch ftm : all) {
      for(final FTStringMatch sm : ftm) {
        for(int s = sm.start; s <= sm.end; ++s) set.add(s);
      }
    }
    return new IntList(set.keys()).sort();
  }

  /**
   * Gets full-text data from the container.
   * If no data is stored for a PRE value, {@code null} is returned.
   * int[0] : [pos0, ..., posn]
   * int[1] : [poi0, ..., poin]
   * @param data data reference
   * @param pre int PRE value
   * @return int[2][n] full-text data or {@code null}
   */
  public FTPos get(final Data data, final int pre) {
    final int p = find(pre);
    return p < 0 || dt != data ? null : pos[p];
  }

  /**
   * Gets full-text data of a constructed node.
   * @param node node
   * @return full-text data or {@code null}
   */
  public FTPos get(final XNode node) {
    return fragments.get(node);
  }

  /**
   * Returns the number of positions that were assigned to a node or one of its descendants.
   * Positions are assigned to the nodes that are tested by a full-text expression; these can be
   * the text nodes or the elements below the returned node.
   * @param node node
   * @return number of positions
   */
  public int size(final XNode node) {
    int count = 0;
    if(node instanceof final DBNode dbnode) {
      final Data data = dbnode.data();
      if(dt != data) return 0;
      // entries are sorted by PRE value: check the ones inside the node
      final int pre = dbnode.pre(), last = pre + data.size(pre, dbnode.dbKind());
      final int p = find(pre);
      for(int i = p < 0 ? -p - 1 : p; i < size && pos[i].pre < last; i++) {
        count += pos[i].size();
      }
    } else if(!fragments.isEmpty()) {
      for(final GNode nd : node.descendantIter(true)) {
        final FTPos ftpos = nd instanceof final XNode xnode ? fragments.get(xnode) : null;
        if(ftpos != null) count += ftpos.size();
      }
    }
    return count;
  }

  /**
   * Returns the index of the specified PRE value.
   * @param pre int PRE value
   * @return index, or negative index -1 if PRE value is not found
   */
  private int find(final int pre) {
    // binary search
    int l = 0, h = size - 1;
    while(l <= h) {
      final int m = l + h >>> 1;
      final int c = pos[m].pre - pre;
      if(c == 0) return m;
      if(c < 0) l = m + 1;
      else h = m - 1;
    }
    return -l - 1;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof final FTPosData ft)) return false;
    if(size != ft.size || !fragments.equals(ft.fragments)) return false;
    for(int p = 0; p < size; p++) {
      if(pos[p].pre != ft.pos[p].pre || !pos[p].equals(ft.pos[p])) return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(int p = 0; p < size; p++) {
      if(!sb.isEmpty()) sb.append('\n');
      sb.append(pos[p]);
    }
    return sb.toString();
  }
}
