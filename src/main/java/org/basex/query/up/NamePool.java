package org.basex.query.up;

import static org.basex.util.Token.*;
import java.util.Arrays;
import org.basex.query.item.ANode;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.util.Atts;

/**
 * This class serves as a container for updated names.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class NamePool {
  /** Names. */
  private QNm[] names = new QNm[1];
  /** Attribute/element flag. */
  private boolean[] attr = new boolean[1];
  /** Number of occurrences. */
  private int[] occ = new int[1];
  /** Number of entries. */
  private int size;

  /**
   * Adds an entry to the pool and increases its number of occurrence.
   * @param name name
   * @param type node type
   */
  public void add(final QNm name, final Type type) {
    if(type != NodeType.ATT && type != NodeType.ELM) return;
    final int i = index(name, type == NodeType.ATT);
    occ[i]++;
  }

  /**
   * Adds an entry to the pool and decreases its number of occurrence.
   * @param node node
   */
  public void remove(final ANode node) {
    if(node.type != NodeType.ATT && node.type != NodeType.ELM) return;
    final int i = index(node.qname(), node.type == NodeType.ATT);
    occ[i]--;
  }

  /**
   * Finds duplicate attributes.
   * @return duplicate attribute, or {@code null}
   */
  QNm duplicate() {
    for(int i = 0; i < size; ++i) if(occ[i] > 1) return names[i];
    return null;
  }

  /**
   * Checks if no namespace conflicts occur.
   * @return success flag
   */
  boolean nsOK() {
    final Atts at = new Atts();
    for(int i = 0; i < size; ++i) {
      if(occ[i] <= 0) continue;
      final QNm nm = names[i];
      final byte[] pref = nm.prefix();
      final byte[] uri = nm.uri();
      final byte[] u = at.string(pref);
      if(u == null) at.add(pref, uri);
      // check if only one uri is assigned to a prefix
      else if(!eq(uri, u)) return false;
    }
    return true;
  }

  /**
   * Returns an index to an existing entry.
   * @param name name to be found
   * @param at attribute/element flag
   * @return index offset, or -1
   */
  private int index(final QNm name, final boolean at) {
    for(int i = 0; i < size; ++i) {
      if(names[i].eq(name) && attr[i] == at) return i;
    }
    if(size == names.length) {
      names = Arrays.copyOf(names, size << 1);
      attr = Arrays.copyOf(attr, size << 1);
      occ = Arrays.copyOf(occ, size << 1);
    }
    names[size] = name;
    attr[size] = at;
    return size++;
  }
}
