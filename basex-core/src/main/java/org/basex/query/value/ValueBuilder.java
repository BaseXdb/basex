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
      sb = tr > 0 && (tr -= sngl.size()) <= 0 ? new TreeSeqBuilder(job) :
        isStr(sngl) && isStr(value) ? new StrSeqBuilder(job) :
        isAtm(sngl) && isAtm(value) ? new AtmSeqBuilder(job) :
        isItr(sngl) && isItr(value) ? itrSeqBuilder(
          sngl instanceof final Itr itr ? itr.itr() : 0, capacity) :
        isDbl(sngl) && isDbl(value) ? new DblSeqBuilder(job) :
        isBln(sngl) && isBln(value) ? new BlnSeqBuilder(job) :
        new ItemSeqBuilder(job);
      sb = sb.add(sngl);
      tree = tr;
    }
    if(tr > 0 && (tr -= value.size()) <= 0) {
      sb = new TreeSeqBuilder(job).add(sb.value(BasicType.ITEM));
      tree = tr;
    }
    builder = sb.add(value);
    return this;
  }

  /**
   * Appends an integer value to the sequence.
   * @param value value to append
   * @return reference to this builder for convenience
   */
  public ValueBuilder add(final long value) {
    job.checkStop();

    SeqBuilder sb = builder;
    if(sb == null) {
      final Value sngl = single;
      if(sngl == null) {
        single = Itr.get(value);
        return this;
      }
      single = null;
      sb = isItr(sngl) ? itrSeqBuilder(value, capacity) : new ItemSeqBuilder(job);
      sb = sb.add(sngl);
    }
    builder = sb.add(value);
    return this;
  }

  /**
   * Returns the built value.
   * @return value
   */
  public Value value() {
    return value(BasicType.ITEM);
  }

  /**
   * Returns the built value and annotates it with the specified type.
   * @param type type (only considered if a new value is created)
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
   * Returns the built array and annotates it with the type of the specified expression.
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
    return value instanceof Str && value.type == BasicType.STRING;
  }

  /**
   * Checks if the specified value is a string.
   * @param value value
   * @return result of check
   */
  static boolean isAtm(final Value value) {
    return value.type == BasicType.UNTYPED_ATOMIC;
  }

  /**
   * Checks if the specified value is an integer value.
   * @param value value
   * @return result of check
   */
  static boolean isItr(final Value value) {
    return value.type == BasicType.INTEGER;
  }

  /**
   * Checks if the specified value is a double value.
   * @param value value
   * @return result of check
   */
  static boolean isDbl(final Value value) {
    return value.type == BasicType.DOUBLE;
  }

  /**
   * Checks if the specified value is a boolean value.
   * @param value value
   * @return result of check
   */
  static boolean isBln(final Value value) {
    return value.type == BasicType.BOOLEAN;
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + Util.className(builder != null ? builder :
      single != null ? single : Empty.VALUE) + ']';
  }

  /** String sequence builder. */
  final class StrSeqBuilder extends SeqBuilder {
    /**
     * Constructor.
     * @param job interruptible job
     */
    StrSeqBuilder(final Job job) {
      super(job);
    }

    /** Values. */
    private final TokenList values = new TokenList(capacity);

    @Override
    public SeqBuilder add(final Item item) {
      if(isStr(item)) {
        values.add(((Str) item).string());
        return this;
      }
      return tree(item);
    }

    @Override
    public Value value(final Type type) {
      return StrSeq.get(values);
    }
  }

  /** Untyped atomic sequence builder. */
  final class AtmSeqBuilder extends SeqBuilder {
    /**
     * Constructor.
     * @param job interruptible job
     */
    AtmSeqBuilder(final Job job) {
      super(job);
    }

    /** Values. */
    private final TokenList values = new TokenList(capacity);

    @Override
    public SeqBuilder add(final Item item) {
      if(isAtm(item)) {
        values.add(((Atm) item).string(null));
        return this;
      }
      return tree(item);
    }

    @Override
    public Value value(final Type type) {
      return StrSeq.get(values, BasicType.UNTYPED_ATOMIC);
    }
  }

  /** Integer sequence builder, collecting values in the narrowest representation. */
  abstract class ItrSeqBuilder extends SeqBuilder {
    /**
     * Constructor.
     * @param job interruptible job
     */
    ItrSeqBuilder(final Job job) {
      super(job);
    }

    @Override
    public final SeqBuilder add(final Item item) {
      return isItr(item) ? add(((Itr) item).itr()) : tree(item);
    }

    @Override
    public abstract SeqBuilder add(long value);

    @Override
    protected SeqBuilder addSequence(final Value value) {
      if(!(value instanceof final ItrSeq seq)) return super.addSequence(value);
      // adopt the values of a native sequence without materializing items
      SeqBuilder sb = this;
      final int sz = (int) seq.size();
      for(int i = 0; i < sz; i++) {
        job.checkStop();
        sb = sb.add(seq.itrAt(i));
      }
      return sb;
    }

    /**
     * Transfers the collected values to the specified builder.
     * @param sb target builder
     */
    abstract void transfer(ItrSeqBuilder sb);

    /**
     * Moves the collected values to a builder that can store the specified value.
     * @param value value that does not fit the current representation
     * @param size number of collected values
     * @return builder to be used for subsequent values
     */
    final SeqBuilder widen(final long value, final int size) {
      // the target is always wider, so transferred values never trigger another widening
      final ItrSeqBuilder sb = itrSeqBuilder(value, Math.max(capacity, size));
      transfer(sb);
      return sb.add(value);
    }
  }

  /**
   * Returns an integer builder that can store the specified value.
   * @param value value to be stored
   * @param cap initial capacity
   * @return builder
   */
  ItrSeqBuilder itrSeqBuilder(final long value, final long cap) {
    return switch(ItrSeq.minWidth(value, value)) {
      case 1 -> new BytSeqBuilder(job, cap);
      case 2 -> new ShrSeqBuilder(job, cap);
      case 4 -> new IntSeqBuilder(job, cap);
      default -> new LongSeqBuilder(job, cap);
    };
  }

  /** Byte sequence builder. */
  final class BytSeqBuilder extends ItrSeqBuilder {
    /** Values. */
    private final ByteList values;

    /**
     * Constructor.
     * @param job interruptible job
     * @param cap initial capacity
     */
    BytSeqBuilder(final Job job, final long cap) {
      super(job);
      values = new ByteList(cap);
    }

    @Override
    public SeqBuilder add(final long value) {
      if(value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) return widen(value, values.size());
      values.add((int) value);
      return this;
    }

    @Override
    void transfer(final ItrSeqBuilder sb) {
      for(int i = 0, s = values.size(); i < s; i++) sb.add(values.get(i));
    }

    @Override
    public Value value(final Type type) {
      return BytSeq.get(values.finish(), BasicType.INTEGER);
    }
  }

  /** Short sequence builder. */
  final class ShrSeqBuilder extends ItrSeqBuilder {
    /** Values. */
    private final ShortList values;

    /**
     * Constructor.
     * @param job interruptible job
     * @param cap initial capacity
     */
    ShrSeqBuilder(final Job job, final long cap) {
      super(job);
      values = new ShortList(cap);
    }

    @Override
    public SeqBuilder add(final long value) {
      if(value < Short.MIN_VALUE || value > Short.MAX_VALUE) return widen(value, values.size());
      values.add((short) value);
      return this;
    }

    @Override
    void transfer(final ItrSeqBuilder sb) {
      for(int i = 0, s = values.size(); i < s; i++) sb.add(values.get(i));
    }

    @Override
    public Value value(final Type type) {
      return ShrSeq.get(values.finish(), BasicType.INTEGER);
    }
  }

  /** Int sequence builder. */
  final class IntSeqBuilder extends ItrSeqBuilder {
    /** Values. */
    private final IntList values;

    /**
     * Constructor.
     * @param job interruptible job
     * @param cap initial capacity
     */
    IntSeqBuilder(final Job job, final long cap) {
      super(job);
      values = new IntList(cap);
    }

    @Override
    public SeqBuilder add(final long value) {
      final int i = (int) value;
      if(i != value) return widen(value, values.size());
      values.add(i);
      return this;
    }

    @Override
    void transfer(final ItrSeqBuilder sb) {
      for(int i = 0, s = values.size(); i < s; i++) sb.add(values.get(i));
    }

    @Override
    public Value value(final Type type) {
      return IntSeq.get(values.finish());
    }
  }

  /** Long sequence builder. */
  final class LongSeqBuilder extends ItrSeqBuilder {
    /** Values. */
    private final LongList values;

    /**
     * Constructor.
     * @param job interruptible job
     * @param cap initial capacity
     */
    LongSeqBuilder(final Job job, final long cap) {
      super(job);
      values = new LongList(cap);
    }

    @Override
    public SeqBuilder add(final long value) {
      values.add(value);
      return this;
    }

    @Override
    void transfer(final ItrSeqBuilder sb) {
      for(int i = 0, s = values.size(); i < s; i++) sb.add(values.get(i));
    }

    @Override
    public Value value(final Type type) {
      return LongSeq.get(values.finish());
    }
  }

  /** Double sequence builder. */
  final class DblSeqBuilder extends SeqBuilder {
    /**
     * Constructor.
     * @param job interruptible job
     */
    DblSeqBuilder(final Job job) {
      super(job);
    }

    /** Values. */
    private final DoubleList values = new DoubleList(capacity);

    @Override
    public SeqBuilder add(final Item item) {
      if(isDbl(item)) {
        values.add(((Dbl) item).dbl());
        return this;
      }
      return tree(item);
    }

    @Override
    public Value value(final Type type) {
      return DblSeq.get(values.finish());
    }
  }

  /** Boolean sequence builder. */
  final class BlnSeqBuilder extends SeqBuilder {
    /**
     * Constructor.
     * @param job interruptible job
     */
    BlnSeqBuilder(final Job job) {
      super(job);
    }

    /** Values. */
    private final BoolList values = new BoolList(capacity);

    @Override
    public SeqBuilder add(final Item item) {
      if(isBln(item)) {
        values.add(((Bln) item).bool(null));
        return this;
      }
      return tree(item);
    }

    @Override
    public Value value(final Type type) {
      return BlnSeq.get(values.finish());
    }
  }

  /** Item sequence builder. */
  final class ItemSeqBuilder extends SeqBuilder {
    /**
     * Constructor.
     * @param job interruptible job
     */
    ItemSeqBuilder(final Job job) {
      super(job);
    }

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
