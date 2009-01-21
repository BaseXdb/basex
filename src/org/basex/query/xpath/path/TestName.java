package org.basex.query.xpath.path;

import org.basex.data.Data;
import org.basex.util.Token;

/**
 * NodeTest testing for a name match.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class TestName extends Test {
  /** Test accepting all names. */
  public static final byte[] ALLNODES = new byte[] { '*' };
  /** Test accepting all names. */
  public static final int ALL = -1;
  /** Unknown tag/attribute name. */
  public static final int UNKNOWN = 0;
  /** Index reference to the Tag/Attribute. */
  public int id;
  /** Tag/Attribute name. */
  public final byte[] name;
  /** Tag reference. */
  private final boolean tag;

  /**
   * Constructor, accepting all tags/attribute names.
   * @param t tag reference
   */
  public TestName(final boolean t) {
    this(ALLNODES, t);
    id = ALL;
  }

  /**
   * Constructor.
   * @param nm the name to match.
   * @param t tag reference
   */
  public TestName(final byte[] nm, final boolean t) {
    name = nm;
    tag = t;
  }

  @Override
  public boolean eval(final Data data, final int pre, final int kind) {
    // only elements and attributes will yield results
    if(kind != Data.ELEM && kind != Data.ATTR) return false;

    // wildcard - accept all nodes
    if(id == ALL) return true;
    
    int nm = kind == Data.ELEM ? data.tagID(pre) : data.attNameID(pre);
    return id == nm;
  }

  @Override
  public void compile(final Data data) {
    if(id != ALL) id = tag ? data.tagID(name) : data.attNameID(name);
  }

  @Override
  public boolean sameAs(final Test test) {
    return test instanceof TestName ? ((TestName) test).id == id : false;
  }

  @Override
  public String toString() {
    return Token.string(name);
  }
}
