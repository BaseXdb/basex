package org.basex.query.func;

import java.util.*;
import java.util.function.*;

import org.basex.core.users.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Definition of a built-in function.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FuncDefinition {
  /** Result type. */
  public final SeqType seqType;
  /** Name of function. */
  public final QNm name;
  /** Parameter names. */
  public final QNm[] params;
  /** Parameter types. */
  public final SeqType[] types;

  /** Function constructor. */
  final Supplier<? extends StandardFunc> supplier;
  /** Minimum and maximum number of arguments. */
  final int[] minMax;
  /** Minimum permission. */
  final Perm perm;

  /** Descriptive parameter string. */
  private final String paramString;
  /** Compiler flags. */
  private final EnumSet<Flag> flags;

  /**
   * Constructs a function signature.
   * @param supplier function implementation constructor
   * @param string descriptive function string, containing the function name and its arguments
   *   in parentheses. Optional arguments are represented in nested square brackets; three dots
   *   indicate that the number of arguments of a function is not limited
   * @param types parameter types
   * @param seqType return type
   * @param flags static function properties
   * @param uri URI
   * @param perm minimum permission
   */
  FuncDefinition(final Supplier<? extends StandardFunc> supplier, final String string,
      final SeqType[] types, final SeqType seqType, final EnumSet<Flag> flags, final byte[] uri,
      final Perm perm) {

    this.supplier = supplier;
    this.seqType = seqType;
    this.types = types;
    this.flags = flags;
    this.perm = perm;

    final int s = string.indexOf('(');
    name = new QNm(NSGlobal.prefix(uri), Token.token(string.substring(0, s)), uri);
    paramString = string.substring(s + 1).replace(")", "");
    minMax = minMax(paramString);

    final String[] prms = Strings.split(paramString.replaceAll("[.\\[\\]]", ""), ',');
    final int pl = prms.length == 1 && prms[0].isEmpty() ? 0 : prms.length;
    params = new QNm[pl];
    for(int p = 0; p < pl; p++) params[p] = new QNm(prms[p]);

    // treat updating expressions as nondeterministic
    if(flags.contains(Flag.UPD)) flags.add(Flag.NDT);
  }

  /**
   * Computes the minimum and maximum number of parameters by analyzing the description string.
   * @param paramString parameter string
   * @return min/max values
   */
  public static int[] minMax(final String paramString) {
    boolean optional = false;
    int min = 0, max = -1;
    for(int d = 0; d < paramString.length(); d++) {
      final char ch = paramString.charAt(d);
      if(ch == ',') {
        if(optional) {
          max++;
        } else {
          min++;
        }
        d++;
      } else if(ch == '[') {
        optional = true;
        max = min;
      } else if(ch == '.') {
        if(!optional) min -= 1;
        max = Integer.MAX_VALUE;
        break;
      } else if(optional) {
        if(max == min) max++;
      } else if(min == 0) {
        min = 1;
      }
    }
    if(max == -1) max = min;
    return new int[] { min, max };
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
    if(arity != 0 && variadic()) {
      final int tl = types.length;
      Array.copy(types, tl, st);
      final SeqType var = types[tl - 1];
      for(int t = tl; t < arity; t++) st[t] = var;
    } else {
      Array.copy(types, arity, st);
    }
    return FuncType.get(anns, seqType, st);
  }

  /**
   * Returns the parameter names for an instance of this function with the given arity.
   * @param arity number of arguments
   * @return names of parameters
   */
  QNm[] paramNames(final int arity) {
    final QNm[] qnames = new QNm[arity];
    final int nl = params.length;
    for(int n = Math.min(arity, nl); --n >= 0;) {
      qnames[n] = params[n];
    }
    if(arity > nl) {
      final String nm = Token.string(params[nl - 1].local());
      final int start = 1;
      for(int n = nl; n < arity; n++) {
        qnames[n] = new QNm(nm + (start + n - nl), "");
      }
    }
    return qnames;
  }

  /**
   * Indicates if this is a variadic function.
   * @return result of check
   */
  public boolean variadic() {
    return minMax[1] == Integer.MAX_VALUE;
  }

  /**
   * Creates a new function instance.
   * @param info input info (can be {@code null})
   * @param args function arguments
   * @return function
   */
  public StandardFunc get(final InputInfo info, final Expr... args) {
    final StandardFunc sf = supplier.get();
    sf.init(info, this, args);
    return sf;
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
    final TokenBuilder tb = new TokenBuilder().add(' ').add(name.string()).add('(');
    int c = 0;
    for(final Object arg : args) {
      if(c++ > 0) tb.add(", ");
      if(arg instanceof Expr || arg instanceof Number) {
        tb.add(arg);
      } else if(arg instanceof Boolean) {
        tb.add(arg + "()");
      } else {
        final String str = arg.toString();
        if(Strings.startsWith(str, ' ')) {
          tb.add(str.substring(1));
        } else {
          tb.add('"' + str.replace("\"", "\"\"") + '"');
        }
      }
    }
    return tb.add(')').toString();
  }

  @Override
  public String toString() {
    return Strings.concat(name.string(), '(', paramString, ')');
  }
}
