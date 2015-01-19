package org.basex.query.func.unit;

import static org.basex.util.Token.*;

/**
 * XQUnit constants.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public interface Constants {
  /** Name: testsuites. */
  byte[] TESTSUITES = token("testsuites");
  /** Name: testsuite. */
  byte[] TESTSUITE = token("testsuite");
  /** Name: testinit. */
  byte[] TESTINIT = token("testinit");
  /** Name: testcase. */
  byte[] TESTCASE = token("testcase");
  /** Name: failure. */
  byte[] FAILURE = token("failure");
  /** Name: line. */
  byte[] LINE = token("line");
  /** Name: column. */
  byte[] COLUMN = token("column");
  /** Name: uri. */
  byte[] URI = token("uri");
  /** Name: error. */
  byte[] ERROR = token("error");
  /** Name: expected. */
  byte[] EXPECTED = token("expected");
  /** Name: returned. */
  byte[] RETURNED = token("returned");
  /** Name: info. */
  byte[] INFO = token("info");
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
