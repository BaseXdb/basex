package org.basex.util.ft;

import java.util.Iterator;
import org.basex.core.Prop;
import org.basex.query.ft.FTOpt;

/**
 * Process a token. Can output multiple {@link Span} tokens for one input token.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Jens Erat
 */
abstract class SpanProcessor extends LanguageDependent {
  /**
   * Factory method.
   * @param p database properties
   * @param f full-text options
   * @return span processor
   */
  abstract SpanProcessor newInstance(final Prop p, final FTOpt f);

  /**
   * Process a token.
   * @param iterator input iterator
   * @return output iterator
   */
  abstract Iterator<Span> process(final Iterator<Span> iterator);

  /**
   * Returns type of {@link SpanProcessor}.
   * @return type
   */
  abstract SPType getType();

  /** Processor type. */
  static enum SPType {
    /** SpanProcessor is stemmer. */
    stemmer,
    /** SpanProcessor has a special function. */
    special;
  }
}
