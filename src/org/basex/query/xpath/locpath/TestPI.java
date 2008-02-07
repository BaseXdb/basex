package org.basex.query.xpath.locpath;

import org.basex.data.Data;
import org.basex.util.Token;

/**
 * XPath Processing Instruction Test. Tests for a specific processing
 * instruction.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class TestPI extends Test {
  /** Name of the processing instruction. */
  private final byte[] name;

  /**
   * Constructor with Literal.
   * @param nm name of the processing instruction
   */
  public TestPI(final byte[] nm) {
    name = nm;
  }

  @Override
  public boolean eval(final Data data, final int pre, final int kind) {
    if(kind != Data.PI) return false;
    final byte[] pi = data.text(pre);
    return startsWith(pi, name);
  }

  @Override
  public boolean sameAs(final Test test) {
    if(!(test instanceof TestPI)) return false;
    final TestPI t = (TestPI) test;
    return Token.eq(name, t.name);
  }

  /**
   * Checks if the first token starts with the second token.
   * @param tok first token
   * @param sub second token
   * @return result of test
   */
  public static boolean startsWith(final byte[] tok, final byte[] sub) {
    final int sl = sub.length;
    final int tl = tok.length;
    if(sl > tl || sl == 0) return false;

    // compare tokens character wise
    for(int s = 0; s < sl; s++) if(sub[s] != tok[s]) return false;
    return sl == tl || tok[sl] == ' ';
  }

  @Override
  public String toString() {
    return "ProcessingInstruction[" + Token.string(name) + ']';
  }
}
