// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.xqj.testcases;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.TreeSet;

@SuppressWarnings("all")
public class SignatureTest extends XQJTestCase {

  private static String logClass(final StringBuffer sb, final String name) throws Exception {
    final Class c = Class.forName("javax.xml.xquery." + name);

    // not all java VMs return information in the same order through the reflection API
    // make sure we get information always in the same order.
    TreeSet set;
    Iterator it;

    // class
    sb.append(Modifier.toString(c.getModifiers()));
    sb.append(' ');
    sb.append(c.getName());
    if (c.getSuperclass() != null) {
      sb.append(" extends ");
      sb.append(c.getSuperclass().getName());
    }
    final Class[] interfaces = c.getInterfaces();
    set = new TreeSet();
    if (interfaces != null && interfaces.length > 0) {
      for(final Class interface1 : interfaces) {
        set.add(interface1.getName());
      }
      sb.append(" implements ");
      it = set.iterator();
      while (it.hasNext()) {
        sb.append((String)it.next());
        if (it.hasNext()) sb.append(',');
      }
    }
    sb.append("\r\n{\r\n");

    // fields
    set = new TreeSet();
    final Field[] fields = c.getFields();
    for (int i = 0; i < fields.length; ++i) {
      if (!fields[i].getDeclaringClass().equals(c)) continue;
      final StringBuffer tmp = new StringBuffer();
      tmp.append(Modifier.toString(fields[i].getModifiers()));
      tmp.append(' ');
      tmp.append(fields[i].getType().getName());
      tmp.append(' ');
      tmp.append(fields[i].getName());
      tmp.append("\r\n");
      set.add(tmp.toString());
    }
    it = set.iterator();
    while (it.hasNext()) {
      sb.append((String)it.next());
    }

    // constructors
    set = new TreeSet();
    final Constructor[] constructors = c.getConstructors();
    for(final Constructor constructor : constructors) {
      final StringBuffer tmp = new StringBuffer();
      tmp.append(Modifier.toString(constructor.getModifiers()));
      tmp.append(' ');
      tmp.append(c.getName());
      tmp.append(" (");
      final Class[] parameters = constructor.getParameterTypes();
      for (int j = 0; j < parameters.length; ++j) {
        tmp.append(parameters[j].getName());
        if (j != parameters.length-1) tmp.append(',');
      }
      tmp.append(')');
      final Class[] exceptions = constructor.getExceptionTypes();
      if (exceptions != null && exceptions.length > 0) {
        tmp.append(" throws ");
        for (int j = 0; j < exceptions.length; ++j) {
          tmp.append(exceptions[j].getName());
          if (j != exceptions.length-1) tmp.append(',');
        }
      }
      tmp.append("\r\n");
      set.add(tmp.toString());
    }
    it = set.iterator();
    while (it.hasNext()) {
      sb.append((String)it.next());
    }

    // methods
    set = new TreeSet();
    final Method[] methods = c.getMethods();
    for (int i = 0; i < methods.length; ++i) {
      final StringBuffer tmp = new StringBuffer();
      if (!methods[i].getDeclaringClass().equals(c)) continue;
      tmp.append(Modifier.toString(methods[i].getModifiers()));
      tmp.append(' ');
      tmp.append(methods[i].getReturnType().getName());
      tmp.append(' ');
      tmp.append(methods[i].getName());
      tmp.append(" (");
      final Class[] parameters = methods[i].getParameterTypes();
      for (int j = 0; j < parameters.length; ++j) {
        tmp.append(parameters[j].getName());
        if (j != parameters.length-1) tmp.append(',');
      }
      tmp.append(')');
      final Class[] exceptions = methods[i].getExceptionTypes();
      if (exceptions != null && exceptions.length > 0) {
        tmp.append(" throws ");
        for (int j = 0; j < exceptions.length; ++j) {
          tmp.append(exceptions[j].getName());
          if (j != exceptions.length-1) tmp.append(',');
        }
      }
      tmp.append("\r\n");
      set.add(tmp.toString());
    }
    it = set.iterator();
    while (it.hasNext()) {
      sb.append((String)it.next());
    }

    sb.append("}\r\n\r\n");

    return sb.toString();
  }

  public static final String signatureFile__ = "xqj.sig";

  public void testSignatures() throws Exception {
    final StringBuffer sb = new StringBuffer();

    logClass(sb, "ConnectionPoolXQDataSource");
    logClass(sb, "PooledXQConnection");
    logClass(sb, "XQCancelledException");
    logClass(sb, "XQConnection");
    logClass(sb, "XQConnectionEvent");
    logClass(sb, "XQConnectionEventListener");
    logClass(sb, "XQConstants");
    logClass(sb, "XQDataFactory");
    logClass(sb, "XQDataSource");
    logClass(sb, "XQDynamicContext");
    logClass(sb, "XQException");
    logClass(sb, "XQExpression");
    logClass(sb, "XQItem");
    logClass(sb, "XQItemAccessor");
    logClass(sb, "XQItemType");
    logClass(sb, "XQMetaData");
    logClass(sb, "XQPreparedExpression");
    logClass(sb, "XQQueryException");
    logClass(sb, "XQResultItem");
    logClass(sb, "XQResultSequence");
    logClass(sb, "XQSequence");
    logClass(sb, "XQSequenceType");
    logClass(sb, "XQStackTraceElement");
    logClass(sb, "XQStackTraceVariable");
    logClass(sb, "XQStaticContext");

    final InputStream signatureStream = getClass().getResourceAsStream(signatureFile__);
    assertNotNull("Signature file '" + signatureFile__ + "' not found in classpath.", signatureStream);

    final InputStreamReader signatureReader = new InputStreamReader(signatureStream, "UTF-8");
    final Reader in = new BufferedReader(signatureReader);
    int ch;
    final StringBuffer expected = new StringBuffer();
    while ((ch = in.read()) > -1) {
      expected.append((char)ch);
    }
    in.close();
    assertEquals("ZZZ", expected.toString(), sb.toString());
  }
}
