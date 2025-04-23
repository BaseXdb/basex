package org.basex.query;

import org.basex.core.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.options.*;

/**
 * Options for fn:doc and fn:doc-available.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public class DocOptions extends Options {
  /** DTD validation. */
  public static final BooleanOption DTD_VALIDATION = new BooleanOption("dtd-validation", false);
  /** Whether external entities are permitted or rejected. */
  public static final BooleanOption ALLOW_EXTERNAL_ENTITIES = new BooleanOption(
      "allow-external-entities", true);
  /** Limit on the maximum number of entity references that may be expanded. */
  public static final ValueOption ENTITY_EXPANSION_LIMIT = new ValueOption("entity-expansion-limit",
      SeqType.INTEGER_ZO, Empty.VALUE);
  /** Whether two calls with same URI and options return the same node. */
  public static final BooleanOption STABLE = new BooleanOption("stable", true);
  /** Remove whitespace-only text nodes. */
  public static final BooleanOption STRIP_SPACE = new BooleanOption("strip-space", false);
  /** Flag for using XInclude. */
  public static final BooleanOption XINCLUDE = new BooleanOption("xinclude", false);
  /** XSD validation. */
  public static final StringOption XSD_VALIDATION = new StringOption("xsd-validation",
      MainOptions.SKIP);
  /** Flag for using XInclude. */
  public static final BooleanOption XSI_SCHEMA_LOCATION = new BooleanOption("xsi-schema-location",
      false);

  /** Custom option (see {@link MainOptions#DTD}). */
  public static final BooleanOption DTD = new BooleanOption("dtd", true);
  /** Custom option (see {@link MainOptions#STRIPNS}). */
  public static final BooleanOption STRIPNS = new BooleanOption("stripns", false);
  /** Custom option (see {@link MainOptions#INTPARSE}). */
  public static final BooleanOption INTPARSE = new BooleanOption("intparse", false);

  /** Default options. */
  public static final DocOptions DEFAULT_DOC_OPTIONS = new DocOptions();
  /** Default options. */
  private static final MainOptions DEFAULT_MAIN_OPTIONS = new DocOptions().toMainOptions(null);

  /**
   * Convert this DocOptions instance to a MainOptions instance.
   * @param mainOpts static main options
   * @return main options
   */
  public MainOptions toMainOptions(final MainOptions mainOpts) {
    if(this == DEFAULT_DOC_OPTIONS && mainOpts.get(MainOptions.CATALOG).isEmpty()) {
      return DEFAULT_MAIN_OPTIONS;
    }
    final MainOptions opts = new MainOptions();
    opts.set(MainOptions.DTDVALIDATION, get(DocOptions.DTD_VALIDATION));
    opts.set(MainOptions.ALLOWEXTERNALENTITIES, get(DocOptions.ALLOW_EXTERNAL_ENTITIES));
    opts.set(MainOptions.ENTITYEXPANSIONLIMIT, get(DocOptions.ENTITY_EXPANSION_LIMIT));
    opts.set(MainOptions.STRIPWS, get(DocOptions.STRIP_SPACE));
    opts.set(MainOptions.XINCLUDE, get(DocOptions.XINCLUDE));
    opts.set(MainOptions.XSDVALIDATION, get(DocOptions.XSD_VALIDATION));
    opts.set(MainOptions.XSISCHEMALOCATION, get(DocOptions.XSI_SCHEMA_LOCATION));
    opts.set(MainOptions.DTD, get(DocOptions.DTD));
    opts.set(MainOptions.STRIPNS, get(DocOptions.STRIPNS));
    opts.set(MainOptions.INTPARSE, get(DocOptions.INTPARSE));
    if(mainOpts != null) {
      opts.set(MainOptions.CATALOG, mainOpts.get(MainOptions.CATALOG));
    }
    return opts;
  }
}