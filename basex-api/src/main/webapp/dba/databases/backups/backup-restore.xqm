(:~
 : Restore backup.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace utils = 'dba/utils' at '../../lib/utils.xqm';

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
  let $target := if($name) then $dba:SUB else $dba:CAT
  return try {
    db:restore($name || '-' || head($backups)),
    utils:redirect(
      $target,
      map { 'name': $name, 'info': utils:info($backups, 'backup', 'restored') }
    )
  } catch * {
    utils:redirect($target, map { 'name': $name, 'error': $err:description })
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
    utils:redirect($dba:CAT, map { 'info': utils:info($names, 'backup', 'restored') })
  } catch * {
    utils:redirect($dba:CAT, map { 'error': $err:description })
  }
};
