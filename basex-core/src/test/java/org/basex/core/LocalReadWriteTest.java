package org.basex.core;

import static org.basex.query.func.Function.*;

import java.util.*;
import java.util.concurrent.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * Concurrent read/write integrity test on a single database (local, multi-threaded execution).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class LocalReadWriteTest extends SandboxTest {
  /**
   * Inserts uniquely-identified nodes from many writer threads while reader threads count
   * nodes concurrently, and verifies that every insert is applied exactly once (writes must
   * be serialized by the locking layer).
   * @throws Exception exception
   */
  @Test @Timeout(60) public void readWriteIntegrity() throws Exception {
    final int writers = 50, readers = 20, reads = 20;
    query(_DB_CREATE.args(NAME, " <root/>", "doc.xml"));
    try {
      final ArrayList<Callable<?>> tasks = new ArrayList<>(writers + readers);
      // writers: each inserts one uniquely-identified node into the same database
      for(int w = 0; w < writers; w++) {
        final int id = w;
        tasks.add(() -> {
          query("insert node <n id='" + id + "'/> into " + _DB_GET.args(NAME) + "/root");
          return null;
        });
      }
      // readers: repeatedly count nodes concurrently with the writers
      for(int r = 0; r < readers; r++) {
        tasks.add(() -> {
          for(int i = 0; i < reads; i++) query("count(" + _DB_GET.args(NAME) + "/root/n)");
          return null;
        });
      }
      parallel(tasks);

      // every insert was applied exactly once, with no lost or duplicate ids
      query("count(" + _DB_GET.args(NAME) + "/root/n)", writers);
      query("count(distinct-values(" + _DB_GET.args(NAME) + "/root/n/@id))", writers);
    } finally {
      query(_DB_DROP.args(NAME));
    }
  }
}
