package org.basex.build.html;

import org.basex.util.options.*;

/**
 * Options for parsing and serializing HTML documents with Validator.nu.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class HtmlOptions extends Options {
  /** fn:parse-html option: encoding. */
  public static final StringOption ENCODING = new StringOption("encoding");
  /** fn:parse-html option: method. */
  public static final StringOption METHOD = new StringOption("method");
  /** fn:parse-html option: html-version. */
  public static final StringOption HTML_VERSION = new StringOption("html-version");
  /** fn:parse-html option: include-template-content. */
  public static final BooleanOption INCLUDE_TEMPLATE_CONTENT =
      new BooleanOption("include-template-content");

  /** Validator.nu option: unicode-normalization-checking. */
  public static final BooleanOption UNICODE_NORMALIZATION_CHECKING =
      new BooleanOption("unicode-normalization-checking", false);
  /** Validator.nu option: mapping-lang-to-xml-lang. */
  public static final BooleanOption MAPPING_LANG_TO_XML_LANG =
      new BooleanOption("mapping-lang-to-xml-lang", false);
  /** Validator.nu option: scripting-enabled. */
  public static final BooleanOption SCRIPTING_ENABLED =
      new BooleanOption("scripting-enabled", false);
  /** Validator.nu option: content-space-policy. */
  public static final EnumOption<XmlViolationPolicy> CONTENT_SPACE_POLICY =
      new EnumOption<>("content-space-policy", XmlViolationPolicy.class);
  /** Validator.nu option: content-non-xml-char-policy. */
  public static final EnumOption<XmlViolationPolicy> CONTENT_NON_XML_CHAR_POLICY =
      new EnumOption<>("content-non-xml-char-policy", XmlViolationPolicy.class);
  /** Validator.nu option: comment-policy. */
  public static final EnumOption<XmlViolationPolicy> COMMENT_POLICY =
      new EnumOption<>("comment-policy", XmlViolationPolicy.class);
  /** Validator.nu option: xmlns-policy. */
  public static final EnumOption<XmlViolationPolicy> XMLNS_POLICY =
      new EnumOption<>("xmlns-policy", XmlViolationPolicy.class);
  /** Validator.nu option: name-policy. */
  public static final EnumOption<XmlViolationPolicy> NAME_POLICY =
      new EnumOption<>("name-policy", XmlViolationPolicy.class);
  /** Validator.nu option: streamability-violation-policy. */
  public static final EnumOption<XmlViolationPolicy> STREAMABILITY_VIOLATION_POLICY =
      new EnumOption<>("streamability-violation-policy", XmlViolationPolicy.class);
  /** Validator.nu option: xml-policy. */
  public static final EnumOption<XmlViolationPolicy> XML_POLICY =
      new EnumOption<>("xml-policy", XmlViolationPolicy.class);
  /** Validator.nu option: heuristics. */
  public static final EnumOption<Heuristics> HEURISTICS =
      new EnumOption<>("heuristics", Heuristics.class);

  /**
   * Default constructor.
   */
  public HtmlOptions() {
  }

  /**
   * Constructor with options to be copied.
   * @param opts options
   */
  public HtmlOptions(final Options opts) {
    super(opts);
  }

  /**
   * Copied from nu.validator.htmlparser.common.XmlViolationPolicy in order to avoid the
   * class path dependency of HtmlOptions on Validator.nu.
   *
   * Copyright (c) 2007 Henri Sivonen
   *
   * Permission is hereby granted, free of charge, to any person obtaining a
   * copy of this software and associated documentation files (the "Software"),
   * to deal in the Software without restriction, including without limitation
   * the rights to use, copy, modify, merge, publish, distribute, sublicense,
   * and/or sell copies of the Software, and to permit persons to whom the
   * Software is furnished to do so, subject to the following conditions:
   *
   * The above copyright notice and this permission notice shall be included in
   * all copies or substantial portions of the Software.
   *
   * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
   * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
   * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
   * DEALINGS IN THE SOFTWARE.
   */

  /**
   * Policy for XML 1.0 violations.
   *
   * @version $Id$
   * @author hsivonen
   */
  public enum XmlViolationPolicy {
      /**
       * Conform to HTML 5, allow XML 1.0 to be violated.
       */
      ALLOW,

      /**
       * Halt when something cannot be mapped to XML 1.0.
       */
      FATAL,

      /**
       * Be non-conforming and alter the infoset to fit
       * XML 1.0 when something would otherwise not be
       * mappable to XML 1.0.
       */
      ALTER_INFOSET
  }

  /**
   * Copied from nu.validator.htmlparser.common.XmlViolationPolicy in order to avoid the
   * class path dependency of HtmlOptions on Validator.nu.
   *
   * Permission is hereby granted, free of charge, to any person obtaining a
   * copy of this software and associated documentation files (the "Software"),
   * to deal in the Software without restriction, including without limitation
   * the rights to use, copy, modify, merge, publish, distribute, sublicense,
   * and/or sell copies of the Software, and to permit persons to whom the
   * Software is furnished to do so, subject to the following conditions:
   *
   * The above copyright notice and this permission notice shall be included in
   * all copies or substantial portions of the Software.
   *
   * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
   * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
   * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
   * DEALINGS IN THE SOFTWARE.
   */

  /**
   * Indicates a request for character encoding sniffer choice.
   *
   * @version $Id$
   * @author hsivonen
   */
  public enum Heuristics {

      /**
       * Perform no heuristic sniffing.
       */
      NONE,

      /**
       * Use both jchardet and ICU4J.
       */
      ALL,

      /**
       * Use jchardet only.
       */
      CHARDET,

      /**
       * Use ICU4J only.
       */
      ICU
  }

}
