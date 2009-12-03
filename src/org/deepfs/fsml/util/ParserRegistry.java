package org.deepfs.fsml.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.basex.core.Main;
import org.deepfs.fsml.parsers.IFileParser;
import org.deepfs.fsml.parsers.TXTParser;

/**
 * Registry for file parsers.
 * @author Workgroup DBIS, University of Konstanz 2009, ISC License
 * @author Bastian Lemke
 */
public class ParserRegistry {

  /** Registry for MetadataAdapter implementations. */
  static final Map<String, Class<? extends IFileParser>> REGISTRY =
      new HashMap<String, Class<? extends IFileParser>>();

  /** Fallback parser for file suffixes that are not registered. */
  static Class<? extends IFileParser> fallbackParser;

  /**
   * Registers a parser implementation with the fs parser.
   * @param suffix the suffix to register the parser implementation for.
   * @param c the parser implementation class.
   */
  public static void register(final String suffix,
      final Class<? extends IFileParser> c) {
    REGISTRY.put(suffix, c);
  }

  /**
   * Registers a fallback parser implementation with the fs parser.
   * @param c the parser implementation class.
   */
  public static void registerFallback(final Class<? extends IFileParser> c) {
    if(fallbackParser != null) {
      Main.debug("Replacing fallback parser with " + c.getName());
    }
    fallbackParser = c;
  }

  static {
    try {
      final Class<?>[] classes = Loader.load(IFileParser.class.getPackage(),
          IFileParser.class);
      for(final Class<?> c : classes) {
        final String name = c.getSimpleName();
        if(!REGISTRY.containsValue(c) && fallbackParser != c)
          Main.debug("Loading % ... FAILED", name);
      }
    } catch(final IOException ex) {
      Main.errln("Failed to load parsers (%)", ex.getMessage());
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
   * @param suffix the file suffix to get the parser for.
   * @return the parser implementation or <code>null</code> if no implementation
   *         is available.
   * @throws ParserException if the parser could not be loaded.
   */
  public IFileParser getParser(final String suffix) throws ParserException {
    IFileParser instance = parserInstances.get(suffix);
    if(instance == null) {
      final Class<? extends IFileParser> clazz = REGISTRY.get(suffix);
      if(clazz != null) {
        try {
          instance = clazz.newInstance();
        } catch(final Exception ex) {
          final StringBuilder sb = new StringBuilder();
          sb.append("Failed to load ");
          sb.append(clazz.getSimpleName());
          sb.append(" for suffix ");
          sb.append(suffix);
          sb.append("(");
          sb.append(ex.getMessage());
          sb.append(")");
          throw new ParserException(sb.toString());
        }
      }
      // put in hash map ... even if null
      parserInstances.put(suffix, instance);
    }
    return instance;
  }

  /**
   * Gets the fallback parser implementation.
   * @return the fallback parser implementation or <code>null</code> if no
   *         fallback parser is available.
   * @throws ParserException if the parser could not be loaded.
   */
  public IFileParser getFallbackParser() throws ParserException {
    if(fallbackParser == null) return null;
    if(fallbackParserInstance == null) {
      try {
        fallbackParserInstance = fallbackParser.newInstance();
        Main.debug("Successfully initialized fallback parser.");
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
}
