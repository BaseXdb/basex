package org.basex.data;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.index.*;
import org.basex.index.name.*;
import org.basex.index.path.*;
import org.basex.index.value.*;
import org.basex.io.random.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class stores and organizes the database table and the index structures
 * for textual content in a compressed memory structure.
 * The table mapping is documented in {@link Data}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MemData extends Data {
  /** Texts. */
  private final TokenSet texts;
  /** Attribute Values. */
  private final TokenSet values;

  /**
   * Constructor.
   * @param paths path index
   * @param nspaces namespaces
   * @param options main options
   */
  public MemData(final PathIndex paths, final Namespaces nspaces, final MainOptions options) {
    this(null, null, paths, nspaces, null, null, options);
  }

  /**
   * Constructor for creating a new, empty database.
   * @param options main options
   */
  public MemData(final MainOptions options) {
    this(null, null, options);
  }

  /**
   * Constructor for building a new database.
   * @param elemNames element name index
   * @param attrNames attribute name index
   * @param paths path index
   * @param nspaces namespaces
   * @param texts texts
   * @param values values
   * @param options main options
   */
  private MemData(final Names elemNames, final Names attrNames, final PathIndex paths,
      final Namespaces nspaces, final TokenSet texts, final TokenSet values,
      final MainOptions options) {

    super(new MetaData(options));
    table = new TableMemAccess(meta);
    if(meta.updindex) idmap = new IdPreMap(meta.lastid);
    this.texts = texts == null ? new TokenSet() : texts;
    this.values = values == null ? new TokenSet() : values;
    this.elemNames = elemNames == null ? new Names(meta) : elemNames;
    this.attrNames = attrNames == null ? new Names(meta) : attrNames;
    this.paths = paths == null ? new PathIndex(this) : paths;
    this.nspaces = nspaces == null ? new Namespaces() : nspaces;
  }

  @Override
  public void createIndex(final IndexType type, final Command cmd) throws IOException {
    final IndexBuilder ib = switch(type) {
      case TEXT, ATTRIBUTE, TOKEN -> new MemValuesBuilder(this, type);
      case FULLTEXT               -> throw new BaseXException(NO_MAINMEM);
      default                     -> throw Util.notExpected();
    };
    try {
      if(cmd != null) cmd.pushJob(ib);
      set(type, ib.build());
    } finally {
      if(cmd != null) cmd.popJob();
    }
  }

  @Override
  public void dropIndex(final IndexType type) throws BaseXException {
    switch(type) {
      case TEXT, ATTRIBUTE, TOKEN: break;
      case FULLTEXT:               throw new BaseXException(NO_MAINMEM);
      default:                     throw Util.notExpected();
    }
    set(type, null);
  }

  /**
   * Assigns the specified index.
   * @param type index to be opened
   * @param index index instance
   */
  private void set(final IndexType type, final ValueIndex index) {
    meta.dirty = true;
    switch(type) {
      case TEXT -> textIndex = index;
      case ATTRIBUTE -> attrIndex = index;
      case TOKEN -> tokenIndex = index;
      case FULLTEXT -> ftIndex = index;
      default -> throw Util.notExpected();
    }
  }

  @Override
  public void startUpdate(final MainOptions opts) { }

  @Override
  public void finishUpdate(final MainOptions opts) { }

  @Override
  public void flush(final boolean all) { }

  @Override
  public byte[] text(final int pre, final boolean text) {
    return (text ? texts : values).key((int) textRef(pre));
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

  @Override
  public boolean inMemory() {
    return true;
  }

  /**
   * Returns the string values of the database.
   * @param text text/attribute flag
   * @return set
   */
  public TokenSet values(final boolean text) {
    return text ? texts : values;
  }

  // UPDATE OPERATIONS ============================================================================

  @Override
  protected void delete(final int pre, final boolean text) { }

  @Override
  protected void updateText(final int pre, final byte[] value, final int kind) {
    indexDelete(pre, -1, 1);
    textRef(pre, textRef(value, kind != ATTR));
    indexAdd(pre, -1, 1, null);
  }

  @Override
  protected long textRef(final byte[] value, final boolean text) {
    return (text ? texts : values).put(value);
  }
}
