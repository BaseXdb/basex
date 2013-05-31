package org.basex.query.util.unit;

import static org.basex.util.Token.*;

/**
 * XQuery Unit tests.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public interface Constants {
  /** Token: test. */
  byte[] TEST = token("test");
  /** Token: skipped. */
  byte[] IGNORE = token("ignore");
  /** Token: before. */
  byte[] BEFORE = token("before");
  /** Token: before. */
  byte[] AFTER = token("after");
  /** Token: before. */
  byte[] BEFORE_MODULE = token("before-module");
  /** Token: before. */
  byte[] AFTER_MODULE = token("after-module");
  /** Token: expected. */
  byte[] EXPECTED = token("expected");

  /** Name: testsuites. */
  String TESTSUITES = "testsuites";
  /** Name: testsuite. */
  String TESTSUITE = "testsuite";
  /** Name: testcase. */
  String TESTCASE = "testcase";
  /** Name: failure. */
  String FAILURE = "failure";
  /** Name: error. */
  String ERROR = "error";
  /** Name: failure. */
  String FAILURES = "failures";
  /** Name: error. */
  String ERRORS = "errors";
  /** Name: skipped. */
  String SKIPPED = "skipped";
  /** Name: error. */
  String TESTS = "tests";
  /** Name: name. */
  String NAME = "name";
  /** Name: message. */
  String MESSAGE = "message";
  /** Name: type. */
  String TYPE = "type";
  /** Name: time. */
  String TIME = "time";
}
