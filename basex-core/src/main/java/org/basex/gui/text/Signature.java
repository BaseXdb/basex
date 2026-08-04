package org.basex.gui.text;

import org.basex.util.list.*;

/**
 * Signature of a function: the argument string of its code completion, and the positions of the
 * parameter names it contains.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param args argument string, enclosed in parentheses
 * @param starts start positions of the parameter names
 * @param ends end positions of the parameter names
 */
record Signature(String args, int[] starts, int[] ends) {
  /**
   * Returns the signature for the specified argument string.
   * @param args argument string, enclosed in parentheses
   * @return signature
   */
  static Signature get(final String args) {
    // the parameter names are the character sequences between the separators
    final IntList starts = new IntList(), ends = new IntList();
    final int al = args.length();
    for(int a = 0; a < al; a++) {
      if(!name(args.charAt(a))) continue;
      starts.add(a);
      while(++a < al && name(args.charAt(a)));
      ends.add(a);
    }
    return new Signature(args, starts.finish(), ends.finish());
  }

  /**
   * Returns the number of parameters.
   * @return number of parameters
   */
  int params() {
    return starts.length;
  }

  /**
   * Returns the index of the parameter with the specified name.
   * @param name parameter name
   * @return index, or {@code -1} if the parameter does not exist
   */
  int param(final String name) {
    final int ps = params(), nl = name.length();
    for(int p = 0; p < ps; p++) {
      if(ends[p] - starts[p] == nl && args.regionMatches(starts[p], name, 0, nl)) return p;
    }
    return -1;
  }

  /**
   * Indicates if the specified character is part of a parameter name.
   * @param ch character
   * @return result of check
   */
  private static boolean name(final char ch) {
    return Character.isLetterOrDigit(ch) || ch == '-';
  }
}
