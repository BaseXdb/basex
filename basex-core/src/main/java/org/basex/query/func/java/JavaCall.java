package org.basex.query.func.java;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.lang.reflect.*;
import java.lang.reflect.Array;
import java.net.*;
import java.util.*;

import javax.xml.datatype.*;
import javax.xml.namespace.*;

import org.basex.core.*;
import org.basex.core.MainOptions.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.QueryModule.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.util.pkg.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.Type;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;
import org.w3c.dom.*;
import org.w3c.dom.Text;

/**
 * This class contains common methods for executing Java code and mapping
 * Java objects to XQuery values.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public abstract class JavaCall extends Arr {
  /** Placeholder for invalid Java arguments. */
  private static final Object INVALID = new Object();

  /** Updating flag. */
  public final boolean updating;
  /** Static context. */
  final StaticContext sc;
  /** Permission. */
  final Perm perm;
  /** Indicates if function parameters are XQuery types. */
  boolean[] xquery;

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

    final Value value = eval(qc, qc.context.options.get(MainOptions.WRAPJAVA));
    if(!updating) return value;

    // updating function: cache output
    qc.updates().addOutput(value, qc);
    return Empty.VALUE;
  }

  /**
   * Returns the result of the evaluated Java function.
   * @param qc query context
   * @param wrap wrap options
   * @return result value
   * @throws QueryException query exception
   */
  protected abstract Value eval(QueryContext qc, WrapOptions wrap) throws QueryException;

  /**
   * Evaluates the arguments.
   * @param qc query context
   * @return arguments
   * @throws QueryException query exception
   */
  final Value[] values(final QueryContext qc) throws QueryException {
    final ValueList list = new ValueList(exprs.length);
    for(final Expr expr : exprs) list.add(expr.value(qc));
    return list.finish();
  }

  /**
   * Returns a candidate with function arguments if the XQuery arguments match the Java arguments.
   * @param args arguments
   * @param params expected parameters
   * @param stat static flag
   * @return candidate with Java arguments, or {@code null}
   * @throws QueryException query exception
   */
  final JavaCandidate candidate(final Value[] args, final Class<?>[] params, final boolean stat)
      throws QueryException {

    // start with second argument if function is not static
    final int s = stat ? 0 : 1, pl = params.length;
    if(pl != args.length - s) return null;

    // function arguments
    final JavaCandidate jc = new JavaCandidate(pl);
    for(int p = 0; p < pl; p++) {
      final Value arg = args[p + s];
      final Class<?> param = params[p];
      final Object value = convert(arg, param, p);
      if(value == INVALID) return null;

      // check if parameter and argument types match exactly
      jc.exact = jc.exact && JavaMapping.type(param, false) == arg.seqType().type;
      jc.arguments[p] = value;
    }
    return jc;
  }

  /**
   * Converts an XQuery value to a Java value.
   * @param arg argument
   * @param param expected parameter
   * @param p parameter offset
   * @return value or {@code null}
   * @throws QueryException query exception
   */
  private Object convert(final Value arg, final Class<?> param, final int p) throws QueryException {
    // XQuery expression: value must not be converted
    if(param == Expr.class) return arg;

    // argument to a Java object if a mapping entry exists
    final Type type = JavaMapping.type(param, true);
    if(type != null && arg.type.instanceOf(type)) return arg.toJava();

    // convert empty array to target type
    if(arg instanceof XQArray && ((XQArray) arg).arraySize() == 0 && param.isArray()) {
      final Class<?> atype = param.getComponentType();
      if(atype == boolean.class) return new boolean[0];
      if(atype == byte.class) return new byte[0];
      if(atype == short.class) return new short[0];
      if(atype == char.class) return new char[0];
      if(atype == int.class) return new int[0];
      if(atype == long.class) return new long[0];
      if(atype == float.class) return new float[0];
      if(atype == double.class) return new double[0];
      if(atype == String.class) return new String[0];
      return new Object[0];
    }

    // convert empty number to the smallest type
    if(arg instanceof ANum) {
      final double d = ((ANum) arg).dbl();
      if((param == byte.class  || param == Byte.class)      && (byte)  d == d) return (byte)   d;
      if((param == short.class || param == Short.class)     && (short) d == d) return (short)  d;
      if((param == char.class  || param == Character.class) && (char)  d == d) return (char)   d;
      if((param == int.class   || param == Integer.class)   && (int)   d == d) return (int)    d;
      if((param == float.class || param == Float.class)     && (float) d == d) return (float)  d;
      if(param == double.class || param == Double.class)                       return d;
    }

    // convert to Java object
    // - if argument is a Java object wrapper, or
    // - if function parameter is not a {@link Value} instance
    final Object value = arg instanceof XQJava ||
      !(xquery != null ? xquery[p] : Value.class.isAssignableFrom(param)) ? arg.toJava() : arg;

    // return value
    // - if argument is an instance of the function parameter, or
    // - if value is null and parameter is not primitive
    if(param.isInstance(value) || value == null && !param.isPrimitive()) return value;

    // give up
    return INVALID;
  }

  /**
   * Finds the best candidate.
   * Removes approximate candidates if some are exactly matching.
   * @param candidates candidates
   * @return best candidate, or {@code null} if multiple candidates are left
   */
  static JavaCandidate bestCandidate(final ArrayList<JavaCandidate> candidates) {
    if(((Checks<JavaCandidate>) jc -> jc.exact).any(candidates)) {
      for(int c = candidates.size() - 1; c >= 0; c--) {
        if(!candidates.get(c).exact) candidates.remove(c);
      }
    }
    return candidates.size() == 1 ? candidates.get(0) : null;
  }

  /**
   * Returns a Java execution error.
   * @param th throwable
   * @param args converted arguments
   * @return exception
   */
  final QueryException executionError(final Throwable th, final Object[] args) {
    Util.debug(th);
    final Throwable root = Util.rootException(th);
    return root instanceof QueryException ? ((QueryException) root).info(info) :
      JAVAEXEC_X_X_X.get(info, root, name(), JavaCall.argTypes(args));
  }

  // STATIC METHODS ===============================================================================

  /**
   * Converts the specified object to an XQuery value.
   * @param object result object
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return value
   * @throws QueryException query exception
   */
  public static Value toValue(final Object object, final QueryContext qc, final InputInfo info)
      throws QueryException {
    return toValue(object, qc, info, qc.context.options.get(MainOptions.WRAPJAVA));
  }

  /**
   * Converts the specified object to an XQuery value.
   * @param object result object
   * @param qc query context
   * @param info input info (can be {@code null})
   * @param wrap wrap options
   * @return value
   * @throws QueryException query exception
   */
  public static Value toValue(final Object object, final QueryContext qc, final InputInfo info,
      final WrapOptions wrap) throws QueryException {

    // return XQuery types unchanged
    if(object instanceof Value) return (Value) object;
    if(object instanceof Iter) return ((Iter) object).value(qc, null);

    if(wrap != WrapOptions.ALL) {
      // null values
      if(object == null) return Empty.VALUE;

      // values with XQuery types
      final Type type = type(object);
      if(type != null) return type.cast(object, qc, null);

      // arrays
      if(object.getClass().isArray()) {
        // empty array
        final int s = Array.getLength(object);
        if(s == 0) return Empty.VALUE;

        // primitive arrays
        if(object instanceof boolean[]) return BlnSeq.get((boolean[]) object);
        if(object instanceof byte[])    return BytSeq.get((byte[]) object);
        if(object instanceof short[])   return ShrSeq.get((short[]) object);
        if(object instanceof long[])    return IntSeq.get((long[]) object);
        if(object instanceof float[])   return FltSeq.get((float[]) object);
        if(object instanceof double[])  return DblSeq.get((double[]) object);
        // char array
        if(object instanceof char[]) {
          final char[] values = (char[]) object;
          final LongList list = new LongList(values.length);
          for(final int value : values) list.add(value);
          return IntSeq.get(list.finish(), AtomType.UNSIGNED_LONG);
        }
        // integer array
        if(object instanceof int[]) {
          final int[] values = (int[]) object;
          final LongList list = new LongList(values.length);
          for(final int value : values) list.add(value);
          return IntSeq.get(list.finish(), AtomType.INT);
        }
        // check for null values
        for(final Object obj : (Object[]) object) {
          if(obj == null) throw JAVANULL.get(info);
        }
        // string array
        if(object instanceof String[]) {
          final String[] values = (String[]) object;
          final TokenList list = new TokenList(values.length);
          for(final String string : values) list.add(string);
          return StrSeq.get(list);
        }
        // any other array (including nested ones)
        final ValueBuilder vb = new ValueBuilder(qc);
        for(final Object value : (Object[]) object) vb.add(toValue(value, qc, info, wrap));
        return vb.value();
      }

      // data structures
      if(wrap == WrapOptions.NONE) {
        final ValueBuilder vb = new ValueBuilder(qc);
        if(object instanceof Iterable) {
          for(final Object obj : (Iterable<?>) object) vb.add(toValue(obj, qc, info, wrap));
        } else if(object instanceof Iterator) {
          final Iterator<?> ir = (Iterator<?>) object;
          while(ir.hasNext()) vb.add(toValue(ir.next(), qc, info, wrap));
        } else if(object instanceof Map) {
          final MapBuilder mb = new MapBuilder(info);
          for(final Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
            final Item key = toValue(entry.getKey(), qc, info, wrap).item(qc, info);
            final Value value = toValue(entry.getValue(), qc, info, wrap);
            mb.put(key, value);
          }
          vb.add(mb.map());
        } else {
          vb.add(Str.get(object.toString()));
        }
        return vb.value();
      }
    }
    // wrap Java object
    return new XQJava(object);
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
  public static JavaCall get(final QNm qname, final Expr[] args, final QueryContext qc,
      final StaticContext sc, final InputInfo info) throws QueryException {

    // rewrite function name, extract argument types
    String name = camelCase(string(qname.local()));
    String[] types = null;
    final int n = name.indexOf('\u00b7');
    if(n != -1) {
      final StringList list = new StringList();
      for(final String type : Strings.split(name.substring(n + 1), '\u00b7')) {
        list.add(classPath(type.replace(DOTS, "[]")));
      }
      types = list.finish();
      name = name.substring(0, n);
    }
    final String uri = string(qname.uri());

    // check if URI starts with "java:" prefix. if yes, skip rewritings
    final boolean enforce = uri.startsWith(JAVA_PREFIX_COLON);
    final String className = classPath(enforce ? uri.substring(JAVA_PREFIX_COLON.length()) :
      uriToClasspath(uri2path(uri)));

    // function in imported Java module
    final ModuleLoader modules = qc.resources.modules();
    final Object module  = modules.findModule(className);
    if(module != null) {
      final Method meth = moduleMethod(module, name, args.length, types, qname, qc, info);
      final Requires req = meth.getAnnotation(Requires.class);
      final Perm perm = req == null ? Perm.ADMIN :
        Perm.get(req.value().name().toLowerCase(Locale.ENGLISH));
      final boolean updating = meth.getAnnotation(Updating.class) != null;
      if(updating) qc.updating();
      return new StaticJavaCall(module, meth, args, perm, updating, sc, info);
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
        Util.debug(ex);
      } catch(final Throwable th) {
        // catch linkage and other errors as well
        throw JAVAINIT_X_X.get(info, Util.className(th), th);
      }

      if(clazz == null) {
        // class not found, java prefix was specified
        if(enforce) throw JAVACLASS_X.get(info, className);
      } else {
        // constructor
        if(name.equals(NEW)) {
          final DynJavaConstr djc = new DynJavaConstr(clazz, types, args, sc, info);
          if(djc.init(enforce)) return djc;
        }
        // field or method
        final DynJavaFunc djf = new DynJavaFunc(clazz, name, types, args, sc, info);
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
   * @param info input info
   * @return method if found
   * @throws QueryException query exception
   */
  private static Method moduleMethod(final Object module, final String name, final int arity,
      final String[] types, final QNm qname, final QueryContext qc, final InputInfo info)
      throws QueryException {

    // find method with identical name and arity
    final IntList arities = new IntList();
    final HashMap<String, ArrayList<Method>> allMethods = methods(module.getClass());
    final ArrayList<Method> candidates = candidates(allMethods, name, types, arity, arities, true);
    final int cs = candidates.size();
    if(cs == 0) {
      final TokenList names = new TokenList();
      for(final String method : allMethods.keySet()) names.add(method);
      throw noMember(name, types, arity, string(qname.string()), arities, names.finish(), info);
    }
    if(cs > 1) throw JAVAMULTIPLE_X_X.get(info, qname.string(),
        paramTypes(candidates.toArray(Executable[]::new), false));

    // single method found: add module locks to query context
    final Method method = candidates.get(0);
    final Lock lock = method.getAnnotation(Lock.class);
    if(lock != null) qc.locks.add(Locking.BASEX_PREFIX + lock.value());
    return method;
  }

  /**
   * Returns an error message if no fields and methods could be chosen for execution.
   * @param name name of field or method
   * @param types types (can be {@code null})
   * @param arity supplied arity
   * @param full full name of field or method
   * @param arities arities of found methods
   * @param names list of available names
   * @param info input info
   * @return exception
   */
  static QueryException noMember(final String name, final String[] types, final int arity,
      final String full, final IntList arities, final byte[][] names, final InputInfo info) {
    // functions with different arities
    if(!arities.isEmpty()) return Functions.wrongArity(arity, arities, full, info);

    // find similar field/method names
    final byte[] nm = token(name);
    final Object similar = Levenshtein.similar(nm, names);
    if(similar != null && eq(nm, (byte[]) similar)) {
      // if name is equal, no function was chosen via exact type matching
      final StringJoiner sj = new StringJoiner(", ", "(", ")");
      for(final String type : types) sj.add(className(type));
      return JAVAARGS_X_X.get(info, full, sj);
    }
    return JAVAMEMBER_X.get(info, similar(full, similar));
  }

  /**
   * Returns a map with all relevant methods for a class.
   * @param clazz class
   * @return methods
   */
  static HashMap<String, ArrayList<Method>> methods(final Class<?> clazz) {
    final HashSet<String> names = new HashSet<>();
    final HashMap<String, ArrayList<Method>> list = new HashMap<>();
    for(final boolean bridge : new boolean[] { false, true }) {
      for(final Method method : clazz.getMethods()) {
        if(bridge == method.isBridge()) {
          final StringBuilder id = new StringBuilder().append(method.getName()).append('-');
          for(final Class<?> type : method.getParameterTypes()) {
            id.append(type.getName()).append('-');
          }
          if(names.add(id.toString())) {
            list.computeIfAbsent(method.getName(), n -> new ArrayList<>(1)).add(method);
          }
        }
      }
    }
    return list;
  }

  /**
   * Returns the methods with the specified name.
   * @param methods methods
   * @param name name
   * @param types types
   * @param arity arity
   * @param arities arities
   * @param stat static calls
   * @return methods
   */
  static ArrayList<Method> candidates(final HashMap<String, ArrayList<Method>> methods,
      final String name, final String[] types, final int arity, final IntList arities,
      final boolean stat) {
    final ArrayList<Method> list = new ArrayList<>(1), mthds = methods.get(name);
    if(mthds != null) {
      for(final Method method : mthds) {
        final Class<?>[] params = method.getParameterTypes();
        final int al = params.length + (stat || isStatic(method) ? 0 : 1);
        if(al == arity) {
          if(typesMatch(params, types)) list.add(method);
        } else {
          arities.add(al);
        }
      }
    }
    return list;
  }

  /**
   * Converts the given string to a Java class name. Slashes will be replaced with dots, and
   * the last package segment will be capitalized and camel-cased.
   * @param string string to convert
   * @return class name
   */
  public static String uriToClasspath(final String string) {
    final String s = string.replace('/', '.');
    final int c = s.lastIndexOf('.') + 1;
    return s.substring(0, c) + Strings.capitalize(camelCase(s.substring(c)));
  }

  /**
   * Converts the given string to camel case.
   * @param string string to convert
   * @return resulting string
   */
  public static String camelCase(final String string) {
    final StringBuilder sb = new StringBuilder();
    boolean upper = false;
    final int sl = string.length();
    for(int s = 0; s < sl; s++) {
      final char ch = string.charAt(s);
      if(ch == '-') {
        upper = true;
      } else if(upper) {
        sb.append(Character.toUpperCase(ch));
        upper = false;
      } else {
        sb.append(ch);
      }
    }
    return sb.toString();
  }

  /**
   * Converts a URI to a directory path.
   * See https://docs.basex.org/wiki/Repository#URI_Rewriting for details.
   * @param uri namespace uri
   * @return converted path
   */
  public static String uri2path(final String uri) {
    String path = uri;
    try {
      final URI u = new URI(uri);
      final TokenBuilder tb = new TokenBuilder();
      if(u.isOpaque()) {
        tb.add(u.getScheme()).add('/').add(u.getSchemeSpecificPart().replace(':', '/'));
      } else {
        final String auth = u.getAuthority();
        if(auth != null) {
          // reverse authority, replace dots by slashes. example: basex.org  ->  org/basex
          final String[] comp = Strings.split(auth, '.');
          for(int c = comp.length - 1; c >= 0; c--) tb.add('/').add(comp[c]);
        }
        // add remaining path
        final String p = u.getPath();
        tb.add(p == null || p.isEmpty() ? "/" : p.replace('.', '/'));
      }
      path = tb.toString();
    } catch(final URISyntaxException ex) {
      Util.debug(ex);
    }

    // replace special characters with dashes; remove multiple slashes
    path = path.replaceAll("[^\\w.-/]+", "-").replaceAll("//+", "/");
    // add "index" string
    if(Strings.endsWith(path, '/')) path += "index";
    // remove heading slash
    if(Strings.startsWith(path, '/')) path = path.substring(1);
    return path;
  }

  /**
   * Returns a fully qualified class name.
   * @param name class string
   * @return normalized name
   */
  public static String classPath(final String name) {
    // prepend standard package if name starts with uppercase letter and has no dots
    //   String  ->  java.lang.String
    //   char[]  ->  char[]
    return name.replaceAll("^([A-Z][^.]+)$", JAVA_LANG_DOT + "$1");
  }

  /**
   * Returns a normalized class name without path to standard package.
   * @param clazz class
   * @return normalized name
   */
  static String className(final Class<?> clazz) {
    return className(clazz.getCanonicalName());
  }

  /**
   * Returns a normalized class name without path to standard package.
   * @param name class string
   * @return normalized name
   */
  static String className(final String name) {
    return name.startsWith(QueryText.JAVA_LANG_DOT) ?
      name.substring(QueryText.JAVA_LANG_DOT.length()) : name;
  }

  /**
   * Checks if the specified executable is static.
   * Constructor is treated as static, as no instance reference exists.
   * @param exec executable (constructor, method)
   * @return result of check
   */
  static boolean isStatic(final Executable exec) {
    return exec instanceof Constructor || Modifier.isStatic(exec.getModifiers());
  }

  /**
   * Checks if the specified field is static.
   * @param field field
   * @return result of check
   */
  static boolean isStatic(final Field field) {
    return Modifier.isStatic(field.getModifiers());
  }

  /**
   * Returns parameters from the specified executables.
   * @param execs executables
   * @param xquery xquery include XQuery types
   * @return string
   */
  static String paramTypes(final Executable[] execs, final boolean xquery) {
    final StringJoiner sj = new StringJoiner(", ");
    for(final Executable exec : execs) sj.add(paramTypes(exec, xquery));
    return sj.toString();
  }

  /**
   * Returns the parameters types from the specified executable.
   * @param exec executable (constructor, method)
   * @param xquery xquery include XQuery types
   * @return string
   */
  static String paramTypes(final Executable exec, final boolean xquery) {
    final StringJoiner sj = new StringJoiner(", ", "(", ")");
    for(final Class<?> param : exec.getParameterTypes()) {
      final Type type = xquery ? JavaMapping.type(param, false) : null;
      sj.add(type != null ? type.toString() : className(param));
    }
    return sj.toString();
  }

  /**
   * Returns a string representation of the XQuery or Java types from the specified arguments.
   * @param args arguments
   * @return types string
   */
  static String argTypes(final Object[] args) {
    final StringJoiner sj = new StringJoiner(", ", "(", ")");
    for(final Object arg : args) sj.add(argType(arg));
    return sj.toString();
  }

  /**
   * Returns a string representation of the XQuery or Java type from the specified argument.
   * @param argument argument
   * @return type string
   */
  static String argType(final Object argument) {
    final Object object = argument instanceof XQJava ? ((XQJava) argument).toJava() : argument;
    return object instanceof Value ? ((Value) object).seqType().toString() :
      object == null ? Util.info(null) :
      Util.className(object);
  }

  /**
   * Compares the types of method parameters with the specified types.
   * @param pTypes parameter types
   * @param qTypes query types (can be {@code null})
   * @return result of check
   */
  static boolean typesMatch(final Class<?>[] pTypes, final String[] qTypes) {
    // no query types: accept method
    if(qTypes == null) return true;
    // compare types
    final int pl = pTypes.length;
    if(pl != qTypes.length) return false;
    for(int p = 0; p < pl; p++) {
      if(!qTypes[p].equals(pTypes[p].getCanonicalName())) return false;
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
    if(object instanceof Document ||
       object instanceof DocumentFragment) return NodeType.DOCUMENT_NODE;
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
  public final void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, NAME, name()), exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(desc()).params(exprs);
  }
}
