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
  /** Position references of database nodes. */
  private final IdentityHashMap<Data, Entries> entries = new IdentityHashMap<>();
  /** Data reference to which the entries are restricted (can be {@code null}). */
  private final Data dt;
  /** Language of the tokenized input (can be {@code null}). */
  private Language lang;

  /**
   * Constructor.
   */
  public FTPosData() {
    this(null);
  }

  /**
   * Creates a new instance of this class.
   * @param data data reference to which the entries are restricted (can be {@code null})
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
  public synchronized void add(final Data data, final int pre, final FTMatches all,
      final Language language) {
    if(dt != null && dt != data) return;
    lang = language;
    entries.computeIfAbsent(data, d -> new Entries()).add(pre, positions(all));
  }

  /**
   * Adds position data for a constructed node.
   * @param node node
   * @param all full-text matches
   * @param language language that was used for tokenizing the input (can be {@code null})
   */
  public synchronized void add(final XNode node, final FTMatches all, final Language language) {
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
        for(int s = sm.start; s <= sm.end; s++) set.add(s);
      }
    }
    return new IntList(set.keys()).sort();
  }

  /**
   * Gets full-text data from the container.
   * If no data is stored for a PRE value, {@code null} is returned.
   * @param data data reference
   * @param pre int PRE value
   * @return full-text data or {@code null}
   */
  public FTPos get(final Data data, final int pre) {
    final Entries ent = entries.get(data);
    if(ent == null) return null;
    final int p = ent.find(pre);
    return p < 0 ? null : ent.pos[p];
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
      final Entries ent = entries.get(data);
      if(ent == null) return 0;
      // entries are sorted by PRE value: check the ones inside the node
      final int pre = dbnode.pre(), last = pre + data.size(pre, dbnode.dbKind());
      final int p = ent.find(pre);
      for(int i = p < 0 ? -p - 1 : p; i < ent.size && ent.pos[i].pre < last; i++) {
        count += ent.pos[i].size();
      }
    } else if(!fragments.isEmpty()) {
      for(final GNode nd : node.descendantIter(true)) {
        final FTPos ftpos = nd instanceof final XNode xnode ? fragments.get(xnode) : null;
        if(ftpos != null) count += ftpos.size();
      }
    }
    return count;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof final FTPosData ft) || !fragments.equals(ft.fragments) ||
        !entries.keySet().equals(ft.entries.keySet())) return false;
    for(final Map.Entry<Data, Entries> entry : entries.entrySet()) {
      if(!entry.getValue().equals(ft.entries.get(entry.getKey()))) return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Entries ent : entries.values()) {
      for(int p = 0; p < ent.size; p++) {
        if(!sb.isEmpty()) sb.append('\n');
        sb.append(ent.pos[p]);
      }
    }
    return sb.toString();
  }

  /**
   * Position references of the nodes of a database, sorted by PRE values.
   */
  private static final class Entries {
    /** Position references. */
    private FTPos[] pos = new FTPos[1];
    /** Number of values. */
    private int size;

    /**
     * Adds positions.
     * @param pre PRE value
     * @param il sorted positions
     */
    void add(final int pre, final IntList il) {
      int c = find(pre);
      if(c < 0) {
        c = -c - 1;
        if(size == pos.length) pos = Arrays.copyOf(pos, Array.newCapacity(size));
        Array.insert(pos, c, 1, size, null);
        pos[c] = new FTPos(pre, il);
        size++;
      } else {
        pos[c].union(il);
      }
    }

    /**
     * Returns the index of the specified PRE value.
     * @param pre int PRE value
     * @return index, or negative index -1 if PRE value is not found
     */
    int find(final int pre) {
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
      if(!(obj instanceof final Entries ent) || size != ent.size) return false;
      for(int p = 0; p < size; p++) {
        if(pos[p].pre != ent.pos[p].pre || !pos[p].equals(ent.pos[p])) return false;
      }
      return true;
    }
  }
}
