package org.basex.data;

import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.basex.core.*;
import org.basex.index.*;
import org.basex.index.name.*;
import org.basex.index.path.*;
import org.basex.index.query.*;
import org.basex.index.resource.*;
import org.basex.index.value.*;
import org.basex.io.*;
import org.basex.io.random.*;
import org.basex.query.util.index.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class represents a database instance. It provides low-level access to all
 * properties and values stored in a single database.
 *
 * An XML node is accessed by its {@code pre} value, which is not stored itself, but
 * given by the table position. The following restrictions are imposed on the data:
 * <ul>
 * <li>The table is limited to 2^31 entries (pre values are signed int's)</li>
 * <li>A maximum of 2^15 different element and attribute names is allowed</li>
 * <li>A maximum of 2^8 different namespaces is allowed</li>
 * </ul>
 * Each node occupies 128 bits. The current storage layout looks as follows:
 *
 * <pre>
 * COMMON ATTRIBUTES:
 * - Byte     0:  KIND: Node kind (bits: 2-0)
 * - Byte 12-15:  UNID: Unique Node ID
 * DOCUMENT NODES (kind = 0):
 * - Byte  3- 7:  TEXT: Text reference
 * - Byte  8-11:  SIZE: Number of descendants
 * ELEMENT NODES (kind = 1):
 * - Byte     0:  ATTS: Number of attributes (bits: 7-3).
 *                      Calculated in real-time if bit range is too small
 * - Byte  1- 2:  NAME: Namespace Flag (bit: 15), Name (bits: 14-0)
 * - Byte     3:  NURI: Namespace URI
 * - Byte  4- 7:  DIST: Distance to parent node
 * - Byte  8-11:  SIZE: Number of descendants
 * TEXT, COMMENT, PI NODES (kind = 2, 4, 5):
 * - Byte  3- 7:  TEXT: Text reference
 * - Byte  8-11:  DIST: Distance to parent node
 * ATTRIBUTE NODES (kind = 3):
 * - Byte     0:  DIST: Distance to parent node (bits: 7-3)
 *                      Calculated in real-time if bit range is too small
 * - Byte  1- 2:  NAME: Namespace Flag (bit: 15), Name (bits: 14-0)
 * - Byte  3- 7:  TEXT: Attribute value reference
 * - Byte    11:  NURI: Namespace (bits: 7-3)
 * </pre>
 *
 * As all methods of this class are optimized for performance, no checks are
 * performed on the arguments (e.g.: if the string value of a text node is
 * requested, the specified pre value must point to a text node).
 *
 * NOTE: the class is not thread-safe. It is imperative that all read/write accesses
 * are synchronized over a single context's read/write lock.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class Data {
  /** Node kind: document (code: {@code 0}). */
  public static final byte DOC = 0x00;
  /** Node kind: element (code: {@code 1}). */
  public static final byte ELEM = 0x01;
  /** Node kind: text (code: {@code 2}). */
  public static final byte TEXT = 0x02;
  /** Node kind: attribute (code: {@code 3}). */
  public static final byte ATTR = 0x03;
  /** Node kind: comment (code: {@code 4}). */
  public static final byte COMM = 0x04;
  /** Node kind: processing instruction (code: {@code 5}). */
  public static final byte PI = 0x05;

  /** Static node counter. */
  private static final AtomicInteger ID = new AtomicInteger();
  /** Unique id. ID can get negative, as subtraction of ids is used for all comparisons. */
  public final int dbid = ID.incrementAndGet();

  /** Resource index. */
  public final Resources resources = new Resources(this);
  /** Meta data. */
  public final MetaData meta;

  /** Element names. */
  public Names elemNames;
  /** Attribute names. */
  public Names attrNames;
  /** Namespace index. */
  public Namespaces nspaces;
  /** Path index. */
  public PathIndex paths;
  /** Text index. */
  public ValueIndex textIndex;
  /** Attribute value index. */
  public ValueIndex attrIndex;
  /** Token index. */
  public ValueIndex tokenIndex;
  /** Full-text index. */
  public ValueIndex ftIndex;

  /** Indicates if distances are to be updated. */
  public boolean updateDists = true;
  /** ID-PRE mapping. */
  public IdPreMap idmap;

  /** Table access file. */
  protected TableAccess table;
  /** Closed flag. */
  protected boolean closed;

  /**
   * Default constructor.
   * @param meta meta data
   */
  protected Data(final MetaData meta) {
    this.meta = meta;
  }

  /**
   * Closes the database.
   */
  public void close() {
    closed = true;
  }

  /**
   * Indicates if the database has been closed.
   * @return result of check
   */
  public final boolean closed() {
    return closed;
  }

  /**
   * Drops the specified index.
   * @param type index to be dropped
   * @param cmd calling command
   * @throws IOException I/O exception
   */
  public abstract void createIndex(IndexType type, Command cmd) throws IOException;

  /**
   * Drops the specified index.
   * @param type index to be dropped
   * @throws BaseXException database exception
   */
  public abstract void dropIndex(IndexType type) throws BaseXException;

  /**
   * Starts an update operation: writes a file to disk to indicate that an update is going on,
   * and exclusively locks the table file.
   * @param opts main options
   * @throws BaseXException database exception
   */
  public abstract void startUpdate(MainOptions opts) throws BaseXException;

  /**
   * Finishes an update operation: removes the update file and the exclusive lock.
   * @param opts main options
   */
  public abstract void finishUpdate(MainOptions opts);

  /**
   * Flushes updated data.
   * @param all flush all data
   */
  public abstract void flush(boolean all);

  /**
   * Returns an index iterator for the specified token.
   * @param search index search definition
   * @return index iterator
   */
  public final IndexIterator iter(final IndexSearch search) {
    return index(search.type()).iter(search);
  }

  /**
   * Returns a cost estimation for searching the specified token.
   * Smaller values are better, a value of zero indicates that no results will be returned.
   * @param search index search definition
   * @return cost estimation, or {@code null} if index access is not possible
   */
  public final IndexCosts costs(final IndexSearch search) {
    return index(search.type()).costs(search);
  }

  /**
   * Returns info on the specified index structure.
   * @param type index type
   * @param options main options
   * @return info
   */
  public final byte[] info(final IndexType type, final MainOptions options) {
    return index(type).info(options);
  }

  /**
   * Returns an index for the specified index type.
   * @param type index type
   * @return index
   */
  public final Index index(final IndexType type) {
    switch(type) {
      case ELEMNAME:  return elemNames;
      case ATTRNAME:  return attrNames;
      case TEXT:      return textIndex;
      case ATTRIBUTE: return attrIndex;
      case TOKEN:     return tokenIndex;
      case FULLTEXT:  return ftIndex;
      case PATH:      return paths;
      default:        throw Util.notExpected();
    }
  }

  /**
   * Returns an atomized content for any node kind.
   * The atomized value can be an attribute value or XML content.
   * @param pre pre value
   * @return atomized value
   */
  public final byte[] atom(final int pre) {
    switch(kind(pre)) {
      case TEXT: case COMM:
        return text(pre, true);
      case ATTR:
        return text(pre, false);
      case PI:
        byte[] txt = text(pre, true);
        final int i = indexOf(txt, ' ');
        return i == -1 ? EMPTY : substring(txt, i + 1);
      default:
        // create atomized text node
        TokenBuilder tb = null;
        byte[] t = EMPTY;
        int p = pre;
        final int s = p + size(p, kind(p));
        while(p < s) {
          final int k = kind(p);
          if(k == TEXT) {
            txt = text(p, true);
            if(t == EMPTY) {
              t = txt;
            } else {
              if(tb == null) tb = new TokenBuilder().add(t);
              tb.add(txt);
            }
          }
          p += attSize(p, k);
        }
        return tb == null ? t : tb.finish();
    }
  }

  /**
   * Returns the common default namespace of all documents of the database.
   * @return namespace, or {@code null} if there is no common namespace
   */
  public byte[] defaultNs() {
    return nspaces.defaultNs(meta.ndocs);
  }

  // RETRIEVING VALUES ============================================================================

  /**
   * Returns a pre value for the specified id.
   * @param id unique node id
   * @return pre value or {@code -1} if id was not found
   */
  public final int pre(final int id) {
    if(meta.updindex) return idmap.pre(id);

    // find pre value in the table; start with specified id
    final int size = meta.size;
    for(int p = Math.max(0, id); p < meta.size; ++p) {
      if(id == id(p)) return p;
    }
    final int ps = Math.min(size, id);
    for(int p = 0; p < ps; ++p) {
      if(id == id(p)) return p;
    }
    // id not found
    return -1;
  }

  /**
   * Returns a unique node id.
   * @param pre pre value
   * @return node id
   */
  public final int id(final int pre) {
    return table.read4(pre, 12);
  }

  /**
   * Returns the node kind, which may be {@link #DOC}, {@link #ELEM}, {@link #TEXT},
   * {@link #ATTR}, {@link #COMM} or {@link #PI}.
   * @param pre pre value
   * @return node kind
   */
  public final int kind(final int pre) {
    return table.read1(pre, 0) & 0x07;
  }

  /**
   * Returns a pre value of the parent node.
   * @param pre pre value
   * @param kind node kind
   * @return pre value of the parent node
   */
  public final int parent(final int pre, final int kind) {
    return pre - dist(pre, kind);
  }

  /**
   * Returns the distance of the specified node.
   * @param pre pre value
   * @param kind node kind
   * @return distance
   */
  public final int dist(final int pre, final int kind) {
    switch(kind) {
      case ELEM:
        return table.read4(pre, 4);
      case TEXT:
      case COMM:
      case PI:
        return table.read4(pre, 8);
      case ATTR:
        int d = table.read1(pre, 0) >> 3 & IO.MAXATTS;
        // skip additional attributes if value is larger than maximum range
        if(d >= IO.MAXATTS) while(d < pre && kind(pre - d) == ATTR) d++;
        return d;
      default:
        return pre + 1;
    }
  }

  /**
   * Returns a size value (number of descendant table entries).
   * @param pre pre value
   * @param kind node kind
   * @return size value
   */
  public final int size(final int pre, final int kind) {
    return kind == ELEM || kind == DOC ? table.read4(pre, 8) : 1;
  }

  /**
   * Returns a number of attributes.
   * @param pre pre value
   * @param kind node kind
   * @return number of attributes
   */
  public final int attSize(final int pre, final int kind) {
    int s = kind == ELEM ? table.read1(pre, 0) >> 3 & IO.MAXATTS : 1;
    // skip additional attributes if value is larger than maximum range
    if(s >= IO.MAXATTS) while(s < meta.size - pre && kind(pre + s) == ATTR) s++;
    return s;
  }

  /**
   * Finds the specified attribute and returns its value.
   * @param att the attribute id of the attribute to be found
   * @param pre pre value
   * @return attribute value or {@code null}
   */
  public final byte[] attValue(final int att, final int pre) {
    final int a = pre + attSize(pre, kind(pre));
    int p = pre;
    while(++p != a) {
      if(nameId(p) == att) return text(p, false);
    }
    return null;
  }

  /**
   * Returns the id of the name of an element, attribute or processing instruction.
   * @param pre pre value
   * @return name id
   */
  public final int nameId(final int pre) {
    return table.read2(pre, 1) & 0x7FFF;
  }

  /**
   * Returns the name of an element, attribute or processing instruction.
   * @param pre pre value
   * @param kind node kind
   * @return name
   */
  public final byte[] name(final int pre, final int kind) {
    if(kind == PI) {
      final byte[] name = text(pre, true);
      final int i = indexOf(name, ' ');
      return i == -1 ? name : substring(name, 0, i);
    }
    return (kind == ELEM ? elemNames : attrNames).key(nameId(pre));
  }

  /**
   * Returns the id of the namespace uri of the addressed element or attribute.
   * @param pre pre value
   * @param kind node kind
   * @return id of the namespace uri
   */
  public final int uriId(final int pre, final int kind) {
    return kind == ELEM || kind == ATTR ? table.read1(pre, kind == ELEM ? 3 : 11) & 0xFF : 0;
  }

  /**
   * Returns the name and namespace uri of the addressed element or attribute.
   * @param pre pre value
   * @param kind node kind
   * @return array with name and namespace uri
   */
  public final byte[][] qname(final int pre, final int kind) {
    final byte[] name = name(pre, kind);
    byte[] uri = null;
    final boolean hasPrefix = indexOf(name, ':') != -1;
    if(hasPrefix || !nspaces.isEmpty()) {
      final int uriId = uriId(pre, kind);
      if(uriId > 0) {
        uri = nspaces.uri(uriId);
      } else if(hasPrefix && eq(prefix(name), XML)) {
        uri = DataText.XML_URI;
      }
    }
    return new byte[][] { name, uri == null ? EMPTY : uri };
  }

  /**
   * Returns the namespace flag of the addressed element.
   * @param pre pre value
   * @return namespace flag
   */
  public final boolean nsFlag(final int pre) {
    return (table.read1(pre, 1) & 0x80) != 0;
  }

  /**
   * Returns all namespace prefixes and uris that are defined for the specified pre value.
   * Should only be called for element nodes.
   * @param pre pre value
   * @return key and value ids
   */
  public final Atts namespaces(final int pre) {
    return nsFlag(pre) ? nspaces.values(pre, this) : new Atts();
  }

  /**
   * Returns the reference to a text (text, comment, pi, pi, document) or attribute value.
   * @param pre pre value
   * @return disk offset
   */
  public final long textRef(final int pre) {
    return table.read5(pre, 3);
  }

  /**
   * Returns a text (text, comment, pi, document) or attribute value.
   * @param pre pre value
   * @param text text/attribute flag
   * @return atomized value
   */
  public abstract byte[] text(int pre, boolean text);

  /**
   * Returns a text (text, comment, pi, document) or attribute value as integer value.
   * {@link Long#MIN_VALUE} is returned if the input is no valid integer.
   * @param pre pre value
   * @param text text/attribute flag
   * @return numeric value
   */
  public abstract long textItr(int pre, boolean text);

  /**
   * Returns a text (text, comment, pi, document) or attribute value as double value.
   * {@link Double#NaN} is returned if the input is no valid double.
   * @param pre pre value
   * @param text text/attribute flag
   * @return numeric value
   */
  public abstract double textDbl(int pre, boolean text);

  /**
   * Returns the byte length of a (possibly compressed) text (text, comment, pi, document).
   * @param pre pre value
   * @param text text/attribute flag
   * @return length
   */
  public abstract int textLen(int pre, boolean text);

  // UPDATE OPERATIONS ============================================================================

  /**
   * Updates (renames) the name of an element, attribute or processing instruction.
   * @param pre pre value of the node to be updated
   * @param kind node kind of the updated node
   * @param name name of the new element, attribute or processing instruction
   * @param uri namespace uri
   */
  public final void update(final int pre, final int kind, final byte[] name, final byte[] uri) {
    meta.update();

    if(kind == PI) {
      updateText(pre, trim(concat(name, SPACE, atom(pre))), PI);
    } else {
      // check if namespace has changed
      final byte[] prefix = prefix(name);
      final int oldUriId = nspaces.uriIdForPrefix(prefix, pre, this);
      final boolean nsFlag = oldUriId == 0 && uri.length != 0 && !eq(prefix, XML);
      final int nsPre = kind == ATTR ? parent(pre, kind) : pre;
      final int uriId = nsFlag ? nspaces.add(nsPre, prefix, uri, this) :
        oldUriId != 0 && eq(nspaces.uri(oldUriId), uri) ? oldUriId : 0;
      final int size = size(pre, kind);

      // write ids of namespace uri and name, and namespace flag
      if(kind == ATTR) {
        // delete old values from attribute indexes
        if(meta.updindex) {
          if(meta.attrindex) attrIndex.delete(new ValueCache(pre, IndexType.ATTRIBUTE, this));
          if(meta.tokenindex) tokenIndex.delete(new ValueCache(pre, IndexType.TOKEN, this));
        }
        table.write1(pre, 11, uriId);
        table.write2(pre, 1, attrNames.put(name));
        if(nsFlag) table.write2(nsPre, 1, 1 << 15 | nameId(nsPre));
        // add new values to attribute indexes
        if(meta.updindex) {
          if(meta.attrindex) attrIndex.add(new ValueCache(pre, IndexType.ATTRIBUTE, this));
          if(meta.tokenindex) tokenIndex.add(new ValueCache(pre, IndexType.TOKEN, this));
        }

      } else {
        // update element name
        final IntList pres = new IntList();
        // update text index
        if(meta.updindex && meta.textindex) {
          final int last = pre + size;
          for(int curr = pre + attSize(pre, kind); curr < last; curr += size(curr, kind(curr))) {
            if(kind(curr) == TEXT) pres.add(curr);
          }
          textIndex.delete(new ValueCache(pres, IndexType.TEXT, this));
        }
        table.write1(pre, 3, uriId);
        final int nameId = elemNames.put(name);
        table.write2(nsPre, 1, (nsFlag || nsFlag(nsPre) ? 1 << 15 : 0) | nameId);
        if(!pres.isEmpty()) textIndex.add(new ValueCache(pres, IndexType.TEXT, this));
      }
    }
  }

  /**
   * Updates (replaces) the value of a single text, comment, pi, attribute or document node.
   * @param pre pre value of the node to be updated
   * @param kind node kind
   * @param value value to be updated (text, comment, pi, attribute, document name)
   */
  public final void update(final int pre, final int kind, final byte[] value) {
    final byte[] val = kind == PI ? trim(concat(name(pre, kind), SPACE, value)) : value;
    if(eq(val, text(pre, kind != ATTR))) return;

    meta.update();
    updateText(pre, val, kind);
    if(kind == DOC) resources.rename(pre, value);
  }

  /**
   * Rapid Replace implementation. Replaces parts of the database with the specified data instance.
   * @param pre pre value of the node to be replaced
   * @param source clip with source data
   */
  public final void replace(final int pre, final DataClip source) {
    meta.update();

    final int sCount = source.size();
    final int tKind = kind(pre);
    final int tSize = size(pre, tKind);
    final int tPar = parent(pre, tKind);
    bufferSize(sCount);

    // update index structures
    indexDelete(pre, id(pre), tSize);

    final Data sData = source.data;
    int sTopPre = source.start;
    for(int sPre = source.start; sPre < source.end; ++sPre) {
      // properties of the source node
      final int sKind = sData.kind(sPre);
      final int sSize = sData.size(sPre, sKind);
      final int sPar = sData.parent(sPre, sKind);
      final int cPre = pre + sPre - source.start;

      // calculate new distance value
      final int cDist;
      if(sPre == sTopPre) {
        // handle top level entry: calculate distance based on target database
        cDist = cPre - tPar;
        // calculate pre value of next top level entry
        sTopPre += sSize;
      } else {
        cDist = sPre - sPar;
      }

      switch(sKind) {
        case DOC:
          // add document
          doc(sSize, sData.text(sPre, true));
          ++meta.ndocs;
          break;
        case ELEM:
          // add element
          final byte[] en = sData.name(sPre, sKind);
          elem(cDist, elemNames.put(en), sData.attSize(sPre, sKind), sSize,
              nspaces.uriIdForPrefix(prefix(en), true), false);
          break;
        case TEXT:
        case COMM:
        case PI:
          // add text
          text(cDist, sData.text(sPre, true), sKind);
          break;
        case ATTR:
          // add attribute
          final byte[] an = sData.name(sPre, sKind);
          attr(cDist, attrNames.put(an), sData.text(sPre, false),
              nspaces.uriIdForPrefix(prefix(an), false));
          break;
      }
    }

    // replace table entries, reset buffer size
    table.replace(pre, buffer(), tSize);
    bufferSize(1);

    // if necessary, increase/decrease size of ancestors and adjust distances of siblings
    final int diff = sCount - tSize;
    if(diff != 0) {
      int p = tPar;
      while(p >= 0) {
        final int k = kind(p);
        size(p, k, size(p, k) + diff);
        p = parent(p, k);
      }
      updateDist(pre + sCount, diff);
    }

    // adjust attribute size of parent if attributes inserted. attribute size
    // of parent cannot be decreased via a replace expression.
    int sPre = source.start;
    if(sData.kind(sPre) == ATTR) {
      int d = 0;
      while(sPre < source.end && sData.kind(sPre++) == ATTR) d++;
      if(d > 1) attSize(tPar, kind(tPar), d + attSize(tPar, ELEM) - 1);
    }

    // add entries to index structures
    indexAdd(pre, meta.lastid - sCount + 1, sCount, source);
  }

  /**
   * Deletes a node and its descendants.
   * @param pre pre value of the node to be deleted
   */
  public final void delete(final int pre) {
    meta.update();

    // delete references in document index
    int kind = kind(pre);
    final int size = size(pre, kind);

    // delete entries in value indexes
    indexDelete(pre, id(pre), size);

    /// delete textual values
    if(kind != DOC && kind != ELEM) delete(pre, kind != ATTR);

    // reduce size of ancestors
    int par = pre;
    // check if we are an attribute (different size counters)
    if(kind == ATTR) {
      par = parent(par, ATTR);
      attSize(par, ELEM, attSize(par, ELEM) - 1);
      size(par, ELEM, size(par, ELEM) - 1);
      kind = kind(par);
    }

    // delete namespace nodes and propagate pre value shifts (before node sizes are touched!)
    nspaces.delete(pre, size, this);

    // reduce size of ancestors
    while(par > 0 && kind != DOC) {
      par = parent(par, kind);
      kind = kind(par);
      size(par, kind, size(par, kind) - size);
    }

    // preserve empty root node
    if(kind(pre) == DOC) --meta.ndocs;

    // delete node from table structure and reduce document size
    table.delete(pre, size);

    updateDist(pre, -size);
  }

  /**
   * Inserts standalone attributes (without root element).
   * @param pre target pre value (insertion position)
   * @param par target parent pre value of node
   * @param source clip with source data
   */
  public final void insertAttr(final int pre, final int par, final DataClip source) {
    // #1168/2: store one by one (otherwise, namespace declarations may be added more than once)
    for(int s = 0; s < source.fragments; s++) {
      final int start = source.start + s;
      insert(pre + s, par, new DataClip(source.data, start, start + 1));
    }
    attSize(par, ELEM, attSize(par, ELEM) + source.size());
  }

  /**
   * Inserts a data instance at the specified pre value. Notes:
   * <ul>
   *   <li> The data instance in the specified data clip must differ from this instance.</li>
   *   <li> Attributes must be inserted via {@link #insertAttr}.</li>
   * </ul>
   * @param pre target pre value (insertion position)
   * @param par target parent pre value of node ({@code -1} if document is added)
   * @param source clip with source data
   */
  public final void insert(final int pre, final int par, final DataClip source) {
    final int sCount = source.size();
    if(sCount == 0) return;

    meta.update();
    resources.docs();

    // resize buffer to cache more entries
    final int bSize = Math.min(sCount, IO.BLOCKSIZE >> IO.NODEPOWER);
    bufferSize(bSize);

    // organize namespaces to avoid duplicate declarations
    final NSScope nsScope = new NSScope(pre, this);

    // indicates if database only contains a dummy node
    final Data sdata = source.data;
    int c = 0, sTopPre = source.start;
    for(int sPre = sTopPre; sPre < source.end; ++sPre, ++c) {
      if(c != 0 && c % bSize == 0) insert(pre + c - bSize);

      // values of source node
      final int sKind = sdata.kind(sPre);
      final int sSize = sdata.size(sPre, sKind);
      final int sPar = sdata.parent(sPre, sKind);

      // pre and dist value of new node
      final int nPre = pre + c, nDist;
      if(sPre == sTopPre) {
        // handle top level entry: calculate distance based on target database
        nDist = nPre - par;
        // calculate pre value of next top level entry
        sTopPre += sSize;
      } else {
        // handle descendant node: calculate distance based on source database
        nDist = sPre - sPar;
      }
      // documents: use -1 as namespace root
      final int nsPre = sKind == DOC ? -1 : nPre - nDist;
      nsScope.loop(nsPre, c);

      switch(sKind) {
        case DOC:
          // add document
          nsScope.open(nPre);
          doc(sSize, sdata.text(sPre, true));
          ++meta.ndocs;
          break;
        case ELEM: {
          // add element.
          final boolean nsFlag = nsScope.open(nPre, sdata.namespaces(sPre));
          final byte[] name = sdata.name(sPre, sKind);
          elem(nDist, elemNames.put(name), sdata.attSize(sPre, sKind), sSize,
              nspaces.uriIdForPrefix(prefix(name), true), nsFlag);
          break;
        }
        case TEXT:
        case COMM:
        case PI:
          // add text, comment or processing instruction
          text(nDist, sdata.text(sPre, true), sKind);
          break;
        case ATTR:
          // add attribute
          final byte[] name = sdata.name(sPre, sKind);
          int uriId = sdata.uriId(sPre, sKind);
          // extend namespace scope and write namespace flag if attribute has a new namespaces
          if(uriId != 0) {
            final byte[] prefix = prefix(name), uri = sdata.nspaces.uri(uriId);
            uriId = nspaces.uriIdForPrefix(prefix, false);
            if(uriId == 0 && !eq(prefix, XML)) {
              uriId = nspaces.add(nsPre, prefix, uri, this);
              table.write2(nsPre, 1, 1 << 15 | nameId(nsPre));
            }
          }
          attr(nDist, attrNames.put(name), sdata.text(sPre, false), uriId);
      }
      nsScope.shift(1);
    }
    // finalize and update namespace structure
    nsScope.close();

    // write final entries and reset buffer
    if(bufferPos != 0) insert(pre + c - 1 - (c - 1) % bSize);
    bufferSize(1);

    // increase size of ancestors
    int cPre = par;
    while(cPre >= 0) {
      final int cKind = kind(cPre);
      size(cPre, cKind, size(cPre, cKind) + sCount);
      cPre = parent(cPre, cKind);
    }

    // update index structures
    indexAdd(pre, id(pre), sCount, source);

    // finally, update distances
    updateDist(pre + sCount, sCount);
  }

  /**
   * This method updates the distance values of the specified pre value
   * and the following siblings of all ancestor-or-self nodes.
   * @param pre root node
   * @param size size to be added/removed
   */
  private void updateDist(final int pre, final int size) {
    if(updateDists) {
      int p = pre;
      while(p < meta.size) {
        final int k = kind(p);
        if(k == DOC) break;
        dist(p, k, dist(p, k) + size);
        p += size(p, k);
      }
    }
  }

  /**
   * Sets the node id.
   * @param pre pre value
   * @param value value to be stored
   */
  public final void id(final int pre, final int value) {
    table.write4(pre, 12, value);
  }

  /**
   * Sets the size value.
   * @param pre pre value
   * @param kind node kind
   * @param value value to be stored
   */
  public final void size(final int pre, final int kind, final int value) {
    if(kind == ELEM || kind == DOC) table.write4(pre, 8, value);
  }

  /**
   * Sets the reference to a text/attribute value.
   * @param pre pre value
   * @param off offset
   */
  protected final void textRef(final int pre, final long off) {
    table.write5(pre, 3, off);
  }

  /**
   * Updates the specified text or attribute value.
   * @param pre pre value
   * @param value content
   * @param kind node kind
   */
  protected abstract void updateText(int pre, byte[] value, int kind);

  /**
   * Sets the distance.
   * @param pre pre value
   * @param kind node kind
   * @param value value
   */
  public final void dist(final int pre, final int kind, final int value) {
    if(kind == ATTR) table.write1(pre, 0, value << 3 | ATTR);
    else if(kind != DOC) table.write4(pre, kind == ELEM ? 4 : 8, value);
  }

  /**
   * Sets the attribute size.
   * @param pre pre value
   * @param kind node kind
   * @param value value
   */
  private void attSize(final int pre, final int kind, final int value) {
    // the magic value 31 is used to signal that there are 31 or more attributes
    if(kind == ELEM) table.write1(pre, 0, Math.min(value, IO.MAXATTS) << 3 | ELEM);
  }

  /**
   * Sets the namespace flag.
   * Should be only called for element nodes.
   * @param pre pre value
   * @param nsFlag namespace flag
   */
  public final void nsFlag(final int pre, final boolean nsFlag) {
    table.write1(pre, 1, table.read1(pre, 1) & 0x7F | (nsFlag ? 0x80 : 0));
  }

  /**
   * Inserts the internal buffer to the storage without updating the table structure.
   * @param pre insert position
   */
  public final void insert(final int pre) {
    table.insert(pre, buffer());
  }

  /**
   * Deletes the specified text entry.
   * @param pre pre value
   * @param text text (text, comment or pi) or attribute flag
   */
  protected abstract void delete(int pre, boolean text);

  // INSERTS WITHOUT TABLE UPDATES ================================================================

  /** Buffer for caching new table entries. */
  private byte[] buffer = new byte[IO.NODESIZE];
  /** Buffer position. */
  private int bufferPos;

  /**
   * Sets the update buffer to a new size.
   * @param size number of table entries
   */
  private void bufferSize(final int size) {
    final int bs = size << IO.NODEPOWER;
    if(buffer.length != bs) buffer = new byte[bs];
  }

  /**
   * Adds a document to the internal update buffer.
   * @param size node size
   * @param value document name
   */
  public final void doc(final int size, final byte[] value) {
    final int id = newID();
    final long v = textRef(value, true);
    s(DOC); s(0); s(0); s(v >> 32);
    s(v >> 24); s(v >> 16); s(v >> 8); s(v);
    s(size >> 24); s(size >> 16); s(size >> 8); s(size);
    s(id >> 24); s(id >> 16); s(id >> 8); s(id);
  }

  /**
   * Adds an element to the internal update buffer.
   * @param dist parent distance
   * @param nameId id of element name
   * @param asize number of attributes
   * @param size node size
   * @param uriId id of namespace uri
   * @param nsFlag namespace flag
   */
  public final void elem(final int dist, final int nameId, final int asize, final int size,
      final int uriId, final boolean nsFlag) {

    // build and insert new entry
    final int id = newID();
    final int n = nsFlag ? 1 << 7 : 0;
    s(Math.min(IO.MAXATTS, asize) << 3 | ELEM);
    s(n | (byte) (nameId >> 8)); s(nameId); s(uriId);
    s(dist >> 24); s(dist >> 16); s(dist >> 8); s(dist);
    s(size >> 24); s(size >> 16); s(size >> 8); s(size);
    s(id >> 24); s(id >> 16); s(id >> 8); s(id);
  }

  /**
   * Adds a text, comment or processing instruction to the internal update buffer.
   * @param dist parent distance
   * @param value string value
   * @param kind node kind
   */
  public final void text(final int dist, final byte[] value, final int kind) {
    // build and insert new entry
    final int id = newID();
    final long v = textRef(value, true);
    s(kind); s(0); s(0); s(v >> 32);
    s(v >> 24); s(v >> 16); s(v >> 8); s(v);
    s(dist >> 24); s(dist >> 16); s(dist >> 8); s(dist);
    s(id >> 24); s(id >> 16); s(id >> 8); s(id);
  }

  /**
   * Adds an attribute to the internal update buffer.
   * @param dist parent distance
   * @param nameId id of attribute name
   * @param value attribute value
   * @param uriId id of namespace uri
   */
  public final void attr(final int dist, final int nameId, final byte[] value, final int uriId) {
    // add attribute to text storage
    final int id = newID();
    final long v = textRef(value, false);
    s(Math.min(IO.MAXATTS, dist) << 3 | ATTR);
    s(nameId >> 8); s(nameId); s(v >> 32);
    s(v >> 24); s(v >> 16); s(v >> 8); s(v);
    s(0); s(0); s(0); s(uriId);
    s(id >> 24); s(id >> 16); s(id >> 8); s(id);
  }

  /**
   * Stores the specified value in the update buffer.
   * @param value value to be stored
   */
  private void s(final int value) {
    buffer[bufferPos++] = (byte) value;
  }

  /**
   * Generates a new id.
   * @return id
   */
  private int newID() {
    return ++meta.lastid;
  }

  /**
   * Stores the specified value in the update buffer.
   * @param value value to be stored
   */
  private void s(final long value) {
    buffer[bufferPos++] = (byte) value;
  }

  /**
   * Returns the byte buffer.
   * @return byte buffer
   */
  private byte[] buffer() {
    final byte[] bb = bufferPos == buffer.length ? buffer : Arrays.copyOf(buffer, bufferPos);
    bufferPos = 0;
    return bb;
  }

  /**
   * Generates a reference for a text (text, comment, pi, pi, document) or attribute value.
   * @param value text to be indexed
   * @param text text/attribute flag
   * @return reference
   */
  protected abstract long textRef(byte[] value, boolean text);

  /**
   * Deletes entries from the index structures.
   * @param pre first pre value of the nodes to delete
   * @param id id (a simple value update is indicated by the value {@code -1})
   * @param size number of descendants
   */
  protected final void indexDelete(final int pre, final int id, final int size) {
    if(id != -1) resources.delete(pre, size);
    if(meta.updindex) {
      if(meta.textindex) textIndex.delete(new ValueCache(pre, size, IndexType.TEXT, this));
      if(meta.attrindex) attrIndex.delete(new ValueCache(pre, size, IndexType.ATTRIBUTE, this));
      if(meta.tokenindex) tokenIndex.delete(new ValueCache(pre, size, IndexType.TOKEN, this));
      if(id != -1) idmap.delete(pre, id, -size);
    }
  }

  /**
   * Inserts new entries in the index structure.
   * @param pre first pre value of the nodes to insert
   * @param id id (a simple value update is indicated by the value {@code -1})
   * @param size number of descendants
   * @param clip data clip to be inserted
   */
  protected final void indexAdd(final int pre, final int id, final int size, final DataClip clip) {
    if(id != -1) resources.insert(pre, clip);
    if(meta.updindex) {
      if(id != -1) idmap.insert(pre, id, size);
      if(meta.textindex) textIndex.add(new ValueCache(pre, size, IndexType.TEXT, this));
      if(meta.attrindex) attrIndex.add(new ValueCache(pre, size, IndexType.ATTRIBUTE, this));
      if(meta.tokenindex) tokenIndex.add(new ValueCache(pre, size, IndexType.TOKEN, this));
    }
  }

  // HELPER FUNCTIONS =============================================================================

  /**
   * Indicates if this data instance is in main memory or on disk.
   * @return result of check
   */
  public abstract boolean inMemory();

  @Override
  public final String toString() {
    final int max = 20;
    final DataPrinter dp = new DataPrinter(this);
    dp.add(0, max);
    return meta.size > max ? dp + Text.DOTS : dp.toString();
  }
}
