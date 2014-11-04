package org.basex.data;

import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.List;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.atomic.*;
import org.basex.index.*;
import org.basex.index.name.*;
import org.basex.index.path.*;
import org.basex.index.query.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.io.random.*;
import org.basex.util.*;
import org.basex.util.hash.*;
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
 * @author BaseX Team 2005-14, BSD License
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
  public MetaData meta;
  /** Element names. */
  public Names elemNames;
  /** Attribute names. */
  public Names attrNames;
  /** Namespace index. */
  public Namespaces nspaces;
  /** Path summary index. */
  public PathSummary paths;
  /** Text index. */
  public Index textIndex;
  /** Attribute value index. */
  public Index attrIndex;
  /** Full-text index instance. */
  public Index ftxtIndex;
  /** Number of current database users. */
  public int pins = 1;

  /** Table access file. */
  TableAccess table;
  /** ID->PRE mapping. */
  IdPreMap idmap;
  /** States if distance caching is active. */
  public boolean cache;

  /**
   * Closes the database.
   */
  public abstract void close();

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
   * @return success flag
   */
  public abstract boolean dropIndex(IndexType type);

  /**
   * Starts an update operation: writes a file to disk to indicate that an update is
   * going on, and exclusively locks the table file.
   * @throws IOException I/O exception
   */
  public abstract void startUpdate() throws IOException;

  /**
   * Finishes an update operation: removes the update file and the exclusive lock.
   */
  public abstract void finishUpdate();

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
   * @return info
   */
  public final byte[] info(final IndexType type) {
    return index(type).info();
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
   * @return attribute value
   */
  public final byte[] attValue(final int att, final int pre) {
    final int a = pre + attSize(pre, kind(pre));
    int p = pre;
    while(++p != a) if(name(p) == att) return text(p, false);
    return null;
  }

  /**
   * Returns a reference to the name of an element, attribute or processing instruction.
   * @param pre pre value
   * @return token reference
   */
  public final int name(final int pre) {
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
    return (kind == ELEM ? elemNames : attrNames).key(name(pre));
  }

  /**
   * Returns a reference to the namespace of the addressed element or attribute.
   * @param pre pre value
   * @param kind node kind
   * @return namespace URI
   */
  public final int uri(final int pre, final int kind) {
    return kind == ELEM || kind == ATTR ?
        table.read1(pre, kind == ELEM ? 3 : 11) & 0xFF : 0;
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
   * Returns all namespace keys and values.
   * Should only be called for element nodes.
   * @param pre pre value
   * @return key and value ids
   */
  public final Atts ns(final int pre) {
    final Atts as = new Atts();
    if(nsFlag(pre)) {
      final int[] nsp = nspaces.get(pre, this);
      for(int n = 0; n < nsp.length; n += 2)
        as.add(nspaces.prefix(nsp[n]), nspaces.uri(nsp[n + 1]));
    }
    return as;
  }

  /**
   * Returns the disk offset of a text (text, comment, pi) or attribute value.
   * @param pre pre value
   * @return disk offset
   */
  public final long textOff(final int pre) {
    return table.read5(pre, 3);
  }

  /**
   * Returns a text (text, comment, pi) or attribute value.
   * @param pre pre value
   * @param text text/attribute flag
   * @return atomized value
   */
  public abstract byte[] text(int pre, boolean text);

  /**
   * Returns a text (text, comment, pi) or attribute value as integer value.
   * {@link Long#MIN_VALUE} is returned if the input is no valid integer.
   * @param pre pre value
   * @param text text/attribute flag
   * @return numeric value
   */
  public abstract long textItr(int pre, boolean text);

  /**
   * Returns a text (text, comment, pi) or attribute value as double value.
   * {@link Double#NaN} is returned if the input is no valid double.
   * @param pre pre value
   * @param text text/attribute flag
   * @return numeric value
   */
  public abstract double textDbl(int pre, boolean text);

  /**
   * Returns the byte length of a (possibly compressed) text (text, comment, pi).
   * @param pre pre value
   * @param text text/attribute flag
   * @return length
   */
  public abstract int textLen(int pre, boolean text);

  // UPDATE OPERATIONS ========================================================

  /**
   * Updates (renames) the name of an element, attribute or processing instruction.
   * @param pre pre value
   * @param kind node kind
   * @param name name of new element, attribute or processing instruction
   * @param uri uri
   */
  public final void update(final int pre, final int kind, final byte[] name, final byte[] uri) {
    meta.update();

    if(kind == PI) {
      updateText(pre, trim(concat(name, SPACE, atom(pre))), PI);
    } else {
      // update/set namespace reference
      final int ouri = nspaces.uri(name, pre, this);
      final boolean ne = ouri == 0 && uri.length != 0;
      final int npre = kind == ATTR ? parent(pre, kind) : pre;
      final int nuri = ne ? nspaces.add(npre, npre, prefix(name), uri, this) :
        ouri != 0 && eq(nspaces.uri(ouri), uri) ? ouri : 0;

      // write namespace uri reference
      table.write1(pre, kind == ELEM ? 3 : 11, nuri);
      // write name reference
      table.write2(pre, 1, (nsFlag(pre) ? 1 << 15 : 0) |
        (kind == ELEM ? elemNames : attrNames).index(name, null, false));
      // write namespace flag
      table.write2(npre, 1, (ne || nsFlag(npre) ? 1 << 15 : 0) | name(npre));
    }
  }

  /**
   * Updates (replaces) the value of a single document, text, comment, pi or
   * attribute node.
   * @param pre pre value to be replaced
   * @param kind node kind
   * @param value value to be updated (element name, text, comment, pi)
   */
  public final void update(final int pre, final int kind, final byte[] value) {
    final byte[] v = kind == PI ? trim(concat(name(pre, kind), SPACE, value)) : value;
    if(eq(v, text(pre, kind != ATTR))) return;

    meta.update();
    updateText(pre, v, kind);
    if(kind == DOC) resources.rename(pre, value);
  }

  /**
   * Rapid Replace implementation. Replaces parts of the database with the specified
   * data instance.
   * @param tpre pre value of target node to be replaced
   * @param source clip with source data
   */
  public final void replace(final int tpre, final DataClip source) {
    meta.update();

    final int size = source.size();
    final Data data = source.data;

    final int tkind = kind(tpre);
    final int tsize = size(tpre, tkind);
    final int tpar = parent(tpre, tkind);
    final int diff = size - tsize;
    buffer(size);
    resources.replace(tpre, tsize, source);

    if(meta.updindex) {
      // update index
      indexDelete(tpre, tsize);
      indexBegin();
    }

    int sTopPre = source.start;
    for(int spre = sTopPre; spre < source.end; ++spre) {
      final int kind = data.kind(spre);
      // size and parent value of source node
      final int ssize = data.size(spre, kind);
      final int spar = data.parent(spre, kind);
      final int pre = tpre + spre - source.start;

      // calculate new distance value
      final int dist;
      if(spre == sTopPre) {
        // handle top level entry: calculate distance based on target database
        dist = pre - tpar;
        // calculate pre value of next top level entry
        sTopPre += ssize;
      } else {
        dist = spre - spar;
      }

      switch(kind) {
        case DOC:
          // add document
          doc(pre, ssize, data.text(spre, true));
          meta.ndocs.incrementAndGet();
          break;
        case ELEM:
          // add element
          byte[] nm = data.name(spre, kind);
          elem(dist, elemNames.index(nm, null, false), data.attSize(spre, kind), ssize,
              nspaces.uri(nm, true), false);
          break;
        case TEXT:
        case COMM:
        case PI:
          // add text
          text(pre, dist, data.text(spre, true), kind);
          break;
        case ATTR:
          // add attribute
          nm = data.name(spre, kind);
          attr(pre, dist, attrNames.index(nm, null, false), data.text(spre, false),
              nspaces.uri(nm, false), false);
          break;
      }
    }

    if(meta.updindex) {
      indexAdd();
      // update ID -> PRE map:
      idmap.delete(tpre, id(tpre), -tsize);
      idmap.insert(tpre, meta.lastid - size + 1, size);
    }

    // update table:
    table.replace(tpre, buffer(), tsize);
    buffer(1);

    // no distance/size update if the two subtrees are of equal size
    if(diff == 0) return;

    // increase/decrease size of ancestors, adjust distances of siblings
    int p = tpar;
    while(p >= 0) {
      final int k = kind(p);
      size(p, k, size(p, k) + diff);
      p = parent(p, k);
    }

    if(!cache) updateDist(tpre + size, diff);

    // adjust attribute size of parent if attributes inserted. attribute size
    // of parent cannot be reduced via a replace expression.
    int spre = source.start;
    if(data.kind(spre) == ATTR) {
      int d = 0;
      while(spre < source.end && data.kind(spre++) == ATTR) d++;
      if(d > 1) attSize(tpar, kind(tpar), d + attSize(tpar, ELEM) - 1);
    }
  }

  /**
   * Deletes a node and its descendants.
   * @param pre pre value of the node to delete
   */
  public final void delete(final int pre) {
    meta.update();

    // delete references in document index
    int k = kind(pre);
    final int s = size(pre, k);
    resources.delete(pre, s);

    // delete entries in value indexes
    if(meta.updindex) indexDelete(pre, s);

    /// delete text or attribute value in heap file
    if(k != DOC && k != ELEM) delete(pre, k != ATTR);

    // reduce size of ancestors
    int par = pre;
    // check if we are an attribute (different size counters)
    if(k == ATTR) {
      par = parent(par, ATTR);
      attSize(par, ELEM, attSize(par, ELEM) - 1);
      size(par, ELEM, size(par, ELEM) - 1);
      k = kind(par);
    }

    // reduce size of ancestors
    while(par > 0 && k != DOC) {
      par = parent(par, k);
      k = kind(par);
      size(par, k, size(par, k) - s);
    }

    // preserve empty root node
    if(kind(pre) == DOC) meta.ndocs.decrementAndGet();

    if(meta.updindex) {
      // delete node and descendants from ID -> PRE map:
      idmap.delete(pre, id(pre), -s);
    }

    // delete node from table structure and reduce document size
    table.delete(pre, s);

    if(!cache) updateDist(pre, -s);

    // delete namespace nodes and propagate PRE value shifts
    nspaces.delete(pre, s, this);
  }

  /**
   * Inserts attributes.
   * @param pre pre value
   * @param par parent of node
   * @param source clip with source data
   */
  public final void insertAttr(final int pre, final int par, final DataClip source) {
    insert(pre, par, source);
    attSize(par, ELEM, attSize(par, ELEM) + source.size());
  }

  /**
   * Inserts a data instance at the specified pre value.
   * Note that the specified data instance must differ from this instance.
   * @param tpre target pre value
   * @param tpar target parent pre value of node ({@code -1} if document is added)
   * @param source clip with source data
   */
  public final void insert(final int tpre, final int tpar, final DataClip source) {
    meta.update();

    // update value and document indexes
    if(meta.updindex) indexBegin();
    resources.insert(tpre, source);

    final int size = source.size();
    final int buf = Math.min(size, IO.BLOCKSIZE >> IO.NODEPOWER);
    // resize buffer to cache more entries
    buffer(buf);

    // find all namespaces in scope to avoid duplicate declarations
    final TokenMap nsScope = nspaces.scope(tpar, this);

    // loop through all entries
    final IntList preStack = new IntList();
    final NSNode nsRoot = nspaces.getCurrent();
    final IntList flagPres = new IntList();
    // track existing NSNodes - their PRE values have to be shifted after each tuple insertion
    final List<NSNode> nsNodesShift = nspaces.getNSNodes(tpre);

    // indicates if database only contains a dummy node
    final Data data = source.data;
    int c = 0, sTopPre = source.start;
    for(int spre = sTopPre; spre < source.end; ++spre, ++c) {
      if(c != 0 && c % buf == 0) insert(tpre + c - buf);

      final int pre = tpre + c;
      final int kind = data.kind(spre);
      // size and parent value of source node
      final int ssize = data.size(spre, kind);
      final int spar = data.parent(spre, kind);

      // calculate new distance value
      final int dist;
      if(spre == sTopPre) {
        // handle top level entry: calculate distance based on target database
        dist = pre - tpar;
        // calculate pre value of next top level entry
        sTopPre += ssize;
      } else {
        // handle descendant node: calculate distance based on source database
        dist = spre - spar;
      }

      // documents: use -1 as namespace root
      final int nsPre = kind == DOC ? -1 : pre - dist;
      if(c == 0) nspaces.root(nsPre, this);
      while(!preStack.isEmpty() && preStack.peek() > nsPre) nspaces.close(preStack.pop());

      switch(kind) {
        case DOC:
          // add document
          nspaces.prepare();
          doc(pre, ssize, data.text(spre, true));
          meta.ndocs.incrementAndGet();
          preStack.push(pre);
          break;
        case ELEM:
          // add element
          nspaces.prepare();
          boolean ne = false;
          if(data.nsFlag(spre)) {
            final Atts at = data.ns(spre);
            for(int a = 0; a < at.size(); ++a) {
              // see if prefix has been declared/ is part of current ns scope
              final byte[] old = nsScope.get(at.name(a));
              if(old == null || !eq(old, at.value(a))) {
                nspaces.add(at.name(a), at.value(a), pre);
                ne = true;
              }
            }
          }
          byte[] nm = data.name(spre, kind);
          elem(dist, elemNames.index(nm, null, false), data.attSize(spre, kind), ssize,
              nspaces.uri(nm, true), ne);
          preStack.push(pre);
          break;
        case TEXT:
        case COMM:
        case PI:
          // add text
          text(pre, dist, data.text(spre, true), kind);
          break;
        case ATTR:
          // add attribute
          nm = data.name(spre, kind);
          // check if prefix already in nsScope or not
          final byte[] attPref = prefix(nm);
          if(data.nsFlag(spre) && nsScope.get(attPref) == null) {
            // add declaration to parent node
            nspaces.add(nsPre, preStack.isEmpty() ? -1 : preStack.peek(), attPref,
            data.nspaces.uri(data.uri(spre, kind)), this);
            // save pre value to set ns flag later for this node. can't be done
            // here as direct table access would interfere with the buffer
            flagPres.add(nsPre);
          }
          attr(pre, dist, attrNames.index(nm, null, false), data.text(spre, false),
              nspaces.uri(nm, false), false);
          break;
      }
      // propagate PRE value shifts to keep namespace structure valid
      Namespaces.incrementPre(nsNodesShift, 1);
    }
    // finalize and update namespace structure
    while(!preStack.isEmpty()) nspaces.close(preStack.pop());
    nspaces.setCurrent(nsRoot);

    if(bp != 0) insert(tpre + c - 1 - (c - 1) % buf);
    // reset buffer to old size
    buffer(1);

    // set ns flags
    for(int f = 0; f < flagPres.size(); f++) {
      final int fl = flagPres.get(f);
      table.write2(fl, 1, name(fl) | 1 << 15);
    }

    // increase size of ancestors
    int p = tpar;
    while(p >= 0) {
      final int k = kind(p);
      size(p, k, size(p, k) + size);
      p = parent(p, k);
    }

    if(meta.updindex) {
      // add the entries to the ID -> PRE mapping:
      idmap.insert(tpre, id(tpre), size);
      indexAdd();
    }

    if(!cache) updateDist(tpre + size, size);
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
   * @param ne namespace flag
   */
  public final void nsFlag(final int pre, final boolean ne) {
    table.write1(pre, 1, table.read1(pre, 1) & 0x7F | (ne ? 0x80 : 0));
  }

  /**
   * Inserts the internal buffer to the storage
   * without updating the table structure.
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
  private void buffer(final int size) {
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
   * @param name element name index
   * @param asize number of attributes
   * @param size node size
   * @param uri namespace uri reference
   * @param ne namespace flag
   */
  public final void elem(final int dist, final int name, final int asize, final int size,
      final int uri, final boolean ne) {

    // build and insert new entry
    final int i = newID();
    final int n = ne ? 1 << 7 : 0;
    s(Math.min(IO.MAXATTS, asize) << 3 | ELEM);
    s(n | (byte) (name >> 8)); s(name); s(uri);
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
   * @param name attribute name
   * @param value attribute value
   * @param uri namespace uri reference
   * @param ne namespace flag (only {@code true} if this is a stand-alone attribute)
   */
  public final void attr(final int pre, final int dist, final int name, final byte[] value,
      final int uri, final boolean ne) {

    // add attribute to text storage
    final int i = newID();
    final long v = index(pre, i, value, ATTR);
    final int n = ne ? 1 << 7 : 0;
    s(Math.min(IO.MAXATTS, dist) << 3 | ATTR);
    s(n | (byte) (name >> 8)); s(name); s(v >> 32);
    s(v >> 24); s(v >> 16); s(v >> 8); s(v);
    s(0); s(0); s(0); s(uri);
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
   * Delete a node and its descendants from the corresponding indexes.
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
  private String toString(final int start, final int end) {
    return string(InfoStorage.table(this, start, end));
  }

  @Override
  public final String toString() {
    final int max = 20;
    return meta.size > max ? toString(0, max) + "..." : toString(0, meta.size);
  }
}
