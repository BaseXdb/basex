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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class ValidateRng extends ValidateFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return check(qc);
  }

  @Override
  public ArrayList<ErrorInfo> errors(final QueryContext qc) throws QueryException {
    return process(new Validation() {
      @Override
      void process(final ValidationHandler handler) throws IOException, QueryException {
        final IO in = read(toNodeOrAtomItem(0, qc), null);
        final Item sch = toNodeOrAtomItem(1, qc);
        final boolean compact = exprs.length > 2 && toBoolean(exprs[2], qc);

        // detect format of schema input
        IO schema;
        try {
          schema = read(sch, null);
        } catch(final QueryException ex) {
          // compact schema: treat string as input
          if(!compact || ex.error() != WHICHRES_X) throw ex;
          schema = new IOContent(sch.string(info));
        }
        schema = prepare(schema, handler);

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
          pmbPut.invoke(pmb, vpClass.getField("ERROR_HANDLER").get(null), handler);

          // enable ID/IDREF checks
          final Object present = flClass.getField("PRESENT").get(null);
          pmbPut.invoke(pmb, rpClass.getField("CHECK_ID_IDREF").get(null), present);

          // create driver
          final Object sr = compact ? csrClass.getMethod("getInstance").invoke(null) : null;
          final Object pm = pmbClass.getMethod("toPropertyMap").invoke(pmb);
          final Object vd = vdClass.getConstructor(pmClass, srClass).newInstance(pm, sr);

          // load schema, validate document
          final Object loaded = vdLoadSchema.invoke(vd, schema.inputSource());
          if(loaded.equals(Boolean.TRUE)) vdValidate.invoke(vd, in.inputSource());

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
