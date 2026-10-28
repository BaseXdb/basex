// This example shows how queries can be executed in an iterative manner.
//
// Documentation: https://docs.basex.org/main/Clients
//
// (C) BaseX Team, BSD License

import { Session } from './BaseXClient.mjs';

try {
  // create session
  const session = await Session.connect('localhost', 1984, 'admin', 'admin');

  // create query instance
  const input = 'for $i in 1 to 10 return <xml>Text { $i }</xml>';
  const query = await session.query(input);

  // loop through all results
  for await (const item of query) {
    console.log(item);
  }

  // close query
  await query.close();

  // close session
  await session.close();
} catch(error) {
  // print exception
  console.error(error.message);
}
