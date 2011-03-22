package org.basex.data;

import static org.basex.util.Token.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.basex.core.cmd.InfoStorage;
import org.basex.index.Index;
import org.basex.index.IndexIterator;
import org.basex.index.IndexToken;
import org.basex.index.Names;
import org.basex.index.IndexToken.IndexType;
import org.basex.io.IO;
import org.basex.io.TableAccess;
import org.basex.util.Atts;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenMap;
import org.deepfs.fs.DeepFS;

/**
 * This class provides access to the database storage.
 * Note that the methods of this class are optimized for performance.
 * They will not check if correct data is requested, i.e. if a text is
 * requested, a pre value must points to a text node.
 *
 * All nodes in the table are accessed by their
 * implicit pre value. The following restrictions are imposed on the data:
 * <ul>
 * <li>The table is limited to 2^31 entries (pre values are signed int's)</li>
 * <li>A maximum of 2^15 different tag and attribute names is allowed</li>
 * <li>A maximum of 2^8 different namespaces is allowed</li>
 * <li>A tag can have a maximum of 32 attributes</li>
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
 *                      Calculated in real-time, if bit range is too small
 * - Byte  1- 2:  NAME: Namespace Flag (bit: 15), Name (bits: 14-0)
 * - Byte     3:  NURI: Namespace URI
 * - Byte  4- 7:  DIST: Distance to parent node
 * - Byte  8-11:  SIZE: Number of descendants
 * TEXT, COMMENT, PI NODES (kind = 2, 4, 5):
 * - Byte  3- 7:  TEXT: Text reference
 * - Byte  8-11:  DIST: Distance to parent node
 * ATTRIBUTE NODES (kind = 3):
 * - Byte     0:  DIST: Distance to parent node (bits: 7-3)
 *                      Calculated in real-time, if bit range is too small
 * - Byte  1- 2:  NAME: Namespace Flag (bit: 15), Name (bits: 14-0)
 * - Byte  3- 7:  TEXT: Attribute value reference
 * - Byte    11:  NURI: Namespace (bits: 7-3)
 * </pre>
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class Data {
  /** Node kind: Document. */
  public static final byte DOC = 0x00;
  /** Node kind: Element. */
  public static final byte ELEM = 0x01;
  /** Node kind: Text. */
  public static final byte TEXT = 0x02;
  /** Node kind: Attribute. */
  public static final byte ATTR = 0x03;
  /** Node kind: Comment. */
  public static final byte COMM = 0x04;
  /** Node kind: Processing Instruction. */
  public static final byte PI = 0x05;

  /** Meta data. */
  public MetaData meta;
  /** Tag index. */
  public Names tags;
  /** Attribute name index. */
  public Names atts;
  /** Namespace index. */
  public Namespaces ns;
  /** Path summary. */
  public PathSummary pthindex;

  /** DeepFS reference. */
  public DeepFS fs;
  /** Index reference for a name attribute. */
  public int nameID;
  /** Index reference for a size attribute. */
  public int sizeID;

  /** Table access file. */
  protected TableAccess table;
  /** Text index. */
  protected Index txtindex;
  /** Attribute value index. */
  protected Index atvindex;
  /** Full-text index instance. */
  protected Index ftxindex;

  /**
   * Dissolves the references to often used tag names and attributes.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  public void init() throws IOException {
    if(meta.deepfs) fs = new DeepFS(this);
    nameID = atts.id(DataText.NAME);
    sizeID = atts.id(DataText.SIZE);
  }

  /**
   * Closes the current database.
   * @throws IOException I/O exception
   */
  public final synchronized void close() throws IOException {
    cls();
  }

  /**
   * Checks if the database contains no documents.
   * Empty databases exclusively contain a single document node.
   * @return result of check
   */
  public final boolean empty() {
    return meta.size == 1;
  }

  /**
   * Checks if the database contains a single document.
   * @return result of check
   */
  public final boolean single() {
    return meta.size == size(0, DOC);
  }

  /**
   * Internal method to close the database.
   * @throws IOException I/O exception
   */
  protected abstract void cls() throws IOException;

  /**
   * Flushes the table data.
   */
  public abstract void flush();

  /**
   * Closes the specified index.
   * @param index index to be closed
   * @throws IOException I/O exception
   */
  public abstract void closeIndex(IndexType index) throws IOException;

  /**
   * Assigns the specified index.
   * @param type index to be opened
   * @param ind index instance
   */
  public abstract void setIndex(IndexType type, Index ind);

  /**
   * Returns the indexed id references for the specified token.
   * @param token index token reference
   * @return id array
   */
  public final synchronized IndexIterator ids(final IndexToken token) {
    switch(token.type()) {
      case TEXT:      return txtindex.ids(token);
      case ATTRIBUTE: return atvindex.ids(token);
      case FULLTEXT:  return ftxindex.ids(token);
      default:  return null;
    }
  }

  /**
   * Returns the number of indexed id references for the specified token.
   * @param token text to be found
   * @return id array
   */
  public final synchronized int nrIDs(final IndexToken token) {
    // token too long.. no results can be expected
    if(token.get().length > MAXLEN) return Integer.MAX_VALUE;
    switch(token.type()) {
      case TEXT:      return txtindex.nrIDs(token);
      case ATTRIBUTE: return atvindex.nrIDs(token);
      case FULLTEXT:  return ftxindex.nrIDs(token);
      default:  return Integer.MAX_VALUE;
    }
  }

  /**
   * Returns the document nodes.
   * A single dummy node is returned if the database is empty.
   * @return root nodes
   */
  public final int[] doc() {
    return meta.paths.doc(this);
  }

  /**
   * Returns the document nodes for the specified path.
   * @param input input path
   * @return root nodes
   */
  public final int[] doc(final String input) {
    return meta.paths.doc(input, this);
  }

  /**
   * Returns info on the specified index structure.
   * @param type index type
   * @return info
   */
  public final synchronized byte[] info(final IndexType type) {
    switch(type) {
      case TAG:       return tags.info();
      case ATTNAME:   return atts.info();
      case TEXT:      return txtindex.info();
      case ATTRIBUTE: return atvindex.info();
      case FULLTEXT:  return ftxindex.info();
      case PATH:      return pthindex.info(this);
      default:        return EMPTY;
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
        while(p != s) {
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
   * Returns a pre value.
   * @param id unique node id
   * @return pre value or -1 if id was not found
   */
  public final int pre(final int id) {
    // find pre value in table
    for(int p = Math.max(0, id); p < meta.size; ++p)
      if(id == id(p)) return p;
    for(int p = 0, ps = Math.min(meta.size, id); p < ps; ++p)
      if(id == id(p)) return p;

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
   * Returns a node kind.
   * @param pre pre value
   * @return node kind
   */
  public final int kind(final int pre) {
    return table.read1(pre, 0) & 0x07;
  }

  /**
   * Returns a pre value of the parent node.
   * @param pre pre value
   * @param k node kind
   * @return pre value of the parent node
   */
  public final int parent(final int pre, final int k) {
    return pre - dist(pre, k);
  }

  /**
   * Returns the distance of the specified node.
   * @param pre pre value
   * @param k node kind
   * @return distance
   */
  private int dist(final int pre, final int k) {
    switch(k) {
      case ELEM:
        return table.read4(pre, 4);
      case TEXT:
      case COMM:
      case PI:
        return table.read4(pre, 8);
      case ATTR:
        int d = table.read1(pre, 0) >> 3 & IO.MAXATTS;
        // skip additional attributes, if value is larger than maximum range
        if(d >= IO.MAXATTS) while(d < pre && kind(pre - d) == ATTR) d++;
        return d;
      default:
        return pre + 1;
    }
  }

  /**
   * Returns a size value (number of descendant table entries).
   * @param pre pre value
   * @param k node kind
   * @return size value
   */
  public final int size(final int pre, final int k) {
    return k == ELEM || k == DOC ? table.read4(pre, 8) : 1;
  }

  /**
   * Returns a number of attributes.
   * @param pre pre value
   * @param k node kind
   * @return number of attributes
   */
  public final int attSize(final int pre, final int k) {
    int s = k == ELEM ? table.read1(pre, 0) >> 3 & IO.MAXATTS : 1;
    // skip additional attributes, if value is larger than maximum range
    if(s >= IO.MAXATTS) while(s < meta.size - pre && kind(pre + s) == ATTR) s++;
    return s;
  }

  /**
   * Finds the specified attribute and returns its value.
   * @param att the attribute id of the attribute to be found
   * @param pre pre value
   * @return attribute value
   */
  public byte[] attValue(final int att, final int pre) {
    final int a = pre + attSize(pre, kind(pre));
    int p = pre;
    while(++p != a) if(name(p) == att) return text(p, false);
    return null;
  }

  /**
   * Returns a reference to the tag or attribute name id.
   * @param pre pre value
   * @return token reference
   */
  public final int name(final int pre) {
    return table.read2(pre, 1) & 0x7FFF;
  }

  /**
   * Returns a tag, attribute or pi name.
   * @param pre pre value
   * @param k node kind
   * @return name reference
   */
  public final byte[] name(final int pre, final int k) {
    if(k == PI) {
      final byte[] name = text(pre, true);
      final int i = indexOf(name, ' ');
      return i == -1 ? name : substring(name, 0, i);
    }
    return (k == ELEM ? tags : atts).key(name(pre));
  }

  /**
   * Returns a reference to the tag or attribute namespace URI.
   * @param pre pre value
   * @return token reference
   * @param k node kind
   */
  public final int uri(final int pre, final int k) {
    return k == ELEM || k == ATTR ?
        table.read1(pre, k == ELEM ? 3 : 11) & 0xFF : 0;
  }

  /**
   * Returns a namespace flag.
   * Should be only called for element nodes.
   * @param pre pre value
   * @return namespace flag
   */
  public final boolean nsFlag(final int pre) {
    return (table.read1(pre, 1) & 0x80) != 0;
  }

  /**
   * Returns namespace key and value ids.
   * Should be only called for element nodes.
   * @param pre pre value
   * @return key and value ids
   */
  public final Atts ns(final int pre) {
    final Atts as = new Atts();
    if(nsFlag(pre)) {
      final int[] nsp = ns.get(pre);
      for(int n = 0; n < nsp.length; n += 2)
        as.add(ns.pref(nsp[n]), ns.uri(nsp[n + 1]));
    }
    return as;
  }

  /**
   * Returns the disk offset of a text (text, comment, pi) or attribute value.
   * @param pre pre value
   * @return disk offset
   */
  protected final long textOff(final int pre) {
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
   * @param pre pre value
   * @param text text/attribute flag
   * @return numeric value
   */
  public abstract long textItr(int pre, boolean text);

  /**
   * Returns a text (text, comment, pi) or attribute value as double value.
   * @param pre pre value
   * @param text text/attribute flag
   * @return numeric value
   */
  public abstract double textDbl(int pre, boolean text);

  /**
   * Returns a text (text, comment, pi) length.
   * @param pre pre value
   * @param text text/attribute flag
   * @return length
   */
  public abstract int textLen(int pre, boolean text);

  // UPDATE OPERATIONS ========================================================

  /**
   * Renames (updates) an element, attribute or pi name.
   * @param pre pre value
   * @param k node kind
   * @param nm new tag, attribute or pi name
   * @param uri uri
   */
  public final void rename(final int pre, final int k, final byte[] nm,
      final byte[] uri) {

    meta.update();
    if(k == PI) {
      text(pre, trim(concat(nm, SPACE, atom(pre))), true);
    } else {
      // update/set namespace reference
      final int ou = ns.uri(nm, pre);
      final boolean ne = ou == 0 && uri.length != 0;
      final int p = k == ATTR ? parent(pre, k) : pre;
      final int u = ne ? ns.add(p, p, pref(nm), uri) :
        ou != 0 && eq(ns.uri(ou), uri) ? ou : 0;

      // write namespace uri reference
      table.write1(pre, k == ELEM ? 3 : 11, u);
      // write name reference
      table.write2(pre, 1, (nsFlag(pre) ? 1 << 15 : 0) |
        (k == ELEM ? tags : atts).index(nm, null, false));
      // write namespace flag
      table.write2(p, 1, (ne || nsFlag(p) ? 1 << 15 : 0) | name(p));
    }
  }

  /**
   * Replaces (updates) a text, comment, pi or attribute value.
   * @param pre pre value
   * @param k node kind
   * @param val value to be updated (tag name, text, comment, pi)
   */
  public final void replace(final int pre, final int k, final byte[] val) {
    meta.update();
    final byte[] v = k == PI ? trim(concat(name(pre, k), SPACE, val)) : val;
    text(pre, v, k != ATTR);
  }

  /**
   * Deletes a node and its descendants.
   * @param pre pre value of the node to delete
   */
  public final void delete(final int pre) {
    meta.update();

    // size of the subtree to delete
    int k = kind(pre);
    int s = size(pre, k);
    ns.delete(pre, s);

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
    int p = pre;
    final boolean empty = p == 0 && s == meta.size;
    if(empty) {
      ++p;
      --s;
    } else {
      if(kind(p) == DOC) --meta.ndocs;
    }

    // delete node from table structure and reduce document size
    table.delete(pre, s);
    updateDist(p, -s);

    // NSNodes have to be checked for pre value shifts after delete
    ns.updatePreValues(pre, s, false, null);

    // restore empty document node
    if(empty) {
      doc(0, 1, EMPTY);
      table.set(0, buffer());
    }
  }

  /**
   * Inserts attributes.
   * @param pre pre value
   * @param par parent of node
   * @param dt data instance to copy from
   */
  public final void insertAttr(final int pre, final int par, final MemData dt) {
    meta.update();
    insert(pre, par, dt);
    attSize(par, ELEM, attSize(par, ELEM) + dt.meta.size);
  }

  /**
   * Inserts a data instance at the specified pre value.
   * Note that the specified data instance must differ from this instance.
   * @param ipre value at which to insert new data
   * @param ipar parent pre value of node
   * @param md data instance to copy from
   */
  public final void insert(final int ipre, final int ipar, final Data md) {
    meta.update();

    final int[] preStack = new int[IO.MAXHEIGHT];
    int l = 0;

    final int ms = md.meta.size;
    final int buf = Math.min(ms, IO.BLOCKSIZE >> IO.NODEPOWER);
    // resize buffer to cache more entries
    buffer(buf);

    // find all namespaces in scope
    final TokenMap nsScope = new TokenMap();
    NSNode n = ns.current;
    do {
      for(int i = 0; i < n.vals.length; i += 2)
        nsScope.add(ns.pref(n.vals[i]), ns.uri(n.vals[i + 1]));
      final int pos = n.fnd(ipar);
      if(pos < 0) break;
      n = n.ch[pos];
    } while(n.pre <= ipar && ipar < n.pre + size(n.pre, ELEM));

    // loop through all entries
    int mpre = -1;
    final NSNode t = ns.current;
    final Set<NSNode> newNodes = new HashSet<NSNode>();
    while(++mpre != ms) {
      if(mpre != 0 && mpre % buf == 0) insert(ipre + mpre - buf);

      final int mk = md.kind(mpre);
      final int mpar = md.parent(mpre, mk);
      final int pre = ipre + mpre;
      final int dis = mpar >= 0 ? mpre - mpar : pre - ipar;
      final int par = pre - dis;

      // find nearest namespace node on the ancestor axis of the insert
      // location. possible candidates for this node are collected and
      // the match with the highest pre value between ancestors and candidates
      // is determined.
      if(mpre == 0) {
        // collect possible candidates for namespace root
        final List<NSNode> cand = new LinkedList<NSNode>();
        NSNode cn = ns.root;
        cand.add(cn);
        int cI;
        while((cI = cn.fnd(par)) > -1) {
          // add candidate to stack
          cn = cn.ch[cI];
          cand.add(0, cn);
        }

        cn = ns.root;
        if(cand.size() > 1) {
          // compare candidates to ancestors of par
          int ancPre = par;
          // take first candidate from stack
          NSNode curr = cand.remove(0);
          while(ancPre > -1 && cn == ns.root) {
            // this is the new root
            if(curr.pre == ancPre) cn = curr;
            // if the current candidate's pre value is lower than the current
            // ancestor of par or par itself we have to look for a potential
            // match for this candidate. therefore we iterate through ancestors
            // till we find one with a lower than or the same pre value as the
            // current candidate.
            else if (curr.pre < ancPre) {
              while((ancPre = parent(ancPre, kind(ancPre))) > curr.pre);
              if(curr.pre == ancPre) cn = curr;
            }
            // no potential for infinite loop, cause dummy root always a match,
            // in this case ancPre ends iteration
            if(cand.size() > 0) curr = cand.remove(0);
          }
        }
        ns.setNearestRoot(cn, par);
      }

      while(l > 0 && preStack[l - 1] > par) ns.close(preStack[--l]);

      switch(mk) {
        case DOC:
          // add document
          doc(pre, md.size(mpre, mk), md.text(mpre, true));
          meta.ndocs++;
          ns.open();
          preStack[l++] = pre;
          break;
        case ELEM:
          // add element
          boolean ne = false;
          if(md.nsFlag(mpre)) {
            final Atts at = md.ns(mpre);
            for(int a = 0; a < at.size; ++a) {
              final byte[] old = nsScope.get(at.key[a]);
              if(old == null || !eq(old, at.val[a])) {
                // we have to keep track of all new NSNodes that are added
                // to the Namespace structure, as their pre values must not
                // be updated. I.e. if an NSNode N with pre value 3 existed
                // prior to inserting and two new nodes are inserted at
                // location pre == 3 we have to make sure N and only N gets
                // updated.
                newNodes.add(ns.add(at.key[a], at.val[a], pre));
                ne = true;
              }
            }
          }
          ns.open();
          byte[] nm = md.name(mpre, mk);
          elem(dis, tags.index(nm, null, false), md.attSize(mpre, mk),
              md.size(mpre, mk), ns.uri(nm, true), ne);
          preStack[l++] = pre;
          break;
        case TEXT:
        case COMM:
        case PI:
          // add text
          text(pre, dis, md.text(mpre, true), mk);
          break;
        case ATTR:
          // add attribute
          nm = md.name(mpre, mk);
          if(md.nsFlag(mpre)) {
            ns.add(par, l == 0 ? ipar : preStack[l - 1], pref(nm),
                md.ns.uri(md.uri(mpre, mk)));
            table.write2(ipar, 1, 1 << 15 | name(ipar));
          }
          attr(pre, dis, atts.index(nm, null, false), md.text(mpre, false),
              ns.uri(nm, false), false);
          break;
      }
    }

    while(l > 0) ns.close(preStack[--l]);
    ns.setRoot(t);

    if(bp != 0) insert(ipre + mpre - 1 - (mpre - 1) % buf);
    // reset buffer to old size
    buffer(1);

    // increase size of ancestors
    int p = ipar;
    while(p >= 0) {
      final int k = kind(p);
      size(p, k, size(p, k) + ms);
      p = parent(p, k);
    }
    updateDist(ipre + ms, ms);

    // NSNodes have to be checked for pre value shifts after insert
    ns.updatePreValues(ipre, ms, true, newNodes);

    // delete old empty root node
    if(size(0, DOC) == 1) delete(0);
  }

  /**
   * This method updates the distance values of the specified pre value
   * and the following siblings.
   * @param pre root node
   * @param s size to be added/removed
   */
  private void updateDist(final int pre, final int s) {
    int p = pre;
    while(p < meta.size) {
      final int k = kind(p);
      dist(p, k, dist(p, k) + s);
      p += size(p, kind(p));
    }
  }

  /**
   * Sets the size value.
   * @param pre pre reference
   * @param k node kind
   * @param v value to be stored
   */
  public final void size(final int pre, final int k, final int v) {
    if(k == ELEM || k == DOC) table.write4(pre, 8, v);
  }

  /**
   * Sets the disk offset of a text/attribute value.
   * @param pre pre value
   * @param off offset
   */
  protected final void textOff(final int pre, final long off) {
    table.write5(pre, 3, off);
  }

  /**
   * Updates the specified text or attribute value.
   * @param pre pre value
   * @param val content
   * @param txt text (text, comment or pi) or attribute flag
   */
  protected abstract void text(final int pre, final byte[] val,
      final boolean txt);

  /**
   * Sets the distance.
   * @param pre pre value
   * @param k node kind
   * @param v value
   */
  private void dist(final int pre, final int k, final int v) {
    if(k == ATTR) table.write1(pre, 0, v << 3 | ATTR);
    else if(k != DOC) table.write4(pre, k == ELEM ? 4 : 8, v);
  }

  /**
   * Sets the attribute size.
   * @param pre pre value
   * @param k node kind
   * @param v value
   */
  private void attSize(final int pre, final int k, final int v) {
    if(k == ELEM) table.write1(pre, 0, v << 3 | ELEM);
  }

  // INSERTS WITHOUT TABLE UPDATES ============================================

  /** Buffer for caching new table entries. */
  private byte[] b = new byte[1 << IO.NODEPOWER];
  /** Buffer position. */
  private int bp;

  /**
   * Sets the update buffer to a new size.
   * @param s number of table entries
   */
  public final void buffer(final int s) {
    final int bs = s << IO.NODEPOWER;
    if(b.length != bs) b = new byte[bs];
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
   * Adds a document entry to the internal update buffer.
   * @param pre pre value
   * @param s node size
   * @param vl document name
   */
  public final void doc(final int pre, final int s, final byte[] vl) {
    final int i = ++meta.lastid;
    final long v = index(vl, pre, true);
    s(DOC); s(0); s(0); s(v >> 32);
    s(v >> 24); s(v >> 16); s(v >> 8); s(v);
    s(s >> 24); s(s >> 16); s(s >> 8); s(s);
    s(i >> 24); s(i >> 16); s(i >> 8); s(i);
  }

  /**
   * Adds an element entry to the internal update buffer.
   * @param d parent distance
   * @param tn tag name index
   * @param as number of attributes
   * @param s node size
   * @param u namespace uri reference
   * @param ne namespace flag
   */
  public final void elem(final int d, final int tn, final int as,
      final int s, final int u, final boolean ne) {

    // build and insert new entry
    final int i = ++meta.lastid;
    final int n = ne ? 1 << 7 : 0;
    s(Math.min(IO.MAXATTS, as) << 3 | ELEM);
    s(n | (byte) (tn >> 8)); s(tn); s(u);
    s(d >> 24); s(d >> 16); s(d >> 8); s(d);
    s(s >> 24); s(s >> 16); s(s >> 8); s(s);
    s(i >> 24); s(i >> 16); s(i >> 8); s(i);
  }

  /**
   * Adds a text entry to the internal update buffer.
   * @param pre insert position
   * @param d parent distance
   * @param vl tag name or text node
   * @param k node kind
   */
  public final void text(final int pre, final int d, final byte[] vl,
      final int k) {

    // build and insert new entry
    final int i = newID();
    final long v = index(vl, pre, true);
    s(k); s(0); s(0); s(v >> 32);
    s(v >> 24); s(v >> 16); s(v >> 8); s(v);
    s(d >> 24); s(d >> 16); s(d >> 8); s(d);
    s(i >> 24); s(i >> 16); s(i >> 8); s(i);
  }

  /**
   * Adds an attribute entry to the internal update buffer.
   * @param pre pre value
   * @param d parent distance
   * @param tn attribute name
   * @param vl attribute value
   * @param u namespace uri reference
   * @param ne namespace flag
   */
  public final void attr(final int pre, final int d, final int tn,
      final byte[] vl, final int u, final boolean ne) {

    // add attribute to text storage
    final long v = index(vl, pre, false);
    final int i = newID();
    final int n = ne ? 1 << 7 : 0;
    s(Math.min(IO.MAXATTS, d) << 3 | ATTR);
    s(n | (byte) (tn >> 8)); s(tn); s(v >> 32);
    s(v >> 24); s(v >> 16); s(v >> 8); s(v);
    s(0); s(0); s(0); s(u);
    s(i >> 24); s(i >> 16); s(i >> 8); s(i);
  }

  /**
   * Stores the specified value in the update buffer.
   * @param v value to be stored
   */
  private void s(final int v) {
    b[bp++] = (byte) v;
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
   * @param v value to be stored
   */
  private void s(final long v) {
    b[bp++] = (byte) v;
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
   * @param txt text to be indexed
   * @param pre pre value
   * @param text text/attribute flag
   * @return reference
   */
  protected abstract long index(final byte[] txt, final int pre,
      final boolean text);

  /**
   * Returns a string representation of the specified table range.
   * Can be called for debugging.
   * @param s start pre value
   * @param e end pre value
   * @return table
   */
  public final String toString(final int s, final int e) {
    return string(InfoStorage.table(this, s, e));
  }

  @Override
  public String toString() {
    return toString(0, meta.size);
  }
}
