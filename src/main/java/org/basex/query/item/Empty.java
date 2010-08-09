package org.basex.query.item;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Empty sequence.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Empty extends Value {
  /** Single instance. */
  public static final Empty SEQ = new Empty();

  /**
   * Private constructor.
   */
  private Empty() {
    super(Type.EMP);
  }

  @Override
  public boolean empty() {
    return true;
  }

  @Override
  public long size(final QueryContext ctx) {
    return 0;
  }

  @Override
  public Object toJava() {
    return new Object[0];
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return Iter.EMPTY;
  }

  @Override
  public Item atomic(final QueryContext ctx, final InputInfo ii) {
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
  public SeqType returned(final QueryContext ctx) {
    return SeqType.ITEM_Z;
  }

  @Override
  public boolean duplicates(final QueryContext ctx) {
    return false;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(Token.token(Type.SEQ.name), SIZE, Token.ZERO);
  }

  @Override
  public String toString() {
    return "()";
  }
}
