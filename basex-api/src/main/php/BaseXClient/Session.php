<?php
/*
 * PHP client for BaseX.
 * Works with BaseX 13.0 and later
 *
 * Documentation: https://docs.basex.org/wiki/Clients
 *
 * (C) BaseX Team, BSD License
 */

namespace BaseXClient;

class Session
{
    // instance variables.
    protected $socket;
    protected $info;
    protected $buffer;
    protected $bpos;
    protected $bsize;

    public function __construct($hostname, $port, $user, $password)
    {
        // create server connection
        $this->socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
        if (!$this->socket) {
            throw $this->error("Socket creation failed");
        }
        if (!socket_connect($this->socket, $hostname, $port)) {
            throw $this->error("Cannot connect");
        }

        // receive challenge: {realm}:{nonce}
        $challenge = explode(':', $this->readString());
        $nonce = $challenge[1];

        // request the password parameters: send username and an empty hash
        $this->send($user.chr(0).chr(0));

        // receive password parameters: {algorithm}:{salt}
        $params = explode(':', $this->readString());
        $salt = $params[1];

        // send hashed password
        $code = hash("sha256", hash("sha256", $salt.$password).$nonce);
        $result = $this->send($code.chr(0));
        if ($result === false) {
            throw $this->error("Write failed");
        }

        // receives success flag
        $result = socket_read($this->socket, 1);
        if ($result === false) {
            throw $this->error("Read failed");
        }
        if ($result != chr(0)) {
            throw new BaseXException("Access denied.");
        }
    }

    /**
     * Executes a command.
     *
     * @param string $command
     * @return string
     */
    public function execute($command)
    {
        // send command to server
        $result = $this->send($command.chr(0));
        if ($result === false) {
            throw $this->error("Write failed");
        }

        // receive result
        $result = $this->receive();
        $this->info = $this->readString();
        if (!$this->ok()) {
            throw new BaseXException($this->info);
        }
        return $result;
    }

    /**
     * Executes a query.
     *
     * @param string $xquery
     * @return Query
     */
    public function query($xquery)
    {
        return new Query($this, $xquery);
    }

    /**
     * Creates a new database, inserts initial content.
     *
     * @param string $name name of the new database
     * @param string $input XML string
     */
    public function create($name, $input)
    {
        $this->sendCmd(8, $name, $input);
    }

    /**
     * Inserts a document in the database at the specified path.
     *
     * @param string $path filesystem-like path
     * @param string $input XML string
     */
    public function add($path, $input)
    {
        $this->sendCmd(9, $path, $input);
    }

    /**
     * Puts (adds or replaces) a document in the opened database.
     *
     * @param string $path filesystem-like path
     * @param string $input XML string
     */
    public function put($path, $input)
    {
        $this->sendCmd(12, $path, $input);
    }

    /**
     * Puts (adds or replaces) a binary resource in the opened database.
     *
     * @param string $path filesystem-like path
     * @param string $input binary data
     */
    public function putBinary($path, $input)
    {
        $this->sendCmd(13, $path, $input);
    }

    /**
     * Status information of the last command/query.
     *
     * @return string|null
     */
    public function info()
    {
        return $this->info;
    }

    /**
     * Closes the connection.
     */
    public function close()
    {
        socket_close($this->socket);
    }

    /**
     * Reads a string.
     *
     * @return string
     */
    public function readString()
    {
        $com = "";
        while (($d = $this->read()) !== chr(0))
        {
            // 0x00 and 0xFF bytes are prefixed with 0xFF
            if ($d === chr(255)) {
                $d = $this->read();
            }
            $com .= $d;
            // copy all bytes up to the next 0x00 or 0xFF byte
            $len = strcspn($this->buffer, "\x00\xFF", $this->bpos, $this->bsize - $this->bpos);
            $com .= substr($this->buffer, $this->bpos, $len);
            $this->bpos += $len;
        }
        return $com;
    }

    /**
     * Was the last command/query successful?
     *
     * @internal not idempotent, not intended for use by client code
     * @return result of check
     */
    public function ok()
    {
        return $this->read() == chr(0);
    }

    /**
     * Receives data.
     * @return string
     */
    public function receive()
    {
        $this->bpos = 0;
        $this->bsize = 0;
        return $this->readString();
    }

    /**
     * Sends data.
     * @param data data string
     */
    public function send($data)
    {
        $result = socket_write($this->socket, $data);
        if ($result === false) {
            throw $this->error("Write failed");
        }
    }

    private function read()
    {
        if ($this->bpos == $this->bsize) {
            $this->bpos = 0;
            $this->bsize = socket_recv($this->socket, $this->buffer, 4096, 0);
            if ($this->bsize === false) {
                throw $this->error("Read failed");
            }
            if ($this->bsize === 0) {
              throw $this->error("Connection closed unexpectedly");
            }
        }
        return $this->buffer[$this->bpos++];
    }

    private function sendCmd($code, $arg, $input)
    {
        // prefix 0x00 and 0xFF bytes with 0xFF (see Server Protocol)
        $input = preg_replace('/[\x00\xFF]/', "\xFF$0", $input);
        $this->send(chr($code).$arg.chr(0).$input.chr(0));
        $this->info = $this->receive();
        if (!$this->ok()) {
            throw new BaseXException($this->info);
        }
    }

    /**
     * Raises a socket error.
     */
    public function error($message) {
        $code = socket_last_error();
        $info = socket_strerror($code);
        return new BaseXException($message.": ".$info." (".$code.")");
    }
}
