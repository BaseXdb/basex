(:~
 : Download backups.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/databases';

(:~
 : Downloads a backup.
 : @param  $backup  name of backup file (ignored by the server)
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
      { 'media-type': 'application/octet-stream' },
      { 'Content-Length': file:size($path) }
    ),
    file:read-binary($path)
  )
};
