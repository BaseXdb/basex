package org.basex.tests.w3c;

import static org.basex.core.Text.*;

import java.io.IOException;

import org.basex.core.Context;
import org.basex.io.out.ArrayOutput;
import org.basex.io.out.PrintOutput;
import org.basex.io.serial.Serializer;
import org.basex.io.serial.SerializerException;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.func.FNSimple;
import org.basex.query.item.ANode;
import org.basex.query.item.Bln;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.util.Args;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.basex.util.hash.TokenSet;

/**
 * XQuery Test Suite wrapper.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class QT3TS {
  /** Maximum number of tests to run. */
  private static final int MAX = Integer.MAX_VALUE;
  /** Maximum length of result output. */
  private int maxout = Integer.MAX_VALUE;

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
  protected final Context context = new Context();
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

    final String qudoc = "doc(' " + path + "/catalog.xml')";
    final QueryProcessor qpdoc = new QueryProcessor(qudoc, context);
    final Value doc = qpdoc.value();

    final String version = string("/*:catalog/@version", doc);
    Util.outln(NL + "QT3 Test Suite " + version);
    Util.out("Parsing queries");

    final String quset = "for $f in //*:test-set/@file return string($f)";
    final QueryProcessor qpset = new QueryProcessor(quset, context);
    qpset.context(doc);

    final Iter ir = qpset.iter();
    for(Item it; (it = ir.next()) != null && total < MAX;) testSet(it);
    qpset.close();

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
    //po.println("CORRECT ________________________________");
    //po.print(NL);
    //po.print(right.finish());
    po.close();

    Util.outln(NL + "Total #Queries: " + total);
    Util.outln("Correct Results: " + correct);
    Util.outln("Conformance: " + pc(correct, total));
    Util.outln("Total time: " + perf);
  }

  /**
   * Runs a single test set.
   * @param set test set
   * @throws Exception exception
   */
  private void testSet(final Item set) throws Exception {
    final String docts = path + '/' + set.toJava();
    final String quts = "doc('" + docts + "')//*:test-case";
    final QueryProcessor qpts = new QueryProcessor(quts, context);
    final Iter irts = qpts.iter();
    for(Item itts; (itts = irts.next()) != null && total < MAX;) {
      testCase(itts);
    }
    qpts.close();
  }

  /**
   * Runs a single test case.
   * @param test node
   * @throws Exception exception
   */
  private void testCase(final Item test) throws Exception {
    final String name = string("@name", test);
    // skip queries that do not match filter
    if(!name.startsWith(single)) return;

    final String quvalue = string("data(.//*:test)", test);
    final String quexpected = ".//*:result/*[1]";
    final QueryProcessor qpexpected = new QueryProcessor(quexpected, context);
    qpexpected.context(test);
    final ANode expected = (ANode) qpexpected.iter().next();
    qpexpected.close();

    final Performance perf = new Performance();
    final QueryProcessor qpvalue = new QueryProcessor(quvalue, context);
    final Result result = new Result();
    try {
      result.value = qpvalue.value();
    } catch(final QueryException ex) {
      result.exception = ex;
    } catch(final Throwable ex) {
      result.error = ex;
    }
    qpvalue.close();

    final long time = perf.getTime() / 1000000;
    if(verbose) Util.outln(name + ": " + time + " ms");

    final String msg = test(result, expected);
    final TokenBuilder tmp = new TokenBuilder();
    tmp.add(name).add(NL);
    tmp.add("Query : ").add(norm(quvalue)).add(NL);
    tmp.add("Result: " + norm(result.toString())).add(NL);
    if(msg == null) {
      tmp.add(NL);
      right.add(tmp.finish());
      correct++;
    } else {
      tmp.add("Expect: " + norm(msg)).add(NL).add(NL);;
      wrong.add(tmp.finish());
    }
    if(++total % 500 == 0) Util.out(".");
  }

  /**
   * Tests the result of a test case.
   * @param result resulting value
   * @param expected expected result
   * @return optional expected test suite result
   * @throws Exception exception
   */
  private String test(final Result result, final ANode expected)
      throws Exception {

    final String type = Token.string(expected.qname().ln());
    final Value value = result.value;

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
      final ArrayOutput ao = new ArrayOutput();
      expected.serialize(Serializer.get(ao));
      msg = ao.toString();
    }
    return msg;
  }

  /**
   * Tests all-of.
   * @param result resulting value
   * @param expected expected result
   * @return optional expected test suite result
   * @throws Exception exception
   */
  private String allOf(final Result result, final ANode expected)
      throws Exception {

    final QueryProcessor qp = new QueryProcessor("*", context);
    try {
      final TokenBuilder tb = new TokenBuilder();
      qp.context(expected);
      final Iter ir = qp.iter();
      for(Item it; (it = ir.next()) != null;) {
        final String msg = test(result, (ANode) it);
        if(msg != null) tb.add(tb.size() != 0 ? ", " : "").add(msg);
      }
      return tb.size() == 0 ? null : tb.toString();
    } finally {
      qp.close();
    }
  }

  /**
   * Tests any-of.
   * @param result resulting value
   * @param expected expected result
   * @return optional expected test suite result
   * @throws Exception exception
   */
  private String anyOf(final Result result, final ANode expected)
      throws Exception {

    final QueryProcessor qp = new QueryProcessor("*", context);
    try {
      final TokenBuilder tb = new TokenBuilder();
      qp.context(expected);
      final Iter ir = qp.iter();
      for(Item it; (it = ir.next()) != null;) {
        final String msg = test(result, (ANode) it);
        if(msg == null) return null;
        tb.add(tb.size() != 0 ? ", " : "").add(msg);
      }
      return tb.toString();
    } finally {
      qp.close();
    }
  }

  /**
   * Tests assertion.
   * @param value resulting value
   * @param expected expected result
   * @return optional expected test suite result
   * @throws Exception exception
   */
  private String assertQuery(final Value value, final ANode expected)
      throws Exception {

    final String string = string("data(.)", expected);
    final QueryProcessor qp = new QueryProcessor(string, context);
    qp.bind("result", value);
    try {
      final Item exp = qp.iter().next();
      return exp instanceof Bln && ((Bln) exp).bool(null) ? null :
        Util.info("'true' expected for: %", string);
    } catch(final QueryException ex) {
      // should not occur
      return ex.getMessage();
    } finally {
      qp.close();
    }
  }

  /**
   * Tests equality.
   * @param value resulting value
   * @param expected expected result
   * @return optional expected test suite result
   * @throws Exception exception
   */
  private String assertEq(final Value value, final ANode expected)
      throws Exception {

    final String string = string("data(.)", expected);
    final QueryProcessor qp = new QueryProcessor(string, context);
    try {
      final Item exp = qp.iter().next();
      final Item res = value instanceof Item ? (Item) value : null;
      return res != null && exp.eq(null, res) ? null :
        Util.info("'%' expected, '%' found.", exp.toJava(), res.toJava());
    } catch(final QueryException ex) {
      // should not occur
      return ex.getMessage();
    } finally {
      qp.close();
    }
  }

  /**
   * Tests deep equals.
   * @param value resulting value
   * @param expected expected result
   * @return optional expected test suite result
   * @throws Exception exception
   */
  private String assertDeepEq(final Value value, final ANode expected)
      throws Exception {

    final String string = string("data(.)", expected);
    final QueryProcessor qp = new QueryProcessor(string, context);
    try {
      final Value exp = qp.value();
      return FNSimple.deep(null, exp.iter(), value.iter()) ? null :
        Util.info("Deep comparison: '%' expected.", exp);
    } catch(final QueryException ex) {
      // should not occur
      return ex.getMessage();
    } finally {
      qp.close();
    }
  }

  /**
   * Tests permutation.
   * @param value resulting value
   * @param expected expected result
   * @return optional expected test suite result
   * @throws Exception exception
   */
  private String assertPermutation(final Value value, final ANode expected)
      throws Exception {

    final String string = string("data(.)", expected);
    final QueryProcessor qp = new QueryProcessor(string, context);
    try {
      // cache expected results
      final TokenSet exp = new TokenSet();
      Iter ir = qp.iter();
      for(Item it; (it = ir.next()) != null;) exp.add(it.atom(null));
      // cache actual results
      final TokenSet res = new TokenSet();
      ir = value.iter();
      for(Item it; (it = ir.next()) != null;) res.add(it.atom(null));

      if(exp.size() != res.size()) return Util.info(
          "Result sizes are different (% vs. %).", exp.size(), res.size());

      for(int i = 1; i <= exp.size(); i++) {
        if(res.id(exp.key(i)) == 0)
          return Util.info("'%' missing in actual result.", exp.key(i));
        if(exp.id(res.key(i)) == 0)
          return Util.info("'%' missing in expected result.", res.key(i));
      }
      return null;
    } catch(final QueryException ex) {
      // should not occur
      return ex.getMessage();
    } finally {
      qp.close();
    }
  }

  /**
   * Tests count.
   * @param value resulting value
   * @param expected expected result
   * @return optional expected test suite result
   * @throws Exception exception
   */
  private String assertCount(final Value value, final ANode expected)
      throws Exception {

    final int exp = Token.toInt(string("data(.)", expected));
    final long res = value.size();
    return exp == res ? null :
      Util.info("% items expected, % found.", exp, res);
  }

  /**
   * Tests string value.
   * @param value resulting value
   * @param expected expected result
   * @return optional expected test suite result
   * @throws Exception exception
   */
  private String assertSerialization(final Value value, final ANode expected)
      throws Exception {

    final String exp = string("data(.)", expected);
    final ArrayOutput ao = new ArrayOutput();
    final Serializer ser = Serializer.get(ao);
    final Iter ir = value.iter();
    for(Item it; (it = ir.next()) != null;) it.serialize(ser);
    final String res = ao.toString();
    return res != null && exp.equals(res) ? null :
      Util.info("Serialization '%' expected, '%' found.", exp, res);
  }

  /**
   * Tests string value.
   * @param value resulting value
   * @param expected expected result
   * @return optional expected test suite result
   * @throws Exception exception
   */
  private String assertSerialError(final Value value, final ANode expected)
      throws Exception {

    final String exp = string("@code", expected);
    final ArrayOutput ao = new ArrayOutput();
    final Serializer ser = Serializer.get(ao);
    final Iter ir = value.iter();
    try {
      for(Item it; (it = ir.next()) != null;) it.serialize(ser);
      return Util.info("Serialization error '%' expected.", exp);
    } catch(final SerializerException qe) {
      final String res = new QueryException(null, qe).code();
      return res.equals(exp) ? null :
        Util.info("Serialization error '%' expected, '%' found.", exp, res);
    }
  }

  /**
   * Tests string value.
   * @param value resulting value
   * @param expected expected result
   * @return optional expected test suite result
   * @throws Exception exception
   */
  private String assertStringValue(final Value value, final ANode expected)
      throws Exception {

    final String exp = string("data(.)", expected);
    final TokenBuilder tb = new TokenBuilder();
    final Iter ir = value.iter();
    for(Item it; (it = ir.next()) != null;) {
      tb.add(tb.size() != 0 ? " " : "").add(it.atom(null));
    }
    final String res = tb.toString();
    return res != null && exp.equals(res) ? null :
      Util.info("String value '%' expected, '%' found.", exp, res);
  }

  /**
   * Tests boolean.
   * @param value resulting value
   * @param exp expected
   * @return optional expected test suite result
   */
  private String assertBoolean(final Value value, final boolean exp) {
    final Bln res = value instanceof Bln ? (Bln) value : null;
    return res != null && res.bool(null) == exp ? null :
      Util.info("Boolean '%' expected, '%' found.", exp, res);
  }

  /**
   * Tests empty sequence.
   * @param value resulting value
   * @return optional expected test suite result
   */
  private String assertEmpty(final Value value) {
    return value instanceof Empty ? null :
      Util.info("Empty sequence expected, '%' found.", value);
  }

  /**
   * Tests error.
   * @param result query result
   * @param expected expected result
   * @return optional expected test suite result
   * @throws Exception exception
   */
  private String assertError(final Result result, final ANode expected)
      throws Exception {

    final String exp = string("@code", expected);
    if(result.exception == null)
      return Util.info("Error code '%' expected.", exp);

    final String res = result.exception.code();
    return exp.equals("*") || exp.equals(res) ? null :
      Util.info("Error code '%' expected, '%' found.", exp, res);
  }

  /**
   * Tests type.
   * @param value resulting value
   * @param expected expected result
   * @return optional expected test suite result
   * @throws Exception exception
   */
  private String assertType(final Value value, final ANode expected)
      throws Exception {

    final String exp = string("data(.)", expected);
    final String res = value.type().toString();
    return exp.equals(res) ? null :
      Util.info("Type '%' expected, '%' found.", exp, res);
  }

  /**
   * Returns the string representation of a query result.
   * @param query query string
   * @param ctx optional context
   * @return optional expected test suite result
   * @throws Exception exception
   */
  private String string(final String query, final Value ctx) throws Exception {
    final QueryProcessor qp = new QueryProcessor(query, context);
    try {
      if(ctx != null) qp.context(ctx);
      return Token.string(qp.iter().next().atom(null));
    } finally {
      qp.close();
    }
  }


  /**
   * Calculates the percentage of correct queries.
   * @param v value
   * @param t total value
   * @return percentage
   */
  private String pc(final int v, final long t) {
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
   * Parses the command-line arguments, specified by the user.
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  private void parseArguments(final String[] args) throws IOException {
    final Args arg = new Args(args, this, " -v" + NL +
        " -v     verbose output", Util.info(CONSOLE, Util.name(this)));

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

  /** Result instance. */
  static class Result {
    /** Resulting value. */
    Value value;
    /** Resulting exception. */
    QueryException exception;
    /** Resulting error. */
    Throwable error;

    @Override
    public String toString() {
      return (value != null ? value : exception != null ?
          exception.getLocalizedMessage() : error).toString();
    }
  }
}
