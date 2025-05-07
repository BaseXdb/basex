package org.basex.query.value.seq;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.tree.*;
import org.basex.query.value.type.*;

/**
 * An interface for creating sequences.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public interface SeqBuilder {
  /**
   * Adds an item to the end of the sequence.
   * @param item item to add
   * @return reference to this builder for convenience
   */
  SeqBuilder add(Item item);

  /**
   * Appends the items of the given value to this builder.
   * @param value value to append
   * @param qc query context
   * @return this builder for convenience
   */
  default SeqBuilder add(final Value value, final QueryContext qc) {
    SeqBuilder sb = this;
    for(final Item item : value) {
      qc.checkStop();
      sb = sb.add(item);
    }
    return sb;
  }

  /**
   * Converts the builder to a tree sequence builder.
   * @param item item to append
   * @param qc query context
   * @return tree sequence builder
   */
  default TreeSeqBuilder tree(final Item item, final QueryContext qc) {
    return new TreeSeqBuilder().add(value(null), qc).add(item);
  }

  /**
   * Creates a sequence containing the items of this builder.
   * @param type type of all items in this sequence
   * @return resulting sequence
   */
  Value value(Type type);
}
