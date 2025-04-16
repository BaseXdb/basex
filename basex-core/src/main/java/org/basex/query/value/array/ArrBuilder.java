package org.basex.query.value.array;

import org.basex.query.value.*;
import org.basex.query.value.type.*;

/**
 * An interface for creating sequences.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public interface ArrBuilder {
  /**
   * Adds a member to the end of the sequence.
   * @param value value to add
   * @return reference to this builder for convenience
   */
  ArrBuilder add(Value value);

  /**
   * Converts the builder to a tree array builder.
   * @param value value to append
   * @return tree sequence builder
   */
  default TreeArrayBuilder tree(Value value) {
    final TreeArrayBuilder ab = new TreeArrayBuilder();
    for(final Value member : array(SeqType.ARRAY).iterable()) ab.add(member);
    return ab.add(value);
  }

  /**
   * Creates an array containing the values of this builder.
   * @param type type of all members in this sequence
   * @return resulting sequence
   */
  XQArray array(ArrayType type);
}
