// This example shows how database commands can be executed.
//
// Documentation: https://docs.basex.org/main/Clients
//
// (C) BaseX Team, BSD License

import { Session } from './BaseXClient.mjs';

try {
  // initialize timer
  const start = performance.now();

  // create session
  const session = await Session.connect('localhost', 1984, 'admin', 'admin');

  // perform command and print returned string
  console.log(await session.execute('xquery 1 to 10'));

  // close session
  await session.close();

  // print time needed
  console.log(`${performance.now() - start} ms.`);
} catch(error) {
  // print exception
  console.error(error.message);
}
