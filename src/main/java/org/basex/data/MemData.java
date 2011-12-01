package org.basex.data;

import org.basex.core.Prop;
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
    table = new TableMemAccess(meta);
    txtindex = new MemValues();
    atvindex = new MemValues();
    tagindex = tag;
    atnindex = att;
    ns = n;
    pthindex = s == null ? new PathSummary(this) : s;
  }

  /**
   * Constructor, adopting meta data from the specified database.
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
  public void text(final int pre, final byte[] val, final boolean txt) {
    textOff(pre, index(meta.size, val, txt));
  }

  @Override
  protected long index(final int pre, final byte[] txt, final boolean text) {
    return ((MemValues) (text ? txtindex : atvindex)).index(txt, pre);
  }
}
