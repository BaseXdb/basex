package org.basex.query.util;

import org.basex.core.*;
import org.basex.util.*;

/**
 * Expression properties that influence query compilation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum Flag {
  /**
   * Node creation. No relocation of expressions that would change number of node constructions
   * Example: node constructor
   */
  CNS,
  /**
   * Context value dependency. Checked to prevent relocations of expressions to different context.
   * Example: context value reference ({@code .}).
   */
  CTX,
  /**
   * Nondeterministic code. Cannot be relocated, pre-evaluated or optimized away.
   * Examples: random:double(), file:write()
   */
  NDT,
  /**
   * Positional access. Prevents simple iterative evaluation.
   * Each expression that contains this flag must also contain {@link #CTX}.
   * Examples: position(), last()
   */
  POS,
  /**
   * Performs updates. Checked to detect if an expression is updating or not, or if code
   * can be optimized away when using {@link MainOptions#MIXUPDATES}.
   * All updating expressions are nondeterministic.
   * Example: delete node
   */
  UPD,
  /**
   * Function invocation. Used to suppress pre-evaluation of built-in functions with
   * functions arguments.
   * Example: fn:fold-left
   */
  HOF,
  /**
   * Checked to detect if an expression modifies the query focus.
   * Examples: simple map, filter, path, transform with
   */
  FCS;

  /**
   * Checks if this is one of the specified candidates.
   * @param candidates candidates
   * @return result of check
   */
  public boolean oneOf(final Flag... candidates) {
    return Enums.oneOf(this, candidates);
  }

  /**
   * Removes flags from a flags array.
   * @param flags flags
   * @param remove flags to be removed
   * @return new array
   */
  public static Flag[] remove(final Flag[] flags, final Flag... remove) {
    Flag[] flgs = flags;
    for(int f = flgs.length - 1; f >= 0; f--) {
      for(int r = remove.length - 1; r >= 0; r--) {
        if(remove[r] == flgs[f]) {
          flgs = Array.remove(flgs, f);
          break;
        }
      }
    }
    return flgs;
  }
}
