package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.lang.reflect.*;
import java.lang.reflect.Array;
import java.math.*;
import java.net.*;
import java.util.*;

import javax.xml.datatype.*;
import javax.xml.namespace.*;

import org.basex.core.locks.*;
import org.basex.core.users.*;
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

/**
 * This class contains common methods for executing Java code and mapping
 * Java objects to XQuery values.
 *
 * @author BaseX Team 2005-14, BSD License
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

  /** Static context. */
  final StaticContext sc;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param args arguments
   */
  JavaMapping(final StaticContext sc, final InputInfo info, final Expr[] args) {
    super(info, args);
    this.sc = sc;
  }

  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final int es = exprs.length;
    final Value[] args = new Value[es];
    for(int e = 0; e < es; ++e) args[e] = qc.value(exprs[e]);
    return toValue(eval(args, qc), qc, sc);
  }

  /**
   * Returns the result of the evaluated Java function.
   * @param args arguments
   * @param qc query context
   * @return arguments
   * @throws QueryException query exception
   */
  protected abstract Object eval(final Value[] args, final QueryContext qc)
      throws QueryException;

  /**
   * Converts the specified result to an XQuery value.
   * @param obj result object
   * @param qc query context
   * @param sc static context
   * @return value
   * @throws QueryException query exception
   */
  public static Value toValue(final Object obj, final QueryContext qc, final StaticContext sc)
      throws QueryException {

    if(obj == null) return Empty.SEQ;
    if(obj instanceof Value) return (Value) obj;
    if(obj instanceof Iter) return ((Iter) obj).value();
    // find XQuery mapping for specified type
    final Type type = type(obj);
    if(type != null) return type.cast(obj, qc, sc, null);

    // primitive arrays
    if(obj instanceof byte[])    return BytSeq.get((byte[]) obj);
    if(obj instanceof long[])    return IntSeq.get((long[]) obj, AtomType.ITR);
    if(obj instanceof char[])    return Str.get(new String((char[]) obj));
    if(obj instanceof boolean[]) return BlnSeq.get((boolean[]) obj);
    if(obj instanceof double[])  return DblSeq.get((double[]) obj);
    if(obj instanceof float[])   return FltSeq.get((float[]) obj);

    // no array: return Java type
    if(!obj.getClass().isArray()) return new Jav(obj, qc);

    // empty array
    final int s = Array.getLength(obj);
    if(s == 0) return Empty.SEQ;
    // string array
    if(obj instanceof String[]) {
      final String[] r = (String[]) obj;
      final byte[][] b = new byte[r.length][];
      for(int v = 0; v < s; v++) b[v] = token(r[v]);
      return StrSeq.get(b);
    }
    // character array
    if(obj instanceof char[][]) {
      final char[][] r = (char[][]) obj;
      final byte[][] b = new byte[r.length][];
      for(int v = 0; v < s; v++) b[v] = token(new String(r[v]));
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
    for(final Object o : objs) vb.add(toValue(o, qc, sc));
    return vb.value();
  }

  /**
   * Gets the specified method from a query module.
   * @param mod query module object
   * @param path path of the module
   * @param name method name
   * @param arity number of arguments
   * @param qc query context
   * @param ii input info
   * @return method if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  private static Method getModMethod(final Object mod, final String path, final String name,
      final long arity, final QueryContext qc, final InputInfo ii) throws QueryException {

    // find method with identical name and arity
    Method meth = null;
    for(final Method m : mod.getClass().getMethods()) {
      if(m.getName().equals(name) && m.getParameterTypes().length == arity) {
        if(meth != null) throw JAVAAMBIG_X.get(ii, "Q{" + path + '}' + name + '#' + arity);
        meth = m;
      }
    }
    if(meth == null) throw FUNCJAVA_X.get(ii, path + ':' + name);

    // check if user has sufficient permissions to call the function
    Perm perm = Perm.ADMIN;
    final QueryModule.Requires req = meth.getAnnotation(QueryModule.Requires.class);
    if(req != null) perm = Perm.get(req.value().name().toLowerCase(Locale.ENGLISH));
    if(!qc.context.user().has(perm)) return null;

    // Add module locks to QueryContext.
    final QueryModule.Lock lock = meth.getAnnotation(QueryModule.Lock.class);
    if(lock != null) {
      for(final String read : lock.read()) qc.readLocks.add(DBLocking.MODULE_PREFIX + read);
      for(final String write : lock.write()) qc.writeLocks.add(DBLocking.MODULE_PREFIX + write);
    }

    return meth;
  }

  /**
   * Converts a module URI to a Java path.
   * @param uri module URI
   * @return module path
   */
  private static String toPath(final byte[] uri) {
    final String path = string(uri), p = ModuleLoader.uri2path(path);
    return p == null ? path : ModuleLoader.capitalize(p).replace("/", ".").substring(1);
  }

  /**
   * Returns a new Java function instance.
   * @param name function name
   * @param args arguments
   * @param qc query context
   * @param sc static context
   * @param ii input info
   * @return Java function or {@code null}
   * @throws QueryException query exception
   */
  static JavaMapping get(final QNm name, final Expr[] args, final QueryContext qc,
      final StaticContext sc, final InputInfo ii) throws QueryException {

    final byte[] uri = name.uri();
    // check if URI starts with "java:" prefix (if yes, module must be Java code)
    final boolean java = startsWith(uri, JAVAPREF);

    // rewrite function name: convert dashes to upper-case initials
    final String local = Strings.camelCase(string(name.local()));

    // check imported Java modules
    final String path = Strings.camelCase(toPath(java ? substring(uri, JAVAPREF.length) : uri));

    final ModuleLoader modules = qc.resources.modules();
    final Object jm  = modules.findImport(path);
    if(jm != null) {
      final Method meth = getModMethod(jm, path, local, args.length, qc, ii);
      if(meth != null) return new JavaModuleFunc(sc, ii, jm, meth, args);
    }

    // only allowed with administrator permissions
    if(!qc.context.user().has(Perm.ADMIN)) return null;

    // check addressed class
    try {
      return new JavaFunc(sc, ii, modules.findClass(path), local, args);
    } catch(final ClassNotFoundException ex) {
      // only throw exception if "java:" prefix was explicitly specified
      if(java) throw FUNCJAVA_X.get(ii, path);
    } catch(final Throwable th) {
      throw JAVAINIT_X.get(ii, th);
    }

    // no function found
    return null;
  }

  /**
   * Returns an appropriate XQuery type for the specified Java object.
   * @param o object
   * @return item type or {@code null} if no appropriate type was found
   */
  private static Type type(final Object o) {
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
   * Returns an appropriate XQuery type for the specified Java class.
   * @param type Java type
   * @return item type or {@code null} if no appropriate type was found
   */
  static Type type(final Class<?> type) {
    final int jl = JAVA.length;
    for(int j = 0; j < jl; ++j) if(JAVA[j] == type) return XQUERY[j];
    return null;
  }

  /**
   * Returns a string representation of all found arguments.
   * @param args array with arguments
   * @return string representation
   */
  static String foundArgs(final Value[] args) {
    // compose found arguments
    final StringBuilder sb = new StringBuilder();
    for(final Value v : args) {
      if(sb.length() != 0) sb.append(", ");
      sb.append(v instanceof Jav ? Util.className(((Jav) v).toJava()) : v.seqType());
    }
    return sb.toString();
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.NDT || super.has(flag);
  }
}
