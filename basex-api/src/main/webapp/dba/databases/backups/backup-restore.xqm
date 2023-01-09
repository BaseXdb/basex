(:~
 : Restore backup.
 :
 : @author Christian Grün, BaseX Team 2005-23, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace util = 'dba/util' at '../../lib/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';
(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Restores a database backup.
 : @param  $name     name of database
 : @param  $backups  timestamps of backups (only first will be considered)
 : @return redirection
 :)
declare
  %updating
  %rest:GET
  %rest:path('/dba/backup-restore')
  %rest:query-param('name',   '{$name}', '')
  %rest:query-param('backup', '{$backups}')
function dba:backup-restore(
  $name     as xs:string,
  $backups  as xs:string+
) as empty-sequence() {
  let $target := $name ?? $dba:SUB !! $dba:CAT
  return try {
    db:restore($name || '-' || head($backups)),
    util:redirect($target, map { 'name': $name, 'info': util:info($backups, 'backup', 'restored') })
  } catch * {
    util:redirect($target, map { 'name': $name, 'error': $err:description })
  }
};

(:~
 : Restores database backups.
 : @param  $names  names of databases
 : @return redirection
 :)
declare
  %updating
  %rest:GET
  %rest:path('/dba/backup-restore-all')
  %rest:query-param('name', '{$names}')
function dba:db-optimize-all(
  $names  as xs:string*
) as empty-sequence() {
  try {
    $names ! db:restore(.),
    util:redirect($dba:CAT, map { 'info': util:info($names, 'backup', 'restored') })
  } catch * {
    util:redirect($dba:CAT, map { 'error': $err:description })
  }
};
