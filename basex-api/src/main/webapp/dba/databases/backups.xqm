(:~
 : Backup operations.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';

(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Creates a database backup.
 : @param  $name  name of database
 :)
declare
  %updating
  %rest:GET
  %rest:path("/dba/create-backup")
  %rest:query-param("name", "{$name}")
function dba:create-backup(
  $name  as xs:string
) {
  dba:action($name, 'Backup was created.', function() { db:create-backup($name) })
};

(:~
 : Drops a database backup.
 : @param  $name     name of database
 : @param  $backups  backup files
 :)
declare
  %updating
  %rest:GET
  %rest:path("/dba/drop-backup")
  %rest:query-param("name",   "{$name}")
  %rest:query-param("backup", "{$backups}")
function dba:drop-backup(
  $name     as xs:string,
  $backups  as xs:string*
) {
  let $n := count($backups)
  let $info := if($n = 1) then 'Backup was dropped.' else $n || ' backups were dropped.'
  return dba:action($name, $info, function() { $backups ! db:drop-backup(.) })
};

(:~
 : Restores a database backup.
 : @param  $name    database
 : @param  $backup  backup file
 :)
declare
  %updating
  %rest:GET
  %rest:path("/dba/restore")
  %rest:query-param("name",   "{$name}")
  %rest:query-param("backup", "{$backup}")
function dba:restore(
  $name    as xs:string,
  $backup  as xs:string
) {
  dba:action($name, 'Database was restored.', function() { db:restore($backup) })
};

(:~
 : Performs a backup operation.
 : @param  $name    database
 : @param  $info    info string
 : @param  $action  updating function
 :)
declare %updating function dba:action(
  $name    as xs:string,
  $info    as xs:string,
  $action  as %updating function(*)
) {
  cons:check(),
  try {
    updating $action(),
    cons:redirect($dba:SUB, map { 'name': $name, 'info': $info })
  } catch * {
    cons:redirect($dba:SUB, map { 'name': $name, 'error': $err:description })
  }
};
