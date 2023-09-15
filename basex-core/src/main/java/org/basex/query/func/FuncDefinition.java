package org.basex.query.func;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.core.users.*;
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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FuncDefinition {
  /** Result type. */
  public final SeqType seqType;

  /** Function constructor. */
  final Supplier<? extends StandardFunc> supplier;
  /** Minimum and maximum number of arguments. */
  final int[] minMax;
  /** Parameter types. */
  final SeqType[] types;
  /** Parameter names. */
  final QNm[] names;
  /** URI. */
  final byte[] uri;
  /** Minimum permission. */
  final Perm perm;

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
   * @param types parameter types
   * @param seqType return type
   * @param flags static function properties
   * @param uri uri
   * @param perm minimum permission
   */
  FuncDefinition(final Supplier<? extends StandardFunc> supplier, final String desc,
      final SeqType[] types, final SeqType seqType, final EnumSet<Flag> flags, final byte[] uri,
      final Perm perm) {

    this.supplier = supplier;
    this.desc = desc;
    this.seqType = seqType;
    this.types = types;
    this.flags = flags;
    this.uri = uri;
    this.perm = perm;
    minMax = minMax(desc);

    // extract parameter names from function descriptions
    final int p = desc.indexOf('(');
    final String[] tmp = Strings.split(desc.substring(p + 1).replaceAll("[\\.\\[\\])]", ""), ',');
    final int tl = tmp.length == 1 && tmp[0].isEmpty() ? 0 : tmp.length;
    names = new QNm[tl];
    for(int n = 0; n < tl; n++) names[n] = new QNm(tmp[n]);

    // treat updating expressions as non-deterministic
    if(flags.contains(Flag.UPD)) flags.add(Flag.NDT);
  }

  /**
   * Computes the minimum and maximum number of parameters by analyzing the description string.
   * @param desc description
   * @return min/max values
   */
  public static int[] minMax(final String desc) {
    boolean optional = false;
    int min = 0, max = -1;
    for(int d = desc.indexOf('(') + 1;; d++) {
      if(d == desc.length()) {
        throw Util.notExpected(desc);
      }
      final char ch = desc.charAt(d);
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
      } else if(ch == ')') {
        break;
      } else {
        if(optional) {
          if(max == min) max++;
        } else {
          if(min == 0) min = 1;
        }
      }
    }
    if(max == -1) max = min;
    return new int[] { min, max };
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
    final int params = names.length;
    for(int n = Math.min(arity, params); --n >= 0;) {
      qnames[n] = names[n];
    }
    if(arity > params) {
      final String name = Token.string(names[params - 1].local());
      final int start = 1;
      for(int n = params; n < arity; n++) {
        qnames[n] = new QNm(name + (start + n - params), "");
      }
    }
    return qnames;
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
   * Indicates if this is a variadic function.
   * @return result of check
   */
  public boolean variadic() {
    return minMax[1] == Integer.MAX_VALUE;
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
