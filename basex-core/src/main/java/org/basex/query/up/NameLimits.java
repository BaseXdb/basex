package org.basex.query.up;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.name.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Collects the names, namespaces, nodes and texts that are added to a database by updates.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class NameLimits {
  /** Target database. */
  private final Data data;
  /** Data instances whose names have been collected. */
  private final Set<Data> sources = Collections.newSetFromMap(new IdentityHashMap<>());
  /** New element names. */
  private final TokenSet elems = new TokenSet();
  /** New attribute names. */
  private final TokenSet attrs = new TokenSet();
  /** New namespace URIs. */
  private final TokenSet uris = new TokenSet();
  /** Number of new nodes. */
  private long nodes;
  /** Maximum number of bytes of new texts. */
  private long texts;
  /** Maximum number of bytes of new attribute values. */
  private long values;

  /**
   * Constructor.
   * @param data target database
   */
  public NameLimits(final Data data) {
    this.data = data;
  }

  /**
   * Adds the nodes, names and namespaces of the specified data clip.
   * @param clip data clip
   */
  public void add(final DataClip clip) {
    final Data source = clip.data;
    nodes += clip.size();
    final boolean added = sources.add(source);
    if(source.inMemory()) {
      for(int pre = clip.start; pre < clip.end; pre++) {
        final int kind = source.kind(pre);
        if(kind != Data.ELEM) add(source.textLen(pre, kind != Data.ATTR), kind != Data.ATTR);
      }
    } else if(added) {
      // the clip of a disk-based instance comprises the complete database
      texts += source.heapSize(true);
      values += source.heapSize(false);
    }
    if(!added) return;

    add(source.elemNames, elems, data.elemNames);
    add(source.attrNames, attrs, data.attrNames);
    final Namespaces nspaces = source.nspaces;
    final int ns = nspaces.size();
    for(int n = 1; n <= ns; n++) add(nspaces.uri(n));
  }

  /**
   * Adds a text or attribute value.
   * @param length length of the value
   * @param text text or attribute value flag
   */
  public void add(final int length, final boolean text) {
    final int bytes = length + Num.length(length);
    if(text) texts += bytes;
    else values += bytes;
  }

  /**
   * Adds a name and its namespace.
   * @param name name
   * @param elem element flag
   */
  public void add(final QNm name, final boolean elem) {
    final byte[] nm = name.string();
    if(elem) {
      if(data.elemNames.index(nm) == 0) elems.add(nm);
    } else {
      if(data.attrNames.index(nm) == 0) attrs.add(nm);
    }
    if(name.hasURI()) add(name.uri());
  }

  /**
   * Checks if the updates exceed the limits of the database.
   * @throws QueryException query exception
   */
  public void check() throws QueryException {
    // see Builder#addElem
    limit(data.elemNames.size() + elems.size(), 0x8000, "distinct element names");
    limit(data.attrNames.size() + attrs.size(), 0x8000, "distinct attribute names");
    limit(data.nspaces.size() + uris.size(), 0x100, "distinct namespaces");
    if(data.nodes() + nodes >= Integer.MAX_VALUE) {
      throw UPDBERROR_X.get(null, "Update would exceed database node limit.");
    }
    // offsets of texts and attribute values must not overlap with the flags of text references
    if(data.heapSize(true) + texts >= Compress.COMPRESS ||
        data.heapSize(false) + values >= Compress.COMPRESS) {
      throw UPDBERROR_X.get(null, "Update would exceed database text limit.");
    }
  }

  /**
   * Adds a namespace URI.
   * @param uri namespace URI
   */
  private void add(final byte[] uri) {
    if(data.nspaces.uriId(uri) == 0) uris.add(uri);
  }

  /**
   * Adds the names of a source that do not exist in the target.
   * @param source source names
   * @param names new names
   * @param target target names
   */
  private static void add(final Names source, final TokenSet names, final Names target) {
    final int ns = source.size();
    for(int n = 1; n <= ns; n++) {
      final byte[] name = source.key(n);
      if(target.index(name) == 0) names.add(name);
    }
  }

  /**
   * Raises an error if a limit is exceeded.
   * @param value value
   * @param limit limit
   * @param message error message
   * @throws QueryException query exception
   */
  private static void limit(final int value, final int limit, final String message)
      throws QueryException {
    if(value >= limit) throw BASEX_LIMIT_X_X.get(null, message, limit);
  }
}
