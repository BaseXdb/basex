// This example shows how external variables can be bound to XQuery expressions.
//
// Documentation: https://docs.basex.org/main/Clients
//
// (C) BaseX Team, BSD License

import { Session } from './BaseXClient.mjs';

try {
  // create session
  const session = await Session.connect('localhost', 1984, 'admin', 'admin');

  // create query instance
  const input = 'declare variable $name external; ' +
    'for $i in 1 to 10 return element { $name } { $i }';
  const query = await session.query(input);

  // bind variable
  await query.bind('name', 'number');

  // print result
  console.log(await query.execute());

  // close query
  await query.close();

  // close session
  await session.close();
} catch(error) {
  // print exception
  console.error(error.message);
}
