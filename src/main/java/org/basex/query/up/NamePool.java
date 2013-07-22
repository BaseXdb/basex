package org.basex.query.up;

import static org.basex.util.Token.*;

import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * This class serves as a container for updated names.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class NamePool {
  /** Name cache. */
  private NameCache[] cache = new NameCache[1];
  /** Number of entries. */
  public int size;

  /**
   * Adds an entry to the pool and increases its number of occurrence.
   * @param name name
   * @param type node type
   */
  public void add(final QNm name, final Type type) {
    if(type != NodeType.ATT && type != NodeType.ELM) return;
    final int i = index(name, type == NodeType.ATT);
    cache[i].add++;
  }

  /**
   * Adds an entry to the pool and decreases its number of occurrence.
   * @param node node
   */
  public void remove(final ANode node) {
    if(node.type != NodeType.ATT && node.type != NodeType.ELM) return;
    final int i = index(node.qname(), node.type == NodeType.ATT);
    cache[i].del = true;
  }

  /**
   * Returns the name of a duplicate attribute, or {@code null}.
   * @return name of duplicate attribute
   */
  QNm duplicate() {
    // if node has been deleted, overall count for duplicates must be bigger 2
    for(int i = 0; i < size; ++i) {
      if(cache[i].attr && cache[i].add > (cache[i].del ? 2 : 1)) return cache[i].name;
    }
    return null;
  }

  /**
   * Checks if no namespace conflicts occur.
   * @return success flag
   */
  boolean nsOK() {
    final Atts at = new Atts();
    for(int i = 0; i < size; ++i) {
      if(cache[i].add <= (cache[i].del ? 1 : 0)) continue;
      final QNm nm = cache[i].name;
      final byte[] pref = nm.prefix();
      final byte[] uri = nm.uri();
      final byte[] u = at.value(pref);
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
      final NameCache nc = cache[i];
      if(nc.name.eq(name) && nc.attr == at) return i;
    }
    if(size == cache.length)
      cache = Array.copy(cache, new NameCache[Array.newSize(size)]);
    final NameCache nc = new NameCache();
    nc.name = name;
    nc.attr = at;
    cache[size] = nc;
    return size++;
  }

  /** Name cache. */
  static final class NameCache {
    /** Name. */
    QNm name;
    /** Attribute/element flag. */
    boolean attr;
    /** Counts the number of times the name is added. */
    int add;
    /** States if the name is deleted. */
    boolean del;
  }
}
