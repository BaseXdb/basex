package org.basex.query.func.java;

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
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.pkg.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.Type;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;
import org.w3c.dom.*;

/**
 * This class contains common methods for executing Java code and mapping
 * Java objects to XQuery values.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class JavaCall extends Arr {
  /** Static context. */
  final StaticContext sc;
  /** Permission. */
  final Perm perm;
  /** Updating flag. */
  final boolean updating;

  /**
   * Constructor.
   * @param args arguments
   * @param perm required permission to run the function
   * @param updating updating flag
   * @param sc static context
   * @param info input info
   */
  JavaCall(final Expr[] args, final Perm perm, final boolean updating, final StaticContext sc,
      final InputInfo info) {
    super(info, SeqType.ITEM_ZM, args);
    this.updating = updating;
    this.sc = sc;
    this.perm = perm;
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    // check permission
    if(!qc.context.user().has(perm)) throw BASEX_PERMISSION_X_X.get(info, perm, this);

    final Value value = toValue(eval(qc), qc, sc);
    if(!updating) return value;

    // updating function: cache output
    qc.updates().addOutput(value, qc);
    return Empty.VALUE;
  }

  /**
   * Returns the result of the evaluated Java function.
   * @param qc query context
   * @return arguments
   * @throws QueryException query exception
   */
  protected abstract Object eval(QueryContext qc) throws QueryException;

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

    if(object == null) return Empty.VALUE;
    if(object instanceof Value) return (Value) object;
    if(object instanceof Iter) return ((Iter) object).value(qc, null);
    // find XQuery mapping for specified type
    final Type type = type(object);
    if(type != null) return type.cast(object, qc, sc, null);

    // primitive arrays
    if(object instanceof byte[])    return BytSeq.get((byte[]) object);
    if(object instanceof long[])    return IntSeq.get((long[]) object);
    if(object instanceof char[])    return Str.get(new String((char[]) object));
    if(object instanceof boolean[]) return BlnSeq.get((boolean[]) object);
    if(object instanceof double[])  return DblSeq.get((double[]) object);
    if(object instanceof float[])   return FltSeq.get((float[]) object);

    // no array: return Java type
    if(!object.getClass().isArray()) return new Jav(object, qc);

    // empty array
    final int s = Array.getLength(object);
    if(s == 0) return Empty.VALUE;
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
      return IntSeq.get(b, AtomType.SHORT);
    }
    // integer array
    if(object instanceof int[]) {
      final int[] r = (int[]) object;
      final long[] b = new long[r.length];
      for(int v = 0; v < s; v++) b[v] = r[v];
      return IntSeq.get(b, AtomType.INT);
    }
    // any other array (also nested ones)
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Object obj : (Object[]) object) vb.add(toValue(obj, qc, sc));
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
  public static JavaCall get(final QNm qname, final Expr[] args, final QueryContext qc,
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
    final boolean enforce = uri.startsWith(JAVAPREF);
    final String className = enforce ? uri.substring(JAVAPREF.length()) :
      Strings.className(Strings.uri2path(uri));

    // function in imported Java module
    final ModuleLoader modules = qc.resources.modules();
    final Object module  = modules.findModule(className);
    if(module != null) {
      final Method meth = moduleMethod(module, name, args.length, types, qname, qc, ii);
      final Requires req = meth.getAnnotation(Requires.class);
      final Perm perm = req == null ? Perm.ADMIN :
        Perm.get(req.value().name().toLowerCase(Locale.ENGLISH));
      final boolean updating = meth.getAnnotation(Updating.class) != null;
      if(updating) qc.updating();
      return new StaticJavaCall(module, meth, args, perm, updating, sc, ii);
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
    if(enforce || (sc.module == null || !eq(sc.module.uri(), qname.uri())) &&
        NSGlobal.prefix(qname.uri()).length == 0) {

      // Java constructor, function, or variable
      Class<?> clazz = null;
      try {
        clazz = modules.findClass(className);
      } catch(final ClassNotFoundException ex) {
        if(enforce) Util.debug(ex);
      } catch(final Throwable th) {
        // catch linkage and other errors as well
        throw JAVAINIT_X_X.get(ii, Util.className(th), th);
      }

      if(clazz == null) {
        // class not found, java prefix was specified
        if(enforce) throw WHICHCLASS_X.get(ii, className);
      } else {
        // constructor
        if(name.equals(NEW)) {
          final DynJavaConstr djc = new DynJavaConstr(clazz, types, args, sc, ii);
          if(djc.init(enforce)) return djc;
        }
        // field or method
        final DynJavaFunc djf = new DynJavaFunc(clazz, name, types, args, sc, ii);
        if(djf.init(enforce)) return djf;
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
   * @param qname original name
   * @param qc query context
   * @param ii input info
   * @return method if found
   * @throws QueryException query exception
   */
  private static Method moduleMethod(final Object module, final String name, final int arity,
      final String[] types, final QNm qname, final QueryContext qc, final InputInfo ii)
      throws QueryException {

    // find method with identical name and arity
    Method method = null;
    final IntList arities = new IntList();
    final Method[] methods = module.getClass().getMethods();
    for(final Method m : methods) {
      if(!m.getName().equals(name)) continue;
      final Class<?>[] pTypes = m.getParameterTypes();
      final int mArity = pTypes.length;
      if(mArity == arity) {
        if(typesMatch(pTypes, types)) {
          if(method != null) throw JAVAMULTIFUNC_X_X.get(ii, qname.string(), arguments(arity));
          method = m;
        }
      } else if(types == null) {
        arities.add(mArity);
      }
    }

    // method found: add module locks to QueryContext
    if(method != null) {
      final Lock lock = method.getAnnotation(Lock.class);
      if(lock != null) qc.locks.add(Locking.BASEX_PREFIX + lock.value());
      return method;
    }

    // no suitable method found: check if method with correct name was found
    final TokenList names = new TokenList();
    for(final Method m : methods) names.add(m.getName());
    throw noFunction(name, arity, string(qname.string()), arities, types, ii, names.finish());
  }

  /**
   * Returns an error message if no fields and methods could be chosen for execution.
   * @param name name of field or method
   * @param arity supplied arity
   * @param full full name of field or method
   * @param arities arities of found methods
   * @param types types (can be {@code null})
   * @param ii input info
   * @param names list of available names
   * @return exception
   */
  static QueryException noFunction(final String name, final int arity, final String full,
      final IntList arities, final String[] types, final InputInfo ii, final byte[][] names) {
    // functions with different arities
    if(!arities.isEmpty()) return Functions.wrongArity(full, arity, arities, ii);

    // find similar field/method names
    final byte[] nm = token(name);
    final Object similar = Levenshtein.similar(nm, names);
    if(similar != null) {
      // if name is equal, no function was chosen via exact type matching
      if(eq(nm, (byte[]) similar)) {
        final TokenBuilder tb = new TokenBuilder();
        for(final String type : types) {
          if(!tb.isEmpty()) tb.add(", ");
          tb.add(type.replaceAll("^.*\\.", ""));
        }
        return JAVAARGS_X_X.get(ii, full, tb);
      }
    }
    return WHICHFUNC_X.get(ii, similar(full, similar));
  }

  /**
   * Compares the types of method parameters with the specified types.
   * @param pTypes parameter types
   * @param qTypes query types (can be {@code null})
   * @return result of check
   */
  protected static boolean typesMatch(final Class<?>[] pTypes, final String[] qTypes) {
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
   * Returns an appropriate XQuery type for the specified Java object.
   * @param object object
   * @return item type or {@code null} if no appropriate type was found
   */
  private static Type type(final Object object) {
    final Type type = JavaMapping.type(object.getClass(), true);
    if(type != null) return type;

    if(object instanceof Element) return NodeType.ELEMENT;
    if(object instanceof Document) return NodeType.DOCUMENT_NODE;
    if(object instanceof DocumentFragment) return NodeType.DOCUMENT_NODE;
    if(object instanceof Attr) return NodeType.ATTRIBUTE;
    if(object instanceof Comment) return NodeType.COMMENT;
    if(object instanceof ProcessingInstruction) return NodeType.PROCESSING_INSTRUCTION;
    if(object instanceof Text) return NodeType.TEXT;

    if(object instanceof Duration) {
      final Duration d = (Duration) object;
      return !d.isSet(DatatypeConstants.YEARS) && !d.isSet(DatatypeConstants.MONTHS)
          ? AtomType.DAY_TIME_DURATION : !d.isSet(DatatypeConstants.HOURS) &&
          !d.isSet(DatatypeConstants.MINUTES) && !d.isSet(DatatypeConstants.SECONDS)
          ? AtomType.YEAR_MONTH_DURATION : AtomType.DURATION;
    }

    if(object instanceof XMLGregorianCalendar) {
      final QName qnm = ((XMLGregorianCalendar) object).getXMLSchemaType();
      if(qnm == DatatypeConstants.DATE) return AtomType.DATE;
      if(qnm == DatatypeConstants.DATETIME) return AtomType.DATE_TIME;
      if(qnm == DatatypeConstants.TIME) return AtomType.TIME;
      if(qnm == DatatypeConstants.GYEARMONTH) return AtomType.G_YEAR_MONTH;
      if(qnm == DatatypeConstants.GMONTHDAY) return AtomType.G_MONTH_DAY;
      if(qnm == DatatypeConstants.GYEAR) return AtomType.G_YEAR;
      if(qnm == DatatypeConstants.GMONTH) return AtomType.G_MONTH;
      if(qnm == DatatypeConstants.GDAY) return AtomType.G_DAY;
    }
    return null;
  }

  /**
   * Returns an XQuery string representation of the Java entity of this expression.
   * @return string
   */
  abstract String desc();

  /**
   * Returns the name of the Java entity.
   * @return string
   */
  abstract String name();

  @Override
  public final String description() {
    return desc() + "(...)";
  }

  @Override
  public final void plan(final QueryPlan plan) {
    plan.add(plan.create(this, NAME, name()), exprs);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(desc()).params(exprs);
  }
}
