package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.options.*;

import de.bottlecaps.markup.*;
import de.bottlecaps.markup.Blitz.Option;
import de.bottlecaps.markup.blitz.ResultHandler;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class FnInvisibleXml extends StandardFunc {
  /** Required class names. */
  private static final String[] CLASSES = { "de.bottlecaps.markup.Blitz",
      "de.bottlecaps.markup.BlitzException", "de.bottlecaps.markup.BlitzParseException",
      "de.bottlecaps.markup.blitz.ResultHandler"};
  /** The function's argument type. */
  public static final SeqType ARG_TYPE = ChoiceItemType.get(BasicType.STRING,
      NodeType.get(NameTest.get(new QNm("ixml")))).seqType(Occ.ZERO_OR_ONE);
  /** The invisible XML parser generator (can be {@code null}). */
  private Generator generator;

  @Override
  public FuncItem value(final QueryContext qc) throws QueryException {
    if(generator == null) {
      for(final String className : CLASSES) {
        try {
          Reflect.forName(className);
        } catch(final Throwable th) {
          throw BASEX_CLASSPATH_X_X.get(info, definition.name, className).cause(th);
        }
      }
      generator = new Generator();
    }
    return generator.generate(qc);
  }

  /**
   * Checks if Invisible XML support is available.
   * @return result of check
   */
  public static boolean available() {
    for(final String className : CLASSES) {
      if(!Reflect.available(className)) return false;
    }
    return true;
  }

  /**
   * Invisible XML parser generator.
   */
  private final class Generator {
    /** Empty Markup Blitz options. */
    private static final Option[] NO_OPTS = new Blitz.Option[0];
    /** Markup Blitz options for fail-on-error. */
    private static final Option[] FAIL_OPTS = new Blitz.Option[] { Blitz.Option.FAIL_ON_ERROR };
    /** Maximum number of entries. */
    private static final int MAX = 15;
    /** Generated parser cache. */
    private static final LinkedHashMap<String, de.bottlecaps.markup.blitz.Parser> PARSERS =
        new LinkedHashMap<>(MAX + 1, 1.0f, true) {
          @Override
          protected boolean removeEldestEntry(
              final Map.Entry<String, de.bottlecaps.markup.blitz.Parser> eldest) {
            return size() > MAX;
          }
        };

    /**
     * Generate a parser from an invisible XML grammar.
     * @param qc query context
     * @return the parsing function
     * @throws QueryException query exception
     */
    public FuncItem generate(final QueryContext qc) throws QueryException {
      final Item grammar = (Item) ARG_TYPE.coerce(arg(0).value(qc), qc, info);
      final IxmlOptions options = toOptions(arg(1), new IxmlOptions(), qc);

      final boolean xml = grammar instanceof XNode;
      final String grmmr;
      try {
        grmmr = grammar.isEmpty() ? Blitz.ixmlGrammar() : xml ?
          grammar.serialize().toString() : Token.string(grammar.string(info));
      } catch(final QueryIOException ex) {
        throw ex.getCause();
      }

      final de.bottlecaps.markup.blitz.Parser parser;
      final Blitz.Option[] opts = options.get(IxmlOptions.FAIL_ON_ERROR) ? FAIL_OPTS : NO_OPTS;
      try {
        parser = parser(grmmr, xml, opts);
      } catch(final BlitzParseException ex) {
        throw IXML_GRM_X_X_X.get(info, ex.getOffendingToken(), ex.getLine(), ex.getColumn());
      } catch(final BlitzException ex) {
        throw IXML_GEN_X.get(info, ex);
      }
      final Var var = new VarScope().addNew(new QNm("input"), Types.STRING_O, qc, info);
      final Var[] params = { var };
      final Expr arg = new VarRef(info, var);
      final ParseInvisibleXml parseFunction = new ParseInvisibleXml(info, parser, arg);
      final FuncType ft = FuncType.get(parseFunction.seqType(), Types.STRING_O);
      return new FuncItem(info, parseFunction, params, AnnList.EMPTY, ft, params.length, null);
    }

    /**
     * Returns a cached parser.
     * @param grammar grammar string
     * @param xml XML grammar flag
     * @param opts generation options
     * @return parser
     */
    private de.bottlecaps.markup.blitz.Parser parser(final String grammar, final boolean xml,
        final Blitz.Option[] opts) {
      final String key = grammar + "\0" + xml + "\0" + Arrays.toString(opts);
      de.bottlecaps.markup.blitz.Parser parser;
      synchronized(PARSERS) {
        parser = PARSERS.get(key);
      }
      if(parser == null) {
        parser = xml ? Blitz.generateFromXml(grammar, opts) : Blitz.generate(grammar, opts);
        synchronized(PARSERS) {
          final de.bottlecaps.markup.blitz.Parser cached = PARSERS.get(key);
          if(cached != null) {
            parser = cached;
          } else {
            PARSERS.put(key, parser);
          }
        }
      }
      return parser;
    }
  }

  /**
   * Result function of fn:invisible-xml: parse invisible XML input.
   */
  private static final class ParseInvisibleXml extends Arr {
    /** Shared {@link MainOptions} instance to avoid expensive per-parse initialization. */
    private static final MainOptions OPTIONS = new MainOptions().seal();
    /** Generated invisible XML parser. */
    private final de.bottlecaps.markup.blitz.Parser parser;

    /**
     * Constructor.
     * @param info input info (can be {@code null})
     * @param args function arguments
     * @param parser generated invisible XML parser
     */
    private ParseInvisibleXml(final InputInfo info, final de.bottlecaps.markup.blitz.Parser parser,
        final Expr... args) {
      super(info, Types.DOCUMENT_O, args);
      this.parser = parser;
    }

    @Override
    public DBNode value(final QueryContext qc) throws QueryException {
      final String input = toString(arg(0), qc);
      try {
        final MemBuilder builder = new MemBuilder(Parser.emptyParser(OPTIONS)).init();
        builder.openDoc(Token.EMPTY);
        parser.parse(input, new DBResultHandler(builder));
        builder.closeDoc();
        return new DBNode(builder.finish());
      } catch(final BlitzParseException ex) {
        throw IXML_INP_X_X_X.get(info, ex.getOffendingToken(), ex.getLine(), ex.getColumn());
      } catch(final BlitzException | IOException ex) {
        throw IXML_RESULT_X.get(info, ex);
      } catch(final UncheckedIOException ex) {
        throw IXML_RESULT_X.get(info, ex.getCause());
      }
    }

    @Override
    public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
      return copyType(new ParseInvisibleXml(info, parser, copyAll(cc, vm, args())));
    }

    @Override
    public void toString(final QueryString qs) {
      qs.token("parse-invisible-xml").params(exprs);
    }
  }

  /**
   * Result handler that builds an in-memory database directly from Markup Blitz result events, so
   * that the document is created without serializing the parse tree to a string and re-parsing it.
   */
  private static final class DBResultHandler implements ResultHandler {
    /** Database builder. */
    private final MemBuilder builder;
    /** Reusable buffer for turning a codepoint run into UTF-8 bytes. */
    private final TokenBuilder token = new TokenBuilder();
    /** Name of a started but not yet opened element (its attributes are still pending). */
    private byte[] element;
    /** Attributes of the pending element. */
    private Atts atts = new Atts();
    /** Namespaces of the pending element. */
    private Atts nsp = new Atts();

    /**
     * Constructor.
     * @param builder database builder
     */
    private DBResultHandler(final MemBuilder builder) {
      this.builder = builder;
    }

    @Override
    public void startElement(final String name) {
      open();
      element = Token.token(name);
    }

    @Override
    public void endElement(final String name) {
      open();
      try {
        builder.closeElem();
      } catch(final IOException ex) {
        throw new UncheckedIOException(ex);
      }
    }

    @Override
    public void attribute(final String name, final int[] codepoints, final int length) {
      final byte[] val = bytes(codepoints, length);
      if(name.equals("xmlns")) {
        nsp.add(Token.EMPTY, val);
      } else if(name.startsWith("xmlns:")) {
        nsp.add(Token.token(name.substring(6)), val);
      } else {
        atts.add(Token.token(name), val);
      }
    }

    @Override
    public void text(final int[] codepoints, final int length) {
      open();
      if(length == 0) return;
      try {
        builder.text(bytes(codepoints, length));
      } catch(final IOException ex) {
        throw new UncheckedIOException(ex);
      }
    }

    /** Opens the pending element with its collected attributes and namespaces. */
    private void open() {
      if(element == null) return;
      try {
        builder.openElem(element, atts, nsp);
      } catch(final IOException ex) {
        throw new UncheckedIOException(ex);
      }
      element = null;
      atts = new Atts();
      nsp = new Atts();
    }

    /**
     * Turns a run of codepoints into UTF-8 bytes.
     * @param codepoints codepoints holding the value
     * @param length number of codepoints
     * @return byte array
     */
    private byte[] bytes(final int[] codepoints, final int length) {
      token.reset();
      for(int i = 0; i < length; i++) token.add(codepoints[i]);
      return token.toArray();
    }
  }

  /**
   * Options for fn:invisible-xml.
   */
  public static final class IxmlOptions extends Options {
    /** Invisible XML option fail-on-error. */
    public static final BooleanOption FAIL_ON_ERROR = new BooleanOption("fail-on-error", false);
  }
}
