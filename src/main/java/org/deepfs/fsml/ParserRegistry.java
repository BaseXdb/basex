package org.deepfs.fsml;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.basex.util.Reflect;
import org.basex.util.Util;
import org.deepfs.fsml.parsers.IFileParser;
import org.deepfs.fsml.parsers.TXTParser;
import org.deepfs.fsml.util.Loader;

/**
 * Registry for file parsers.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Bastian Lemke
 */
public final class ParserRegistry {

  /** Registry for MetadataAdapter implementations. */
  private static final Map<String, Class<? extends IFileParser>> REGISTRY =
      new HashMap<String, Class<? extends IFileParser>>();

  /** Fallback parser for file suffixes that are not registered. */
  private static Class<? extends IFileParser> fallbackParser;

  /**
   * Registers a parser implementation with the fs parser.
   * @param suffix the suffix to register the parser implementation for
   * @param c the parser implementation class
   */
  public static void register(final String suffix,
      final Class<? extends IFileParser> c) {
    REGISTRY.put(suffix, c);
  }

  /**
   * Registers a fallback parser implementation with the fs parser.
   * @param c the parser implementation class
   */
  public static void registerFallback(final Class<? extends IFileParser> c) {
    if(fallbackParser != null) {
      Util.debug("Replacing fallback parser with " + c.getName());
    }
    fallbackParser = c;
  }

  static {
    try {
      final Class<?>[] classes = Loader.load(IFileParser.class.getPackage(),
          IFileParser.class);
      for(final Class<?> c : classes) {
        final String name = Util.name(c);
        if(!REGISTRY.containsValue(c) && fallbackParser != c)
          Util.debug("Loading % ... FAILED", name);
      }
    } catch(final IOException ex) {
      Util.errln("Failed to load parsers (%)", ex.getMessage());
    }
  }

  /** Instantiated parsers. */
  private final Map<String, IFileParser> parserInstances;
  /** Instantiated fallback parser. */
  private IFileParser fallbackParserInstance;

  /** Constructor. */
  public ParserRegistry() {
    final int size = (int) Math.ceil(REGISTRY.size() / 0.75f);
    parserInstances = new HashMap<String, IFileParser>(size);
    fallbackParserInstance = new TXTParser();
  }

  /**
   * Gets a parser implementation for given file suffix.
   * @param suffix the file suffix to get the parser for
   * @return the parser implementation or {@code null} if no implementation
   *         is available
   * @throws ParserException if the parser could not be loaded
   */
  IFileParser getParser(final String suffix) throws ParserException {
    IFileParser instance = parserInstances.get(suffix);
    if(instance == null) {
      final Class<? extends IFileParser> clazz = REGISTRY.get(suffix);
      if(clazz != null) {
        instance = (IFileParser) Reflect.get(clazz);
        if(instance == null) {
          throw new ParserException("Failed to load " + Util.name(clazz) +
              " for suffix " + suffix);
        }
      }
      // put in hash map ... even if null
      parserInstances.put(suffix, instance);
    }
    return instance;
  }

  /**
   * Gets the fallback parser implementation.
   * @return the fallback parser implementation or {@code null} if no
   *         fallback parser is available
   * @throws ParserException if the parser could not be loaded
   */
  public IFileParser getFallbackParser() throws ParserException {
    if(fallbackParser == null) return null;
    if(fallbackParserInstance == null) {
      try {
        fallbackParserInstance = fallbackParser.newInstance();
        Util.debug("Successfully initialized fallback parser.");
      } catch(final Exception ex) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Failed to load fallback parser (");
        sb.append(ex.getMessage());
        sb.append(")");
        throw new ParserException(sb.toString());
      }
    }
    return fallbackParserInstance;
  }

  /**
   * Returns all available parsers.
   * @return all available parsers
   */
  public String[][] availableParsers() {
    final String[][] parsers = new String[REGISTRY.size()][];
    int i = 0;
    for(final Entry<String, Class<? extends IFileParser>> parser :
      REGISTRY.entrySet()) {
      parsers[i++] = new String[] { parser.getKey(),
          parser.getValue().getName()};
    }
    return parsers;
  }
}
