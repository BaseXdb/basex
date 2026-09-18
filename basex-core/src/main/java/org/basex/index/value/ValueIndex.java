package org.basex.index.value;

import static org.basex.util.Token.*;

import java.util.function.*;

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
   * Flushes the buffered data.
   * @param close database is closed
   */
  public abstract void flush(boolean close);

  /**
   * Deletes the entries of the specified nodes, called before the nodes are removed from the table.
   * @param pre PRE value of the first node
   * @param size number of nodes
   */
  @SuppressWarnings("unused")
  public void delete(final int pre, final int size) { }

  /**
   * Adds the entries of the specified nodes, called after the nodes have been inserted into the
   * table.
   * @param pre PRE value of the first node
   * @param size number of nodes
   */
  @SuppressWarnings("unused")
  public void insert(final int pre, final int size) { }

  /**
   * Deletes the entries affected by a rename, called before the name is changed.
   * @param pre PRE value of the node to be renamed
   * @param kind node kind
   */
  @SuppressWarnings("unused")
  public void rename(final int pre, final int kind) { }

  /**
   * Adds the entries affected by a rename, called after the name has been changed.
   * @param pre PRE value of the renamed node
   * @param kind node kind
   */
  @SuppressWarnings("unused")
  public void renamed(final int pre, final int kind) { }

  /**
   * Finishes an update transaction.
   */
  public void finishUpdate() { }

  /**
   * Optimizes the index structure.
   */
  public void optimize() { }

  /**
   * Returns the units whose inclusion depends on the name of a node: the child text nodes of an
   * element (text and full-text index), an attribute (attribute and token index), or an element
   * (full-text index for mixed content).
   * @param pre PRE value of the renamed node
   * @param kind node kind
   * @return PRE values of the units
   */
  protected final IntList renamedUnits(final int pre, final int kind) {
    final int unit = IndexNames.kind(type, data.meta);
    if(unit == Data.TEXT) return kind == Data.ELEM ? childTexts(pre) : new IntList(0);
    return kind == unit ? new IntList(1).add(pre) : new IntList(0);
  }

  /**
   * Passes the keys of a node and their positions to a consumer; values exceeding the maximum
   * length are skipped.
   * @param data data reference
   * @param type index type
   * @param pre PRE value
   * @param consumer consumer of keys and positions
   */
  static void keys(final Data data, final IndexType type, final int pre,
      final ObjIntConsumer<byte[]> consumer) {
    final boolean text = type == IndexType.TEXT;
    if(type == IndexType.TOKEN) {
      int pos = 0;
      for(final byte[] token : distinctTokens(data.text(pre, false))) consumer.accept(token, pos++);
    } else if(data.textLen(pre, text) <= data.meta.maxlen) {
      consumer.accept(data.text(pre, text), 0);
    }
  }

  /**
   * Returns the PRE values of the child text nodes of an element.
   * @param pre PRE value of the element
   * @return PRE values
   */
  private IntList childTexts(final int pre) {
    final IntList pres = new IntList();
    final int last = pre + data.size(pre, Data.ELEM);
    for(int curr = pre + data.attSize(pre, Data.ELEM); curr < last;) {
      final int kind = data.kind(curr);
      if(kind == Data.TEXT) pres.add(curr);
      curr += data.size(curr, kind);
    }
    return pres;
  }
}
