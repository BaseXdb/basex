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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class MemData extends Data {
  /**
   * Constructor.
   * @param ps path summary
   * @param ns namespaces
   * @param opts database options
   */
  public MemData(final PathSummary ps, final Namespaces ns, final Options opts) {
    this(null, null, ps, ns, opts, null, null);
  }

  /**
   * Constructor.
   * @param tag tag index
   * @param att attribute name index
   * @param ps path summary
   * @param ns namespaces
   * @param opts database options
   * @param txt text index
   * @param atv attribute value index
   */
  public MemData(final Names tag, final Names att, final PathSummary ps,
      final Namespaces ns, final Options opts, final Index txt, final Index atv) {

    meta = new MetaData(opts);
    table = new TableMemAccess(meta);
    if(meta.updindex) {
      idmap = new IdPreMap(meta.lastid);
      txtindex = txt == null ? new UpdatableMemValues(this) : txt;
      atvindex = atv == null ? new UpdatableMemValues(this) : atv;
    } else {
      txtindex = txt == null ? new MemValues(this) : txt;
      atvindex = atv == null ? new MemValues(this) : atv;
    }
    tagindex = tag == null ? new Names(meta) : tag;
    atnindex = att == null ? new Names(meta) : att;
    paths = ps == null ? new PathSummary(this) : ps;
    nspaces = ns == null ? new Namespaces() : ns;
  }

  /**
   * Light-weight constructor, adopting data structures from the specified database.
   * @param data data reference
   */
  public MemData(final Data data) {
    this(data.tagindex, data.atnindex, data.paths, null, data.meta.options,
        data.txtindex, data.atvindex);
  }

  /**
   * Constructor, creating a new, empty database.
   * @param opts database options
   */
  public MemData(final Options opts) {
    this(null, null, opts);
  }

  @Override
  public void close() { }

  @Override
  public void closeIndex(final IndexType type) { }

  @Override
  public void setIndex(final IndexType type, final Index index) { }

  @Override
  public boolean startUpdate() { return true; }

  @Override
  public void finishUpdate() { }

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
  protected void delete(final int pre, final boolean text) { }

  @Override
  public void updateText(final int pre, final byte[] val, final int kind) {
    final int id = id(pre);
    if(meta.updindex) {
      final boolean txt = kind != ATTR;
      ((MemValues) (txt ? txtindex : atvindex)).delete(text(pre, txt), id);
    }
    textOff(pre, index(pre, id, val, kind));
  }

  @Override
  protected long index(final int pre, final int id, final byte[] txt, final int kind) {
    return ((MemValues) (kind == ATTR ? atvindex : txtindex)).
        put(txt, meta.updindex ? id : pre);
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
