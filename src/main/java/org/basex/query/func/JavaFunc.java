package org.basex.query.func;

import static javax.xml.datatype.DatatypeConstants.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Arr;
import org.basex.query.expr.Expr;
import org.basex.query.item.AtomType;
import org.basex.query.item.Empty;
import org.basex.query.item.FuncType;
import org.basex.query.item.Jav;
import org.basex.query.item.NodeType;
import org.basex.query.item.Type;
import org.basex.query.item.Value;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * Java function binding.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class JavaFunc extends Arr {
  /** New keyword. */
  private static final String NEW = "new";
  /** Input Java types. */
  private static final Class<?>[] JAVA = {
    String.class,     boolean.class, Boolean.class,      byte.class,
    Byte.class,       short.class,   Short.class,        int.class,
    Integer.class,    long.class,    Long.class,         float.class,
    Float.class,      double.class,  Double.class,       BigDecimal.class,
    BigInteger.class, QName.class,   CharSequence.class, byte[].class,
    Object[].class,   Map.class
  };
  /** Resulting XQuery types. */
  private static final Type[] XQUERY = {
    AtomType.STR, AtomType.BLN, AtomType.BLN, AtomType.BYT,
    AtomType.BYT, AtomType.SHR, AtomType.SHR, AtomType.INT,
    AtomType.INT, AtomType.LNG, AtomType.LNG, AtomType.FLT,
    AtomType.FLT, AtomType.DBL, AtomType.DBL, AtomType.DEC,
    AtomType.ITR, AtomType.QNM, AtomType.STR, AtomType.HEX,
    AtomType.SEQ, FuncType.ANY_FUN
  };
  /** Java class. */
  private final Class<?> cls;
  /** Java method. */
  private final String mth;

  /**
   * Constructor.
   * @param ii input info
   * @param c Java class
   * @param m Java method/field
   * @param a arguments
   */
  public JavaFunc(final InputInfo ii, final Class<?> c, final String m,
      final Expr[] a) {
    super(ii, a);
    cls = c;
    mth = m;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Value[] arg = new Value[expr.length];
    for(int a = 0; a < expr.length; ++a) {
      arg[a] = expr[a].value(ctx);
      if(arg[a].empty()) XPEMPTY.thrw(input, desc());
    }

    Object result = null;
    try {
      result = mth.equals(NEW) ? constructor(arg) : method(arg);
    } catch(final InvocationTargetException ex) {
      JAVAERR.thrw(input, ex.getCause());
    } catch(final Exception ex) {
      FUNJAVA.thrw(input, desc(), arg);
    }
    return result == null ? Empty.ITER : iter(result);
  }

  /**
   * Calls a constructor.
   * @param ar arguments
   * @return resulting object
   * @throws Exception exception
   */
  private Object constructor(final Value[] ar) throws Exception {
    for(final Constructor<?> con : cls.getConstructors()) {
      final Object[] arg = args(con.getParameterTypes(), ar, true);
      if(arg != null) return con.newInstance(arg);
    }
    throw new Exception();
  }

  /**
   * Calls a constructor.
   * @param ar arguments
   * @return resulting object
   * @throws Exception exception
   */
  private Object method(final Value[] ar) throws Exception {
    // check if a field with the specified name exists
    try {
      final Field f = cls.getField(mth);
      final boolean st = Modifier.isStatic(f.getModifiers());
      if(ar.length == (st ? 0 : 1)) {
        return f.get(st ? null : instObj(ar[0]));
      }
    } catch(final NoSuchFieldException ex) { /* ignored */ }

    for(final Method meth : cls.getMethods()) {
      if(!meth.getName().equals(mth)) continue;
      final boolean st = Modifier.isStatic(meth.getModifiers());
      final Object[] arg = args(meth.getParameterTypes(), ar, st);
      if(arg != null) return meth.invoke(st ? null : instObj(ar[0]), arg);
    }

    throw new Exception();
  }

  /**
   * Creates the instance on which a non-static field getter or method is
   * invoked.
   * @param v XQuery value
   * @return Java object
   * @throws QueryException query exception
   */
  private Object instObj(final Value v) throws QueryException {
    return cls.isInstance(v) ? v :
      v instanceof Jav ? ((Jav) v).val : v.toJava();
  }

  /**
   * Checks if the arguments conform with the specified parameters.
   * @param params parameters
   * @param args arguments
   * @param stat static flag
   * @return argument array or {@code null}
   * @throws QueryException query exception
   */
  private Object[] args(final Class<?>[] params, final Value[] args,
      final boolean stat) throws QueryException {

    final int s = stat ? 0 : 1;
    final int l = args.length - s;
    if(l != params.length) return null;

    /** Function arguments. */
    final Object[] val = new Object[l];
    int a = 0;

    for(final Class<?> par : params) {
      final Value arg = args[s + a];

      final Object next;
      if(par.isInstance(arg)) {
        next = arg;
      } else {
        final Type jtype = type(par);
        if(jtype == null || !arg.type.instance(jtype)
            && !jtype.instance(arg.type)) return null;
        next = arg.toJava();
      }
      val[a++] = next;
    }
    return val;
  }

  /**
   * Returns an appropriate XQuery data type for the specified Java class.
   * @param type Java type
   * @return xquery type
   */
  private static Type type(final Class<?> type) {
    for(int j = 0; j < JAVA.length; ++j) {
      if(JAVA[j].isAssignableFrom(type)) return XQUERY[j];
    }
    return AtomType.JAVA;
  }

  /**
   * Returns an appropriate XQuery data type for the specified Java object.
   * @param o object
   * @return xquery type
   */
  public static Type type(final Object o) {
    final Type t = type(o.getClass());
    if(t != AtomType.JAVA) return t;

    if(o instanceof Element) return NodeType.ELM;
    if(o instanceof Document) return NodeType.DOC;
    if(o instanceof DocumentFragment) return NodeType.DOC;
    if(o instanceof Attr) return NodeType.ATT;
    if(o instanceof Comment) return NodeType.COM;
    if(o instanceof ProcessingInstruction) return NodeType.PI;
    if(o instanceof Text) return NodeType.TXT;

    if(o instanceof Duration) {
      final Duration d = (Duration) o;
      return !d.isSet(YEARS) && !d.isSet(MONTHS) ? AtomType.DTD :
        !d.isSet(HOURS) && !d.isSet(MINUTES) && !d.isSet(SECONDS) ?
          AtomType.YMD : AtomType.DUR;
    }
    if(o instanceof XMLGregorianCalendar) {
      final QName type = ((XMLGregorianCalendar) o).getXMLSchemaType();
      if(type == DATE) return AtomType.DAT;
      if(type == DATETIME) return AtomType.DTM;
      if(type == TIME) return AtomType.TIM;
      if(type == GYEARMONTH) return AtomType.YMO;
      if(type == GMONTHDAY) return AtomType.MDA;
      if(type == GYEAR) return AtomType.YEA;
      if(type == GMONTH) return AtomType.MON;
      if(type == GDAY) return AtomType.DAY;
    }
    return AtomType.JAVA;
  }

  /**
   * Converts the specified object to an iterator.
   * @param res object
   * @return iterator
   */
  private Iter iter(final Object res) {
    if(!res.getClass().isArray()) return new Jav(res).iter();

    final ItemCache ic = new ItemCache();
    if(res instanceof boolean[]) {
      for(final Object o : (boolean[]) res) ic.add(new Jav(o));
    } else if(res instanceof char[]) {
      for(final Object o : (char[]) res) ic.add(new Jav(o));
    } else if(res instanceof byte[]) {
      for(final Object o : (byte[]) res) ic.add(new Jav(o));
    } else if(res instanceof short[]) {
      for(final Object o : (short[]) res) ic.add(new Jav(o));
    } else if(res instanceof int[]) {
      for(final Object o : (int[]) res) ic.add(new Jav(o));
    } else if(res instanceof long[]) {
      for(final Object o : (long[]) res) ic.add(new Jav(o));
    } else if(res instanceof float[]) {
      for(final Object o : (float[]) res) ic.add(new Jav(o));
    } else if(res instanceof double[]) {
      for(final Object o : (double[]) res) ic.add(new Jav(o));
    } else {
      for(final Object o : (Object[]) res) ic.add(new Jav(o));
    }
    return ic;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, NAM, Token.token(cls + "." + mth));
    for(final Expr arg : expr) arg.plan(ser);
    ser.closeElement();
  }

  @Override
  public String desc() {
    return cls.getName() + "." + mth + "(...)" +
      (mth.equals(NEW) ? " constructor" : " method");
  }

  @Override
  public String toString() {
    return cls + "." + mth + PAR1 + toString(SEP) + PAR2;
  }
}
