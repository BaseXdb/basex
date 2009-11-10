package org.basex.data;

import static org.basex.util.Token.*;
import java.io.IOException;

import org.basex.core.proc.InfoTable;
import org.basex.index.Index;
import org.basex.index.IndexIterator;
import org.basex.index.IndexToken;
import org.basex.index.Names;
import org.basex.util.IntList;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.deepfs.fs.DeepFS;

/**
 * This class provides access to the database. The storage
 * representation depends on the underlying implementation.
 * Note that the methods of this class are optimized for performance.
 * They will not check if you request wrong data. If you ask for a text
 * node, e.g., get sure your pre value actually points to a text node.
 * The same applies to the update operations; if you write an attribute
 * to an element node, your database will get messed up.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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
  /** Node kind: Processing instruction. */
  public static final byte PI = 0x05;

  /** Index types. */
  public enum Type {
    /** Attribute index. */ ATN,
    /** Tag index.       */ TAG,
    /** Text index.      */ TXT,
    /** Attribute index. */ ATV,
    /** Full-text index. */ FTX,
  };

  /** Meta data. */
  public MetaData meta;
  /** Tag index. */
  public Names tags;
  /** Attribute name index. */
  public Names atts;
  /** Namespace index. */
  public Namespaces ns;
  /** Path Summary. */
  public PathSummary path;

  /** Text index. */
  protected Index txtindex;
  /** Attribute value index. */
  protected Index atvindex;
  /** Full-text index instance. */
  protected Index ftxindex;

  /** File system reference. */
  public DeepFS fs;
  /** Index Reference for name tag. */
  public int nameID;
  /** Index References. */
  public int sizeID;

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
  public final void close() throws IOException {
    if(fs != null) fs.close();
    cls();
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
  public abstract void closeIndex(Type index) throws IOException;

  /**
   * Assigns the specified index.
   * @param type index to be opened
   * @param ind index instance
   */
  public abstract void setIndex(Type type, Index ind);

  /**
   * Returns a unique node id.
   * @param pre pre value
   * @return node id
   */
  public abstract int id(int pre);

  /**
   * Returns a pre value.
   * @param id unique node id
   * @return pre value or -1 if id was not found
   */
  public abstract int pre(int id);

  /**
   * Returns a node kind.
   * @param pre pre value
   * @return node kind
   */
  public abstract int kind(int pre);

  /**
   * Returns a pre value of the parent node.
   * @param pre pre value
   * @param kind node kind
   * @return pre value of the parent node
   */
  public abstract int parent(int pre, int kind);

  /**
   * Returns the distance of the specified node.
   * @param pre pre value
   * @param k node kind
   * @return distance
   */
  protected abstract int dist(final int pre, final int k);

  /**
   * Returns a size value (number of descendant table entries).
   * @param pre pre value
   * @param kind node kind
   * @return size value
   */
  public abstract int size(int pre, int kind);

  /**
   * Returns a tag id (reference to the tag index).
   * @param pre pre value
   * @return token reference
   */
  public abstract int tagID(int pre);

  /**
   * Returns an id for the specified tag.
   * @param tok token to be found
   * @return name reference
   */
  public final int tagID(final byte[] tok) {
    return tags.id(tok);
  }

  /**
   * Returns a tag name.
   * @param pre pre value
   * @return name reference
   */
  public final byte[] tag(final int pre) {
    return tags.key(tagID(pre));
  }

  /**
   * Returns a tag namespace (reference to the tag namespace).
   * @param pre pre value
   * @return token reference
   */
  public abstract int tagNS(int pre);

  /**
   * Returns namespace key and value ids.
   * @param pre pre value
   * @return key and value ids
   */
  public abstract int[] ns(int pre);

  /**
   * Returns a text.
   * @param pre pre value
   * @return atomized value
   */
  public abstract byte[] text(int pre);

  /**
   * Returns a text as double value.
   * @param pre pre value
   * @return numeric value
   */
  public abstract double textNum(int pre);

  /**
   * Returns a text length.
   * @param pre pre value
   * @return length
   */
  public abstract int textLen(int pre);

  /**
   * Returns an attribute name.
   * @param pre pre value
   * @return name reference
   */
  public final byte[] attName(final int pre) {
    return atts.key(attNameID(pre));
  }

  /**
   * Returns an attribute name id (reference to the attribute name index).
   * @param pre pre value
   * @return token reference
   */
  public abstract int attNameID(int pre);

  /**
   * Returns an id for the specified attribute name.
   * @param tok token to be found
   * @return name reference
   */
  public final int attNameID(final byte[] tok) {
    return atts.id(tok);
  }

  /**
   * Returns an attribute namespace (reference to the attribute namespace).
   * @param pre pre value
   * @return token reference
   */
  public abstract int attNS(int pre);

  /**
   * Returns an attribute value.
   * @param pre pre value
   * @return atomized value
   */
  public abstract byte[] attValue(int pre);

  /**
   * Returns an attribute value length.
   * @param pre pre value
   * @return length
   */
  public abstract int attLen(int pre);

  /**
   * Returns an attribute value as double value.
   * @param pre pre value
   * @return numeric value
   */
  public abstract double attNum(int pre);

  /**
   * Sets the distance for the specified node.
   * @param pre pre value
   * @param kind node kind
   * @param v value
   */
  public abstract void dist(final int pre, final int kind, final int v);

  /**
   * Sets the attribute size.
   * @param pre pre value
   * @param kind node kind
   * @param v value
   */
  public abstract void attSize(final int pre, final int kind, final int v);

  /**
   * Stores a size value to the table.
   * @param pre pre reference
   * @param kind node kind
   * @param val value to be stored
   */
  public abstract void size(final int pre, final int kind, final int val);
  
  /**
   * Finds the specified attribute and returns its value.
   * @param att the attribute id of the attribute to be found
   * @param pre pre value
   * @return attribute value
   */
  public final byte[] attValue(final int att, final int pre) {
    final int a = pre + attSize(pre, kind(pre));
    int p = pre;
    while(++p != a) if(attNameID(p) == att) return attValue(p);
    return null;
  }

  /**
   * Returns a number of attributes.
   * @param pre pre value
   * @param kind node kind
   * @return number of attributes
   */
  public abstract int attSize(int pre, int kind);

  /**
   * Returns the indexed id references for the specified token.
   * @param token index token reference
   * @return id array
   */
  public final IndexIterator ids(final IndexToken token) {
    if(token.get().length > MAXLEN) return null;
    switch(token.type()) {
      case TXT: return txtindex.ids(token);
      case ATV: return atvindex.ids(token);
      case FTX: return ftxindex.ids(token);
      default:  return null;
    }
  }

  /**
   * Returns the number of indexed id references for the specified token.
   * @param token text to be found
   * @return id array
   */
  public final int nrIDs(final IndexToken token) {
    // token to long.. no results can be expected
    if(token.get().length > MAXLEN) return Integer.MAX_VALUE;
    switch(token.type()) {
      case TXT: return txtindex.nrIDs(token);
      case ATV: return atvindex.nrIDs(token);
      case FTX: return ftxindex.nrIDs(token);
      default:  return Integer.MAX_VALUE;
    }
  }

  /**
   * Returns info on the specified index structure.
   * @param type index type
   * @return info
   */
  public final byte[] info(final Type type) {
    switch(type) {
      case TAG: return tags.info();
      case ATN: return atts.info();
      case TXT: return txtindex.info();
      case ATV: return atvindex.info();
      case FTX: return ftxindex.info();
      default: return EMPTY;
    }
  }

  /**
   * Returns the document nodes.
   * @return root nodes
   */
  public final int[] doc() {
    final IntList il = new IntList();
    for(int i = 0; i < meta.size; i += size(i, Data.DOC)) il.add(i);
    return il.finish();
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
        return text(pre);
      case ATTR:
        return attValue(pre);
      case PI:
        final byte[] txt = text(pre);
        return substring(txt, indexOf(txt, ' ') + 1);
      default:
        return atm(pre);
    }
  }

  /**
   * Atomizes content of the specified pre value.
   * @param pre pre value
   * @return atomized value
   */
  private byte[] atm(final int pre) {
    // create atomized text node
    final TokenBuilder tb = new TokenBuilder();
    int p = pre;
    final int s = p + size(p, kind(p));
    while(p != s) {
      final int k = kind(p);
      if(k == TEXT) tb.add(text(p));
      p += attSize(p, k);
    }
    return tb.finish();
  }

  /**
   * Deletes a node and its descendants.
   * @param pre pre value of the node to delete
   */
  public final void delete(final int pre) {
    meta.update();
    //if(fs != null) fs.delete(pre);
    
    // size of the subtree to delete
    int k = kind(pre);
    int s = size(pre, k);
    // ignore deletions of single root node
    if(pre == 0 && s == meta.size) return;
    // reduce size of ancestors
    int par = pre;
    // check if we are an attribute (different size counters)
    if(k == ATTR) {
      par = parent(par, ATTR);
      attSize(par, ELEM, attSize(par, ELEM) - 1);
      size(par, ELEM, size(par, ELEM) - 1);
      k = kind(par);
    }
    
    // reduce size of remaining ancestors
    while(par > 0 && k != DOC) {
      par = parent(par, k);
      k = kind(par);
      size(par, k, size(par, k) - s);
    }
    // preserve empty root node
    int p = pre;
    final boolean empty = p == 0 && s == meta.size;
    if(empty) {
      p++;
      s = size(p, kind(p));
    }
    // delete node from table structure and reduce document size
    delete(p, s);
    meta.size -= s;
    updateDist(p, -s);
    // restore root node
    if(empty) {
      size(0, DOC, 1);
      update(0, Token.EMPTY, true);
    }
  }

  /**
   * Updates a tag name, text node, comment or processing instruction.
   * @param pre pre of the text node to change
   * @param val value to be updated
   */
  public final void update(final int pre, final byte[] val) {
    meta.update();
    if(kind(pre) == ELEM) {
      tagID(pre, tags.index(val, null, false));
    } else {
      update(pre, val, true);
    }
  }

  /**
   * Updates an attribute name and value.
   * @param pre pre of node to insert after
   * @param name attribute name
   * @param val attribute value
   */
  public final void update(final int pre, final byte[] name, final byte[] val) {
    meta.update();
    update(pre, val, false);
    attNameID(pre, atts.index(name, val, false));
  }

  /**
   * Inserts a tag name, text node, comment or processing instruction.
   * @param pre pre value
   * @param par parent of node
   * @param val value to be inserted
   * @param kind node kind
   */
  public final void insert(final int pre, final int par, final byte[] val,
      final int kind) {

    meta.update();
    if(kind == ELEM) {
      insertElem(pre, pre - par, val, 1, 1);
    } else if(kind == DOC) {
      insertDoc(pre, 1, val);
    } else {
      insertText(pre, pre - par, val, kind);
    }
    updateTable(pre, par, 1);
  }

  /**
   * Inserts an attribute.
   * @param pre pre value
   * @param par parent of node
   * @param name attribute name
   * @param val attribute value
   */
  public final void insert(final int pre, final int par, final byte[] name,
      final byte[] val) {

    meta.update();
    // insert attribute and increase attSize of parent element
    insertAttr(pre, pre - par, name, val);
    attSize(par, ELEM, attSize(par, ELEM) + 1);
    updateTable(pre, par, 1);
  }

  /**
   * Inserts a data instance at the specified pre value.
   * Note that the specified data instance must differ from this instance.
   * @param pre value at which to insert new data
   * @param par parent pre value of node
   * @param dt data instance to copy from
   */
  public final void insert(final int pre, final int par, final Data dt) {
    meta.update();

    // first source node to be copied; if input is a document, skip first node
    final int sa = dt.kind(0) == DOC && par > 0 ? 1 : 0;
    // number of nodes to be inserted
    final int ss = dt.size(sa, dt.kind(sa));

    // copy database entries
    for(int s = sa; s < sa + ss; s++) {
      final int k = dt.kind(s);
      final int r = dt.parent(s, k);
      // recalculate distance for root nodes
      // [CG] Updates/Insert: test collections
      final int d = r < sa ? pre - par : s - r;
      final int p = pre + s - sa;

      switch(k) {
        case ELEM:
          // add element
          insertElem(p, d, dt.tag(s), dt.attSize(s, k), dt.size(s, k));
          break;
        case DOC:
          // add document
          insertDoc(p, dt.size(s, k), dt.text(s));
          break;
        case TEXT:
        case COMM:
        case PI:
          // add text
          insertText(p, d, dt.text(s), k);
          break;
        case ATTR:
          // add attribute
          insertAttr(p, d, dt.attName(s), dt.attValue(s));
          break;
      }
    }
    // update table if no document was inserted
    if(par != 0) updateTable(pre, par, ss);

    // delete old empty root node
    if(size(0, DOC) == 1) delete(0);
  }
  
  /**
   * Inserts a data instance at the specified pre value.
   * Note that the specified data instance must differ from this instance.
   * @param pre value at which to insert new data
   * @param par parent pre value of node
   * @param dt data instance to copy from
   */
  public final void insertSeq(final int pre, final int par, final Data dt) {

    // [LK] should be merged again with insert()
    
    meta.update();
    final int sa = 1;
    // number of nodes to be inserted
    final int ss = dt.size(0, dt.kind(0));

    // copy database entries
    for(int s = sa; s < ss; s++) {
      final int k = dt.kind(s);
      final int r = dt.parent(s, k);
      final int p = pre + s - 1;
      final int d = r > 0 ? s - r : p - par;

      switch(k) {
        case ELEM:
          // add element
          insertElem(p, d, dt.tag(s), dt.attSize(s, k), dt.size(s, k));
          break;
        case DOC:
          // add document
          insertDoc(p, dt.size(s, k), dt.text(s));
          break;
        case TEXT:
        case COMM:
        case PI:
          // add text
          insertText(p, d, dt.text(s), k);
          break;
        case ATTR:
          // add attribute
          insertAttr(p, d, dt.attName(s), dt.attValue(s));
          break;
      }
    }
    // [LK] test insertion of document nodes
    updateTable(pre, par, ss - 1);
  }

  /**
   * This method is called after a table modification. It updates the
   * size values of the ancestors and the distance values of the
   * following siblings.
   * @param pre root node
   * @param par parent node
   * @param s size to be added
   */
  private void updateTable(final int pre, final int par, final int s) {
    // increase sizes
    int p = par;
    while(p >= 0) {
      final int k = kind(p);
      size(p, k, size(p, k) + s);
      p = parent(p, k);
    }
    updateDist(pre + s, s);
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

  // PROTECTED UPDATE OPERATIONS ==============================================

  /**
   * Updates the specified text or attribute value.
   * @param pre pre value
   * @param val content
   * @param txt text flag
   */
  protected abstract void update(final int pre, final byte[] val,
      final boolean txt);
  
  /**
   * Inserts an element node without updating the size and distance values
   * of the table.
   * @param pre insert position
   * @param dis parent distance
   * @param tag tag name index
   * @param as number of attributes
   * @param s node size
   */
  protected abstract void insertElem(final int pre, final int dis,
      final byte[] tag, final int as, final int s);

  /**
   * Inserts text node without updating the size and distance values
   * of the table.
   * @param pre insert position
   * @param s node size
   * @param val tag name or text node
   */
  protected abstract void insertDoc(final int pre, final int s,
      final byte[] val);

  /**
   * Inserts a text, comment or processing instruction
   * without updating the size and distance values of the table.
   * @param pre insert position
   * @param dis parent distance
   * @param val tag name or text node
   * @param kind node kind
   */
  protected abstract void insertText(final int pre, final int dis,
      final byte[] val, final int kind);

  /**
   * Inserts an attribute
   * without updating the size and distance values of the table.
   * @param pre pre value
   * @param dis parent distance
   * @param name attribute name
   * @param val attribute value
   */
  protected abstract void insertAttr(final int pre, final int dis,
      final byte[] name, final byte[] val);

  /**
   * Deletes the specified number of entries from the table.
   * @param pre pre value of the first node to delete
   * @param nr number of entries to be deleted
   */
  protected abstract void delete(final int pre, final int nr);

  /**
   * Stores the tag ID.
   * @param pre pre value
   * @param v tag id
   */
  protected abstract void tagID(final int pre, final int v);

  /**
   * Sets the attribute name ID.
   * @param pre pre value
   * @param v attribute name ID
   */
  protected abstract void attNameID(final int pre, final int v);
  
  @Override
  public String toString() {
    return string(InfoTable.table(this, 0, meta.size));
  }
}
