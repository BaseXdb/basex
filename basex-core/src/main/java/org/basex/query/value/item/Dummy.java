package org.basex.query.value.item;

import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Dummy item (only used at compile time).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Dummy extends Value {
  /** Input expression. */
  private final Expr expr;
  /** Focus single item. */
  private final boolean item;

  /**
   * Constructor.
   * @param expr input expression
   * @param item focus single item
   */
  public Dummy(final Expr expr, final boolean item) {
    super(expr.seqType().type);
    this.expr = expr;
    this.item = item;
  }

  @Override
  public SeqType seqType() {
    return item ? expr.seqType().with(Occ.EXACTLY_ONE) : expr.seqType();
  }

  @Override
  public long size() {
    return item ? 1 : expr.size();
  }

  @Override
  public Data data() {
    return expr.data();
  }

  @Override
  public String toJava() {
    throw Util.notExpected();
  }

  @Override
  public boolean equals(final Object obj) {
    throw Util.notExpected();
  }

  @Override
  public void toXml(final QueryPlan plan) {
    throw Util.notExpected();
  }

  @Override
  public BasicIter<Item> iter() {
    throw Util.notExpected();
  }

  @Override
  public Value materialize(final Predicate<Data> test, final InputInfo ii, final QueryContext qc)
      throws QueryException {
    throw Util.notExpected();
  }

  @Override
  public boolean materialized(final Predicate<Data> test, final InputInfo ii)
      throws QueryException {
    throw Util.notExpected();
  }

  @Override
  public Item insertValue(final long pos, final Value value, final QueryContext qc) {
    throw Util.notExpected();
  }

  @Override
  public Item removeItem(final long pos, final QueryContext qc) {
    throw Util.notExpected();
  }

  @Override
  public Value subSeq(final long start, final long length, final QueryContext qc) {
    throw Util.notExpected();
  }

  @Override
  public void cache(final boolean lazy, final InputInfo ii) throws QueryException {
    throw Util.notExpected();
  }

  @Override
  public boolean sameType() {
    throw Util.notExpected();
  }

  @Override
  public Item itemAt(final long index) {
    throw Util.notExpected();
  }

  @Override
  public Value reverse(final QueryContext qc) {
    throw Util.notExpected();
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    throw Util.notExpected();
  }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    throw Util.notExpected();
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    throw Util.notExpected();
  }

  @Override
  public void refineType(final Expr ex) {
    throw Util.notExpected();
  }

  @Override
  public void toString(final QueryString qs) {
    final TokenList list = new TokenList().add(seqType().toString());
    if(data() != null) list.add(data().meta.name);
    qs.token(getClass()).params(list.finish());
  }
}
