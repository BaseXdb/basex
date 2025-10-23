package org.basex.core;

import org.basex.util.options.*;

/**
 * Common option definitions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public interface CommonOptions {
  // XML parsing options

  /** XSD validation option value. */
  String SKIP = "skip";
  /** XSD validation option value. */
  String STRICT = "strict";

  /** Internal option: {@link MainOptions#DTDVALIDATION}. */
  BooleanOption DTD_VALIDATION = new BooleanOption("dtd-validation", false);
  /** Internal option: {@link MainOptions#STRIPNS}. */
  BooleanOption STRIP_SPACE = new BooleanOption("strip-space", false);
  /** Internal option: {@link MainOptions#XINCLUDE}. */
  BooleanOption XINCLUDE = new BooleanOption("xinclude", false);
  /** Internal option: {@link MainOptions#XSDVALIDATION}. */
  StringOption XSD_VALIDATION = new StringOption("xsd-validation", SKIP);
  /** Internal option: {@link MainOptions#XSILOCATION}. */
  BooleanOption XSI_SCHEMA_LOCATION = new BooleanOption("xsi-schema-location", false);

  /** Internal option: {@link MainOptions#STRIPNS}. */
  BooleanOption STRIPNS = new BooleanOption("stripns", false);
  /** Internal option: {@link MainOptions#INTPARSE}. */
  BooleanOption INTPARSE = new BooleanOption("intparse", false);
  /** Internal option: {@link MainOptions#DTD}. */
  BooleanOption DTD = new BooleanOption("dtd", true);
  /** Internal option: {@link MainOptions#CATALOG}. */
  StringOption CATALOG = new StringOption("catalog", "");

  /** Base URI. */
  StringOption BASE_URI = new StringOption("base-uri");
  /** Encoding. */
  StringOption ENCODING = new StringOption("encoding");
  /** Normalize newlines. */
  BooleanOption NORMALIZE_NEWLINES = new BooleanOption("normalize-newlines");
}
