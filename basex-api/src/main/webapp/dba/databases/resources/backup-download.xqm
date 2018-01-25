(:~
 : Download resources.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/databases';

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
  file:read-binary(db:option('dbpath') || '/' || $backup)
};
