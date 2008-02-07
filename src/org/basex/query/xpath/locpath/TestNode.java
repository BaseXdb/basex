package org.basex.query.xpath.locpath;

import org.basex.data.Data;
import org.basex.query.xpath.XPText;
import org.basex.util.Token;

/**
 * Node Test testing for a specific NodeType.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 * @author Christian Gruen
 */
public abstract class TestNode extends Test {
  /** node() kind. */
  public static final TestNode NODE = new TestNode() {
    @Override
    public boolean eval(final Data data, final int pre, final int kind) {
      return true;
    }

    @Override
    public String toString() {
      return Token.string(XPText.NODE) + "()";
    }
  };

  /** text() kind. */
  public static final TestNode TEXT = new TestNode() {
    @Override
    public boolean eval(final Data data, final int pre, final int kind) {
      return kind == Data.TEXT;
    }

    @Override
    public String toString() {
      return Token.string(XPText.TEXT) + "()";
    }
  };

  /** comment() kind. */
  public static final TestNode COMM = new TestNode() {
    @Override
    public boolean eval(final Data data, final int pre, final int kind) {
      return kind == Data.COMM;
    }

    @Override
    public String toString() {
      return Token.string(XPText.COMMENT) + "()";
    }
  };

  /** processing-instruction() kind. */
  public static final TestNode PI = new TestNode() {
    @Override
    public boolean eval(final Data data, final int pre, final int kind) {
      return kind == Data.PI;
    }

    @Override
    public String toString() {
      return Token.string(XPText.PI) + "()";
    }
  };

  @Override
  public final boolean sameAs(final Test test) {
    return test == this;
  }
}
