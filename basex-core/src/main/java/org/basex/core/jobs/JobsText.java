package org.basex.core.jobs;

import org.basex.query.value.item.*;

/**
 * This class assembles texts which are used for the job management.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public interface JobsText {
  /** QName. */
  QNm Q_JOBS = new QNm("jobs");
  /** QName. */
  QNm Q_JOB = new QNm("job");
  /** QName. */
  QNm Q_ID = new QNm("id");
  /** QName. */
  QNm Q_DURATION = new QNm("duration");
  /** QName. */
  QNm Q_STATE = new QNm("state");
  /** QName. */
  QNm Q_START = new QNm("start");
  /** QName. */
  QNm Q_END = new QNm("end");
  /** QName. */
  QNm Q_INTERVAL = new QNm("interval");
  /** QName. */
  QNm Q_USER = new QNm("user");
  /** QName. */
  QNm Q_READS = new QNm("reads");
  /** QName. */
  QNm Q_WRITES = new QNm("writes");
  /** QName. */
  QNm Q_TIME = new QNm("time");
  /** QName. */
  QNm Q_TYPE = new QNm("type");
}
