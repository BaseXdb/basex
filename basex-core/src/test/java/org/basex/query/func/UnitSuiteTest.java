package org.basex.query.func;

import java.io.*;

import org.basex.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.junit.jupiter.api.*;

/**
 * Integration tests for the XQUnit test runner: annotations, lifecycle hooks and the
 * structure of the generated test report.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UnitSuiteTest extends SandboxTest {
  /** Reports every kind of test outcome. */
  @Test public void report() {
    final String doc = run("report",
        "declare %unit:test function _:ok() { unit:assert(true()) };"
      + "declare %unit:test function _:assert-fail() { unit:assert(false(), 'info') };"
      + "declare %unit:test function _:equals() { unit:assert-equals(1, 2) };"
      + "declare %unit:test function _:err() { 1 + <a/> };"
      + "declare %unit:test function _:fail() { unit:fail('boom') };"
      + "declare %unit:test('expected', 'FORG0001') function _:expected() { 1 + <a/> };"
      + "declare %unit:test('expected', 'FOAR0001', 'FORG0001') function _:multi() { 1 + <a/> };"
      + "declare %unit:test('expected', 'FORG0001') function _:not-raised() { () };"
      + "declare %unit:test %unit:ignore('skip') function _:skipped() { () };"
      + "declare %unit:test %unit:timeout(0.1) function _:timeout() { prof:sleep(2000) };");

    // aggregated counts
    query(doc + "/testsuites/testsuite/@tests/data()", 10);
    query(doc + "//testsuite/@failures/data()", 4);
    query(doc + "//testsuite/@errors/data()", 2);
    query(doc + "//testsuite/@skipped/data()", 1);
    query("count(" + doc + "//testcase)", 10);

    // passed tests carry neither failure nor error
    query("count(" + doc + "//testcase[@name = ('ok', 'expected', 'multi')]/(failure | error))", 0);

    // assertion failures and unit:fail are reported as failures, with their info
    query("count(" + doc + "//testcase[@name = ('assert-fail', 'fail', 'not-raised')]/failure)", 3);
    query(doc + "//testcase[@name = 'assert-fail']/failure/info/data()", "info");
    query(doc + "//testcase[@name = 'not-raised']/failure/expected/data()", "err:FORG0001");

    // assert-equals reports the returned and expected items
    query("exists(" + doc + "//testcase[@name = 'equals']/failure/returned)", true);
    query("exists(" + doc + "//testcase[@name = 'equals']/failure/expected)", true);

    // uncaught errors and timeouts are reported as errors
    query(doc + "//testcase[@name = 'err']/error/@type/data()", "FORG0001");
    query(doc + "//testcase[@name = 'timeout']/error/@type/data()", "unit:timeout");

    // skipped tests carry the ignore message
    query(doc + "//testcase[@name = 'skipped']/@skipped/data()", "skip");
  }

  /** Lifecycle hooks fire, and in the correct order. */
  @Test public void lifecycle() {
    final String doc = run("lifecycle",
        "declare %unit:before-module function _:bm() { store:put('log', 'M') };"
      + "declare %unit:before function _:b() { store:put('log', store:get('log') || 'B') };"
      + "declare %unit:test function _:t() { unit:assert(store:get('log') = 'MB') };"
      + "declare %unit:after function _:a() { store:put('log', store:get('log') || 'A') };"
      + "declare %unit:after-module function _:am() "
      + "  { store:put('log', store:get('log') || 'X') };");

    // the test passed: before-module and before ran, in order, before the test
    query("count(" + doc + "//testcase/(failure | error))", 0);
    // full order, including after and after-module (inspected after the run)
    query("store:get('log')", "MBAX");
  }

  /** Errors in initializing functions are reported as test-init errors. */
  @Test public void initError() {
    final String doc = run("init",
        "declare %unit:before-module function _:init() { 1 + <a/> };"
      + "declare %unit:test function _:t() { unit:assert(true()) };");

    // no test was run; a testinit error was reported instead
    query(doc + "//testsuite/@tests/data()", 0);
    query(doc + "//testsuite/@errors/data()", 1);
    query(doc + "//testinit/@name/data()", "init");
    query(doc + "//testinit/error/@type/data()", "FORG0001");
  }

  /**
   * Runs a unit test module and returns a {@code doc(...)} accessor for its serialized report.
   * @param name base file name
   * @param module module body (without namespace declaration)
   * @return doc() expression addressing the report
   */
  private static String run(final String name, final String module) {
    try {
      final IOFile mod = new IOFile(sandbox(), name + ".xqm");
      mod.write("module namespace _ = '_'; " + module);
      final ArrayOutput ao = new ArrayOutput();
      new org.basex.core.cmd.Test(mod.path()).run(context, ao);
      final IOFile report = new IOFile(sandbox(), name + ".xml");
      report.write(ao.finish());
      return "doc('" + report.url() + "')";
    } catch(final IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
