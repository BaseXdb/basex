// This example shows how new documents can be added.
//
// Documentation: https://docs.basex.org/main/Clients
//
// (C) BaseX Team, BSD License

import { Session } from './BaseXClient.mjs';

try {
  // create session
  const session = await Session.connect('localhost', 1984, 'admin', 'admin');

  // create empty database
  await session.execute('create db database');
  console.log(session.info());

  // add document
  await session.add('world/World.xml', '<x>Hello World!</x>');
  console.log(session.info());

  // add document
  await session.add('Universe.xml', '<x>Hello Universe!</x>');
  console.log(session.info());

  // run query on database
  console.log(await session.execute("xquery collection('database')"));

  // drop database
  await session.execute('drop db database');

  // close session
  await session.close();
} catch(error) {
  // print exception
  console.error(error.message);
}
