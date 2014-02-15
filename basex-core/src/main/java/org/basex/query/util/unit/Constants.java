package org.basex.query.util.unit;

import static org.basex.util.Token.*;

/**
 * XQuery Unit tests.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public interface Constants {
  /** Annotation: test. */
  byte[] TEST = token("test");
  /** Annotation: skipped. */
  byte[] IGNORE = token("ignore");
  /** Annotation: before. */
  byte[] BEFORE = token("before");
  /** Annotation: before. */
  byte[] AFTER = token("after");
  /** Annotation: before. */
  byte[] BEFORE_MODULE = token("before-module");
  /** Annotation: before. */
  byte[] AFTER_MODULE = token("after-module");
  /** Annotation: expected. */
  byte[] EXPECTED = token("expected");
  /** Name: returned. */
  byte[] RETURNED = token("returned");

  /** Name: testsuites. */
  byte[] TESTSUITES = token("testsuites");
  /** Name: testsuite. */
  byte[] TESTSUITE = token("testsuite");
  /** Name: testcase. */
  byte[] TESTCASE = token("testcase");
  /** Name: failure. */
  byte[] FAILURE = token("failure");
  /** Name: line. */
  byte[] LINE = token("line");
  /** Name: column. */
  byte[] COLUMN = token("column");
  /** Name: error. */
  byte[] ERROR = token("error");
  /** Name: failures. */
  byte[] FAILURES = token("failures");
  /** Name: error. */
  byte[] ERRORS = token("errors");
  /** Name: skipped. */
  byte[] SKIPPED = token("skipped");
  /** Name: error. */
  byte[] TESTS = token("tests");
  /** Name: name. */
  byte[] NAME = token("name");
  /** Name: item. */
  byte[] ITEM = token("item");
  /** Name: type. */
  byte[] TYPE = token("type");
  /** Name: time. */
  byte[] TIME = token("time");
}
