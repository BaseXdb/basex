package org.basex.query.util;

import java.text.*;

import org.basex.query.util.hash.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.options.*;

/**
 * Options for comparing values.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class DeepEqualOptions extends Options {
  /** Option: base-uri. */
  public static final BooleanOption BASE_URI =
      new BooleanOption("base-uri", false);
  /** Option: comments. */
  public static final BooleanOption COMMENTS =
      new BooleanOption("comments", false);
  /** Option: debug. */
  public static final BooleanOption DEBUG =
      new BooleanOption("debug", false);
  /** Option: false-on-error. */
  public static final BooleanOption FALSE_ON_ERROR =
      new BooleanOption("false-on-error", false);
  /** Option: id-property. */
  public static final BooleanOption ID_PROPERTY =
      new BooleanOption("id-property", false);
  /** Option: idrefs-property. */
  public static final BooleanOption IDREFS_PROPERTY =
      new BooleanOption("idrefs-property", false);
  /** Option: in-scope-namespaces. */
  public static final BooleanOption IN_SCOPE_NAMESPACES =
      new BooleanOption("in-scope-namespaces", false);
  /** Option: namespace-prefixes. */
  public static final BooleanOption NAMESPACE_PREFIXES =
      new BooleanOption("namespace-prefixes", false);
  /** Option: nilled-property. */
  public static final BooleanOption NILLED_PROPERTY =
      new BooleanOption("nilled-property", false);
  /** Option: normalization-form. */
  public static final EnumOption<Normalizer.Form> NORMALIZATION_FORM =
      new EnumOption<>("normalization-form", Normalizer.Form.class);
  /** Option: normalize-space. */
  public static final BooleanOption NORMALIZE_SPACE =
      new BooleanOption("normalize-space", false);
  /** Option: ordered. */
  public static final BooleanOption ORDERED =
      new BooleanOption("ordered", true);
  /** Option: processing-instructions. */
  public static final BooleanOption PROCESSING_INSTRUCTIONS =
      new BooleanOption("processing-instructions", false);
  /** Option: timezones. */
  public static final BooleanOption TIMEZONES =
      new BooleanOption("timezones", false);
  /** Option: type-annotations. */
  public static final BooleanOption TYPE_ANNOTATIONS =
      new BooleanOption("type-annotations", false);
  /** Option: type-variety. */
  public static final BooleanOption TYPE_VARIETY =
      new BooleanOption("type-variety", true);
  /** Option: typed-values. */
  public static final BooleanOption TYPED_VALUES =
      new BooleanOption("typed-values", true);
  /** Option: unordered-elements. */
  public static final ValueOption UNORDERED_ELEMENTS =
      new ValueOption("unordered-elements", SeqType.QNAME_ZM);
  /** Option: whitespace. */
  public static final EnumOption<Whitespace> WHITESPACE =
      new EnumOption<>("whitespace", Whitespace.PRESERVE);

  /** Whitespace mode. */
  public enum Whitespace {
    /** Preserve.  */ PRESERVE,
    /** Strip.     */ STRIP,
    /** Normalize. */ NORMALIZE;

    @Override
    public String toString() {
      return EnumOption.string(name());
    }
  }

  /** QNames. */
  private QNmSet qnames;

  /**
   * Checks if the specified QName is among the unordered element names.
   * @param qname QName
   * @return element names
   */
  public boolean unordered(final QNm qname) {
    if(qnames == null) {
      qnames = new QNmSet();
      for(final Item qnm : get(UNORDERED_ELEMENTS)) qnames.add((QNm) qnm);
    }
    return qnames.contains(qname);
  }
}
