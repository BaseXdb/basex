package org.basex.query.item;

import static org.basex.query.QueryText.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.iter.ValueIter;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Empty sequence.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Empty extends Value {
  /** Single instance. */
  public static final Empty SEQ = new Empty();
  /** Empty iterator. */
  public static final ValueIter ITER = new ValueIter() {
    @Override public Item next() { return null; }
    @Override public Item get(final long i) { return null; }
    @Override public Value value() { return SEQ; }
    @Override public long size() { return 0; }
    @Override public boolean reset() { return true; }
  };

  /**
   * Private constructor.
   */
  private Empty() {
    super(AtomType.EMP);
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public boolean isVacuous() {
    return true;
  }

  @Override
  public long size() {
    return 0;
  }

  @Override
  public Object toJava() {
    return new Object[0];
  }

  @Override
  public ValueIter iter() {
    return ITER;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) {
    return null;
  }

  @Override
  public Item ebv(final QueryContext ctx, final InputInfo ii) {
    return Bln.FALSE;
  }

  @Override
  public Item test(final QueryContext ctx, final InputInfo ii) {
    return null;
  }

  @Override
  public SeqType type() {
    return SeqType.EMP;
  }

  @Override
  public boolean iterable() {
    return true;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(AtomType.SEQ.string(), SIZE, Token.ZERO);
  }

  @Override
  public int hash(final InputInfo ii) {
    return 0;
  }

  @Override
  public String toString() {
    return "()";
  }

  @Override
  public int writeTo(final Item[] arr, final int start) {
    return 0;
  }

  @Override
  public Item itemAt(final long pos) {
    throw Util.notexpected();
  }

  @Override
  public boolean homogenous() {
    return true;
  }
}
