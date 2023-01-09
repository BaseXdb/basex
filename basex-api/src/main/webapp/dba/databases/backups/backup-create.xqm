(:~
 : Create backup.
 :
 : @author Christian Grün, BaseX Team 2005-23, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace html = 'dba/html' at '../../lib/html.xqm';
import module namespace util = 'dba/util' at '../../lib/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';
(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Form for creating a backup.
 : @param  $name  database (empty string for general data)
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/backup-create')
  %rest:query-param('name', '{$name}', '')
  %output:method('html')
function dba:backup-create(
  $name  as xs:string
) as element(html) {
  html:wrap(map { 'header': ($dba:CAT, $name) },
    <tr>
      <td>
        <form action='backup-create' method='post' autocomplete='off'>
          <input type='hidden' name='name' value='{ $name }'/>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            (html:link($name, $dba:SUB, map { 'name': $name }), ' » ')[$name],
            html:button('backup-create', 'Create Backup')
          }</h2>
          <table>
            <tr>
              <td>Comment:</td>
              <td>
                <input type='text' name='comment' id='comment' size='64' placeholder='optional'/>
                { html:focus('comment') }
              </td>
            </tr>
            <tr>
              <td>Compress Files:</td>
              <td>{
                html:checkbox('compress', 'true', true(), '')
              }</td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
  )
};

(:~
 : Creates a backup.
 : @param  $name      database (empty string for general data)
 : @param  $comment   comment
 : @param  $compress  compress files
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/backup-create')
  %rest:query-param('name',     '{$name}', '')
  %rest:query-param('comment',  '{$comment}')
  %rest:query-param('compress', '{$compress}')
function dba:db-rename(
  $name      as xs:string,
  $comment   as xs:string,
  $compress  as xs:string?
) as empty-sequence() {
  try {
    db:create-backup($name, map { 'comment': $comment, 'compress': boolean($compress) }),
    util:redirect($dba:SUB, map { 'name': $name, 'info': 'Backup was created.' })
  } catch * {
    util:redirect($dba:SUB, map { 'name': $name, 'error': $err:description })
  }
};

(:~
 : Creates backups.
 : @param  $names  names of databases
 : @return redirection
 :)
declare
  %updating
  %rest:GET
  %rest:path('/dba/backup-create-all')
  %rest:query-param('name', '{$names}')
function dba:db-optimize-all(
  $names  as xs:string*
) as empty-sequence() {
  try {
    $names ! db:create-backup(.),
    util:redirect($dba:CAT, map { 'info': util:info($names, 'database', 'backed up') })
  } catch * {
    util:redirect($dba:CAT, map { 'error': $err:description })
  }
};
