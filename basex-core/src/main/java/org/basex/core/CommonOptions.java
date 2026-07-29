package org.basex.core;

import org.basex.util.*;

/**
 * Option names that cross class boundaries: a name is listed here if some component reads or
 * writes it via an {@link org.basex.util.options.Options} reference whose concrete class it does
 * not know. Names that merely occur in several option sets do not belong here; a shared constant
 * would prevent no error, as those options are only addressed via their typed instances.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public interface CommonOptions {
  // XML parsing: keys of MainOptions#XMLPARSINGMAP, probed by name in Docs#check

  /** Option name: {@link MainOptions#DTDVALIDATION}. */
  String DTD_VALIDATION = "dtd-validation";
  /** Option name: {@link MainOptions#STRIPWS}. */
  String STRIP_SPACE = "strip-space";
  /** Option name: {@link MainOptions#XINCLUDE}. */
  String XINCLUDE = "xinclude";
  /** Option name: {@link MainOptions#XSDVALIDATION}. */
  String XSD_VALIDATION = "xsd-validation";
  /** Option name: {@link MainOptions#XSILOCATION}. */
  String USE_XSI_SCHEMA_LOCATION = "use-xsi-schema-location";
  /** Option name: {@link MainOptions#STRIPNS}. */
  String STRIPNS = "stripns";
  /** Option name: {@link MainOptions#INTPARSE}. */
  String INTPARSE = "intparse";
  /** Option name: {@link MainOptions#DTD}. */
  String DTD = "dtd";
  /** Option name: {@link MainOptions#CATALOG}. */
  String CATALOG = "catalog";

  // Resource retrieval: resolved by name in ParseFn, StandardFunc#trusted, MainOptions

  /** Option name: base URI. */
  String BASE_URI = "base-uri";
  /** Option name: encoding. */
  String ENCODING = "encoding";
  /** Option name: newline normalization. */
  String NORMALIZE_NEWLINES = "normalize-newlines";
  /** Option name: fallback for invalid characters. */
  String FALLBACK = "fallback";
  /** Option name: trusted processing. No default: absence means "use FNXMLTRUSTED". */
  String TRUSTED = "trusted";

  // Option values

  /** Value of {@link #XSD_VALIDATION}: no validation. */
  String SKIP = "skip";
  /** Value of {@link #XSD_VALIDATION}: strict validation. */
  String STRICT = "strict";
  /** Value of {@link #XSD_VALIDATION}: lax validation (requires a schema-aware processor). */
  String LAX = "lax";

  /** Value of {@link #STRIP_SPACE}; translated to {@link MainOptions#STRIPWS}. */
  enum StripSpace {
    /** Strip whitespace-only text nodes.   */ ALL,
    /** Preserve whitespace-only text nodes. */ NONE,
    /** Equivalent to {@link #NONE}: no external control is available. */ CONDITIONAL;

    @Override
    public String toString() {
      return Enums.string(this);
    }
  }
}
