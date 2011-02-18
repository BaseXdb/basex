package org.basex.util.ft;

import static java.util.EnumSet.*;
import java.util.EnumSet;

/**
 * This class contains language tokens which are valid for the xml:lang
 * attribute.
 * <p>
 * As specified by W3C the values of the attribute are language identifiers as
 * defined by IETF BCP 47, Tags for the Identification of Languages.
 *
 * @see <a href="http://www.w3.org/TR/REC-xml/#sec-lang-tag"
 *      >http://www.w3.org/TR/REC-xml/#sec-lang-tag</a>
 * @see <a href="http://tools.ietf.org/html/bcp47"
 *      >http://tools.ietf.org/html/bcp47</a>
 * @see <a href="http://www.iana.org/assignments/language-subtag-registry"
 *      >http://www.iana.org/assignments/language-subtag-registry</a>
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Dimitar Popov
 * @author Jens Erat
 */
public enum Language {
  /** English. */     EN("English"),
  /** Abkhaz. */      AB("Abkhaz"),
  /** Afar. */        AA("Afar"),
  /** Afrikaans. */   AF("Afrikaans"),
  /** Akan. */        AK("Akan"),
  /** Albanian. */    SQ("Albanian"),
  /** Amharic. */     AM("Amharic"),
  /** Arabic. */      AR("Arabic"),
  /** Armenian. */    HY("Armenian"),
  /** Assamese. */    AS("Assamese"),
  /** Avaric. */      AV("Avaric"),
  /** Avestan. */     AE("Avestan"),
  /** Aymara. */      AY("Aymara"),
  /** Azerbaijani. */ AZ("Azerbaijani"),
  /** Bambara. */     BM("Bambara"),
  /** Bashkir. */     BA("Bashkir"),
  /** Basque. */      EU("Basque"),
  /** Belarusian. */  BE("Belarusian"),
  /** Bengali. */     BN("Bengali"),
  /** Bihari. */      BH("Bihari"),
  /** Bislama. */     BI("Bislama"),
  /** Bosnian. */     BS("Bosnian"),
  /** Breton. */      BR("Breton"),
  /** Bulgarian. */   BG("Bulgarian"),
  /** Burmese. */     MY("Burmese"),
  /** Catalan. */     CA("Catalan"),
  /** Chamorro. */    CH("Chamorro"),
  /** Chechen. */     CE("Chechen"),
  /** Chichewa */     NY("Chichewa"),
  /** Chinese. */     ZH("Chinese", false),
  /** Chuvash. */     CV("Chuvash"),
  /** Cornish. */     KW("Cornish"),
  /** Corsican. */    CO("Corsican"),
  /** Cree. */        CR("Cree"),
  /** Croatian. */    HR("Croatian"),
  /** Czech. */       CS("Czech"),
  /** Danish. */      DA("Danish"),
  /** Dihevi. */      DV("Divehi"),
  /** Dutch. */       NL("Dutch"),
  /** Dzongkha. */    DZ("Dzongkha"),
  /** Esperanto. */   EO("Esperanto"),
  /** Estonian. */    ET("Estonian"),
  /** Ewe. */         EE("Ewe"),
  /** Faroese. */     FO("Faroese"),
  /** Fijian. */      FJ("Fijian"),
  /** Finnish. */     FI("Finnish"),
  /** French. */      FR("French"),
  /** Fula. */        FF("Fula"),
  /** Scottish. */    GD("Gaelic"),
  /** Galician. */    GL("Galician"),
  /** Georgian. */    KA("Georgian"),
  /** German. */      DE("German"),
  /** Greek. */       EL("Greek"),
  /** Guarani. */     GN("Guaran\0u0ed"),
  /** Gujarati. */    GU("Gujarati"),
  /** Haitian. */     HT("Haitian"),
  /** Hausa. */       HA("Hausa"),
  /** Hebrew. */      HE("Hebrew"),
  /** Herero. */      HZ("Herero"),
  /** Hindi. */       HI("Hindi"),
  /** Hiri Motu. */   HO("Hiri Motu"),
  /** Hungarian. */   HU("Hungarian"),
  /** Icelandic. */   IS("Icelandic"),
  /** Ido. */         IO("Ido"),
  /** Igbo. */        IG("Igbo"),
  /** Indonesian. */  ID("Indonesian"),
  /** Interlingua. */ IA("Interlingua"),
  /** Interlingue. */ IE("Interlingue"),
  /** Inuktitut. */   IU("Inuktitut"),
  /** Inupiaq. */     IK("Inupiaq"),
  /** Irish. */       GA("Irish"),
  /** Italian. */     IT("Italian"),
  /** Japanese. */    JA("Japanese", false),
  /** Javanese. */    JV("Javanese"),
  /** Kalaallisut. */ KL("Kalaallisut"),
  /** Kannada. */     KN("Kannada"),
  /** Kanuri. */      KR("Kanuri"),
  /** Kashmiri. */    KS("Kashmiri"),
  /** Kazakh. */      KK("Kazakh"),
  /** Khmer. */       KM("Khmer"),
  /** Kikuyu. */      KI("Kikuyu"),
  /** Kinyarwanda. */ RW("Kinyarwanda"),
  /** Kirghiz. */     KY("Kirghiz"),
  /** Kirundi. */     RN("Kirundi"),
  /** Komi. */        KV("Komi"),
  /** Kongo. */       KG("Kongo"),
  /** Korean. */      KO("Korean", false),
  /** Kurdish. */     KU("Kurdish"),
  /** Kwanyama. */    KJ("Kwanyama"),
  /** Lao. */         LO("Lao"),
  /** Latin. */       LA("Latin"),
  /** Latvian. */     LV("Latvian"),
  /** Limburgish. */  LI("Limburgish"),
  /** Lingala. */     LN("Lingala"),
  /** Lithuanian. */  LT("Lithuanian"),
  /** Luba Kat. */    LU("Luba Katanga"),
  /** Luxembourg. */  LB("Luxembourgish"),
  /** Macedonian. */  MK("Macedonian"),
  /** Malagasy. */    MG("Malagasy"),
  /** Malay. */       MS("Malay"),
  /** Malayalam. */   ML("Malayalam"),
  /** Maltese. */     MT("Maltese"),
  /** Manx. */        GV("Manx"),
  /** Maori. */       MI("Maori"),
  /** Marathi. */     MR("Marathi"),
  /** Marshallese. */ MH("Marshallese"),
  /** Mongolian. */   MN("Mongolian"),
  /** Nauru. */       NA("Nauru"),
  /** Navajo. */      NV("Navajo"),
  /** Ndonga. */      NG("Ndonga"),
  /** Nepali. */      NE("Nepali"),
  /** N. Ndebele. */  ND("North Ndebele"),
  /** N. Sami. */     SE("Northern Sami"),
  /** Norwegian. */   NO("Norwegian"),
  /** Norweg. B. */   NB("Norwegian Bokm\u00e5l"),
  /** Norweg. N. */   NN("Norwegian Nynorsk"),
  /** Nuosu. */       II("Nuosu"),
  /** Occitan. */     OC("Occitan"),
  /** Ojibwe. */      OJ("Ojibwe"),
  /** Old Church. */  CU("Old Church Slavonic"),
  /** Oriya. */       OR("Oriya"),
  /** Oromo. */       OM("Oromo"),
  /** Ossetian. */    OS("Ossetian"),
  /** Pali. */        PI("Pali"),
  /** Panjabi. */     PA("Panjabi"),
  /** Pashto. */      PS("Pashto"),
  /** Persian. */     FA("Persian"),
  /** Polish. */      PL("Polish"),
  /** Portuguese. */  PT("Portuguese"),
  /** Quechua. */     QU("Quechua"),
  /** Romanian. */    RO("Romanian"),
  /** Romansh. */     RM("Romansh"),
  /** Russian. */     RU("Russian"),
  /** Sami. */        SM("Samoan"),
  /** Sango. */       SG("Sango"),
  /** Sanskrit. */    SA("Sanskrit"),
  /** Sardinian. */   SC("Sardinian"),
  /** Serbian. */     SR("Serbian"),
  /** Shona. */       SN("Shona"),
  /** Sindhi. */      SD("Sindhi"),
  /** Sinhala. */     SI("Sinhala"),
  /** Slovak. */      SK("Slovak"),
  /** Slovenian. */   SL("Slovene"),
  /** Spanish. */     ES("Spanish"),
  /** Somali. */      SO("Somali"),
  /** S. Ndebele. */  NR("South Ndebele"),
  /** S. Sotho. */    ST("Southern Sotho"),
  /** Sundanese. */   SU("Sundanese"),
  /** Swahili. */     SW("Swahili"),
  /** Swati. */       SS("Swati"),
  /** Swedish. */     SV("Swedish"),
  /** Syriac. */      SY("Syriac"),
  /** Tagalog. */     TL("Tagalog"),
  /** Tahitian. */    TY("Tahitian"),
  /** Tajik. */       TG("Tajik"),
  /** Tamil. */       TA("Tamil"),
  /** Tatar. */       TT("Tatar"),
  /** Telugu. */      TE("Telugu"),
  /** Thai. */        TH("Thai", false),
  /** Tibetan. */     BO("Tibetan"),
  /** Tigrinya. */    TI("Tigrinya"),
  /** Tonga. */       TO("Tonga"),
  /** Tsonga. */      TS("Tsonga"),
  /** Tswana. */      TN("Tswana"),
  /** Turkish. */     TR("Turkish"),
  /** Turkmen. */     TK("Turkmen"),
  /** Twi. */         TW("Twi"),
  /** Uighur. */      UG("Uighur"),
  /** Ukrainian. */   UK("Ukrainian"),
  /** Urdu. */        UR("Urdu"),
  /** Uzbek. */       UZ("Uzbek"),
  /** Venda. */       VE("Venda"),
  /** Vietnamese. */  VI("Vietnamese"),
  /** Volapuk. */     VO("Volap\u00fck"),
  /** Walloon. */     WA("Walloon"),
  /** Western Fr. */  FY("Western Frisian"),
  /** Wolof. */       WO("Wolof"),
  /** Welsh. */       CY("Welsh"),
  /** Xhosa. */       XH("Xhosa"),
  /** Yiddish. */     YI("Yiddish"),
  /** Yoruba. */      YO("Yoruba"),
  /** Zhuang. */      ZA("Zhuang"),
  /** Zulu. */        ZU("Zulu");

  /** Default language. */
  public static final Language DEFAULT = EN;
  /** Subset with western languages. */
  public static final EnumSet<Language> WESTERN = noneOf(Language.class);

  static {
    for(final Language lt : Language.values()) if(lt.ws) WESTERN.add(lt);
  }

  /** Whether language uses white-spaces (e. g., Chinese does not). */
  private final boolean ws;
  /** Full name. */
  private final String full;

  /**
   * Constructor.
   * @param n name of language
   */
  private Language(final String n) {
    this(n, true);
  }

  /**
   * Constructor.
   * @param f full name of language
   * @param w is language whitespace-tokenizable?
   */
  private Language(final String f, final boolean w) {
    full = f;
    ws = w;
  }

  /**
   * Returns the enumeration value of the specified language, or {@code null}.
   * @param lang name or code of language
   * @return language code
   */
  public static Language get(final String lang) {
    final String ln = lang.replaceAll("-.*", "");
    for(final Language lt : values()) {
      if(ln.equalsIgnoreCase(lt.full) ||
         ln.equalsIgnoreCase(lt.name())) return lt;
    }
    return null;
  }

  /**
   * Checks if the specified language is supported by the available tokenizers
   * and stemmers.
   * @param lang language to check
   * @param stem stemming flag
   * @return result of check
   */
  public static boolean supported(final Language lang, final boolean stem) {
    // Use default language if no language was specified
    final Language ln = lang != null ? lang : Language.DEFAULT;
    // Check tokenizers
    boolean supp = false;
    for(final Tokenizer t : Tokenizer.IMPL) supp |= t.supports(ln);
    if(!supp || !stem) return supp;
    // Check stemmers (if applied)
    for(final Stemmer s : Stemmer.IMPL) if(s.supports(ln)) return true;
    return false;
  }

  @Override
  public String toString() {
    return full;
  }
}
