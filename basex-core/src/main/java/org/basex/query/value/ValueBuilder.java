package org.basex.query.value;

import org.basex.core.jobs.*;
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
  /** Interruptible job. */
  private final Job job;
  /** Capacity. */
  private final long capacity;

  /** Builder, instantiated if there is more than one value. */
  private SeqBuilder builder;
  /** First value. */
  private Value single;
  /** Count down for building a tree (ignored if {@code 0} or smaller). */
  private int tree;

  /**
   * Constructor.
   * @param job interruptible job
   */
  public ValueBuilder(final Job job) {
    this(job, -1);
  }

  /**
   * Constructor.
   * @param job interruptible job
   * @param capacity initial capacity ({@link Long#MIN_VALUE}: create no compact data structures)
   */
  public ValueBuilder(final Job job, final long capacity) {
    this.job = job;
    this.capacity = capacity;
  }

  /**
   * Builds a tree.
   * @param min minimum size to build tree
   * @return reference to this builder for convenience
   */
  public ValueBuilder tree(final int min) {
    tree = min;
    return this;
  }

  /**
   * Appends a value to the sequence.
   * @param value value to append
   * @return reference to this builder for convenience
   */
  public ValueBuilder add(final Value value) {
    if(value == Empty.VALUE) return this;
    job.checkStop();

    SeqBuilder sb = builder;
    int tr = tree;
    if(sb == null) {
      final Value sngl = single;
      if(sngl == null) {
        single = value;
        return this;
      }
      single = null;
      sb = tr > 0 && (tr -= sngl.size()) <= 0 ? new TreeSeqBuilder() :
        isStr(sngl) && isStr(value) ? new StrSeqBuilder() :
        isAtm(sngl) && isAtm(value) ? new AtmSeqBuilder() :
        isInt(sngl) && isInt(value) ? new IntSeqBuilder() :
        isDbl(sngl) && isDbl(value) ? new DblSeqBuilder() :
        isBln(sngl) && isBln(value) ? new BlnSeqBuilder() :
        new ItemSeqBuilder();
      sb = sb.add(sngl, job);
      tree = tr;
    }
    if(tr > 0 && (tr -= value.size()) <= 0) {
      sb = new TreeSeqBuilder().add(sb.value(AtomType.ITEM), job);
      tree = tr;
    }
    builder = sb.add(value, job);
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
      return builder != null ? builder.value(type) : single != null ? single : Empty.VALUE;
    } finally {
      builder = null;
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
    return Util.className(this) + '[' + Util.className(builder != null ? builder :
      single != null ? single : Empty.VALUE) + ']';
  }

  /** String sequence builder. */
  final class StrSeqBuilder extends SeqBuilder {
    /** Values. */
    private final TokenList values = new TokenList(capacity);

    @Override
    public SeqBuilder add(final Item item) {
      if(isStr(item)) {
        values.add(((Str) item).string());
        return this;
      }
      return tree(item, job);
    }

    @Override
    public Value value(final Type type) {
      return StrSeq.get(values);
    }
  }

  /** Untyped atomic sequence builder. */
  final class AtmSeqBuilder extends SeqBuilder {
    /** Values. */
    private final TokenList values = new TokenList(capacity);

    @Override
    public SeqBuilder add(final Item item) {
      if(isAtm(item)) {
        values.add(((Atm) item).string(null));
        return this;
      }
      return tree(item, job);
    }

    @Override
    public Value value(final Type type) {
      return StrSeq.get(values, AtomType.UNTYPED_ATOMIC);
    }
  }

  /** Integer sequence builder. */
  final class IntSeqBuilder extends SeqBuilder {
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
      return tree(item, job);
    }

    @Override
    public Value value(final Type type) {
      return IntSeq.get(values.finish());
    }
  }

  /** Double sequence builder. */
  final class DblSeqBuilder extends SeqBuilder {
    /** Values. */
    private final DoubleList values = new DoubleList(capacity);

    @Override
    public SeqBuilder add(final Item item) {
      if(isDbl(item)) {
        values.add(((Dbl) item).dbl());
        return this;
      }
      return tree(item, job);
    }

    @Override
    public Value value(final Type type) {
      return DblSeq.get(values.finish());
    }
  }

  /** Boolean sequence builder. */
  final class BlnSeqBuilder extends SeqBuilder {
    /** Values. */
    private final BoolList values = new BoolList(capacity);

    @Override
    public SeqBuilder add(final Item item) {
      if(isBln(item)) {
        values.add(((Bln) item).bool(null));
        return this;
      }
      return tree(item, job);
    }

    @Override
    public Value value(final Type type) {
      return BlnSeq.get(values.finish());
    }
  }

  /** Item sequence builder. */
  final class ItemSeqBuilder extends SeqBuilder {
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
