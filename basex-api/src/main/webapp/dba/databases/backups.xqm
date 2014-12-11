(:~
 : Backup operations.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace _ = 'dba/databases';

import module namespace web = 'dba/web' at '../modules/web.xqm';

(:~
 : Creates a database backup.
 : @param  $name  name of database
 :)
declare
  %updating
  %rest:GET
  %rest:path("dba/create-backup")
  %rest:query-param("name", "{$name}")
function _:create-backup(
  $name  as xs:string
) {
  _:action($name, 'Backup was created.', "db:create-backup($n)", map { 'n': $name })
};

(:~
 : Drops a database backup.
 : @param  $name     name of database
 : @param  $backups  backup files
 :)
declare
  %updating
  %rest:GET
  %rest:path("dba/drop-backup")
  %rest:query-param("name",   "{$name}")
  %rest:query-param("backup", "{$backups}")
function _:drop-backup(
  $name     as xs:string,
  $backups  as xs:string*
) {
  let $n := count($backups)
  let $info := if($n = 1) then 'Backup was dropped.' else $n || ' backups were dropped.'
  return _:action($name, $info, "$b ! db:drop-backup(.)", map { 'b': $backups })
};

(:~
 : Restores a database backup.
 : @param  $name    database
 : @param  $backup  backup file
 :)
declare
  %updating
  %rest:GET
  %rest:path("dba/restore")
  %rest:query-param("name",   "{$name}")
  %rest:query-param("backup", "{$backup}")
function _:restore(
  $name    as xs:string,
  $backup  as xs:string
) {
  _:action($name, 'Database was restored.', "db:restore($b)", map { 'b': $backup })
};

(:~
 : Performs a backup operation.
 : @param  $name   database
 : @param  $info   info string
 : @param  $query  query to execute 
 : @param  $args   query arguments
 :)
declare %updating function _:action(
  $name   as xs:string,
  $info   as xs:string,
  $query  as xs:string,
  $args   as map(*)
) {
  web:check(),
  try {
    web:update($query, $args),
    web:redirect('database', map {
      'name': $name,
      'info': $info
    })
  } catch * {
    web:redirect('database', map { 'name': $name, 'error': $err:description })
  }
};
