package org.basex.query.value.array;

import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.list.*;

/**
 * A builder for efficiently creating an {@link XQArray}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayBuilder {
  /** Sequence builder, only instantiated if there are at least two members. */
  private ArrBuilder array;
  /** Capacity ({@link Integer#MIN_VALUE}: create no compact data structures). */
  private long capacity;
  /** The first added value is cached. */
  private Value single;

  /**
   * Constructor.
   */
  public ArrayBuilder() {
    this(-1);
  }

  /**
   * Constructor.
   * @param capacity initial capacity ({@link Integer#MIN_VALUE}: create no compact data structures)
   */
  public ArrayBuilder(final long capacity) {
    this.capacity = capacity;
  }

  /**
   * Appends a member to the array.
   * @param value value to append
   * @return reference to this builder for convenience
   */
  public ArrayBuilder add(final Value value) {
    final Value sngl = single;
    if(sngl != null) {
      single = null;
      if(capacity != Integer.MIN_VALUE) {
        array = isStr(sngl) && isStr(value) ? new StrArrBuilder() :
                isAtm(sngl) && isAtm(value) ? new AtmArrBuilder() :
                isInt(sngl) && isInt(value) ? new IntArrBuilder() :
                isDbl(sngl) && isDbl(value) ? new DblArrBuilder() :
                isBln(sngl) && isBln(value) ? new BlnArrBuilder() : null;
      }
      if(array == null) array = new TreeArrayBuilder();
      add(sngl);
    }
    if(array != null) {
      array = array.add(value);
    } else {
      single = value;
    }
    return this;
  }

  /**
   * Returns a {@link Value} representation of the items currently stored in this builder.
   * @return value
   */
  public XQArray array() {
    return array(SeqType.ARRAY);
  }

  /**
   * Returns a {@link Value} representation of the items currently stored in this builder
   * annotated with the given item type.
   * @param type type (only considered if new result value is created)
   * @return value
   */
  public XQArray array(final ArrayType type) {
    try {
      return array != null ? array.array(type) : single != null ? XQArray.singleton(single) :
        XQArray.empty();
    } finally {
      array = null;
      single = null;
    }
  }

  /**
   * Creates an {@link XQArray} containing the members of this builder.
   * @param expr expression that created the array (can be {@code null})
   * @return resulting array
   */
  public XQArray array(final Expr expr) {
    return expr != null ? array((ArrayType) expr.seqType().type) : array();
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
    return value.seqType().eq(SeqType.UNTYPED_ATOMIC_O);
  }

  /**
   * Checks if the specified value is an integer value.
   * @param value value
   * @return result of check
   */
  static boolean isInt(final Value value) {
    return value.seqType().eq(SeqType.INTEGER_O);
  }

  /**
   * Checks if the specified value is a double value.
   * @param value value
   * @return result of check
   */
  static boolean isDbl(final Value value) {
    return value.seqType().eq(SeqType.DOUBLE_O);
  }

  /**
   * Checks if the specified value is a boolean value.
   * @param value value
   * @return result of check
   */
  static boolean isBln(final Value value) {
    return value.seqType().eq(SeqType.BOOLEAN_O);
  }

  /** String array builder. */
  final class StrArrBuilder implements ArrBuilder {
    /** Values. */
    private final TokenList values = new TokenList(capacity);

    @Override
    public ArrBuilder add(final Value value) {
      if(isStr(value)) {
        values.add(((Str) value).string());
        return this;
      }
      return tree(value);
    }

    @Override
    public XQArray array(final ArrayType type) {
      return new StrArray(values.finish());
    }
  }

  /** Untyped atomic array  builder. */
  final class AtmArrBuilder implements ArrBuilder {
    /** Values. */
    private final TokenList values = new TokenList(capacity);

    @Override
    public ArrBuilder add(final Value value) {
      if(isAtm(value)) {
        values.add(((Atm) value).string(null));
        return this;
      }
      return tree(value);
    }

    @Override
    public XQArray array(final ArrayType type) {
      return new AtmArray(values.finish());
    }
  }

  /** Integer array builder. */
  final class IntArrBuilder implements ArrBuilder {
    /** Values. */
    private final LongList values = new LongList(capacity);

    @Override
    public ArrBuilder add(final Value value) {
      if(isInt(value)) {
        values.add(((Int) value).itr());
        return this;
      }
      return tree(value);
    }

    @Override
    public XQArray array(final ArrayType type) {
      final int vs = values.size();
      final long first = values.get(0);
      boolean range = true;
      int v = 0;
      while(range && ++v < vs) {
        range &= values.get(v) == first + v;
      }
      return v == vs ? new RangeArray(first, vs, true) : new IntArray(values.finish());
    }
  }

  /** Double array builder. */
  final class DblArrBuilder implements ArrBuilder {
    /** Values. */
    private final DoubleList values = new DoubleList(capacity);

    @Override
    public ArrBuilder add(final Value value) {
      if(isDbl(value)) {
        values.add(((Dbl) value).dbl());
        return this;
      }
      return tree(value);
    }

    @Override
    public XQArray array(final ArrayType type) {
      return new DblArray(values.finish());
    }
  }

  /** Boolean array builder. */
  final class BlnArrBuilder implements ArrBuilder {
    /** Values. */
    private final BoolList values = new BoolList(capacity);

    @Override
    public ArrBuilder add(final Value value) {
      if(isBln(value)) {
        values.add(((Bln) value).bool(null));
        return this;
      }
      return tree(value);
    }

    @Override
    public XQArray array(final ArrayType type) {
      return new BlnArray(values.finish());
    }
  }
}
