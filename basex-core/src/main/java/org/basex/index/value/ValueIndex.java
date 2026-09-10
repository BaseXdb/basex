package org.basex.index.value;

import java.io.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.util.list.*;

/**
 * Index for texts, attribute values and full-texts.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class ValueIndex implements Index {
  /** Index type. */
  protected final IndexType type;
  /** Data instance. */
  protected final Data data;

  /**
   * Constructor, initializing the index structure.
   * @param data data reference
   * @param type index type
   */
  protected ValueIndex(final Data data, final IndexType type) {
    this.data = data;
    this.type = type;
  }

  /**
   * Returns the number of index entries.
   * @return number of index entries
   */
  public abstract int size();

  /**
   * Deletes entries from the index.
   * @param values value cache with [key, id-list] pairs
   */
  public abstract void delete(ValueCache values);

  /**
   * Add entries to the index.
   * @param values value cache with [key, id-list] pairs
   */
  public abstract void add(ValueCache values);

  /**
   * Flushes the buffered data.
   */
  public abstract void flush();

  /**
   * Deletes the entries of the specified nodes, called before the nodes are removed from the table.
   * @param pre PRE value of the first node
   * @param size number of nodes
   */
  public void delete(final int pre, final int size) {
    update(new ValueCache(pre, size, type, data), false);
  }

  /**
   * Adds the entries of the specified nodes, called after the nodes have been inserted into the table.
   * @param pre PRE value of the first node
   * @param size number of nodes
   */
  public void insert(final int pre, final int size) {
    update(new ValueCache(pre, size, type, data), true);
  }

  /**
   * Deletes the entries affected by a rename, called before the name is changed.
   * @param pre PRE value of the node to be renamed
   * @param kind node kind
   */
  public void rename(final int pre, final int kind) {
    update(renameCache(pre, kind), false);
  }

  /**
   * Adds the entries affected by a rename, called after the name has been changed.
   * @param pre PRE value of the renamed node
   * @param kind node kind
   */
  public void renamed(final int pre, final int kind) {
    update(renameCache(pre, kind), true);
  }

  /**
   * Finishes an update transaction.
   */
  public void finishUpdate() { }

  /**
   * Optimizes the index structure.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  public void optimize() throws IOException { }

  /**
   * Adds or deletes the specified entries.
   * @param values value cache (can be {@code null})
   * @param add add or delete entries
   */
  private void update(final ValueCache values, final boolean add) {
    if(values == null || values.isEmpty()) return;
    if(add) add(values);
    else delete(values);
  }

  /**
   * Returns the entries affected by a rename: the inclusion of text nodes depends on the name of
   * the parent element, that of attributes on their own name.
   * @param pre PRE value of the renamed node
   * @param kind node kind
   * @return value cache, or {@code null} if no entries are affected
   */
  private ValueCache renameCache(final int pre, final int kind) {
    final boolean text = type == IndexType.TEXT;
    if(kind == Data.ATTR) return text ? null : new ValueCache(pre, type, data);
    if(kind != Data.ELEM || !text) return null;
    // collect child text nodes
    final IntList pres = new IntList();
    final int last = pre + data.size(pre, kind);
    for(int curr = pre + data.attSize(pre, kind); curr < last;) {
      final int k = data.kind(curr);
      if(k == Data.TEXT) pres.add(curr);
      curr += data.size(curr, k);
    }
    return new ValueCache(pres, type, data);
  }
}
