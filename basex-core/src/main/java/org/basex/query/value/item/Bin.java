package org.basex.query.value.item;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract class for binary items.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class Bin extends Item {
  /** Binary data. */
  byte[] data;

  /**
   * Constructor.
   * @param data binary data
   * @param type type
   */
  Bin(final byte[] data, final Type type) {
    super(type);
    this.data = data;
  }

  /**
   * Returns the binary content.
   * @param info input info (can be {@code null})
   * @return content
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public byte[] binary(final InputInfo info) throws QueryException {
    return data;
  }

  @Override
  public final boolean comparable(final Item item) {
    return item instanceof Bin;
  }

  @Override
  public final boolean equal(final Item item, final Collation coll, final InputInfo ii)
      throws QueryException {
    final byte[] binary = item instanceof final Bin bin ? bin.binary(ii) : parse(item, ii);
    return Token.eq(binary(ii), binary);
  }

  @Override
  public final boolean atomicEqual(final Item item) throws QueryException {
    return this == item || item instanceof final Bin bin &&
        Token.eq(binary(null), bin.binary(null));
  }

  @Override
  public final int compare(final Item item, final Collation coll, final boolean transitive,
      final InputInfo ii) throws QueryException {
    final byte[] binary = item instanceof final Bin bin ? bin.binary(ii) : parse(item, ii);
    return Token.compare(binary(ii), binary);
  }

  /**
   * Converts the given item to a byte array.
   * @param item item to be converted
   * @param info input info (can be {@code null})
   * @return byte array
   * @throws QueryException query exception
   */
  public abstract byte[] parse(Item item, InputInfo info) throws QueryException;

  @Override
  public BufferInput input(final InputInfo ii) throws QueryException {
    return new ArrayInput(data);
  }

  @Override
  public final byte[] toJava() throws QueryException {
    return binary(null);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final Bin bin && type == bin.type &&
        Token.eq(data, bin.data);
  }
}
