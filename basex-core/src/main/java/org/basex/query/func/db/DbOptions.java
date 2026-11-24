package org.basex.query.func.db;

import java.util.*;

import org.basex.core.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Database options that can be supplied to functions, with values only for supplied options.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class DbOptions extends MainOptions {
  /** Parsing options. */
  private static final Map<String, Option<?>> PARSING_DEFS = supported(PARSING);
  /** Parsing options and the replace option. */
  private static final Map<String, Option<?>> PUT_DEFS = supported(Array.add(PARSING, REPLACE));
  /** Indexing and parsing options. */
  private static final Map<String, Option<?>> CREATE_DEFS = supported(CREATING);
  /** Indexing options. */
  private static final Map<String, Option<?>> INDEXING_DEFS = supported(INDEXING);
  /** Replace option. */
  private static final Map<String, Option<?>> REPLACE_DEFS = supported(REPLACE);

  /** Supported options. */
  private final Map<String, Option<?>> definitions;

  /**
   * Constructor.
   * @param definitions supported options
   */
  DbOptions(final Map<String, Option<?>> definitions) {
    super(false);
    this.definitions = definitions;
  }

  @Override
  protected final Map<String, Option<?>> definitions() {
    return definitions;
  }

  /**
   * Returns the supported options, addressed by their lower-case names.
   * @param options supported options
   * @return options
   */
  private static Map<String, Option<?>> supported(final Option<?>... options) {
    final TreeMap<String, Option<?>> map = new TreeMap<>();
    for(final Option<?> option : options) {
      map.put(option.name().toLowerCase(Locale.ENGLISH), option);
    }
    return Collections.unmodifiableSortedMap(map);
  }

  /** Parsing options. */
  public static final class Parsing extends DbOptions {
    /** Constructor. */
    public Parsing() {
      super(PARSING_DEFS);
    }
  }

  /** Parsing options and the replace option. */
  public static final class Put extends DbOptions {
    /** Constructor. */
    public Put() {
      super(PUT_DEFS);
    }
  }

  /** Indexing and parsing options. */
  public static final class Create extends DbOptions {
    /** Constructor. */
    public Create() {
      super(CREATE_DEFS);
    }
  }

  /** Indexing options. */
  public static final class Indexing extends DbOptions {
    /** Constructor. */
    public Indexing() {
      super(INDEXING_DEFS);
    }
  }

  /** Replace option. */
  public static final class Replace extends DbOptions {
    /** Constructor. */
    public Replace() {
      super(REPLACE_DEFS);
    }
  }
}
