package org.basex.data;

import org.basex.core.Prop;
import org.basex.index.Index;
import org.basex.index.IndexToken.IndexType;
import org.basex.index.MemValues;
import org.basex.index.Names;
import org.basex.io.TableMemAccess;
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

    meta = new MetaData("", pr);
    table = new TableMemAccess(meta, null, 16);
    txtindex = new MemValues();
    atvindex = new MemValues();
    tags = tag;
    atts = att;
    ns = n;
    pthindex = s;
  }

  /**
   * Constructor, adopting meta data from the specified database.
   * @param data data reference
   */
  public MemData(final Data data) {
    this(data.tags, data.atts, new Namespaces(), data.pthindex, data.meta.prop);
  }

  /**
   * Constructor, creating a new, empty database.
   * @param pr property reference
   */
  public MemData(final Prop pr) {
    this(new Names(0), new Names(0), new Namespaces(), new PathSummary(), pr);
  }

  @Override
  public void flush() { }

  @Override
  protected void cls() { }

  @Override
  public void closeIndex(final IndexType index) { }

  @Override
  public void setIndex(final IndexType type, final Index ind) { }

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
  public void text(final int pre, final byte[] val, final boolean txt) {
    textOff(pre, index(val, meta.size, txt));
  }

  @Override
  protected long index(final byte[] txt, final int pre, final boolean text) {
    return ((MemValues) (text ? txtindex : atvindex)).index(txt, pre);
  }
}
