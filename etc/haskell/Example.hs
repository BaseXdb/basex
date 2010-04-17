module Example where

import BaseXClient
import Network ( withSocketsDo )
import Data.Time.Clock ( getCurrentTime, diffUTCTime )
import Control.Applicative ( (<$>), (<*>), pure )

query :: String
query = "xquery 1 to 10"

main :: IO ()
main = withSocketsDo $ do
	-- start time
    start <- getCurrentTime
    -- connect to the server
    (Just session) <- connect "localhost" 1984 "admin" "admin"
    -- execute and print the query
    putStrLn . either id content <$> execute session query
    -- close the session
    close session
    -- print time difference
    (diffUTCTime <$> getCurrentTime <*> pure start) >>= print
