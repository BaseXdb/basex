package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.io.PrintOutput;
import org.basex.query.xquery.XQContext;
import org.basex.util.Token;

/**
 * PI Node.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FPI extends FNode {
  /** PI name. */
  public final QNm name;
  /** PI value. */
  private final byte[] val;
  
  /**
   * Constructor.
   * @param n name
   * @param v value
   * @param p parent
   */
  public FPI(final QNm n, final byte[] v, final Node p) {
    super(Type.PI);
    name = n;
    val = v;
    par = p;
  }

  @Override
  public byte[] str() {
    return val;
  }
  
  @Override
  public QNm qname() {
    return name;
  }
  
  @Override
  public byte[] nname() {
    return name.str();
  }
  
  @Override
  public void serialize(final Serializer ser,  final XQContext ctx,
      final int level) throws Exception  {
    ser.pi(name.str(), val);
  }

  @Override
  public String toString() {
    return "<? " + Token.string(name.str()) + " " + Token.string(val) + "?>";
  }
  
  @Override
  public FPI copy() {
    return new FPI(name, val, par);
  }

  /**
   * Serializes the specified processing-instruction.
   * @param out output stream
   * @param n name of processing instruction
   * @param v value to be serialized
   * @throws IOException I/O exception
   */
  public static void serialize(final PrintOutput out, final byte[] n,
      final byte[] v) throws IOException {
    out.print("<?");
    out.print(n);
    out.print(' ');
    out.print(v);
    out.print("?>");
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.emptyElement(this, NAM, name.str(), VAL, val);
  }
}
