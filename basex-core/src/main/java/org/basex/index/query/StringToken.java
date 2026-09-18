package org.basex.index.query;

import org.basex.index.*;

/**
 * This class defines access to index text tokens.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param type index type
 * @param token value to be found
 */
public record StringToken(IndexType type, byte[] token) implements IndexSearch { }
