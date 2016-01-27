package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.lang.reflect.*;
import java.util.*;

import javax.xml.datatype.*;
import javax.xml.namespace.*;

import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.QueryModule.*;
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
 * @author BaseX Team 2005-16, BSD License
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
    super(info, args);
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
    if(!qc.context.user().has(perm)) throw BASX_PERM_X.get(info, perm);

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

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.NDT || super.has(flag);
  }

  // STATIC METHODS ===============================================================================

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
    final int s = java.lang.reflect.Array.getLength(obj);
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
    final ValueBuilder vb = new ValueBuilder();
    for(final Object o : objs) vb.add(toValue(o, qc, sc));
    return vb.value();
  }

  /**
   * Returns a new Java function instance.
   * @param qname function name
   * @param args arguments
   * @param qc query context
   * @param sc static context
   * @param ii input info
   * @return Java function or {@code null}
   * @throws QueryException query exception
   */
  static JavaFunction get(final QNm qname, final Expr[] args, final QueryContext qc,
      final StaticContext sc, final InputInfo ii) throws QueryException {

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

    // try to find function in imported Java modules
    final ModuleLoader modules = qc.resources.modules();
    final Object module  = modules.findModule(className);
    if(module != null) {
      final Method meth = moduleMethod(module, name, args.length, types, qc, ii);
      if(meth != null) {
        final Requires req = meth.getAnnotation(Requires.class);
        final Perm perm = req == null ? Perm.ADMIN :
          Perm.get(req.value().name().toLowerCase(Locale.ENGLISH));
        return new JavaModuleFunc(sc, ii, module, meth, args, perm);
      }
    }

    // try to find matching Java variable or method
    try {
      final Class<?> clazz = modules.findClass(className);
      if(name.equals(NEW) || exists(clazz, name))
        return new JavaFunc(sc, ii, clazz, name, types, args);
    } catch(final ClassNotFoundException ex) {
      if(java) Util.debug(ex);
    } catch(final Throwable th) {
      throw JAVAINIT_X_X.get(ii, Util.className(th), th);
    }

    // no function found: raise error only if "java:" prefix was specified
    if(java) throw JAVAWHICH_X_X_X.get(ii, className, name, args.length);
    return null;
  }

  /**
   * Gets the specified method from a query module.
   * @param module query module object
   * @param name method name
   * @param arity number of arguments
   * @param types types provided in the query (can be {@code null})
   * @param qc query context
   * @param ii input info
   * @return method if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  private static Method moduleMethod(final Object module, final String name, final int arity,
      final String[] types, final QueryContext qc, final InputInfo ii) throws QueryException {

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
      if(meth != null) throw JAVAAMB_X_X_X.get(ii, clazz.getName(), name, arity);
      meth = m;
    }

    if(meth == null) {
      if(methArity != -1) throw JAVAARITY_X_X_X_X_X.get(ii,
          clazz.getName(), name, methArity, arity, arity == 1 ? "" : "s");
      throw JAVAWHICH_X_X_X.get(ii, clazz.getName(), name, arity);
    }

    // Add module locks to QueryContext.
    final Lock lock = meth.getAnnotation(Lock.class);
    if(lock != null) {
      for(final String read : lock.read()) qc.readLocks.add(DBLocking.MODULE_PREFIX + read);
      for(final String write : lock.write()) qc.writeLocks.add(DBLocking.MODULE_PREFIX + write);
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
   * @param o object
   * @return item type or {@code null} if no appropriate type was found
   */
  private static Type type(final Object o) {
    final Type t = JavaMapping.type(o.getClass(), true);
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
   * Returns a string representation of all found arguments.
   * @param args array with arguments
   * @return string representation
   */
  protected static String foundArgs(final Value[] args) {
    // compose found arguments
    final StringBuilder sb = new StringBuilder();
    for(final Value v : args) {
      if(sb.length() != 0) sb.append(", ");
      sb.append(v instanceof Jav ? Util.className(((Jav) v).toJava()) : v.seqType());
    }
    return sb.toString();
  }

  /**
   * Converts the arguments to values that match the specified function parameters.
   * {@code null} is returned if conversion is not possible.
   * @param pTypes parameter types
   * @param vTypes indicates which parameter types are values
   * @param args arguments
   * @param stat static flag
   * @return converted arguments, or {@code null}
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
