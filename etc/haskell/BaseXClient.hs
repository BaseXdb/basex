-------------------------------------------------------------------------------
-- |
-- Module      : BaseX.Client
-- Copyright   : (C) Workgroup DBIS, University of Konstanz 2005-10
-- License     : ISC
--
-- Maintainer  : leo@woerteler.de
-- Stability   : experimental
-- Portability : portable
--
-- This module provides methods to connect to and communicate with the
-- BaseX Server.
--
-- It requires the PureMD5 package fom Hackage: 'cabal install PureMD5'.
--
-------------------------------------------------------------------------------
-- 
-- Example:
-- 
-- import BaseX.Client
-- import Network ( withSocketsDo )
-- 
-- main :: IO ()
-- main = withSocketsDo $ do
--     (Just session) <- connect "localhost" 1984 "admin" "admin"
--     putStrLn . either id content <$> execute session "xquery 1 to 10"
--     close session
-- 
-------------------------------------------------------------------------------

module BaseXClient ( connect, execute, close, Result(..) ) where

import Network ( withSocketsDo, PortID(..), PortNumber(..), connectTo )
import Control.Applicative ( (<$>) )
import System.IO ( Handle, hGetChar, hPutStr, hClose, BufferMode(..),
    hSetBuffering)
import qualified Data.Digest.Pure.MD5 as MD5 ( md5 )
import Data.ByteString.Lazy.UTF8 ( fromString )

data Session = Session Handle
    deriving Show

data Result = Result { info :: String, content :: String }
    deriving Show    

-- | Connects to the BaseX server at host:port and establishes a session 
-- with the given user name and password.
connect :: String             -- ^ host name / IP
        -> PortNumber         -- ^ port
        -> String             -- ^ user name
        -> String             -- ^ password
        -> IO (Maybe Session)
connect host port user pass = do
    h <- connectTo host (PortNumber port)
    hSetBuffering h NoBuffering
    creds user pass <$> readString h >>= hPutStr h
    success <- ('\0' ==) <$> hGetChar h
    if success
        then return (Just $ Session h)
        else return Nothing
    where
        creds user pass ts = user ++ "\0" ++ md5 (md5 pass ++ ts) ++ "\0"
        md5 = show . MD5.md5 . fromString

-- | Executes a database command on the server and returns the result.
execute :: Session                   -- ^ BaseX session
        -> String                    -- ^ db command
        -> IO (Either String Result)
execute (Session h) cmd = do
    hPutStr h (cmd ++ "\0")
    res <- readString h
    inf <- readString h
    success <- ('\0' ==) <$> hGetChar h
    return $ if success
        then Right Result { info = inf, content = res }
        else Left inf

-- | Closes the connection.
close :: Session -> IO ()
close (Session h) = hPutStr h "exit\0" >> hClose h

readString :: Handle -> IO String
readString h = do
    c <- hGetChar h
    if c /= '\0' then (c:) <$> readString h else return []
