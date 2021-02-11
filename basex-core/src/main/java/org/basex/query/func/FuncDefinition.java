package org.basex.query.func;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.Supplier;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Definition of built-in function.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FuncDefinition {
  /** Function constructor. */
  final Supplier<? extends StandardFunc> supplier;
  /** Minimum and maximum number of arguments. */
  final int[] minMax;
  /** Parameter types. */
  final SeqType[] params;
  /** Sequence type. */
  public final SeqType seqType;
  /** URI. */
  final byte[] uri;

  /** Description. */
  private final String desc;
  /** Compiler flags. */
  private final EnumSet<Flag> flags;

  /**
   * Constructs a function signature.
   * @param supplier function implementation constructor
   * @param desc descriptive function string, containing the function name and its
   *             arguments in parentheses. Optional arguments are represented in nested
   *             square brackets; three dots indicate that the number of arguments of a
   *             function is not limited
   * @param params parameter types
   * @param seqType return type
   * @param flags static function properties
   * @param uri uri
   */
  FuncDefinition(final Supplier<? extends StandardFunc> supplier, final String desc,
      final SeqType[] params, final SeqType seqType, final EnumSet<Flag> flags, final byte[] uri) {

    this.supplier = supplier;
    this.desc = desc;
    this.seqType = seqType;
    this.params = params;
    this.flags = flags;
    this.uri = uri;
    minMax = minMax(desc, params);

    // treat updating expressions as non-deterministic
    if(flags.contains(Flag.UPD)) flags.add(Flag.NDT);
  }

  /**
   * Computes the minimum and maximum number of arguments by analyzing the description string.
   * @param desc description
   * @param args arguments
   * @return min/max values
   */
  public static int[] minMax(final String desc, final SeqType[] args) {
    // count number of minimum and maximum arguments by analyzing the description
    final int b = desc.indexOf('['), al = args.length;
    if(b == -1) return new int[] { al, al };

    int c = b + 1 < desc.length() && desc.charAt(b + 1) == ',' ? 1 : 0;
    for(int i = 0; i < b; i++) {
      if(desc.charAt(i) == ',') c++;
    }
    return new int[] { c, desc.contains(DOTS) ? Integer.MAX_VALUE : al };
  }

  /**
   * Returns the namespace URI of this function.
   * @return function
   */
  public byte[] uri() {
    return uri;
  }

  /**
   * Indicates if an expression has the specified compiler property.
   * @param flag flag
   * @return result of check
   * @see Expr#has(Flag...)
    */
  public boolean has(final Flag flag) {
    return flags.contains(flag);
  }

  /**
   * Returns the function type of this function with the given arity.
   * @param arity number of arguments
   * @param anns annotations
   * @return function type
   */
  FuncType type(final int arity, final AnnList anns) {
    final SeqType[] st = new SeqType[arity];
    if(arity != 0 && minMax[1] == Integer.MAX_VALUE) {
      final int pl = params.length;
      Array.copy(params, pl, st);
      final SeqType var = params[pl - 1];
      for(int p = pl; p < arity; p++) st[p] = var;
    } else {
      Array.copy(params, arity, st);
    }
    return FuncType.get(anns, seqType, st);
  }

  /**
   * Returns the names of the function parameters.
   * @return names of function parameters
   */
  String[] names() {
    final String names = desc.replaceFirst(".*?\\(", "").replace(",...", "").
        replaceAll("[\\[\\])\\s]", "");
    return names.isEmpty() ? new String[0] : Strings.split(names, ',');
  }

  /**
   * Returns the the parameter names for an instance of this function with the given arity.
   * @param arity number of arguments
   * @return names of parameters
   */
  QNm[] paramNames(final int arity) {
    final String[] strings = names();
    final QNm[] names = new QNm[arity];
    final int nl = strings.length;
    for(int n = Math.min(arity, nl); --n >= 0;) names[n] = new QNm(strings[n]);
    if(arity > nl) {
      final String[] parts = strings[nl - 1].split("(?=\\d+$)", 2);
      final int start = Integer.parseInt(parts[1]);
      for(int n = nl; n < arity; n++) names[n] = new QNm(parts[0] + (start + n - nl + 1), "");
    }
    return names;
  }

  /**
   * Returns the local name of the function.
   * @return name
   */
  public byte[] local() {
    return Token.token(desc.substring(0, desc.indexOf('(')));
  }

  /**
   * Returns the prefixed name of the annotation.
   * @return name
   */
  byte[] id() {
    final TokenBuilder tb = new TokenBuilder();
    if(!Token.eq(uri, FN_URI)) tb.add(NSGlobal.prefix(uri)).add(':');
    return tb.add(local()).finish();
  }

  /**
   * Returns a string representation of the function with the specified
   * arguments. All objects are wrapped with quotes, except for the following ones:
   * <ul>
   * <li>numbers (integer, long, float, double)</li>
   * <li>booleans (which will be suffixed with parentheses)</li>
   * <li>strings starting with a space (which will be chopped)</li>
   * </ul>
   * @param args arguments
   * @return string representation with leading space (simplifies nesting of returned string)
   */
  String args(final Object... args) {
    final TokenBuilder tb = new TokenBuilder();
    for(final Object arg : args) {
      if(!tb.isEmpty()) tb.add(", ");
      if(arg == null) {
        tb.add("()");
      } else if(arg instanceof Expr) {
        tb.add(arg);
      } else if(arg instanceof Number) {
        tb.add(arg);
      } else if(arg instanceof Boolean) {
        tb.add(arg + "()");
      } else {
        final String str = arg.toString();
        if(Strings.startsWith(str, ' ')) {
          tb.add(str.substring(1));
        } else {
          tb.add('"' + str.replaceAll("\"", "\"\"") + '"');
        }
      }
    }
    return ' ' + toString().replaceAll("\\(.*", "(") + tb + ')';
  }

  /**
   * Creates a new function instance.
   * @param sc static context
   * @param ii input info
   * @param args function arguments
   * @return function
   */
  public StandardFunc get(final StaticContext sc, final InputInfo ii, final Expr... args) {
    final StandardFunc sf = supplier.get();
    sf.init(sc, ii, this, args);
    return sf;
  }

  @Override
  public String toString() {
    return Strings.concat(NSGlobal.prefix(uri), ":", desc);
  }
}
