package org.basex.data;

import org.basex.core.*;
import org.basex.index.*;
import org.basex.index.name.*;
import org.basex.index.path.*;
import org.basex.index.value.*;
import org.basex.io.random.*;
import org.basex.util.*;

/**
 * This class stores and organizes the database table and the index structures
 * for textual content in a compressed memory structure.
 * The table mapping is documented in {@link Data}.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class MemData extends Data {
  /**
   * Constructor.
   * @param paths path summary
   * @param nspaces namespaces
   * @param opts database options
   */
  public MemData(final PathSummary paths, final Namespaces nspaces, final MainOptions opts) {
    this(null, null, paths, nspaces, opts, null, null);
  }

  /**
   * Constructor.
   * @param elmindex element name index
   * @param atnindex attribute name index
   * @param paths path summary
   * @param nspaces namespaces
   * @param options database options
   * @param txtindex text index
   * @param atvindex attribute value index
   */
  private MemData(final Names elmindex, final Names atnindex, final PathSummary paths,
      final Namespaces nspaces, final MainOptions options, final Index txtindex,
      final Index atvindex) {

    meta = new MetaData(options);
    table = new TableMemAccess(meta);
    final boolean up = meta.updindex;
    if(up) idmap = new IdPreMap(meta.lastid);
    this.textIndex = txtindex == null ? new MemValues(this, up) : txtindex;
    this.attrIndex = atvindex == null ? new MemValues(this, up) : atvindex;
    this.elemNames = elmindex == null ? new Names(meta) : elmindex;
    this.attrNames = atnindex == null ? new Names(meta) : atnindex;
    this.paths = paths == null ? new PathSummary(this) : paths;
    this.nspaces = nspaces == null ? new Namespaces() : nspaces;
  }

  /**
   * Light-weight constructor, adopting data structures from the specified database.
   * @param data data reference
   */
  public MemData(final Data data) {
    this(data.elemNames, data.attrNames, data.paths, null, data.meta.options, data.textIndex,
        data.attrIndex);
  }

  /**
   * Constructor, creating a new, empty database.
   * @param opts database options
   */
  public MemData(final MainOptions opts) {
    this(null, null, opts);
  }

  /**
   * Finishes build process.
   */
  public void finish() {
    values(true).finish();
    values(false).finish();
  }

  @Override
  public void close() { }

  @Override
  public void createIndex(final IndexType type, final Command cmd) {
    values(type == IndexType.TEXT).create(type);
  }

  @Override
  public boolean dropIndex(final IndexType type) {
    return values(type == IndexType.TEXT).drop();
  }

  @Override
  public void startUpdate() { }

  @Override
  public void finishUpdate() { }

  @Override
  public void flush(final boolean all) { }

  @Override
  public byte[] text(final int pre, final boolean text) {
    return values(text).key((int) textOff(pre));
  }

  @Override
  public long textItr(final int pre, final boolean text) {
    return Token.toLong(text(pre, text));
  }

  @Override
  public double textDbl(final int pre, final boolean text) {
    return Token.toDouble(text(pre, text));
  }

  @Override
  public int textLen(final int pre, final boolean text) {
    return text(pre, text).length;
  }

  // UPDATE OPERATIONS ========================================================

  @Override
  protected void delete(final int pre, final boolean text) { }

  @Override
  public void updateText(final int pre, final byte[] value, final int kind) {
    final int id = id(pre);
    if(meta.updindex) {
      final boolean text = kind != ATTR;
      values(text).delete(text(pre, text), id);
    }
    textOff(pre, index(pre, id, value, kind));
  }

  @Override
  protected long index(final int pre, final int id, final byte[] txt, final int kind) {
    return values(kind != ATTR).put(txt, meta.updindex ? id : pre);
  }

  @Override
  protected void indexDelete(final int pre, final int size) {
    final boolean textI = meta.textindex, attrI = meta.attrindex;
    if(textI || attrI) {
      final int l = pre + size;
      for(int p = pre; p < l; ++p) {
        final int k = kind(p);
        // consider nodes which are attribute, text, comment, or proc. instruction
        final boolean text = k == TEXT || k == COMM || k == PI;
        if(text || k == ATTR) values(text).delete(text(p, text), id(p));
      }
    }
  }

  @Override
  public boolean inMemory() {
    return true;
  }

  /**
   * Returns the specified value index.
   * @param text text index
   * @return index
   */
  private MemValues values(final boolean text) {
    return (MemValues) (text ? textIndex : attrIndex);
  }
}
