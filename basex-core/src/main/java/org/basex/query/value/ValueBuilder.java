package org.basex.query.value;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.seq.tree.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * A builder for efficiently creating a {@link Value}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ValueBuilder {
  /** QueryContext. */
  private final QueryContext qc;

  /** Sequence builder, only instantiated if there are at least two items. */
  private SeqBuilder sequence;
  /** Capacity ({@link Integer#MIN_VALUE}: create no compact data structures). */
  private final long capacity;
  /** The first added value is cached. */
  private Value single;

  /**
   * Constructor.
   * @param qc query context (required for interrupting running queries)
   */
  public ValueBuilder(final QueryContext qc) {
    this(qc, -1);
  }

  /**
   * Constructor.
   * @param qc query context (required for interrupting running queries)
   * @param capacity initial capacity ({@link Integer#MIN_VALUE}: create no compact data structures)
   */
  public ValueBuilder(final QueryContext qc, final long capacity) {
    this.qc = qc;
    this.capacity = capacity;
  }

  /**
   * Appends a value to the sequence.
   * @param value value to append
   * @return reference to this builder for convenience
   */
  public ValueBuilder add(final Value value) {
    if(!value.isEmpty()) {
      qc.checkStop();
      if(sequence == null) {
        final Value sngl = single;
        if(sngl == null) {
          single = value;
          return this;
        }
        single = null;
        sequence = capacity == Integer.MIN_VALUE ? new TreeSeqBuilder() :
                   isStr(sngl) && isStr(value) ? new StrSeqBuilder() :
                   isAtm(sngl) && isAtm(value) ? new AtmSeqBuilder() :
                   isInt(sngl) && isInt(value) ? new IntSeqBuilder() :
                   isDbl(sngl) && isDbl(value) ? new DblSeqBuilder() :
                   isBln(sngl) && isBln(value) ? new BlnSeqBuilder() :
                   new ItemSeqBuilder();
        add(sngl);
      }
      sequence = sequence.add(value, qc);
    }
    return this;
  }

  /**
   * Returns a {@link Value} representation of the items currently stored in this builder.
   * @return value
   */
  public Value value() {
    return value(AtomType.ITEM);
  }

  /**
   * Returns a {@link Value} representation of the items currently stored in this builder
   * annotated with the given item type.
   * @param type type (only considered if new result value is created)
   * @return value
   */
  public Value value(final Type type) {
    try {
      return sequence != null ? sequence.value(type) : single != null ? single : Empty.VALUE;
    } finally {
      sequence = null;
      single = null;
    }
  }

  /**
   * Returns a {@link Value} representation of the items currently stored in this builder
   * annotated with the type of the given expression.
   * @param expr expression that created the value (can be {@code null})
   * @return value
   */
  public Value value(final Expr expr) {
    return expr != null ? value(expr.seqType().type) : value();
  }

  /**
   * Checks if the specified value is a string.
   * @param value value
   * @return result of check
   */
  static boolean isStr(final Value value) {
    return value instanceof Str && value.type == AtomType.STRING;
  }

  /**
   * Checks if the specified value is a string.
   * @param value value
   * @return result of check
   */
  static boolean isAtm(final Value value) {
    return value.type == AtomType.UNTYPED_ATOMIC;
  }

  /**
   * Checks if the specified value is an integer value.
   * @param value value
   * @return result of check
   */
  static boolean isInt(final Value value) {
    return value.type == AtomType.INTEGER;
  }

  /**
   * Checks if the specified value is a double value.
   * @param value value
   * @return result of check
   */
  static boolean isDbl(final Value value) {
    return value.type == AtomType.DOUBLE;
  }

  /**
   * Checks if the specified value is a boolean value.
   * @param value value
   * @return result of check
   */
  static boolean isBln(final Value value) {
    return value.type == AtomType.BOOLEAN;
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + Util.className(sequence != null ? sequence :
      single != null ? single : Empty.VALUE) + ']';
  }

  /** String sequence builder. */
  final class StrSeqBuilder implements SeqBuilder {
    /** Values. */
    private final TokenList values = new TokenList(capacity);

    @Override
    public SeqBuilder add(final Item item) {
      if(isStr(item)) {
        values.add(((Str) item).string());
        return this;
      }
      return tree(item, qc);
    }

    @Override
    public Value value(final Type type) {
      return StrSeq.get(values);
    }
  }

  /** Untyped atomic sequence builder. */
  final class AtmSeqBuilder implements SeqBuilder {
    /** Values. */
    private final TokenList values = new TokenList(capacity);

    @Override
    public SeqBuilder add(final Item item) {
      if(isAtm(item)) {
        values.add(((Atm) item).string(null));
        return this;
      }
      return tree(item, qc);
    }

    @Override
    public Value value(final Type type) {
      return StrSeq.get(values, AtomType.UNTYPED_ATOMIC);
    }
  }

  /** Integer sequence builder. */
  final class IntSeqBuilder implements SeqBuilder {
    /** Values. */
    private final IntList values = new IntList(capacity);

    @Override
    public SeqBuilder add(final Item item) {
      if(isInt(item)) {
        final int i = ((Itr) item).toInt();
        if(i != Integer.MIN_VALUE) {
          values.add(i);
          return this;
        }
      }
      return tree(item, qc);
    }

    @Override
    public Value value(final Type type) {
      return IntSeq.get(values.finish());
    }
  }

  /** Double sequence builder. */
  final class DblSeqBuilder implements SeqBuilder {
    /** Values. */
    private final DoubleList values = new DoubleList(capacity);

    @Override
    public SeqBuilder add(final Item item) {
      if(isDbl(item)) {
        values.add(((Dbl) item).dbl());
        return this;
      }
      return tree(item, qc);
    }

    @Override
    public Value value(final Type type) {
      return DblSeq.get(values.finish());
    }
  }

  /** Boolean sequence builder. */
  final class BlnSeqBuilder implements SeqBuilder {
    /** Values. */
    private final BoolList values = new BoolList(capacity);

    @Override
    public SeqBuilder add(final Item item) {
      if(isBln(item)) {
        values.add(((Bln) item).bool(null));
        return this;
      }
      return tree(item, qc);
    }

    @Override
    public Value value(final Type type) {
      return BlnSeq.get(values.finish());
    }
  }

  /** Item sequence builder. */
  final class ItemSeqBuilder implements SeqBuilder {
    /** Items. */
    private final ItemList items = new ItemList(capacity);

    @Override
    public SeqBuilder add(final Item item) {
      items.add(item);
      return this;
    }

    @Override
    public Value value(final Type type) {
      return items.value(type);
    }
  }
}
