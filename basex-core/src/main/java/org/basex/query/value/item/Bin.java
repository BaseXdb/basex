package org.basex.query.value.item;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract class for binary items.
 *
 * @author BaseX Team 2005-24, BSD License
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
  public final int compare(final Item item, final Collation coll, final boolean transitive,
      final InputInfo ii) throws QueryException {
    final byte[] bin = item instanceof Bin ? ((Bin) item).binary(ii) : parse(item, ii);
    return Token.compare(binary(ii), bin);
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
    if(this == obj) return true;
    if(!(obj instanceof Bin)) return false;
    final Bin b = (Bin) obj;
    return type == b.type && Token.eq(data, b.data);
  }
}
