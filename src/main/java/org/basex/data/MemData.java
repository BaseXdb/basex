package org.basex.data;

import org.basex.core.Prop;
import org.basex.index.IdPreMap;
import org.basex.index.Index;
import org.basex.index.IndexToken.IndexType;
import org.basex.index.path.PathSummary;
import org.basex.index.value.MemValues;
import org.basex.index.Names;
import org.basex.io.random.TableMemAccess;
import org.basex.util.Token;

/**
 * This class stores and organizes the database table and the index structures
 * for textual content in a compressed memory structure.
 * The table mapping is documented in {@link Data}.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class MemData extends Data {
  /**
   * Constructor.
   * @param tag tag index
   * @param att attribute name index
   * @param n namespaces
   * @param s path summary
   * @param pr database properties
   */
  public MemData(final Names tag, final Names att, final Namespaces n,
      final PathSummary s, final Prop pr) {

    meta = new MetaData(pr);
    if(meta.updindex) idmap = new IdPreMap(meta.lastid);
    table = new TableMemAccess(meta);
    txtindex = new MemValues(this);
    atvindex = new MemValues(this);
    tagindex = tag;
    atnindex = att;
    ns = n;
    pthindex = s == null ? new PathSummary(this, 0) : s;
  }

  /**
   * Constructor, adopting data structures from the specified database.
   * @param data data reference
   */
  public MemData(final Data data) {
    this(data.tagindex, data.atnindex, new Namespaces(),
         data.pthindex, data.meta.prop);
  }

  /**
   * Constructor, creating a new, empty database.
   * @param pr property reference
   */
  public MemData(final Prop pr) {
    this(new Names(0), new Names(0), new Namespaces(), null, pr);
  }

  @Override
  public void flush() { }

  @Override
  public void close() { }

  @Override
  public void closeIndex(final IndexType type) { }

  @Override
  public void setIndex(final IndexType type, final Index index) { }

  @Override
  public boolean lock() {
    return true;
  }

  @Override
  public boolean unlock() {
    return true;
  }

  @Override
  public byte[] text(final int pre, final boolean text) {
    return ((MemValues) (text ? txtindex : atvindex)).key((int) textOff(pre));
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
  protected void delete(final int pre, final boolean text) {
  }

  @Override
  public void updateText(final int pre, final byte[] val, final int kind) {
    final boolean txt = kind != ATTR;
    final int id = id(pre);
    if(meta.updindex) {
      ((MemValues) (txt ? txtindex : atvindex)).delete(text(pre, txt), id);
    }
    textOff(pre, index(id, val, kind));
  }

  @Override
  protected long index(final int id, final byte[] txt, final int kind) {
    return ((MemValues) (kind == ATTR ? atvindex : txtindex)).index(txt, id);
  }

  @Override
  protected void indexDelete(final int pre, final int size) {
    final int l = pre + size;
    for(int p = pre; p < l; ++p) {
      final int k = kind(p);
      final boolean isAttr = k == ATTR;
      // skip nodes which are not attribute, text, comment, or proc. instruction
      if(isAttr || k == TEXT || k == COMM || k == PI) {
        final byte[] key = text(p, !isAttr);
        ((MemValues) (isAttr ? atvindex : txtindex)).delete(key, id(p));
      }
    }
  }
}
