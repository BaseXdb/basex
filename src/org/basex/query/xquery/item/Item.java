package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQText.*;
import java.io.IOException;
import java.math.BigDecimal;
import org.basex.BaseX;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;
import org.basex.query.xquery.util.Scoring;
import org.basex.util.Token;

/**
 * Abstract item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Item extends Expr {
  /** Score value. */
  protected double score = Scoring.DEFAULT;
  /** Data type. */
  public Type type;
  
  /**
   * Constructor.
   * @param t data type.
   */
  protected Item(final Type t) {
    type = t;
  }
  
  @Override
  public Expr comp(final XQContext ctx) {
    return this;
  }

  @Override
  public boolean i() {
    return true;
  }

  @Override
  public final boolean n() {
    return type.num;
  }

  /**
   * Checks if this is an untyped item.
   * @return result of check
   */
  public final boolean u() {
    return type.unt;
  }

  /**
   * Checks if this is a string item.
   * @return result of check
   */
  public final boolean s() {
    return type.str;
  }

  /**
   * Checks if this is a duration.
   * @return result of check
   */
  public final boolean d() {
    return type.dur;
  }

  /**
   * Checks if this is a node.
   * @return result of check
   */
  public final boolean node() {
    return type.node;
  }

  /**
   * Returns the sequence size or 1.
   * @return result of check
   */
  public int size() {
    return 1;
  }

  /**
   * Returns a hash code.
   * @return hash code
   */
  public int hash() {
    return Token.hash(str());
  }

  /**
   * Returns a Java object.
   * @return string representation
   */
  public Object java() {
    return Token.string(str());
  }

  /**
   * Returns a boolean representation of the item.
   * @return boolean value
   * @throws XQException evaluation exception
   */
  public boolean bool() throws XQException {
    Err.or(CONDTYPE, type, this);
    return false;
  }

  /**
   * Returns a decimal representation of the item.
   * @return decimal value
   * @throws XQException evaluation exception
   */
  @SuppressWarnings("unused")
  public BigDecimal dec() throws XQException {
    BaseX.notexpected();
    return null;
  }

  /**
   * Returns an integer (long) representation of the item.
   * @return long value
   * @throws XQException evaluation exception
   */
  @SuppressWarnings("unused")
  public long itr() throws XQException {
    BaseX.notexpected();
    return 0;
  }

  /**
   * Returns a float representation of the item.
   * @return float value
   * @throws XQException evaluation exception
   */
  @SuppressWarnings("unused")
  public float flt() throws XQException {
    BaseX.notexpected();
    return 0;
  }

  /**
   * Returns a double representation of the item.
   * @return double value
   * @throws XQException evaluation exception
   */
  @SuppressWarnings("unused")
  public double dbl() throws XQException {
    BaseX.notexpected();
    return 0;
  }

  @Override
  public Iter iter(final XQContext ctx) {
    return iter();
  }

  /**
   * Returns an item iterator.
   * @return iterator
   */
  public Iter iter() {
    return new Iter() {
      private boolean more;
      @Override
      public Item next() { return (more ^= true) ? Item.this : null; }
      @Override
      public long size() { return 1; }
      @Override
      public void reset() { more = false; }
      @Override
      public String toString() { return Item.this.toString(); }
    };
  }

  /**
   * Checks the items for equality.
   * @param it item to be compared.
   * @return result of check
   * @throws XQException evaluation exception
   */
  public abstract boolean eq(Item it) throws XQException;

  /**
   * Returns the difference between the current and the specified item.
   * @param it item to be compared.
   * @return difference
   * @throws XQException evaluation exception
   */
  @SuppressWarnings("unused")
  public int diff(final Item it) throws XQException {
    Err.cmp(it, this);
    return 0;
  }

  /**
   * Returns a score value.
   * @return score value
   */
  public double score() {
    return score;
  }

  /**
   * Sets a new score value.
   * @param s score value
   */
  public void score(final double s) {
    score = s;
  }

  /**
   * Throws a cast error.
   * @param val cast value
   * @throws XQException evaluation exception
   */
  public final void castErr(final Object val) throws XQException {
    String str = val instanceof byte[] ? Token.string((byte[]) val) :
      val.toString();
    if(str.length() > 30) str = str.substring(0, 30) + "...";
    Err.or(FUNCAST, type, str);
  }

  /**
   * Serializes the item.
   * @param ser serializer
   * @param ctx query context
   * @param level current level
   * @throws IOException exception
   */
  @SuppressWarnings("unused")
  public void serialize(final Serializer ser, final XQContext ctx,
      final int level) throws IOException {
    ser.item(str());
  }

  @Override
  public boolean uses(final Using u) {
    return false;
  }
  
  @Override
  public Type returned() {
    return type;
  }

  @Override
  public final byte[] name() {
    return type.name;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this);
    ser.text(str());
    ser.closeElement(this);
  }

  @Override
  public final String color() {
    return "66CCFF";
  }

  @Override
  public String toString() {
    return Token.string(str());
  }
  
  /**
   * Creates an XQuery item from the specified Java object.
   * @param o input object
   * @return result or null
  public static Item create(final Object o) {
    if(o instanceof String) return Str.get(Token.token(o.toString()));
    if(o instanceof Float) return Flt.get((Float) o);
    if(o instanceof Double) return Dbl.get((Double) o);
    if(o instanceof Float) return Flt.get((Float) o);
    if(o instanceof BigDecimal) return Dec.get((BigDecimal) o);
    if(o instanceof BigInteger) return Itr.get(((BigInteger) o).longValue());
    if(o instanceof Long) return new Dec(new BigDecimal((Long) o), Type.LNG);
    if(o instanceof Integer) return new Itr((Integer) o, Type.INT);
    if(o instanceof Short) return new Itr((Short) o, Type.SHR);
    if(o instanceof Byte) return new Itr((Byte) o, Type.BYT);
    return null;
  }
   */
}
