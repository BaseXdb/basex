// JavaScript client for BaseX (Node.js).
// Works with BaseX 13.0 and later
//
// Documentation: https://docs.basex.org/main/Clients
//
// (C) BaseX Team, BSD License

import { createHash } from 'node:crypto';
import { connect } from 'node:net';

/**
 * Returns the hex-encoded SHA-256 hash of a string.
 * @param {string} string string to be hashed
 * @returns {string} hash
 */
const sha256 = (string) => createHash('sha256').update(string, 'utf8').digest('hex');

/**
 * Socket connection with buffered reads and serialized requests.
 */
class Connection {
  #socket;
  #buffer = Buffer.alloc(0);
  #pos = 0;
  #wait = null;
  #ended = false;
  #error = null;
  #queue = Promise.resolve();

  /**
   * Constructor.
   * @param {import('node:net').Socket} socket socket
   */
  constructor(socket) {
    this.#socket = socket;
    socket.on('data', (data) => {
      this.#buffer = this.#pos < this.#buffer.length ?
        Buffer.concat([this.#buffer.subarray(this.#pos), data]) : data;
      this.#pos = 0;
      this.#notify();
    });
    socket.on('end', () => { this.#ended = true; this.#notify(); });
    socket.on('error', (error) => { this.#error = error; this.#notify(); });
  }

  /**
   * Runs a request after all pending requests have been completed.
   * @param {() => Promise<any>} request request
   * @returns {Promise<any>} result of the request
   */
  run(request) {
    const result = this.#queue.then(request);
    this.#queue = result.catch(() => {});
    return result;
  }

  /**
   * Sends a string, followed by a 0 byte.
   * @param {string} string string
   */
  send(string) {
    this.write(Buffer.from(string, 'utf8'));
  }

  /**
   * Sends bytes, followed by a 0 byte.
   * @param {Buffer} bytes bytes
   */
  write(bytes) {
    this.#socket.write(Buffer.concat([bytes, Buffer.of(0)]));
  }

  /**
   * Returns the next byte without consuming it.
   * @returns {Promise<number>} byte
   */
  async peek() {
    await this.#fill();
    return this.#buffer[this.#pos];
  }

  /**
   * Returns the next byte.
   * @returns {Promise<number>} byte
   */
  async byte() {
    await this.#fill();
    return this.#buffer[this.#pos++];
  }

  /**
   * Returns the bytes up to the next 0 byte; 0x00 and 0xFF bytes prefixed with 0xFF are unescaped.
   * @returns {Promise<Buffer>} bytes
   */
  async bytes() {
    const parts = [];
    while(true) {
      await this.#fill();
      const buffer = this.#buffer, start = this.#pos;
      let p = start;
      while(p < buffer.length && buffer[p] !== 0x00 && buffer[p] !== 0xFF) p++;
      parts.push(buffer.subarray(start, p));
      this.#pos = p;
      if(p === buffer.length) continue;
      this.#pos++;
      if(buffer[p] === 0x00) return Buffer.concat(parts);
      parts.push(Buffer.of(await this.byte()));
    }
  }

  /**
   * Returns the string up to the next 0 byte.
   * @returns {Promise<string>} string
   */
  async string() {
    return (await this.bytes()).toString('utf8');
  }

  /**
   * Reads the success flag.
   * @returns {Promise<boolean>} success
   */
  async ok() {
    return await this.byte() === 0;
  }

  /**
   * Sends a query command and returns the result.
   * @param {number} code command code
   * @param {string} arg argument
   * @returns {Promise<string>} result
   */
  exec(code, arg) {
    return this.run(async () => {
      this.send(String.fromCharCode(code) + arg);
      const result = await this.string();
      if(!await this.ok()) throw new Error(await this.string());
      return result;
    });
  }

  /**
   * Closes the socket.
   */
  close() {
    this.#socket.end();
  }

  /**
   * Destroys the socket.
   */
  destroy() {
    this.#socket.destroy();
  }

  /**
   * Waits until unread bytes are available.
   */
  async #fill() {
    while(this.#pos >= this.#buffer.length) {
      if(this.#error) throw this.#error;
      if(this.#ended) throw new Error('Connection closed.');
      await new Promise((resolve) => { this.#wait = resolve; });
    }
  }

  /**
   * Resumes a waiting read.
   */
  #notify() {
    const wait = this.#wait;
    this.#wait = null;
    wait?.();
  }
}

/**
 * Session for communicating with a BaseX server.
 */
export class Session {
  #connection;
  #info = '';

  /**
   * Constructor; use {@link Session.connect} to create sessions.
   * @param {Connection} connection connection
   */
  constructor(connection) {
    this.#connection = connection;
  }

  /**
   * Connects to a server and creates a session.
   * @param {string} host     host name
   * @param {number} port     port
   * @param {string} username user name
   * @param {string} password password
   * @returns {Promise<Session>} session
   */
  static async connect(host, port, username, password) {
    const socket = connect({ host, port });
    socket.setNoDelay(true);
    const connection = new Connection(socket);
    try {
      // receive challenge: {realm}:{nonce}
      const nonce = (await connection.string()).split(':')[1];

      // request the password parameters: send username and an empty hash
      connection.send(username);
      connection.send('');

      // receive password parameters: {algorithm}:{salt} (servers < 13.0 send \1)
      if(await connection.peek() === 1) {
        throw new Error('Server does not support salted authentication (BaseX < 13.0).');
      }
      const salt = (await connection.string()).split(':')[1];

      // send hashed password
      connection.send(sha256(sha256(salt + password) + nonce));
      if(!await connection.ok()) throw new Error('Access denied.');
    } catch(error) {
      connection.destroy();
      throw error;
    }
    return new Session(connection);
  }

  /**
   * Executes a database command and returns the result.
   * @param {string} command command
   * @returns {Promise<string>} result
   */
  execute(command) {
    const connection = this.#connection;
    return connection.run(async () => {
      connection.send(command);
      const result = await connection.string();
      this.#info = await connection.string();
      if(!await connection.ok()) throw new Error(this.#info);
      return result;
    });
  }

  /**
   * Creates a query instance.
   * @param {string} query query string
   * @returns {Promise<Query>} query
   */
  async query(query) {
    return new Query(this.#connection, await this.#connection.exec(0, query));
  }

  /**
   * Creates a database.
   * @param {string} name             name of the database
   * @param {string | Uint8Array} input XML input
   * @returns {Promise<void>}
   */
  create(name, input) {
    return this.#sendInput(8, name, input);
  }

  /**
   * Adds a document to the opened database.
   * @param {string} path             path of the document
   * @param {string | Uint8Array} input XML input
   * @returns {Promise<void>}
   */
  add(path, input) {
    return this.#sendInput(9, path, input);
  }

  /**
   * Puts (adds or replaces) a document in the opened database.
   * @param {string} path             path of the document
   * @param {string | Uint8Array} input XML input
   * @returns {Promise<void>}
   */
  put(path, input) {
    return this.#sendInput(12, path, input);
  }

  /**
   * Puts (adds or replaces) a binary resource in the opened database.
   * @param {string} path             path of the resource
   * @param {string | Uint8Array} input binary input
   * @returns {Promise<void>}
   */
  putBinary(path, input) {
    return this.#sendInput(13, path, input);
  }

  /**
   * Returns information on the last executed command.
   * @returns {string} info
   */
  info() {
    return this.#info;
  }

  /**
   * Closes the session.
   * @returns {Promise<void>}
   */
  close() {
    const connection = this.#connection;
    return connection.run(async () => {
      connection.send('exit');
      connection.close();
    });
  }

  /**
   * Sends a command with input.
   * @param {number} code               command code
   * @param {string} arg                argument
   * @param {string | Uint8Array} input input
   * @returns {Promise<void>}
   */
  #sendInput(code, arg, input) {
    const connection = this.#connection;
    return connection.run(async () => {
      // prefix 0x00 and 0xFF bytes with 0xFF
      const bytes = typeof input === 'string' ? Buffer.from(input, 'utf8') : input;
      let size = bytes.length;
      for(const b of bytes) if(b === 0x00 || b === 0xFF) size++;
      const escaped = Buffer.alloc(size);
      let p = 0;
      for(const b of bytes) {
        if(b === 0x00 || b === 0xFF) escaped[p++] = 0xFF;
        escaped[p++] = b;
      }
      connection.send(String.fromCharCode(code) + arg);
      connection.write(escaped);
      this.#info = await connection.string();
      if(!await connection.ok()) throw new Error(this.#info);
    });
  }
}

/**
 * Query instance.
 */
export class Query {
  #connection;
  #id;
  #cache = [];
  #pos = 0;

  /**
   * Constructor; use {@link Session#query} to create queries.
   * @param {Connection} connection connection
   * @param {string} id             query id
   */
  constructor(connection, id) {
    this.#connection = connection;
    this.#id = id;
  }

  /**
   * Binds a value to an external variable.
   * @param {string} name  name of the variable
   * @param {string} value value
   * @param {string} type  type (empty string for xs:untypedAtomic)
   * @returns {Promise<void>}
   */
  async bind(name, value, type = '') {
    this.#cache = [];
    await this.#connection.exec(3, `${this.#id}\0${name}\0${value}\0${type}`);
  }

  /**
   * Binds a value to the context.
   * @param {string} value value
   * @param {string} type  type (empty string for xs:untypedAtomic)
   * @returns {Promise<void>}
   */
  async context(value, type = '') {
    this.#cache = [];
    await this.#connection.exec(14, `${this.#id}\0${value}\0${type}`);
  }

  /**
   * Checks if more results are available.
   * @returns {Promise<boolean>} result of check
   */
  async more() {
    if(this.#cache.length === 0) {
      const connection = this.#connection;
      this.#cache = await connection.run(async () => {
        connection.send(String.fromCharCode(4) + this.#id);
        const cache = [];
        while(await connection.byte() > 0) cache.push(await connection.string());
        if(!await connection.ok()) throw new Error(await connection.string());
        return cache;
      });
      this.#pos = 0;
    }
    const more = this.#pos < this.#cache.length;
    if(!more) this.#cache = [];
    return more;
  }

  /**
   * Returns the next result.
   * @returns {Promise<string | undefined>} result, or undefined if no more results are available
   */
  async next() {
    return await this.more() ? this.#cache[this.#pos++] : undefined;
  }

  /**
   * Iterates over all results.
   * @returns {AsyncGenerator<string>} results
   */
  async *[Symbol.asyncIterator]() {
    while(await this.more()) yield this.#cache[this.#pos++];
  }

  /**
   * Executes the query and returns the result as a single string.
   * @returns {Promise<string>} result
   */
  execute() {
    return this.#connection.exec(5, this.#id);
  }

  /**
   * Returns query information.
   * @returns {Promise<string>} info
   */
  info() {
    return this.#connection.exec(6, this.#id);
  }

  /**
   * Returns the serialization parameters.
   * @returns {Promise<string>} parameters
   */
  options() {
    return this.#connection.exec(7, this.#id);
  }

  /**
   * Checks if the query is updating.
   * @returns {Promise<boolean>} result of check
   */
  async updating() {
    return await this.#connection.exec(30, this.#id) === 'true';
  }

  /**
   * Closes the query.
   * @returns {Promise<void>}
   */
  async close() {
    await this.#connection.exec(2, this.#id);
  }
}
