<?php
/*
 * PHP client for BaseX.
 * Works with BaseX 7.0 and later
 *
 * Documentation: https://docs.basex.org/wiki/Clients
 *
 * (C) BaseX Team 2005-24, BSD License
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

        // receive timestamp
        $ts = $this->readString();
        // Hash container
        if (strpos($ts, ':') !== false) {
            // digest-auth
            $challenge = explode(':', $ts, 2);
            $md5 = hash("md5", hash("md5", $user.':'.$challenge[0].':'.$password).$challenge[1]);
        } else {
            // Legacy: cram-md5
            $md5 = hash("md5", hash("md5", $password).$ts);
        }

        // send username and hashed password/timestamp
        $result = $this->send($user.chr(0).$md5.chr(0));
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
     * Replaces content at the specified path by the given document.
     *
     * @param string $path filesystem-like path
     * @param string $input XML string
     */
    public function replace($path, $input)
    {
        $this->sendCmd(12, $path, $input);
    }

    /**
     * Stores binary content at the specified path.
     *
     * @param string $path filesystem-like path
     * @param string $input binary data
     */
    public function store($path, $input)
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
        while (($d = $this->read()) != chr(0)) {
            $com .= $d;
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
