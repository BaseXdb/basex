package org.basex.query.value.seq;

import org.basex.core.jobs.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.tree.*;
import org.basex.query.value.type.*;

/**
 * Abstract sequence builder.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class SeqBuilder {
  /**
   * Appends an item.
   * @param item item to append
   * @return reference to this builder for convenience
   */
  public abstract SeqBuilder add(Item item);

  /**
   * Appends items.
   * @param value items to append
   * @param job interruptible job
   * @return this builder for convenience
   */
  public SeqBuilder add(final Value value, final Job job) {
    SeqBuilder sb = this;
    for(final Item item : value) {
      job.checkStop();
      sb = sb.add(item);
    }
    return sb;
  }

  /**
   * Creates a value containing the items of this builder.
   * @param type type of all items in this sequence
   * @return resulting value
   */
  public abstract Value value(Type type);

  /**
   * Converts the builder to a tree sequence builder.
   * @param item item to append
   * @param job interruptible job
   * @return tree sequence builder
   */
  protected final TreeSeqBuilder tree(final Item item, final Job job) {
    return new TreeSeqBuilder().add(value(null), job).add(item);
  }
}
