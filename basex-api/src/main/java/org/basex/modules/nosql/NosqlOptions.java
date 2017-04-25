package org.basex.modules.nosql;

import java.util.Locale;

import org.basex.build.JsonOptions;
import org.basex.util.options.EnumOption;
import org.basex.util.options.NumberOption;
import org.basex.util.options.StringOption;
/**
 * Options for MongoDB.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Prakash Thapa
 */
public class NosqlOptions extends JsonOptions {
    /** url (String). */
    public static final StringOption URL = new StringOption("url");
    /** host (String). */
    public static final StringOption HOST = new StringOption("host");
    /** port (Integer). */
    public static final NumberOption PORT = new NumberOption("port");
    /** username (String). */
    public static final StringOption USERNAME = new StringOption("user");
    /** password (String). */
    public static final StringOption PASSWORD = new StringOption("password");
    /** database (String). */
    public static final StringOption DATABASE = new StringOption("database");
    /** bucket (String). */
    public static final StringOption BUCKET = new StringOption("bucket");
    /** type (String). */
    public static final EnumOption<NosqlFormat> TYPE =
            new EnumOption<>("type", NosqlFormat.XML);
    /** return result type */
    public enum NosqlFormat {
      /** json. */ JSON,
      /** xml. */ XML;

      @Override
      public String toString() {
        return super.toString().toLowerCase(Locale.ENGLISH);
      }
    }
}
