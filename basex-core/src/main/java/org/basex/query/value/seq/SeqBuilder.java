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
  /** Interruptible job. */
  protected final Job job;

  /**
   * Constructor.
   * @param job interruptible job
   */
  protected SeqBuilder(final Job job) {
    this.job = job;
  }

  /**
   * Appends an item.
   * @param item item to append
   * @return reference to this builder for convenience
   */
  protected abstract SeqBuilder add(Item item);

  /**
   * Appends an integer value.
   * @param value value to append
   * @return reference to this builder for convenience
   */
  public SeqBuilder add(final long value) {
    return add(Itr.get(value));
  }

  /**
   * Appends items.
   * @param value items to append
   * @return this builder for convenience
   */
  public final SeqBuilder add(final Value value) {
    return value.size() == 1 ? add((Item) value) : addSequence(value);
  }

  /**
   * Appends a sequence.
   * @param value items to append
   * @return this builder for convenience
   */
  protected SeqBuilder addSequence(final Value value) {
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
   * @return tree sequence builder
   */
  protected final SeqBuilder tree(final Item item) {
    return new TreeSeqBuilder(job).add(value(null)).add(item);
  }
}
