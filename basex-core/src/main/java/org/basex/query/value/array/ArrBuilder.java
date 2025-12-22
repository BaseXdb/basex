package org.basex.query.value.array;

import org.basex.query.value.*;
import org.basex.query.value.type.*;

/**
 * Abstract array builder.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class ArrBuilder {
  /**
   * Appends a member.
   * @param value value to append
   * @return reference to this builder for convenience
   */
  abstract ArrBuilder add(Value value);

  /**
   * Creates an array containing the values of this builder.
   * @param type type of all members in this sequence
   * @return resulting sequence
   */
  abstract XQArray array(ArrayType type);
}
