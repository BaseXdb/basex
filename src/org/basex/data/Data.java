package org.basex.data;

import java.io.IOException;
import org.basex.build.fs.FSText;
import org.basex.index.Index;
import org.basex.index.Names;
import org.basex.io.PrintOutput;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.query.xpath.expr.FTOption;

/**
 * This class provides access to the database. The storage
 * representation depends on the underlying implementation.
 * Note that the methods of this class are optimized for performance.
 * They won't check if you ask for wrong data. If you request a text
 * node, e.g., get sure your pre value actually points to a text node.
 * The same applies to the update operations; if you write an attribute
 * to an element node, your database will get messed up.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Data  {
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

  /** Meta data. */
  public MetaData meta;
  /** Table size. */
  public int size;
  /** Tag index. */
  public Names tags;
  /** Attribute name index. */
  public Names atts;
  /** Statistic data. */
  public Stats stats;

  /** Text index. */
  protected Index txtindex;
  /** Attribute value index. */
  protected Index atvindex;
  /** Word index. */
  protected Index wrdindex;
  /**  Fulltext index instance. **/
  protected Index ftxindex;

  /** File system indicator. */
  public boolean deepfs;
  /** Index Reference for xmlns attribute. */
  public int xmlnsID;
  /** Index Reference for name tag. */
  public int nameID;
  /** Index Reference for size attribute. */
  public int sizeID;
  /** Index Reference for suffix attribute. */
  public int suffixID;
  /** Index Reference for time attribute. */
  public int timeID;
  /** Index Reference for directory tag. */
  public int dirID;
  /** Index Reference for file tag. */
  public int fileID;
  /** Index Reference for content tag. */
  public int contentID;

  /**
   * Dissolves the references to often used tag names and attributes.
   */
  public final void initNames() {
    deepfs = tags.id(FSText.DEEPFS) != 0;
    xmlnsID = atts.id(FSText.XMLNS);
    nameID = atts.id(FSText.NAME);    
    if(deepfs) {
      fileID = tags.id(FSText.FILE);
      dirID = tags.id(FSText.DIR);
      contentID = tags.id(FSText.CONTENT);
      sizeID = atts.id(FSText.SIZE);
      suffixID = atts.id(FSText.SUFFIX);
      timeID = atts.id(FSText.MTIME);
    }
  }
  
  /**
   * Flushes the table data.
   */
  public abstract void flush();
  
  /**
   * Closes the current database.
   * @throws IOException in case the database could not be closed
   */
  public abstract void close() throws IOException;
  
  /**
   * Closes the specified index.
   * @param index index to be closed
   * @throws IOException in case the index could not be closed
   */
  public abstract void closeIndex(Index.TYPE index) throws IOException;

  /**
   * Opens the specified index.
   * @param type index to be opened
   * @param ind index instance
   * @throws IOException in case the index could not be opened
   */
  public abstract void openIndex(Index.TYPE type, Index ind)
    throws IOException;

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
   * Returns a text.
   * @param pre pre value
   * @return atomized value
   */
  public abstract byte[] text(int pre);

  /**
   * Returns the specified text snippet.
   * @param pre pre value
   * @param off offset
   * @param len length
   * @return atomized value
   */
  public abstract byte[] text(int pre, int off, int len);
  
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
   * Finds the specified attribute and returns its value.
   * @param att attribute to be found
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
   * @param type index to be looked up
   * @param token token to be found
   * @return id array
   */
  public final int[] ids(final Index.TYPE type, final byte[] token) {
    switch(type) {
      case TXT: return txtindex.ids(token);
      case ATV: return atvindex.ids(token);
      case WRD: return wrdindex.ids(token);
      case FTX: return ftxindex.ids(token);
      default:  return null;
    }
  }

  /**
   * Returns the number of indexed id references for the specified token.
   * @param type index type.
   * @param token text to be found
   * @return id array
   */
  public final int nrIDs(final Index.TYPE type, final byte[] token) {
    if(token.length > Token.MAXLEN) return Integer.MAX_VALUE;

    switch(type) {
      case TXT: return txtindex.nrIDs(token);
      case ATV: return atvindex.nrIDs(token);
      case WRD: return wrdindex.nrIDs(token);
      default:  return Integer.MAX_VALUE;
    }
  }

  /**
   * Returns the indexed id references for the specified fulltext token.
   * @param fulltext token to be looked up
   * @param ftOption fulltext options
   * @return id array
   */
  public abstract int[][] ftIDs(byte[] fulltext, FTOption ftOption);
  
  /**
   * Returns the number of indexed id references for the specified token.
   * @param token token to be looked up
   * @return id array
   */
  public abstract int nrFTIDs(byte[] token);

  /**
   * Returns the ids for the specified range expression.
   * Each token between tok0 and tok1 is returned as result.
   * @param tok0 start token defining the range
   * @param itok0 token included in range boundaries
   * @param tok1 end token defining the range
   * @param itok1 token included in range boundaries
   * @return ids
   */
  public abstract int[][] ftIDRange(final byte[] tok0, final boolean itok0, 
      final byte[] tok1, final boolean itok1);
  
  /**
   * Returns info on the index structures.
   * @param out output stream
   * @throws IOException I/O exception
   */
  public abstract void info(PrintOutput out) throws IOException;

  /**
   * Returns if the specified tag is no leaf element.
   * @param id id of the tag name
   * @return result of check
   */
  public final boolean noLeaf(final int id) {
    return tags.noLeaf(id);
  }

  /**
   * Returns the number of occurrences of the specified tag. 
   * @param id id of the tag name
   * @return number of occurrences
   */
  public final int nrTags(final int id) {
    return tags.counter(id);
  }

  /**
   * Returns an atomized content for any node kind.
   * The atomized value can be an attribute value or XML content.
   * @param pre pre value
   * @return atomized value
   */
  public final byte[] atom(final int pre) {
    final int k = kind(pre);
    return k == TEXT || k == COMM || k == PI ? text(pre) : 
           k == ATTR ? attValue(pre) : atm(pre);
  }

  /**
   * Returns a atomized numeric content for any node kind.
   * The atomized value can be an attribute value or XML content.
   * @param pre pre value
   * @return atomized value
   */
  public final double atomNum(final int pre) {
    final int k = kind(pre);
    return k == TEXT || k == COMM || k == PI ? textNum(pre) : 
           k == ATTR ? attNum(pre) : Token.toDouble(atm(pre));
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
   * Updates a tag name, text node, comment or processing instruction.
   * @param pre pre of the text node to change
   * @param val value to be updated
   */
  public abstract void update(int pre, byte[] val);
  
  /**
   * Updates an attribute name and value.
   * @param pre pre of node to insert after
   * @param name attribute name
   * @param val attribute value
   */
  public abstract void update(int pre, byte[] name, byte[] val);
  
  /**
   * Deletes a node and its descendants.
   * @param pre pre value of the node to delete
   */
  public abstract void delete(final int pre);
  
  /**
   * Inserts a tag name, text node, comment or processing instruction.
   * @param pre pre value
   * @param par parent of node
   * @param val value to be inserted
   * @param kind node kind
   */
  public abstract void insert(int pre, int par, byte[] val, byte kind);
  
  /**
   * Inserts an attribute.
   * @param pre pre value
   * @param par parent of node
   * @param name attribute name
   * @param val attribute value
   */
  public abstract void insert(int pre, int par, byte[] name, byte[] val);
  
  /**
   * Insert a data instance at the specified pre value.
   * Note that the specified data instance must differ from this instance.
   * @param pre pre value
   * @param par parent of node
   * @param d data instance to copy from
   */
  public abstract void insert(int pre, int par, Data d);
}
