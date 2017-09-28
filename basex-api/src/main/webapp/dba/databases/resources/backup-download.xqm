(:~
 : Download resources.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../../modules/cons.xqm';

(:~
 : Downloads a database backup.
 : @param  $backup  name of backup file (ignored)
 : @return binary data
 :)
declare
  %rest:path("/dba/backup/{$backup}")
  %output:media-type("application/octet-stream")
function dba:backup-download(
  $backup  as xs:string
) as xs:base64Binary {
  cons:check(),
  file:read-binary(db:system()/globaloptions/dbpath || '/' || $backup)
};
