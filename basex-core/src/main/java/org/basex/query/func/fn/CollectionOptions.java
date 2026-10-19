package org.basex.query.func.fn;

import static org.basex.query.value.type.Types.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.util.regex.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.options.*;

/**
 * Options for fn:collection and fn:uri-collection.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CollectionOptions extends Options {
  /** Form: database. */
  public static final String DATABASE = "database";
  /** Form: directory. */
  public static final String DIRECTORY = "directory";
  /** Form: archive. */
  public static final String ARCHIVE = "archive";

  /** Whether two calls with same URI and options return the same nodes. */
  public static final BooleanOption STABLE = new BooleanOption("stable", true);
  /** Kind of the resource identified by the collection URI. */
  public static final StringOption FORM = new StringOption("form");
  /** Include resources of sub-collections. */
  public static final BooleanOption RECURSIVE = new BooleanOption("recursive", true);
  /** Skip resources that cannot be processed. */
  public static final BooleanOption IGNORE_ERRORS = new BooleanOption("ignore-errors", false);
  /** Regular expression for filtering resource URIs. */
  public static final StringOption MATCH = new StringOption("match");
  /** Flags for the regular expression. */
  public static final StringOption MATCH_FLAGS = new StringOption("match-flags", "");
  /** Mapping from file extensions to media types. */
  public static final ValueOption FILE_EXTENSIONS = new ValueOption("file-extensions",
      MapType.get(BasicType.STRING, STRING_O).seqType());
  /** Options for parsing XML resources. */
  public static final OptionsOption<DocOptions> XML_OPTIONS =
      new OptionsOption<>("xml-options", DocOptions.class);
  /** Options for parsing JSON resources. */
  public static final ValueOption JSON_OPTIONS = new ValueOption("json-options", MAP_O);
  /** Options for parsing text resources. */
  public static final ValueOption TEXT_OPTIONS = new ValueOption("text-options", MAP_O);
  /** Options for parsing HTML resources. */
  public static final ValueOption HTML_OPTIONS = new ValueOption("html-options", MAP_O);
  /** Options for parsing CSV resources. */
  public static final ValueOption CSV_OPTIONS = new ValueOption("csv-options", MAP_O);

  /**
   * Returns the regular expression for filtering resource URIs.
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return regular expression or {@code null}
   * @throws QueryException query exception
   */
  public RegExpr regExpr(final QueryContext qc, final InputInfo info) throws QueryException {
    final String match = get(MATCH);
    return match == null ? null :
      RegExFn.regExpr(Token.token(match), Token.token(get(MATCH_FLAGS)), qc, info);
  }

  /**
   * Returns the media types assigned to file extensions.
   * @param info input info (can be {@code null})
   * @return media types, indexed by file extensions
   * @throws QueryException query exception
   */
  public Map<String, MediaType> mediaTypes(final InputInfo info) throws QueryException {
    if(!(get(FILE_EXTENSIONS) instanceof final XQMap map)) return Map.of();
    final Map<String, MediaType> types = new HashMap<>();
    map.forEach((key, value) -> types.put(Token.string(key.string(info)),
        new MediaType(Token.string(((Item) value).string(info)))));
    return types;
  }

  /**
   * Returns the options for parsing XML resources, including the stability flag.
   * @return options
   */
  public DocOptions xmlOptions() {
    final DocOptions xml = get(XML_OPTIONS);
    final boolean stable = get(STABLE);
    // default options: share the global instance
    if(xml == null && stable) return QueryResources.DOC_OPTIONS;
    final DocOptions options = xml != null ? xml : new DocOptions();
    options.set(DocOptions.STABLE, stable);
    return options;
  }
}
