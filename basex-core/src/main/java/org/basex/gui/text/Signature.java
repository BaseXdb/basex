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
   * Returns the argument string for the specified parameters.
   * @param params parameter names
   * @param optional optionality of the parameters
   * @return argument string, enclosed in parentheses, with question marks after optional names
   */
  static String args(final StringList params, final BoolList optional) {
    final StringBuilder args = new StringBuilder().append('(');
    final int ps = params.size();
    for(int p = 0; p < ps; p++) {
      if(p > 0) args.append(", ");
      args.append(params.get(p));
      if(optional.get(p)) args.append('?');
    }
    return args.append(')').toString();
  }

  /**
   * Merges this signature with the signature of another arity of the same function.
   * @param other other signature
   * @return merged signature, or {@code null} if the parameter names differ
   */
  Signature merge(final Signature other) {
    final Signature longer = params() < other.params() ? other : this;
    final Signature shorter = longer == this ? other : this;
    for(int p = 0; p < shorter.params(); p++) {
      if(!shorter.name(p).equals(longer.name(p))) return null;
    }
    // parameters beyond the smallest arity are optional
    final int min = Math.min(required(), other.required()), ps = longer.params();
    final StringList params = new StringList(ps);
    final BoolList optional = new BoolList(ps);
    for(int p = 0; p < ps; p++) {
      params.add(longer.name(p));
      optional.add(p >= min);
    }
    return get(args(params, optional));
  }

  /**
   * Returns the name of the specified parameter.
   * @param p index of the parameter
   * @return name
   */
  String name(final int p) {
    return args.substring(starts[p], ends[p]);
  }

  /**
   * Returns the number of required parameters.
   * @return number of parameters without a trailing question mark
   */
  private int required() {
    final int ps = params();
    int r = 0;
    while(r < ps && args.charAt(ends[r]) != '?') r++;
    return r;
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
