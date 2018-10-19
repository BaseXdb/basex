package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.lang.reflect.*;
import java.lang.reflect.Array;
import java.util.*;

import javax.xml.datatype.*;
import javax.xml.namespace.*;

import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.QueryModule.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
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
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public abstract class JavaFunction extends Arr {
  /** New keyword. */
  static final String NEW = "new";

  /** Static context. */
  final StaticContext sc;
  /** Permission. */
  final Perm perm;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param args arguments
   * @param perm required permission to run the function
   */
  JavaFunction(final StaticContext sc, final InputInfo info, final Expr[] args, final Perm perm) {
    super(info, SeqType.ITEM_ZM, args);
    this.sc = sc;
    this.perm = perm;
  }

  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    // check permission
    if(!qc.context.user().has(perm)) throw BASEX_PERMISSION_X_X.get(info, perm, this);

    final int es = exprs.length;
    final Value[] args = new Value[es];
    for(int e = 0; e < es; ++e) args[e] = exprs[e].value(qc);
    return toValue(eval(args, qc), qc, sc);
  }

  /**
   * Returns the result of the evaluated Java function.
   * @param args arguments
   * @param qc query context
   * @return arguments
   * @throws QueryException query exception
   */
  protected abstract Object eval(Value[] args, QueryContext qc) throws QueryException;

  @Override
  public boolean has(final Flag... flags) {
    return Flag.NDT.in(flags) || super.has(flags);
  }

  // STATIC METHODS ===============================================================================

  /**
   * Converts the specified result to an XQuery value.
   * @param object result object
   * @param qc query context
   * @param sc static context
   * @return value
   * @throws QueryException query exception
   */
  public static Value toValue(final Object object, final QueryContext qc, final StaticContext sc)
      throws QueryException {

    if(object == null) return Empty.SEQ;
    if(object instanceof Value) return (Value) object;
    if(object instanceof Iter) return ((Iter) object).value(qc);
    // find XQuery mapping for specified type
    final Type type = type(object);
    if(type != null) return type.cast(object, qc, sc, null);

    // primitive arrays
    if(object instanceof byte[])    return BytSeq.get((byte[]) object);
    if(object instanceof long[])    return IntSeq.get((long[]) object, AtomType.ITR);
    if(object instanceof char[])    return Str.get(new String((char[]) object));
    if(object instanceof boolean[]) return BlnSeq.get((boolean[]) object);
    if(object instanceof double[])  return DblSeq.get((double[]) object);
    if(object instanceof float[])   return FltSeq.get((float[]) object);

    // no array: return Java type
    if(!object.getClass().isArray()) return new Jav(object, qc);

    // empty array
    final int s = Array.getLength(object);
    if(s == 0) return Empty.SEQ;
    // string array
    if(object instanceof String[]) {
      final String[] r = (String[]) object;
      final byte[][] b = new byte[r.length][];
      for(int v = 0; v < s; v++) b[v] = token(r[v]);
      return StrSeq.get(b);
    }
    // character array
    if(object instanceof char[][]) {
      final char[][] r = (char[][]) object;
      final byte[][] b = new byte[r.length][];
      for(int v = 0; v < s; v++) b[v] = token(new String(r[v]));
      return StrSeq.get(b);
    }
    // short array
    if(object instanceof short[]) {
      final short[] r = (short[]) object;
      final long[] b = new long[r.length];
      for(int v = 0; v < s; v++) b[v] = r[v];
      return IntSeq.get(b, AtomType.SHR);
    }
    // integer array
    if(object instanceof int[]) {
      final int[] r = (int[]) object;
      final long[] b = new long[r.length];
      for(int v = 0; v < s; v++) b[v] = r[v];
      return IntSeq.get(b, AtomType.INT);
    }
    // any other array (also nested ones)
    final Object[] objects = (Object[]) object;
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Object obj : objects) vb.add(toValue(obj, qc, sc));
    return vb.value();
  }

  /**
   * Returns a new Java function instance.
   * @param qname function name
   * @param args arguments
   * @param qc query context
   * @param sc static context
   * @param info input info
   * @return Java function or {@code null}
   * @throws QueryException query exception
   */
  static JavaFunction get(final QNm qname, final Expr[] args, final QueryContext qc,
      final StaticContext sc, final InputInfo info) throws QueryException {

    // rewrite function name, extract argument types
    String name = Strings.camelCase(string(qname.local()));
    String[] types = null;
    final int n = name.indexOf('\u00b7');
    if(n != -1) {
      types = Strings.split(name.substring(n + 1), '\u00b7');
      name = name.substring(0, n);
    }
    final String uri = string(qname.uri());

    // check if URI starts with "java:" prefix. if yes, skip rewritings
    final boolean java = uri.startsWith(JAVAPREF);
    final String className = java ? uri.substring(JAVAPREF.length()) :
      Strings.className(Strings.uri2path(uri));

    // function in imported Java module
    final ModuleLoader modules = qc.resources.modules();
    final Object module  = modules.findModule(className);
    if(module != null) {
      final Method meth = moduleMethod(module, name, args.length, types, qc, info);
      final Requires req = meth.getAnnotation(Requires.class);
      final Perm perm = req == null ? Perm.ADMIN :
        Perm.get(req.value().name().toLowerCase(Locale.ENGLISH));
      return new JavaModuleFunc(sc, info, module, meth, args, perm);
    }

    /* skip Java class lookup if...
     * - no java prefix was supplied, and
     * - if URI equals namespace of library module or if it is globally declared
     *
     * examples:
     * - declare function local:f($i) { if($i) then local:f($i - 1) else () }
     * - module namespace _ = '_'; declare function _:_() { _:_() };
     * - fn:does-not-exist(), util:not-available()
     */
    if(java || (sc.module == null || !eq(sc.module.uri(), qname.uri())) &&
        NSGlobal.prefix(qname.uri()).length == 0) {

      // Java constructor, function, or variable
      Class<?> clazz = null;
      try {
        clazz = modules.findClass(className);
      } catch(final ClassNotFoundException ex) {
        if(java) Util.debug(ex);
      } catch(final Throwable th) {
        // catch linkage and other errors as well
        throw JAVAINIT_X_X.get(info, Util.className(th), th);
      }

      if(clazz == null) {
        // class not found, java prefix was specified
        if(java) throw WHICHCLASS_X.get(info, className);
      } else {
        if(name.equals(NEW) || exists(clazz, name))
          return new JavaFunc(sc, info, clazz, name, types, args);
        // function not found, java prefix was specified
        if(java) throw WHICHJAVA_X_X_X.get(info, className, name, args.length);
      }
    }

    return null;
  }

  /**
   * Gets the specified method from a query module.
   * @param module query module object
   * @param name method name
   * @param arity number of arguments
   * @param types types provided in the query (can be {@code null})
   * @param qc query context
   * @param info input info
   * @return method if found
   * @throws QueryException query exception
   */
  private static Method moduleMethod(final Object module, final String name, final int arity,
      final String[] types, final QueryContext qc, final InputInfo info) throws QueryException {

    // find method with identical name and arity
    Method meth = null;
    int methArity = -1;

    final Class<?> clazz = module.getClass();
    for(final Method m : clazz.getMethods()) {
      if(!m.getName().equals(name)) continue;
      final Class<?>[] pTypes = m.getParameterTypes();
      methArity = pTypes.length;
      if(methArity != arity) continue;
      methArity = -1;
      if(!typeMatches(pTypes, types)) continue;
      if(meth != null) throw JAVAAMB_X_X_X.get(info, clazz.getName(), name, arity);
      meth = m;
    }

    if(meth == null) {
      if(methArity != -1) throw JAVAARITY_X_X_X_X.get(
          info, clazz.getName(), name, methArity, arguments(arity));
      throw WHICHJAVA_X_X_X.get(info, clazz.getName(), name, arity);
    }

    // add module locks to QueryContext
    final Lock lock = meth.getAnnotation(Lock.class);
    if(lock != null) {
      for(final String read : lock.read()) qc.readLocks.add(Locking.JAVA_PREFIX + read);
      for(final String write : lock.write()) qc.writeLocks.add(Locking.JAVA_PREFIX + write);
    }
    return meth;
  }

  /**
   * Compares the types of method parameters with the specified types.
   * @param pTypes parameter types
   * @param qTypes query types
   * @return result of check
   */
  protected static boolean typeMatches(final Class<?>[] pTypes, final String[] qTypes) {
    // no query types: accept method
    if(qTypes == null) return true;
    // compare types
    final int pl = pTypes.length;
    if(pl != qTypes.length) return false;
    for(int p = 0; p < pl; p++) {
      if(!qTypes[p].equals(pTypes[p].getName())) return false;
    }
    return true;
  }

  /**
   * Checks if a method or variable with the specified name exists.
   * @param clazz clazz
   * @param name method/variable name
   * @return result of check
   */
  private static boolean exists(final Class<?> clazz, final String name) {
    final int tp = name.indexOf('\u00b7');
    final String nm = tp == -1 ? name : name.substring(0, tp);
    for(final Field f : clazz.getFields()) {
      if(f.getName().equals(nm)) return true;
    }
    for(final Method m : clazz.getMethods()) {
      if(m.getName().equals(nm)) return true;
    }
    return false;
  }

  /**
   * Returns an appropriate XQuery type for the specified Java object.
   * @param object object
   * @return item type or {@code null} if no appropriate type was found
   */
  private static Type type(final Object object) {
    final Type type = JavaMapping.type(object.getClass(), true);
    if(type != null) return type;

    if(object instanceof Element) return NodeType.ELM;
    if(object instanceof Document) return NodeType.DOC;
    if(object instanceof DocumentFragment) return NodeType.DOC;
    if(object instanceof Attr) return NodeType.ATT;
    if(object instanceof Comment) return NodeType.COM;
    if(object instanceof ProcessingInstruction) return NodeType.PI;
    if(object instanceof Text) return NodeType.TXT;

    if(object instanceof Duration) {
      final Duration d = (Duration) object;
      return !d.isSet(DatatypeConstants.YEARS) && !d.isSet(DatatypeConstants.MONTHS)
          ? AtomType.DTD : !d.isSet(DatatypeConstants.HOURS) &&
          !d.isSet(DatatypeConstants.MINUTES) && !d.isSet(DatatypeConstants.SECONDS)
          ? AtomType.YMD : AtomType.DUR;
    }

    if(object instanceof XMLGregorianCalendar) {
      final QName qnm = ((XMLGregorianCalendar) object).getXMLSchemaType();
      if(qnm == DatatypeConstants.DATE) return AtomType.DAT;
      if(qnm == DatatypeConstants.DATETIME) return AtomType.DTM;
      if(qnm == DatatypeConstants.TIME) return AtomType.TIM;
      if(qnm == DatatypeConstants.GYEARMONTH) return AtomType.YMO;
      if(qnm == DatatypeConstants.GMONTHDAY) return AtomType.MDA;
      if(qnm == DatatypeConstants.GYEAR) return AtomType.YEA;
      if(qnm == DatatypeConstants.GMONTH) return AtomType.MON;
      if(qnm == DatatypeConstants.GDAY) return AtomType.DAY;
    }
    return null;
  }

  /**
   * Returns a string representation of all found arguments.
   * @param args arguments
   * @return string representation
   */
  protected static String foundArgs(final Value[] args) {
    // compose found arguments
    final StringBuilder sb = new StringBuilder();
    for(final Value arg : args) {
      if(sb.length() != 0) sb.append(", ");
      sb.append(arg instanceof Jav ? Util.className(((Jav) arg).toJava()) : arg.seqType());
    }
    return sb.toString();
  }

  /**
   * Converts the arguments to values that match the specified function parameters.
   * @param pTypes parameter types
   * @param vTypes indicates which parameter types are values
   * @param args arguments
   * @param stat static flag
   * @return converted arguments, or {@code null} if the conversion is not possible.
   * @throws QueryException query exception
   */
  protected static Object[] javaArgs(final Class<?>[] pTypes, final boolean[] vTypes,
      final Value[] args, final boolean stat) throws QueryException {

    // start with second argument if function is not static
    final int s = stat ? 0 : 1, pl = pTypes.length;
    if(pl != args.length - s) return null;

    // function arguments
    final boolean[] vType = vTypes == null ? values(pTypes) : vTypes;
    final Object[] vals = new Object[pl];
    for(int p = 0; p < pl; p++) {
      final Class<?> param = pTypes[p];
      final Value arg = args[s + p];

      if(arg.type.instanceOf(JavaMapping.type(param, true))) {
        // convert to Java object if an XQuery type exists for the function parameter
        vals[p] = arg.toJava();
      } else {
        // convert to Java object if
        // - argument is of type {@link Jav}, wrapping a Java object, or
        // - function parameter is not of type {@link Value}, or a sub-class of it
        vals[p] = arg instanceof Jav || !vType[p] ? arg.toJava() : arg;
        // check if argument is an instance of the function parameter
        if(!param.isInstance(vals[p])) {
          // if no, check if argument is an empty sequence; otherwise, give up
          if(arg.isEmpty() && !param.isPrimitive()) {
            vals[p] = null;
          } else {
            return null;
          }
        }
      }
    }
    return vals;
  }

  /**
   * Returns a boolean array that indicated which of the specified function parameters are of
   * (sub)class {@link Value}.
   * @param params parameters
   * @return array
   */
  protected static boolean[] values(final Class<?>[] params) {
    final int l = params.length;
    final boolean[] vals = new boolean[l];
    for(int a = 0; a < l; a++) vals[a] = Value.class.isAssignableFrom(params[a]);
    return vals;
  }
}
