package org.basex.util.ft;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.basex.core.Prop;
import org.basex.query.ft.FTOpt;
import org.basex.util.Util;

/**
 * Calculates SpanTokens within a seperate thread and caches them.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Jens Erat
 */
final class SpanThreadedQueue extends SpanProcessor {
  /** Queue used for caching. */
  final BlockingQueue<Span> queue = new LinkedBlockingQueue<Span>();
  /** special span token used as last element marker. */
  static final Span LAST = new Span(null, 0, 0, false);

  @Override
  SPType type() {
    return SPType.special;
  }

  @Override
  SpanProcessor get(final Prop p, final FTOpt f) {
    return null;
  }

  @Override
  Iterator<Span> process(final Iterator<Span> iterator) {
    final Runnable r = new Runnable() {
     @Override
      public void run() {
        while(true) {
            final boolean hasNext = iterator.hasNext();
            try {
            if(!hasNext) break;
              final Span next = iterator.next();
              queue.put(next);

            } catch(final InterruptedException e) {
              // [DP][JE] how to deal with this exception?
              e.printStackTrace();
            }
        }
        try {
          queue.put(LAST);
        } catch(final InterruptedException e) {
          // [DP][JE] Auto-generated catch block
          e.printStackTrace();
        }
      }
    };
    final Thread t = new Thread(r);
    t.start();
    return new Iterator<Span>() {
      Span buffer;

      @Override
      public boolean hasNext() {
        if (LAST == buffer) return false;
        if (null != buffer) return true;
        try {
          buffer = queue.take();
        } catch(final InterruptedException e) {
          // [DP][JE] Auto-generated catch block
          e.printStackTrace();
        }
        return LAST != buffer;
      }

      @Override
      public Span next() {
        if (hasNext()) {
          final Span res = buffer;
          buffer = null;
          return res;
        }
        throw new NoSuchElementException();
      }

      @Override
      public void remove() {
        Util.notimplemented();
      }
    };
  }

  @Override
  int prec() {
    return 0;
  }

  @Override
  public EnumSet<Language> languages() {
    return EnumSet.allOf(Language.class);
  }

}
