(:~
 : Download resources.
 :
 : @author Christian Grün, BaseX Team 2005-21, BSD License
 :)
module namespace dba = 'dba/databases';

(:~
 : Downloads a database backup.
 : @param  $backup  name of backup file (ignored)
 : @return binary data
 :)
declare
  %rest:GET
  %rest:path('/dba/backup/{$backup}')
function dba:backup-download(
  $backup  as xs:string
) as item()+ {
  let $path := db:option('dbpath') || '/' || $backup
  return (
    web:response-header(
      map { 'media-type': 'application/octet-stream' },
      map { 'Content-Length': file:size($path) }
    ),
    file:read-binary($path)
  )
};
