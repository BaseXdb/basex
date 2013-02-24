package org.basex.query.util.unit;

import static org.basex.util.Token.*;

import org.basex.query.value.item.*;

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
  /** Token: timeout. */
  byte[] TIMEOUT = token("timeout");

  /** QName: testsuites. */
  QNm Q_TESTSUITES = new QNm("testsuites");
  /** QName: testsuite. */
  QNm Q_TESTSUITE = new QNm("testsuite");
  /** QName: testcase. */
  QNm Q_TESTCASE = new QNm("testcase");
  /** QName: failure. */
  QNm Q_FAILURE = new QNm("failure");
  /** QName: error. */
  QNm Q_ERROR = new QNm("error");
  /** QName: failure. */
  QNm Q_FAILURES = new QNm("failures");
  /** QName: error. */
  QNm Q_ERRORS = new QNm("errors");
  /** QName: skipped. */
  QNm Q_SKIPPED = new QNm("skipped");
  /** QName: error. */
  QNm Q_TESTS = new QNm("tests");
  /** QName: name. */
  QNm Q_NAME = new QNm("name");
  /** QName: message. */
  QNm Q_MESSAGE = new QNm("message");
  /** QName: type. */
  QNm Q_TYPE = new QNm("type");
  /** QName: time. */
  QNm Q_TIME = new QNm("time");
}
