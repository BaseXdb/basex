package org.basex.query.up;

import static org.basex.util.Token.*;

import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * This class serves as a container for updated names.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class NamePool {
  /** Name cache. */
  private NameCache[] cache = new NameCache[1];
  /** Number of entries. */
  private int size;

  /**
   * Adds an entry to the pool and increases its number of occurrence.
   * @param name name
   * @param type node type
   */
  public void add(final QNm name, final NodeType type) {
    if(type != NodeType.ATTRIBUTE && type != NodeType.ELEMENT) return;
    final int i = index(name, type == NodeType.ATTRIBUTE);
    cache[i].add++;
  }

  /**
   * Adds an entry to the pool and decreases its number of occurrence.
   * @param node node
   */
  public void remove(final ANode node) {
    if(node.type != NodeType.ATTRIBUTE && node.type != NodeType.ELEMENT) return;
    final int i = index(node.qname(), node.type == NodeType.ATTRIBUTE);
    cache[i].del = true;
  }

  /**
   * Returns the name of a duplicate attribute.
   * @return name of duplicate attribute or {@code null}
   */
  QNm duplicate() {
    // if node has been deleted, overall count for duplicates must be bigger 2
    for(int i = 0; i < size; ++i) {
      final NameCache nc = cache[i];
      if(nc.attr && nc.add > (nc.del ? 2 : 1)) return nc.name;
    }
    return null;
  }

  /**
   * Checks if no namespace conflicts occur.
   * @return conflicting namespaces or {@code null}
   */
  byte[][] nsOK() {
    final Atts at = new Atts();
    for(int i = 0; i < size; ++i) {
      final NameCache nc = cache[i];
      if(nc.add <= (nc.del ? 1 : 0)) continue;
      final QNm nm = nc.name;
      final byte[] pref = nm.prefix();
      final byte[] uri = nm.uri();
      // attributes with empty URI don't conflict with anything
      if(nc.attr && uri.length == 0) continue;
      final byte[] u = at.value(pref);
      if(u == null) at.add(pref, uri);
      // check if only one uri is assigned to a prefix
      else if(!eq(uri, u)) return new byte[][] { uri, u };
    }
    return null;
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
      cache = Array.copy(cache, new NameCache[Array.newCapacity(size)]);
    final NameCache nc = new NameCache();
    nc.name = name;
    nc.attr = at;
    cache[size] = nc;
    return size++;
  }

  /** Name cache. */
  private static final class NameCache {
    /** Name. */
    private QNm name;
    /** Attribute/element flag. */
    private boolean attr;
    /** Counts the number of times the name is added. */
    private int add;
    /** States if the name is deleted. */
    private boolean del;
  }
}
