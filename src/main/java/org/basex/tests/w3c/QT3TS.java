package org.basex.tests.w3c;

import static org.basex.core.Text.*;
import static org.basex.tests.w3c.QT3Constants.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.Set;
import org.basex.io.IO;
import org.basex.io.out.PrintOutput;
import org.basex.query.QueryProcessor;
import org.basex.query.func.FNSimple;
import org.basex.query.item.SeqType;
import org.basex.tests.w3c.qt3api.XQEmpty;
import org.basex.tests.w3c.qt3api.XQException;
import org.basex.tests.w3c.qt3api.XQItem;
import org.basex.tests.w3c.qt3api.XQValue;
import org.basex.tests.w3c.qt3api.XQuery;
import org.basex.util.Args;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.basex.util.list.ObjList;

/**
 * XQuery Test Suite wrapper.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class QT3TS {
  /** Maximum length of result output. */
  private int maxout = 2000;

  /** Correct results. */
  private final TokenBuilder right = new TokenBuilder();
  /** Wrong results. */
  private final TokenBuilder wrong = new TokenBuilder();
  /** Number of tested queries. */
  private int total;
  /** Number of correct queries. */
  private int correct;

  /** Query filter string. */
  private String single = "";
  /** Verbose flag. */
  private boolean verbose;

  /** Database context. */
  protected final Context ctx = new Context();
  /** Global environments. */
  private ObjList<QT3Env> genvs = new ObjList<QT3Env>();
  /** Default path to the test suite. */
  protected String path = "g:/XML/w3c/qt3ts/";

  /**
   * Main method of the test class.
   * @param args command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    try {
      new QT3TS().run(args);
    } catch(final IOException ex) {
      Util.errln(ex);
      System.exit(1);
    }
  }

  /**
   * Runs all tests.
   * @param args command-line arguments
   * @throws Exception exception
   */
  private void run(final String[] args) throws Exception {
    parseArguments(args);

    final Performance perf = new Performance();
    new Set(Prop.CHOP, false).execute(ctx);

    final XQuery qdoc = new XQuery("doc(' " + path + "/" + CATALOG + "')", ctx);
    final XQItem doc = qdoc.next();
    final String version = string("*:catalog/@version", doc);
    Util.outln(NL + "QT3 Test Suite " + version);
    Util.out("Parsing queries");

    final XQuery qenv = new XQuery("*:catalog/*:environment", ctx).context(doc);
    for(final XQItem ienv : qenv) genvs.add(new QT3Env(ctx, ienv, ""));
    qenv.close();

    final XQuery qset = new XQuery(
        "for $f in //*:test-set/@file return string($f)", ctx).context(doc);
    for(final XQItem it : qset) testSet(it.getString());
    qset.close();
    qdoc.close();

    Util.outln(NL + "Writing log file...");
    final PrintOutput po = new PrintOutput(path + "qt3ts.log");
    po.println("TEST RESULTS ___________________________");
    po.println(NL + "Total #Queries: " + total);
    po.println("Correct Results: " + correct);
    po.println("Conformance: " + pc(correct, total));
    po.print(NL);
    po.println("WRONG __________________________________");
    po.print(NL);
    po.print(wrong.finish());
    if(verbose) {
      po.println("CORRECT ________________________________");
      po.print(NL);
      po.print(right.finish());
    }
    po.close();

    Util.outln(NL + "Total #Queries: " + total);
    Util.outln("Correct Results: " + correct);
    Util.outln("Conformance: " + pc(correct, total));
    Util.outln("Total time: " + perf);
  }

  /**
   * Runs a single test set.
   * @param name name of test set
   * @throws Exception exception
   */
  private void testSet(final String name) throws Exception {
    // feature: schemaImport schemaValidation staticTyping
    // spec: XQ10+ XP30+ XQ30+ XT30+
    // xsd-version: 1.1

    final XQuery qudoc = new XQuery("doc(' " + path + '/' + name + "')", ctx);
    final XQItem doc = qudoc.next();
    final String base = IO.get(doc.getBaseURI()).dir();

    final XQuery qenv = new XQuery("*:test-set/*:environment", ctx).
        context(doc);
    final ObjList<QT3Env> envs = new ObjList<QT3Env>();
    for(final XQItem ienv : qenv) envs.add(new QT3Env(ctx, ienv, base));
    qenv.close();

    final XQuery qts = new XQuery("*:test-set/*:test-case", ctx).context(doc);
    for(final XQItem its : qts) testCase(its, envs, base);
    qts.close();
    qudoc.close();
  }

  /**
   * Runs a single test case.
   * @param test node
   * @param envs environments
   * @param base base uri
   * @throws Exception exception
   */
  private void testCase(final XQItem test, final ObjList<QT3Env> envs,
      final String base) throws Exception {

    final String name = string("@name", test);
    // skip queries that do not match filter
    if(!name.startsWith(single)) return;

    final String quvalue = string("data(*:test)", test);
    final XQuery qexp = new XQuery("*:result/*[1]", ctx).context(test);
    final XQItem expected = qexp.next();

    final Performance perf = new Performance();
    final HashMap<String, XQuery> vars = new HashMap<String, XQuery>();
    XQuery qusrc = null;

    // check for local environment
    QT3Env e = null;
    final XQuery qenv = new XQuery("*:environment[not(@*)]", ctx).context(test);
    final XQItem ienv = qenv.next();
    if(ienv != null) e = new QT3Env(ctx, ienv, base);
    qenv.close();

    if(e == null) {
      final String env = string("*:environment/@ref", test);
      if(!env.isEmpty()) {
        // check for set-based and global environments
        e = envs(envs, env);
        if(e == null) e = envs(genvs, env);
        if(e == null) Util.errln("%: environment '%' not found.", name, env);
      }
    }

    if(e != null) {
      for(final HashMap<String, String> src : e.sources) {
        final String role = src.get(ROLE);
        if(".".equals(role)) {
          qusrc = new XQuery("doc('" + src.get(FILE) + "')", ctx);
        }
      }
      for(final HashMap<String, String> par : e.params) {
        final String select = par.get(SELECT).replaceAll("\"|'", "");
        for(final HashMap<String, String> src : e.sources) {
          if(!select.equals(src.get(URI))) continue;
          vars.put(par.get(NNAME),
              new XQuery("'" + src.get(FILE) + "'", ctx));
          break;
        }
      }
    }

    XQuery qvalue = null;
    final QT3Result result = new QT3Result();
    try {
      // open context document
      final XQItem context = qusrc != null ? qusrc.next() : null;
      // run query
      qvalue = new XQuery(quvalue, ctx).context(context);
      for(final Entry<String, XQuery> s : vars.entrySet()) {
        qvalue.bind(s.getKey(), s.getValue().next());
      }
      result.value = qvalue.value();
    } catch(final XQException ex) {
      result.exc = ex.getException();
    } catch(final Throwable ex) {
      result.error = ex;
    }

    final long time = perf.getTime() / 1000000;
    if(verbose) Util.outln(name + ": " + time + " ms");

    final String msg = test(result, expected);
    final TokenBuilder tmp = new TokenBuilder();
    tmp.add(name).add(NL);
    tmp.add(norm(quvalue)).add(NL);

    String res;
    try {
      res = result.error != null ? "Error: " + result.error.toString() :
        result.exc != null ? "Error: " + result.exc.getLocalizedMessage() :
          string("serialize(., map {'indent':='no'})", result.value);
    } catch(final Exception ex) {
      res = ex.getLocalizedMessage();
    }

    tmp.add("Result: " + norm(res)).add(NL);
    if(msg == null) {
      tmp.add(NL);
      right.add(tmp.finish());
      correct++;
    } else {
      tmp.add("Expect: " + norm(msg)).add(NL).add(NL);;
      wrong.add(tmp.finish());
    }
    if(++total % 500 == 0) Util.out(".");

    for(final String s : vars.keySet()) vars.get(s).close();
    if(qvalue != null) qvalue.close();
    if(qusrc != null) qusrc.close();
    qexp.close();
  }

  /**
   * Returns the specified environment, or {@code null}.
   * @param envs environments
   * @param ref reference
   * @return environment
   */
  private QT3Env envs(final ObjList<QT3Env> envs, final String ref) {
    for(final QT3Env e : envs) if(e.name.equals(ref)) return e;
    return null;
  }

  /**
   * Tests the result of a test case.
   * @param result resulting value
   * @param expected expected result
   * @return optional expected test suite result
   * @throws Exception exception
   */
  private String test(final QT3Result result, final XQItem expected)
      throws Exception {

    final String type = expected.getName();
    final XQValue value = result.value;

    String msg;
    if(type.equals("error")) {
      msg = assertError(result, expected);
    } else if(type.equals("all-of")) {
      msg = allOf(result, expected);
    } else if(type.equals("any-of")) {
      msg = anyOf(result, expected);
    } else if(value != null) {
      if(type.equals("assert")) {
        msg = assertQuery(value, expected);
      } else if(type.equals("assert-count")) {
        msg = assertCount(value, expected);
      } else if(type.equals("assert-deep-eq")) {
        msg = assertDeepEq(value, expected);
      } else if(type.equals("assert-empty")) {
        msg = assertEmpty(value);
      } else if(type.equals("assert-eq")) {
        msg = assertEq(value, expected);
      } else if(type.equals("assert-false")) {
        msg = assertBoolean(value, false);
      } else if(type.equals("assert-permutation")) {
        msg = assertPermutation(value, expected);
      } else if(type.equals("assert-serialization")) {
        msg = assertSerialization(value, expected);
      } else if(type.equals("assert-serialization-error")) {
        msg = assertSerialError(value, expected);
      } else if(type.equals("assert-string-value")) {
        msg = assertStringValue(value, expected);
      } else if(type.equals("assert-true")) {
        msg = assertBoolean(value, true);
      } else if(type.equals("assert-type")) {
        msg = assertType(value, expected);
      } else {
        msg = "Test type not supported: " + type;
      }
    } else {
      msg = expected.toString();
    }
    return msg;
  }

  /**
   * Tests error.
   * @param result query result
   * @param expected expected result
   * @return optional expected test suite result
   */
  private String assertError(final QT3Result result, final XQItem expected) {
    final String exp = string("@" + CODE, expected);
    if(result.exc == null)
      return Util.info("Error code '%' expected.", exp);

    final String res = result.exc.code();
    return exp.equals("*") || exp.equals(res) ? null :
      Util.info("Error code '%' expected, '%' found.", exp, res);
  }

  /**
   * Tests all-of.
   * @param result resulting value
   * @param expected expected result
   * @return optional expected test suite result
   * @throws Exception exception
   */
  private String allOf(final QT3Result result, final XQItem expected)
      throws Exception {

    final XQuery query = new XQuery("*", ctx).context(expected);
    try {
      final TokenBuilder tb = new TokenBuilder();
      for(final XQItem it : query) {
        final String msg = test(result, it);
        if(msg != null) tb.add(tb.size() != 0 ? ", " : "").add(msg);
      }
      return tb.size() == 0 ? null : tb.toString();
    } finally {
      query.close();
    }
  }

  /**
   * Tests any-of.
   * @param result resulting value
   * @param expected expected result
   * @return optional expected test suite result
   * @throws Exception exception
   */
  private String anyOf(final QT3Result result, final XQItem expected)
      throws Exception {

    final XQuery query = new XQuery("*", ctx).context(expected);
    final TokenBuilder tb = new TokenBuilder();
    try {
      for(final XQItem it : query) {
        final String msg = test(result, it);
        if(msg == null) return null;
        tb.add(tb.size() != 0 ? ", " : "").add(msg);
      }
      return tb.toString();
    } finally {
      query.close();
    }
  }

  /**
   * Tests assertion.
   * @param value resulting value
   * @param expected expected result
   * @return optional expected test suite result
   */
  private String assertQuery(final XQValue value, final XQItem expected) {
    final String string = string("data(.)", expected);
    final XQuery query = new XQuery(string, ctx);
    try {
      final XQItem exp = query.bind("result", value).next();
      return exp.getBoolean() ? null :
        Util.info("'true' expected for: %", string);
    } catch(final XQException ex) {
      // should not occur
      return ex.getException().getMessage();
    } finally {
      query.close();
    }
  }

  /**
   * Tests count.
   * @param value resulting value
   * @param expected expected result
   * @return optional expected test suite result
   */
  private String assertCount(final XQValue value, final XQItem expected) {
    final int exp = Token.toInt(string("data(.)", expected));
    final int res = value.getSize();
    return exp == res ? null :
      Util.info("% items expected, % found.", exp, res);
  }

  /**
   * Tests equality.
   * @param value resulting value
   * @param expected expected result
   * @return optional expected test suite result
   * @throws Exception exception
   */
  private String assertEq(final XQValue value, final XQItem expected)
      throws Exception {

    final XQuery query = new XQuery(string(".", expected), ctx);
    try {
      final XQItem exp = query.next();
      final XQItem res = value instanceof XQItem ? (XQItem) value : null;
      return res != null && exp.internal().eq(null, res.internal()) ?
          null : Util.info(exp);
    } catch(final XQException err) {
      return err.getException().getMessage();
    } finally {
      query.close();
    }
  }

  /**
   * Tests deep equals.
   * @param value resulting value
   * @param expected expected result
   * @return optional expected test suite result
   * @throws Exception exception
   */
  private String assertDeepEq(final XQValue value, final XQItem expected)
      throws Exception {

    final XQuery query = new XQuery(string("data(.)", expected), ctx);
    try {
      final XQValue exp = query.value();
      return FNSimple.deep(null, exp.internal().iter(),
          value.internal().iter()) ? null :
        Util.info(exp);
    } finally {
      query.close();
    }
  }

  /**
   * Tests permutation.
   * @param value resulting value
   * @param expected expected result
   * @return optional expected test suite result
   */
  private String assertPermutation(final XQValue value, final XQItem expected) {
    final XQuery query = new XQuery(string("data(.)", expected), ctx);
    try {
      // cache expected results
      final HashSet<String> exp = new HashSet<String>();
      for(final XQItem it : query) exp.add(it.getString());
      // cache actual results
      final HashSet<String> res = new HashSet<String>();
      for(final XQItem it : value) res.add(it.getString());

      if(exp.size() != res.size()) return Util.info(
          "Result sizes are different (% vs. %).", exp.size(), res.size());

      for(final Object s : exp.toArray()) {
        if(!res.contains(s))
          return Util.info("'%' missing in actual result.", s);
      }
      for(final Object s : res.toArray()) {
        if(!exp.contains(s))
          return Util.info("'%' missing in expected result.", s);
      }
      return null;
    } finally {
      query.close();
    }
  }

  /**
   * Tests string value.
   * @param value resulting value
   * @param expected expected result
   * @return optional expected test suite result
   */
  private String assertSerialization(final XQValue value,
      final XQItem expected) {

    final String exp = normNL(string("data(.)", expected));
    final String res = string("serialize(., map {'indent':='no'})", value);
    return exp.equals(normNL(res)) ? null : Util.info(exp);
  }

  /**
   * Tests string value.
   * @param value resulting value
   * @param expected expected result
   * @return optional expected test suite result
   */
  private String assertSerialError(final XQValue value, final XQItem expected) {
    final String exp = string("@" + CODE, expected);
    try {
      value.toString();
      return Util.info("Serialization error '%' expected.", exp);
    } catch(final RuntimeException qe) {
      final String res = qe.getMessage().replaceAll("\\[|\\].*\r?\n?.*", "");
      return exp.equals("*") || exp.equals(res) ? null :
        Util.info("Error code '%' expected, '%' found.", exp, res);
    }
  }

  /**
   * Tests string value.
   * @param value resulting value
   * @param expected expected result
   * @return optional expected test suite result
   */
  private String assertStringValue(final XQValue value, final XQItem expected) {
    final String exp = string("data(.)", expected);
    final TokenBuilder tb = new TokenBuilder();
    int c = 0;
    for(final XQItem it : value) {
      if(c != 0) tb.add(' ');
      tb.add(it.getString());
      c++;
    }
    final String res = tb.toString();
    return res != null && exp.equals(res) ? null : Util.info(exp);
  }

  /**
   * Tests boolean.
   * @param value resulting value
   * @param exp expected
   * @return optional expected test suite result
   */
  private String assertBoolean(final XQValue value, final boolean exp) {
    return value.getType().eq(SeqType.BLN) &&
      (((XQItem) value).getBoolean()) == exp ? null : Util.info(exp);
  }

  /**
   * Tests empty sequence.
   * @param value resulting value
   * @return optional expected test suite result
   */
  private String assertEmpty(final XQValue value) {
    return value == XQEmpty.EMPTY ? null : Util.info("");
  }

  /**
   * Tests type.
   * @param value resulting value
   * @param expected expected result
   * @return optional expected test suite result
   */
  private String assertType(final XQValue value, final XQItem expected) {
    final String exp = string("data(.)", expected);
    final String res = value.getType().toString();
    return exp.equals(res) ? null :
      Util.info("Type '%' expected, '%' found.", exp, res);
  }

  /**
   * Returns the string representation of a query result.
   * @param query query string
   * @param value optional context value
   * @return optional expected test suite result
   */
  String string(final String query, final XQValue value) {
    return XQuery.string(query, value, ctx);
  }

  /**
   * Calculates the percentage of correct queries.
   * @param v value
   * @param t total value
   * @return percentage
   */
  private static String pc(final int v, final long t) {
    return (t == 0 ? 100 : v * 10000 / t / 100d) + "%";
  }

  /**
   * Removes comments from the specified string.
   * @param in input string
   * @return result
   */
  private String norm(final String in) {
    return QueryProcessor.removeComments(in, maxout);
  }

  /**
   * Removes comments from the specified string.
   * @param in input string
   * @return result
   */
  private static String normNL(final String in) {
    return in.replaceAll("\r\n|\r|\n", Prop.NL);
  }

  /**
   * Parses the command-line arguments, specified by the user.
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  private void parseArguments(final String[] args) throws IOException {
    final Args arg = new Args(args, this, " -v [pat]" + NL +
        " [pat] perform tests starting with a pattern" + NL +
        " -v     verbose output",
        Util.info(CONSOLE, Util.name(this)));

    while(arg.more()) {
      if(arg.dash()) {
        final char c = arg.next();
        if(c == 'v') {
          verbose = true;
        } else {
          arg.usage();
        }
      } else {
        single = arg.string();
        maxout = Integer.MAX_VALUE;
      }
    }
  }
}
