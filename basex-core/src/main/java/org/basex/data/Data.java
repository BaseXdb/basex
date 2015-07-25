package org.basex.data;

import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.index.*;
import org.basex.index.name.*;
import org.basex.index.path.*;
import org.basex.index.query.*;
import org.basex.index.resource.*;
import org.basex.index.value.*;
import org.basex.io.*;
import org.basex.io.random.*;
import org.basex.query.*;
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
 * @author BaseX Team 2005-15, BSD License
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
  /** Path summary index. */
  public PathSummary paths;
  /** Text index. */
  public ValueIndex textIndex;
  /** Attribute value index. */
  public ValueIndex attrIndex;
  /** Full-text index instance. */
  public ValueIndex ftxtIndex;

  /** Table access file. */
  TableAccess table;
  /** ID->PRE mapping. */
  IdPreMap idmap;
  /** States if distance caching is active. */
  public boolean cache;

  /**
   * Default constructor.
   * @param meta meta data
   */
  protected Data(final MetaData meta) {
    this.meta = meta;
  }

  /**
   * Unpins the database.
   */
  public abstract void unpin();

  /**
   * Closes the database.
   */
  public abstract void close();

  /**
   * Drops the specified index.
   * @param type index to be dropped
   * @param options main options
   * @param cmd calling command
   * @throws IOException I/O exception
   */
  public abstract void createIndex(IndexType type, MainOptions options, Command cmd)
      throws IOException;

  /**
   * Drops the specified index.
   * @param type index to be dropped
   * @return success flag
   */
  public abstract boolean dropIndex(IndexType type);

  /**
   * Starts an update operation: writes a file to disk to indicate that an update is
   * going on, and exclusively locks the table file.
   * @param opts main options
   * @throws IOException I/O exception
   */
  public abstract void startUpdate(final MainOptions opts) throws IOException;

  /**
   * Finishes an update operation: removes the update file and the exclusive lock.
   * @param opts main options
   */
  public abstract void finishUpdate(final MainOptions opts);

  /**
   * Flushes updated data.
   * @param all flush all data
   */
  public abstract void flush(final boolean all);

  /**
   * Returns an index iterator for the specified token.
   * @param token index token reference
   * @return index iterator
   */
  public final IndexIterator iter(final IndexToken token) {
    return index(token.type()).iter(token);
  }

  /**
   * Returns a cost estimation for searching the specified token.
   * Smaller values are better, a value of zero indicates that no results will be returned.
   * @param token text to be found
   * @return cost estimation
   */
  public final int costs(final IndexToken token) {
    return index(token.type()).costs(token);
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
   * Returns the index reference for the specified index type.
   * @param type index type
   * @return index
   */
  final Index index(final IndexType type) {
    switch(type) {
      case TAG:       return elemNames;
      case ATTNAME:   return attrNames;
      case TEXT:      return textIndex;
      case ATTRIBUTE: return attrIndex;
      case FULLTEXT:  return ftxtIndex;
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
              if(tb == null) tb = new TokenBuilder(t);
              tb.add(txt);
            }
          }
          p += attSize(p, k);
        }
        return tb == null ? t : tb.finish();
    }
  }

  // RETRIEVING VALUES ========================================================

  /**
   * Returns a pre value for the specified id.
   * @param id unique node id
   * @return pre value or {@code -1} if id was not found
   */
  public final int pre(final int id) {
    return meta.updindex ? idmap.pre(id) : findPre(id);
  }

  /**
   * Returns a pre value for the specified id.
   * @param ids unique node ids
   * @param off start offset
   * @param len number of ids
   * @return sorted pre values
   */
  public final int[] pre(final int[] ids, final int off, final int len) {
    if(meta.updindex) return idmap.pre(ids, off, len);
    final IntList il = new IntList(len - off);
    for(int i = off; i < len; ++i) il.add(findPre(ids[i]));
    return il.sort().finish();
  }

  /**
   * Returns a pre value for the specified id by scanning the table.
   * @param id unique node id
   * @return pre value or -1 if id was not found
   */
  private int findPre(final int id) {
    // find pre value in table; start with specified id
    for(int p = Math.max(0, id); p < meta.size; ++p) if(id == id(p)) return p;
    final int ps = Math.min(meta.size, id);
    for(int p = 0; p < ps; ++p) if(id == id(p)) return p;
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
  public int dist(final int pre, final int kind) {
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
   * @return attribute value, or {@code null}
   */
  public final byte[] attValue(final int att, final int pre) {
    final int a = pre + attSize(pre, kind(pre));
    int p = pre;
    while(++p != a) if(nameId(p) == att) return text(p, false);
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
   * @return name reference
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
        uri = QueryText.XML_URI;
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
   * Returns the disk offset of a text (text, comment, pi, pi, document) or attribute value.
   * @param pre pre value
   * @return disk offset
   */
  public final long textOff(final int pre) {
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

  // UPDATE OPERATIONS ========================================================

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

      // write ids of namespace uri and name, and namespace flag
      if(kind == ATTR) {
        table.write1(pre, 11, uriId);
        table.write2(pre, 1, attrNames.index(name, null, false));
        if(nsFlag) table.write2(nsPre, 1, 1 << 15 | nameId(nsPre));
      } else {
        table.write1(pre, 3, uriId);
        final int nameId = elemNames.index(name, null, false);
        table.write2(nsPre, 1, (nsFlag || nsFlag(nsPre) ? 1 << 15 : 0) | nameId);
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
    resources.replace(pre, tSize, source);

    // initialize update of updatable index structures
    if(meta.updindex) {
      indexDelete(pre, tSize);
      indexBegin();
    }

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
          doc(cPre, sSize, sData.text(sPre, true));
          ++meta.ndocs;
          break;
        case ELEM:
          // add element
          final byte[] en = sData.name(sPre, sKind);
          elem(cDist, elemNames.index(en, null, false), sData.attSize(sPre, sKind), sSize,
              nspaces.uriIdForPrefix(prefix(en), true), false);
          break;
        case TEXT:
        case COMM:
        case PI:
          // add text
          text(cPre, cDist, sData.text(sPre, true), sKind);
          break;
        case ATTR:
          // add attribute
          final byte[] an = sData.name(sPre, sKind);
          attr(cPre, cDist, attrNames.index(an, null, false), sData.text(sPre, false),
              nspaces.uriIdForPrefix(prefix(an), false));
          break;
      }
    }

    // update ID -> PRE map and index structures
    if(meta.updindex) {
      idmap.delete(pre, id(pre), -tSize);
      idmap.insert(pre, meta.lastid - sCount + 1, sCount);
      indexAdd();
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
      if(!cache) updateDist(pre + sCount, diff);
    }

    // adjust attribute size of parent if attributes inserted. attribute size
    // of parent cannot be decreased via a replace expression.
    int sPre = source.start;
    if(sData.kind(sPre) == ATTR) {
      int d = 0;
      while(sPre < source.end && sData.kind(sPre++) == ATTR) d++;
      if(d > 1) attSize(tPar, kind(tPar), d + attSize(tPar, ELEM) - 1);
    }
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
    resources.delete(pre, size);

    // delete entries in value indexes
    if(meta.updindex) indexDelete(pre, size);

    /// delete text or attribute value in heap file
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

    if(meta.updindex) {
      // delete node and descendants from ID -> PRE map:
      idmap.delete(pre, id(pre), -size);
    }

    // delete node from table structure and reduce document size
    table.delete(pre, size);

    if(!cache) updateDist(pre, -size);
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
    meta.update();

    // update value and document indexes
    if(meta.updindex) indexBegin();
    resources.insert(pre, source);

    // resize buffer to cache more entries
    final int sCount = source.size();
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
          doc(nPre, sSize, sdata.text(sPre, true));
          ++meta.ndocs;
          break;
        case ELEM: {
          // add element.
          final boolean nsFlag = nsScope.open(nPre, sdata.namespaces(sPre));
          final byte[] name = sdata.name(sPre, sKind);
          elem(nDist, elemNames.index(name, null, false), sdata.attSize(sPre, sKind), sSize,
              nspaces.uriIdForPrefix(prefix(name), true), nsFlag);
          break;
        }
        case TEXT:
        case COMM:
        case PI:
          // add text, comment or processing instruction
          text(nPre, nDist, sdata.text(sPre, true), sKind);
          break;
        case ATTR: {
          // add attribute
          final byte[] name = sdata.name(sPre, sKind);
          int uriId = sdata.uriId(sPre, sKind);
          // extend namespace scope and write namespace flag if attribute has a new namespaces
          if(uriId != 0) {
            final byte[] prefix = prefix(name), uri = sdata.nspaces.uri(uriId);
            uriId = nspaces.uriIdForPrefix(prefix, false);
            if(uriId == 0 && !Token.eq(prefix, Token.XML)) {
              uriId = nspaces.add(nsPre, prefix, uri, this);
              table.write2(nsPre, 1, 1 << 15 | nameId(nsPre));
            }
          }
          attr(nPre, nDist, attrNames.index(name, null, false), sdata.text(sPre, false), uriId);
        }
      }
      nsScope.shift(1);
    }
    // finalize and update namespace structure
    nsScope.close();

    // write final entries and reset buffer
    if(bp != 0) insert(pre + c - 1 - (c - 1) % bSize);
    bufferSize(1);

    // increase size of ancestors
    int cPre = par;
    while(cPre >= 0) {
      final int cKind = kind(cPre);
      size(cPre, cKind, size(cPre, cKind) + sCount);
      cPre = parent(cPre, cKind);
    }

    // add entries to the ID -> PRE mapping
    if(meta.updindex) {
      idmap.insert(pre, id(pre), sCount);
      indexAdd();
    }

    // finally, update distances
    if(!cache) updateDist(pre + sCount, sCount);
  }

  /**
   * This method updates the distance values of the specified pre value
   * and the following siblings of all ancestor-or-self nodes.
   * @param pre root node
   * @param size size to be added/removed
   */
  private void updateDist(final int pre, final int size) {
    int p = pre;
    while(p < meta.size) {
      final int k = kind(p);
      if(k == DOC) break;
      dist(p, k, dist(p, k) + size);
      p += size(p, k);
    }
  }

  /**
   * Sets the size value.
   * @param pre pre reference
   * @param kind node kind
   * @param value value to be stored
   */
  public final void size(final int pre, final int kind, final int value) {
    if(kind == ELEM || kind == DOC) table.write4(pre, 8, value);
  }

  /**
   * Sets the disk offset of a text/attribute value.
   * @param pre pre value
   * @param off offset
   */
  final void textOff(final int pre, final long off) {
    table.write5(pre, 3, off);
  }

  /**
   * Updates the specified text or attribute value.
   * @param pre pre value
   * @param value content
   * @param kind node kind
   */
  protected abstract void updateText(final int pre, final byte[] value, final int kind);

  /**
   * Sets the distance.
   * @param pre pre value
   * @param kind node kind
   * @param value value
   */
  public void dist(final int pre, final int kind, final int value) {
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
  protected abstract void delete(final int pre, final boolean text);

  // INSERTS WITHOUT TABLE UPDATES ============================================

  /** Buffer for caching new table entries. */
  private byte[] b = new byte[IO.NODESIZE];
  /** Buffer position. */
  private int bp;

  /**
   * Sets the update buffer to a new size.
   * @param size number of table entries
   */
  private void bufferSize(final int size) {
    final int bs = size << IO.NODEPOWER;
    if(b.length != bs) b = new byte[bs];
  }

  /**
   * Adds a document entry to the internal update buffer.
   * @param pre pre value
   * @param size node size
   * @param value document name
   */
  public final void doc(final int pre, final int size, final byte[] value) {
    final int i = newID();
    final long v = index(pre, i, value, DOC);
    s(DOC); s(0); s(0); s(v >> 32);
    s(v >> 24); s(v >> 16); s(v >> 8); s(v);
    s(size >> 24); s(size >> 16); s(size >> 8); s(size);
    s(i >> 24); s(i >> 16); s(i >> 8); s(i);
  }

  /**
   * Adds an element entry to the internal update buffer.
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
    final int i = newID();
    final int n = nsFlag ? 1 << 7 : 0;
    s(Math.min(IO.MAXATTS, asize) << 3 | ELEM);
    s(n | (byte) (nameId >> 8)); s(nameId); s(uriId);
    s(dist >> 24); s(dist >> 16); s(dist >> 8); s(dist);
    s(size >> 24); s(size >> 16); s(size >> 8); s(size);
    s(i >> 24); s(i >> 16); s(i >> 8); s(i);
  }

  /**
   * Adds a text entry to the internal update buffer.
   * @param pre pre value
   * @param dist parent distance
   * @param value string value
   * @param kind node kind
   */
  public final void text(final int pre, final int dist, final byte[] value, final int kind) {
    // build and insert new entry
    final int i = newID();
    final long v = index(pre, i, value, kind);
    s(kind); s(0); s(0); s(v >> 32);
    s(v >> 24); s(v >> 16); s(v >> 8); s(v);
    s(dist >> 24); s(dist >> 16); s(dist >> 8); s(dist);
    s(i >> 24); s(i >> 16); s(i >> 8); s(i);
  }

  /**
   * Adds an attribute entry to the internal update buffer.
   * @param pre pre value
   * @param dist parent distance
   * @param nameId id of attribute name
   * @param value attribute value
   * @param uriId id of namespace uri
   */
  public final void attr(final int pre, final int dist, final int nameId, final byte[] value,
      final int uriId) {

    // add attribute to text storage
    final int i = newID();
    final long v = index(pre, i, value, ATTR);
    s(Math.min(IO.MAXATTS, dist) << 3 | ATTR);
    s(nameId >> 8); s(nameId); s(v >> 32);
    s(v >> 24); s(v >> 16); s(v >> 8); s(v);
    s(0); s(0); s(0); s(uriId);
    s(i >> 24); s(i >> 16); s(i >> 8); s(i);
  }

  /**
   * Stores the specified value in the update buffer.
   * @param value value to be stored
   */
  private void s(final int value) {
    b[bp++] = (byte) value;
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
    b[bp++] = (byte) value;
  }

  /**
   * Returns the byte buffer.
   * @return byte buffer
   */
  private byte[] buffer() {
    final byte[] bb = bp == b.length ? b : Arrays.copyOf(b, bp);
    bp = 0;
    return bb;
  }

  /**
   * Indexes a text and returns the reference.
   * @param pre pre value
   * @param id id value
   * @param value text to be indexed
   * @param kind node kind
   * @return reference
   */
  protected abstract long index(final int pre, final int id, final byte[] value, final int kind);

  /** Notify the index structures that an update operation is started. */
  void indexBegin() { }

  /** Notify the index structures that an add operation is finished. */
  void indexAdd() { }

  /** Notify the index structures that a delete operation is finished. */
  void indexDelete() { }

  /**
   * Deletes a node and its descendants from the corresponding indexes.
   * @param pre pre value of the node to delete
   * @param size number of descendants
   */
  protected abstract void indexDelete(final int pre, final int size);

  // HELPER FUNCTIONS ===================================================================

  /**
   * Indicates if this data instance is in main memory or on disk.
   * @return result of check
   */
  public abstract boolean inMemory();

  /**
   * Returns a string representation of the specified table range. Can be called
   * for debugging.
   * @param start start pre value
   * @param end end pre value
   * @return table
   */
  public String toString(final int start, final int end) {
    return string(InfoStorage.table(this, start, end));
  }

  @Override
  public final String toString() {
    final int max = 20;
    return meta.size > max ? toString(0, max) + "..." : toString(0, meta.size);
  }
}
