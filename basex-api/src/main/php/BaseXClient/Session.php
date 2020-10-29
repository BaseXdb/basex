<?php
/*
 * PHP client for BaseX.
 * Works with BaseX 7.0 and later
 *
 * Documentation: https://docs.basex.org/wiki/Clients
 *
 * (C) BaseX Team 2005-15, BSD License
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
        if (!socket_connect($this->socket, $hostname, $port)) {
            throw new BaseXException("Can't communicate with server.");
        }

        // receive timestamp
        $ts = $this->readString();
        // Hash container
        if (false !== strpos($ts, ':')) {
            // digest-auth
            $challenge = explode(':', $ts, 2);
            $md5 = hash("md5", hash("md5", $user . ':' . $challenge[0] . ':' . $password) . $challenge[1]);
        } else {
            // Legacy: cram-md5
            $md5 = hash("md5", hash("md5", $password) . $ts);
        }

        // send username and hashed password/timestamp
        socket_write($this->socket, $user . chr(0) . $md5 . chr(0));

        // receives success flag
        if (socket_read($this->socket, 1) != chr(0)) {
            throw new BaseXException("Access denied.");
        }
    }

    /**
     * Execute BaseX command.
     *
     * @param string $command
     * @return string
     */
    public function execute($command)
    {
        // send command to server
        socket_write($this->socket, $command.chr(0));

        // receive result
        $result = $this->receive();
        $this->info = $this->readString();
        if ($this->ok() != true) {
            throw new BaseXException($this->info);
        }
        return $result;
    }

    /**
     * Execute XQuery query.
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
     * @param string $input XML to insert
     */
    public function create($name, $input)
    {
        $this->sendCmd(8, $name, $input);
    }

    /**
     * Inserts a document in the database at the specified path.
     *
     * @param string $path filesystem-like path
     * @param string $input XML to insert
     */
    public function add($path, $input)
    {
        $this->sendCmd(9, $path, $input);
    }

    /**
     * Replaces content at the specified path by the given document.
     *
     * @param string $path filesystem-like path
     * @param string $input XML to insert
     */
    public function replace($path, $input)
    {
        $this->sendCmd(12, $path, $input);
    }

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
     * Close the connection.
     */
    public function close()
    {
        socket_write($this->socket, "exit".chr(0));
        socket_close($this->socket);
    }

    private function init()
    {
        $this->bpos = 0;
        $this->bsize = 0;
    }

    /**
     * @internal
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

    private function read()
    {
        if ($this->bpos == $this->bsize) {
            $this->bsize = socket_recv($this->socket, $this->buffer, 4096, 0);
            $this->bpos = 0;
        }
        return $this->buffer[$this->bpos++];
    }

    private function sendCmd($code, $arg, $input)
    {
        socket_write($this->socket, chr($code).$arg.chr(0).$input.chr(0));
        $this->info = $this->receive();
        if ($this->ok() != true) {
            throw new BaseXException($this->info);
        }
    }

    public function send($str)
    {
        socket_write($this->socket, $str.chr(0));
    }

    /**
     * Was the last command/query successful?
     *
     * @internal not idempotent, not intended for use by client code
     * @return bool
     */
    public function ok()
    {
        return $this->read() == chr(0);
    }

    /**
     * @internal
     * @return string
     */
    public function receive()
    {
        $this->init();
        return $this->readString();
    }
}
