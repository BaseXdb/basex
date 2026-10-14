// This example shows how new databases can be created.
//
// Documentation: https://docs.basex.org/wiki/Clients
//
// (C) BaseX Team, BSD License

import { Session } from './BaseXClient.mjs';

try {
  // create session
  const session = await Session.connect('localhost', 1984, 'admin', 'admin');

  // create new database
  await session.create('database', '<x>Hello World!</x>');
  console.log(session.info());

  // run query on database
  console.log(await session.execute("xquery doc('database')"));

  // drop database
  await session.execute('drop db database');

  // close session
  await session.close();
} catch(error) {
  // print exception
  console.error(error.message);
}
