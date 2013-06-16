package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.lang.reflect.*;
import java.lang.reflect.Array;
import java.math.*;
import java.net.*;

import javax.xml.datatype.*;
import javax.xml.namespace.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.pkg.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.Type;
import org.basex.util.*;
import org.w3c.dom.*;
import org.w3c.dom.Text;

/**
 * This class contains common methods for executing Java code and mapping
 * Java objects to XQuery values.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class JavaMapping extends Arr {
  /** New keyword. */
  static final String NEW = "new";
  /** Input Java types. */
  private static final Class<?>[] JAVA = {
    String.class,     boolean.class,    Boolean.class, byte.class,     Byte.class,
    short.class,      Short.class,      int.class,     Integer.class,  long.class,
    Long.class,       float.class,      Float.class,   double.class,   Double.class,
    BigDecimal.class, BigInteger.class, QName.class,   char.class,     Character.class,
    URI.class,        URL.class
  };
  /** Resulting XQuery types. */
  private static final Type[] XQUERY = {
    AtomType.STR, AtomType.BLN, AtomType.BLN, AtomType.BYT, AtomType.BYT,
    AtomType.SHR, AtomType.SHR, AtomType.INT, AtomType.INT, AtomType.LNG,
    AtomType.LNG, AtomType.FLT, AtomType.FLT, AtomType.DBL, AtomType.DBL,
    AtomType.DEC, AtomType.ITR, AtomType.QNM, AtomType.STR, AtomType.STR,
    AtomType.URI, AtomType.URI
  };

  /**
   * Constructor.
   * @param ii input info
   * @param a arguments
   */
  JavaMapping(final InputInfo ii, final Expr[] a) {
    super(ii, a);
  }

  @Override
  public final Iter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public final Value value(final QueryContext ctx) throws QueryException {
    final int es = expr.length;
    final Value[] args = new Value[es];
    for(int e = 0; e < es; ++e) args[e] = ctx.value(expr[e]);
    return toValue(eval(args, ctx), ctx);
  }

  /**
   * Returns the result of the evaluated Java function.
   * @param args arguments
   * @param ctx query context
   * @return arguments
   * @throws QueryException query exception
   */
  protected abstract Object eval(final Value[] args, final QueryContext ctx)
      throws QueryException;

  /**
   * Converts the specified result to an XQuery value.
   * @param obj result object
   * @param ctx query context
   * @return value
   * @throws QueryException query exception
   */
  public static Value toValue(final Object obj, final QueryContext ctx)
      throws QueryException {

    if(obj == null) return Empty.SEQ;
    if(obj instanceof Value) return (Value) obj;
    if(obj instanceof Iter) return ((Iter) obj).value();
    // find XQuery mapping for specified type
    final Type type = type(obj);
    if(type != null) return type.cast(obj, ctx, null);

    // primitive arrays
    if(obj instanceof byte[])    return BytSeq.get((byte[]) obj);
    if(obj instanceof long[])    return IntSeq.get((long[]) obj, AtomType.ITR);
    if(obj instanceof char[])    return Str.get(new String((char[]) obj));
    if(obj instanceof boolean[]) return BlnSeq.get((boolean[]) obj);
    if(obj instanceof double[])  return DblSeq.get((double[]) obj);
    if(obj instanceof float[])   return FltSeq.get((float[]) obj);

    // no array: return Java type
    if(!obj.getClass().isArray()) return new Jav(obj, ctx);
    final int s = Array.getLength(obj);
    // empty array
    if(s == 0) return Empty.SEQ;
    // string array
    if(obj instanceof String[]) {
      final String[] r = (String[]) obj;
      final byte[][] b = new byte[r.length][];
      for(int v = 0; v < s; v++) b[v] = Token.token(r[v]);
      return StrSeq.get(b);
    }
    // character array
    if(obj instanceof char[][]) {
      final char[][] r = (char[][]) obj;
      final byte[][] b = new byte[r.length][];
      for(int v = 0; v < s; v++) b[v] = Token.token(new String(r[v]));
      return StrSeq.get(b);
    }
    // short array
    if(obj instanceof short[]) {
      final short[] r = (short[]) obj;
      final long[] b = new long[r.length];
      for(int v = 0; v < s; v++) b[v] = r[v];
      return IntSeq.get(b, AtomType.SHR);
    }
    // integer array
    if(obj instanceof int[]) {
      final int[] r = (int[]) obj;
      final long[] b = new long[r.length];
      for(int v = 0; v < s; v++) b[v] = r[v];
      return IntSeq.get(b, AtomType.INT);
    }
    // any other array (also nested ones)
    final Object[] objs = (Object[]) obj;
    final ValueBuilder vb = new ValueBuilder(objs.length);
    for(final Object o : objs) vb.add(toValue(o, ctx));
    return vb.value();
  }

  /**
   * Gets the specified method from a query module.
   * @param mod query module object
   * @param path path of the module
   * @param name method name
   * @param arity number of arguments
   * @param ctx query context
   * @param ii input info
   * @return method if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  private static Method getModMethod(final Object mod, final String path,
      final String name, final long arity, final QueryContext ctx, final InputInfo ii)
          throws QueryException {
    // find method with identical name and arity
    Method meth = null;
    for(final Method m : mod.getClass().getMethods()) {
      if(m.getName().equals(name) && m.getParameterTypes().length == arity) {
        if(meth != null) throw JAVAAMBIG.thrw(ii, path + ':' + name);
        meth = m;
      }
    }
    if(meth == null) throw FUNCJAVA.thrw(ii, path + ':' + name);

    // check if user has sufficient permissions to call the function
    Perm perm = Perm.ADMIN;
    final QueryModule.Requires req = meth.getAnnotation(QueryModule.Requires.class);
    if(req != null) perm = Perm.get(req.value().name());
    if(!ctx.context.user.has(perm)) return null;
    return meth;
  }

  /**
   * Converts a module URI to a path.
   * @param uri module URI
   * @return module path
   */
  private static String toPath(final byte[] uri) {
    final String path = string(uri), p = ModuleLoader.uri2path(path);
    return p == null ? path : ModuleLoader.capitalize(p).replace("/", ".").substring(1);
  }

  /**
   * Converts the given name to camel case.
   * @param ln name to convert
   * @return resulting name
   */
  private static String camelCase(final byte[] ln) {
    final TokenBuilder tb = new TokenBuilder();
    boolean dash = false;
    for(int p = 0; p < ln.length; p += cl(ln, p)) {
      final int ch = cp(ln, p);
      if(dash) {
        tb.add(Character.toUpperCase(ch));
        dash = false;
      } else {
        dash = ch == '-';
        if(!dash) tb.add(ch);
      }
    }
    return tb.toString();
  }

  /**
   * Returns a new Java function instance.
   * @param qname function name
   * @param args arguments
   * @param ctx query context
   * @param ii input info
   * @return Java function, or {@code null}
   * @throws QueryException query exception
   */
  static JavaMapping get(final QNm qname, final Expr[] args, final QueryContext ctx,
      final InputInfo ii) throws QueryException {

    final byte[] uri = qname.uri();
    // check if URI starts with "java:" prefix (if yes, module must be Java code)
    final boolean java = startsWith(uri, JAVAPREF);

    // rewrite function name: convert dashes to upper-case initials
    final String name = camelCase(qname.local());

    // check imported Java modules
    final String path = toPath(java ? substring(uri, JAVAPREF.length) : uri);

    final Object jm  = ctx.modules.findImport(path);
    if(jm != null) {
      final Method meth = getModMethod(jm, path, name, args.length, ctx, ii);
      if(meth != null) return new JavaModuleFunc(ii, jm, meth, args);
    }

    // only allowed with administrator permissions
    if(!ctx.context.user.has(Perm.ADMIN)) return null;

    // check addressed class
    try {
      final Class<?> clz = ctx.modules.findClass(path);
      return new JavaFunc(ii, clz, name, args);
    } catch(final ClassNotFoundException ex) {
      // only throw exception if "java:" prefix was explicitly specified
      if(java) throw FUNCJAVA.thrw(ii, uri);
    } catch(final Throwable th) {
      throw JAVAINIT.thrw(ii, th);
    }

    // no function found
    return null;
  }

  /**
   * Returns an appropriate XQuery data type for the specified Java object.
   * @param o object
   * @return xquery type, or {@code null} if no appropriate type was found
   */
  public static Type type(final Object o) {
    final Type t = type(o.getClass());
    if(t != null) return t;

    if(o instanceof Element) return NodeType.ELM;
    if(o instanceof Document) return NodeType.DOC;
    if(o instanceof DocumentFragment) return NodeType.DOC;
    if(o instanceof Attr) return NodeType.ATT;
    if(o instanceof Comment) return NodeType.COM;
    if(o instanceof ProcessingInstruction) return NodeType.PI;
    if(o instanceof Text) return NodeType.TXT;

    if(o instanceof Duration) {
      final Duration d = (Duration) o;
      return !d.isSet(DatatypeConstants.YEARS) && !d.isSet(DatatypeConstants.MONTHS)
          ? AtomType.DTD : !d.isSet(DatatypeConstants.HOURS) &&
          !d.isSet(DatatypeConstants.MINUTES) && !d.isSet(DatatypeConstants.SECONDS)
          ? AtomType.YMD : AtomType.DUR;
    }

    if(o instanceof XMLGregorianCalendar) {
      final QName type = ((XMLGregorianCalendar) o).getXMLSchemaType();
      if(type == DatatypeConstants.DATE) return AtomType.DAT;
      if(type == DatatypeConstants.DATETIME) return AtomType.DTM;
      if(type == DatatypeConstants.TIME) return AtomType.TIM;
      if(type == DatatypeConstants.GYEARMONTH) return AtomType.YMO;
      if(type == DatatypeConstants.GMONTHDAY) return AtomType.MDA;
      if(type == DatatypeConstants.GYEAR) return AtomType.YEA;
      if(type == DatatypeConstants.GMONTH) return AtomType.MON;
      if(type == DatatypeConstants.GDAY) return AtomType.DAY;
    }
    return null;
  }

  /**
   * Returns an appropriate XQuery data type for the specified Java class.
   * @param type Java type
   * @return xquery type
   */
  protected static Type type(final Class<?> type) {
    for(int j = 0; j < JAVA.length; ++j) {
      if(JAVA[j].isAssignableFrom(type)) return XQUERY[j];
    }
    return null;
  }

  /**
   * Returns a string representation of all found arguments.
   * @param args array with arguments
   * @return string representation
   */
  protected static String foundArgs(final Value[] args) {
    // compose found arguments
    final StringBuilder sb = new StringBuilder();
    for(final Value v : args) {
      if(sb.length() != 0) sb.append(", ");
      sb.append(v instanceof Jav ? ((Jav) v).toJava().getClass().getSimpleName() :
        v.type());
    }
    return sb.toString();
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.NDT || super.has(flag);
  }
}
