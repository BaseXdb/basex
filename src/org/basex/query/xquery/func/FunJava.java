package org.basex.query.xquery.func;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Arr;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Jav;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * Java function definition.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FunJava extends Arr {
  /** Java class. */
  public final Class<?> cls;
  /** Java method. */
  public final String mth;

  /**
   * Constructor.
   * @param c Java class
   * @param m Java method/field
   * @param a arguments
   */
  public FunJava(final Class<?> c, final String m, final Expr[] a) {
    super(a);
    cls = c;
    mth = m;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Item[] arg = new Item[expr.length];
    for(int a = 0; a < expr.length; a++) {
      arg[a] = ctx.iter(expr[a]).atomic(this, false);
    }
    
    Object result = null;
    try {
      result = mth.equals("new") ? constructor(arg) : method(arg);
    } catch(Exception ex) {
      //ex.getCause().toString()
      Err.or(FUNJAVA, info());
    }

    //if(result == null) Err.or(FUNJAVA, info());
    return result == null ? Iter.EMPTY : iter(result);
    //return iter(result);
  }
  
  /**
   * Calls a constructor.
   * @param ar arguments
   * @return resulting object
   * @throws Exception exception
   */
  private Object constructor(final Item[] ar) throws Exception {
    for(Constructor<?> con : cls.getConstructors()) {
      final Object[] arg = args(con.getParameterTypes(), ar, true);
      if(arg != null) return con.newInstance(arg);
    }
    return null;
  }
  
  /**
   * Calls a constructor.
   * @param ar arguments
   * @return resulting object
   * @throws Exception exception
   */
  private Object method(final Item[] ar) throws Exception {
    // check if a field with the specified name exists
    try {
      final Field f = cls.getField(mth);
      final boolean st = Modifier.isStatic(f.getModifiers());
      if(ar.length == (st ? 0 : 1)) {
        return f.get(st ? null : ((Jav) ar[0]).val);
      }
    } catch(NoSuchFieldException ex) {
    }

    for(Method meth : cls.getMethods()) {
      if(!meth.getName().equals(mth)) continue;
      final boolean st = Modifier.isStatic(meth.getModifiers());
      final Object[] arg = args(meth.getParameterTypes(), ar, st);
      if(arg != null) return meth.invoke(st ? null : ((Jav) ar[0]).val, arg);
    }
    return null;
  }
  
  /**
   * Checks if the arguments fit to the specified parameters.
   * @param params parameters
   * @param args arguments
   * @param stat static flag
   * @return argument array or null
   * @throws XQException query exception
   */
  private Object[] args(final Class<?>[] params, final Item[] args,
      final boolean stat) throws XQException {

    int s = stat ? 0 : 1;
    int l = args.length - s;
    if(l != params.length) return null;
    
    /** Function arguments. */
    final Object[] val = new Object[l];
    int a = 0;
    
    for(Class<?> par : params) {
      //BaseX.debug("-1- " + par.getCanonicalName());
      final Type jType = jType(par);
      if(jType == null) return null;
      
      final Item arg = args[s + a];

      //BaseX.debug("-2- " + xq + " = Java?");
      if(jType == Type.JAVA && arg.type != Type.JAVA) return null;
      
      //BaseX.debug("-3- " + xq + " instance of " + it.type + " ?");
      //if(!xq.instance(it.type)) return null;
      if(!arg.type.instance(jType) && !jType.instance(arg.type)) return null;
      //BaseX.debug("-3- " + it);
      
      final Object o = jType.j(arg);
      if(o == null) return null;
      
      //BaseX.debug("-4- " + o.getClass() + " => " + par);
      val[a++] = o;
    }
    return val;
  }

  /**
   * Returns an XQUery data type for a Java object.
   * @param o object
   * @return xquery type
   */
  public static Type jType(final Object o) {
    final Type t = jType(o.getClass());
    if(t != Type.JAVA) return t;

    if(o instanceof Element) return Type.ELM;
    if(o instanceof Document) return Type.DOC;
    if(o instanceof Attr) return Type.ATT;
    if(o instanceof Comment) return Type.COM;
    if(o instanceof ProcessingInstruction) return Type.PI;
    if(o instanceof Text) return Type.TXT;
    
    if(o instanceof Duration) {
      final QName type = ((Duration) o).getXMLSchemaType();
      if(type == DatatypeConstants.DURATION_DAYTIME) return Type.DTD;
      if(type == DatatypeConstants.DURATION_YEARMONTH) return Type.YMD;
      if(type == DatatypeConstants.DURATION) return Type.DUR;
    }
    if(o instanceof XMLGregorianCalendar) {
      final QName type = ((XMLGregorianCalendar) o).getXMLSchemaType();
      if(type == DatatypeConstants.DATE) return Type.DAT;
      if(type == DatatypeConstants.DATETIME) return Type.DTM;
      if(type == DatatypeConstants.TIME) return Type.TIM;
      if(type == DatatypeConstants.GYEARMONTH) return Type.YMO;
      if(type == DatatypeConstants.GMONTHDAY) return Type.MDA;
      if(type == DatatypeConstants.GYEAR) return Type.YEA;
      if(type == DatatypeConstants.GMONTH) return Type.MON;
      if(type == DatatypeConstants.GDAY) return Type.DAY;
    }
    return null;
  }

  /**
   * Returns an XQUery data type for a Java class.
   * @param par Java type
   * @return xquery type
   */
  public static Type jType(final Class<?> par) {
    if(par == String.class) return Type.STR;
    if(par == boolean.class || par == Boolean.class) return Type.BLN;
    if(par == char.class || par == Character.class) return Type.USH;
    if(par == byte.class || par == Byte.class) return Type.BYT;
    if(par == short.class || par == Short.class) return Type.SHR;
    if(par == int.class || par == Integer.class) return Type.INT;
    if(par == long.class || par == Long.class) return Type.LNG;
    if(par == float.class || par == Float.class) return Type.FLT;
    if(par == double.class || par == Double.class) return Type.DBL;
    if(par == BigDecimal.class) return Type.DEC;
    if(par == BigInteger.class) return Type.ITR;
    if(par == QName.class) return Type.QNM;
    return Type.JAVA;
  }
  
  /**
   * Converts the specified object to an iterator.
   * @param res object
   * @return iterator
   */
  private Iter iter(final Object res) {
    if(!res.getClass().isArray()) return new Jav(res).iter();
    
    final SeqIter seq = new SeqIter();
    if(res instanceof boolean[]) {
      for(Object o : (boolean[]) res) seq.add(new Jav(o));
    } else if(res instanceof char[]) {
      for(Object o : (char[]) res) seq.add(new Jav(o));
    } else if(res instanceof byte[]) {
      for(Object o : (byte[]) res) seq.add(new Jav(o));
    } else if(res instanceof short[]) {
      for(Object o : (short[]) res) seq.add(new Jav(o));
    } else if(res instanceof int[]) {
      for(Object o : (int[]) res) seq.add(new Jav(o));
    } else if(res instanceof long[]) {
      for(Object o : (long[]) res) seq.add(new Jav(o));
    } else if(res instanceof float[]) {
      for(Object o : (float[]) res) seq.add(new Jav(o));
    } else if(res instanceof double[]) {
      for(Object o : (double[]) res) seq.add(new Jav(o));
    } else {
      for(Object o : (Object[]) res) seq.add(new Jav(o));
    }
    return seq;
  }

  @Override
  public String toString() {
    return cls + "." + mth + "(" + toString(", ") + ")";
  }

  @Override
  public String info() {
    return cls + "." + mth + "(...)";
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this, NAM, Token.token(cls + "." + mth));
    for(Expr arg : expr) arg.plan(ser);
    ser.closeElement(this);
  }
}
