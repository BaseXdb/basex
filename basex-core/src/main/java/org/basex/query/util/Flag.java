package org.basex.query.util;

import org.basex.core.*;
import org.basex.util.*;

/**
 * Expression properties that influence query compilation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public enum Flag {
  /**
   * Node creation. No relocation of expressions that would change number of node constructions
   * Example: node constructor.
   */
  CNS,
  /**
   * Context dependency. Checked to prevent relocations of expressions to different context.
   * Example: context item ({@code .}).
   */
  CTX,
  /**
   * Non-deterministic code. Cannot be relocated, pre-evaluated or optimized away.
   * Examples: random:double(), file:write().
   */
  NDT,
  /**
   * Positional access. Prevents simple iterative evaluation.
   * Each expression that contains this flag must also contain {@link #CTX}.
   * Examples: position(), last().
   */
  POS,
  /**
   * Performs updates. Checked to detect if an expression is updating or not, or if code
   * can be optimized away when using {@link MainOptions#MIXUPDATES}.
   * All updating expressions are non-deterministic.
   * Example: delete node.
   */
  UPD,
  /**
   * Function invocation. Used to suppress pre-evaluation of built-in functions with
   * functions arguments.
   * Example: fn:fold-left.
   */
  HOF;

  /**
   * Removes this flag from the specified array.
   * @param flags flags
   * @return new array
   */
  public Flag[] remove(final Flag[] flags) {
    final int i = indexOf(flags);
    return i == -1 ? flags : Array.remove(flags, i);
  }

  /**
   * Checks this flag is contained in the specified array.
   * @param flags flags
   * @return result of check
   */
  public boolean in(final Flag[] flags) {
    return indexOf(flags) != -1;
  }

  /**
   * Returns the index of this flag in the array.
   * @param flags flags
   * @return index or {@code -1}
   */
  private int indexOf(final Flag[] flags) {
    final int fl = flags.length;
    for(int f = 0; f < fl; f++) {
      if(flags[f] == this) return f;
    }
    return -1;
  }
}
