package org.basex.api.rest;

import java.io.*;

import org.basex.api.*;
import org.basex.data.*;
import org.basex.io.serial.*;
import org.basex.server.*;

/**
 * REST-based evaluation of database commands.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class RESTCommand extends RESTCode {
  /** Query input. */
  protected final String input;

  /**
   * Constructor.
   * @param in command to be executed
   */
  RESTCommand(final String in) {
    input = in;
  }

  @Override
  void run(final HTTPContext ctx) throws HTTPException, IOException {
    // open addressed database
    open(ctx);
    // set default content type to raw
    final String sprop = SerializerProp.S_METHOD[0] + "=" + DataText.M_TEXT +
            ',' + ctx.serialization;
    initResponse(new SerializerProp(sprop), ctx);

    // perform command
    final Session session = ctx.session;
    session.setOutputStream(ctx.out);
    session.execute(input);
  }
}
