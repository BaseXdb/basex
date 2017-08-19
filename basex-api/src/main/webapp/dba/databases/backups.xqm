(:~
 : Backup operations.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Creates a database backup.
 : @param  $name  name of database
 : @return redirection
 :)
declare
  %updating
  %rest:GET
  %rest:path("/dba/backup-create")
  %rest:query-param("name", "{$name}")
function dba:backup-create(
  $name  as xs:string
) as empty-sequence() {
  dba:action($name, 'Backup was created.', function() {
    db:create-backup($name)
  })
};

(:~
 : Drops a database backup.
 : @param  $name     name of database
 : @param  $backups  backup files
 : @return redirection
 :)
declare
  %updating
  %rest:GET
  %rest:path("/dba/backup-drop")
  %rest:query-param("name",   "{$name}")
  %rest:query-param("backup", "{$backups}")
function dba:backup-drop(
  $name     as xs:string,
  $backups  as xs:string*
) as empty-sequence() {
  dba:action($name, util:info($backups, 'backup', 'dropped'), function() {
    $backups ! db:drop-backup(.)
  })
};

(:~
 : Restores a database backup.
 : @param  $name    database
 : @param  $backup  backup file
 : @return redirection
 :)
declare
  %updating
  %rest:GET
  %rest:path("/dba/backup-restore")
  %rest:query-param("name",   "{$name}")
  %rest:query-param("backup", "{$backup}")
function dba:backup-restore(
  $name    as xs:string,
  $backup  as xs:string
) as empty-sequence() {
  dba:action($name, 'Database was restored.', function() { db:restore($backup) })
};

(:~
 : Performs a backup operation.
 : @param  $name    database
 : @param  $info    info string
 : @param  $action  updating function
 : @return redirection
 :)
declare %updating function dba:action(
  $name    as xs:string,
  $info    as xs:string,
  $action  as %updating function(*)
) as empty-sequence() {
  cons:check(),
  try {
    updating $action(),
    cons:redirect($dba:SUB, map { 'name': $name, 'info': $info })
  } catch * {
    cons:redirect($dba:SUB, map { 'name': $name, 'error': $err:description })
  }
};
