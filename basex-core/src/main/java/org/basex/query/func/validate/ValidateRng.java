package org.basex.query.func.validate;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.xml.sax.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class ValidateRng extends ValidateFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return check(qc);
  }

  @Override
  public final ArrayList<ErrorInfo> errors(final QueryContext qc) throws QueryException {
    return validate(new Validation() {
      @Override
      void validate() throws IOException, QueryException {
        final IO input = read(toNodeOrAtomItem(arg(0), false, qc), null);
        final Item schema = toNodeOrAtomItem(arg(1), false, qc);
        final boolean compact = toBooleanOrFalse(arg(2), qc);

        // detect format of schema input
        IO schm;
        try {
          schm = read(schema, null);
        } catch(final QueryException ex) {
          // compact schema: treat string as input
          if(!compact || ex.error() != WHICHRES_X) throw ex;
          schm = new IOContent(schema.string(info));
        }
        schm = prepare(schm);

        try {
          /*
          PropertyMapBuilder pmb = new PropertyMapBuilder();
          pmb.put(ValidateProperty.ERROR_HANDLER, handler);
          pmb.put(RngProperty.CHECK_ID_IDREF, Flag.PRESENT);

          SchemaReader sr = compact ? CompactSchemaReader.getInstance() : null;
          ValidationDriver vd = new ValidationDriver(pmb.toPropertyMap(), sr);

          if(vd.loadSchema(schema.inputSource())) vd.validate(in.inputSource());
          */
          final Class<?>
            piClass  = Class.forName("com.thaiopensource.util.PropertyId"),
            pmClass  = Class.forName("com.thaiopensource.util.PropertyMap"),
            pmbClass = Class.forName("com.thaiopensource.util.PropertyMapBuilder"),
            flClass  = Class.forName("com.thaiopensource.validate.Flag"),
            srClass  = Class.forName("com.thaiopensource.validate.SchemaReader"),
            vpClass  = Class.forName("com.thaiopensource.validate.ValidateProperty"),
            vdClass  = Class.forName("com.thaiopensource.validate.ValidationDriver"),
            rpClass  = Class.forName("com.thaiopensource.validate.prop.rng.RngProperty"),
            csrClass = Class.forName("com.thaiopensource.validate.rng.CompactSchemaReader");

          final Method
            pmbPut = pmbClass.getMethod("put", piClass, Object.class),
            vdLoadSchema = vdClass.getMethod("loadSchema", InputSource.class),
            vdValidate = vdClass.getMethod("validate", InputSource.class);

          // assign error handler
          final Object pmb = pmbClass.getConstructor().newInstance();
          pmbPut.invoke(pmb, vpClass.getField("ERROR_HANDLER").get(null), this);

          // enable ID/IDREF checks
          final Object present = flClass.getField("PRESENT").get(null);
          pmbPut.invoke(pmb, rpClass.getField("CHECK_ID_IDREF").get(null), present);

          // create driver
          final Object sr = compact ? csrClass.getMethod("getInstance").invoke(null) : null;
          final Object pm = pmbClass.getMethod("toPropertyMap").invoke(pmb);
          final Object vd = vdClass.getConstructor(pmClass, srClass).newInstance(pm, sr);

          // load schema, validate document
          final Object loaded = vdLoadSchema.invoke(vd, schm.inputSource());
          if(loaded.equals(Boolean.TRUE)) vdValidate.invoke(vd, input.inputSource());
        } catch(final ClassNotFoundException ex) {
          Util.debug(ex);
          throw VALIDATE_NOTFOUND_X.get(info);
        } catch(final Exception ex) {
          throw VALIDATE_ERROR_X.get(info, Util.rootException(ex));
        }
      }
    });
  }
}
