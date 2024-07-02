(:~
 : Create backup.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace html = 'dba/html' at '../../lib/html.xqm';
import module namespace utils = 'dba/utils' at '../../lib/utils.xqm';

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
  %rest:POST
  %rest:path('/dba/backup-create')
  %rest:query-param('name', '{$name}', '')
  %output:method('html')
  %output:html-version('5')
function dba:backup-create(
  $name  as xs:string
) as element(html) {
  html:wrap({ 'header': ($dba:CAT, $name) },
    <tr>
      <td>
        <form method='post' autocomplete='off'>
          <input type='hidden' name='name' value='{ $name }'/>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            (html:link($name, $dba:SUB, { 'name': $name }), ' » ')[$name],
            html:button('backup-create-do', 'Create Backup')
          }</h2>
          <table>
            <tr>
              <td>Comment:</td>
              <td>
                <input type='text' name='comment' size='64' placeholder='optional'
                  autofocus='autofocus'/>
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
  %rest:path('/dba/backup-create-do')
  %rest:query-param('name',     '{$name}', '')
  %rest:query-param('comment',  '{$comment}')
  %rest:query-param('compress', '{$compress}')
function dba:backup-create-do(
  $name      as xs:string,
  $comment   as xs:string,
  $compress  as xs:string?
) as empty-sequence() {
  try {
    db:create-backup($name, { 'comment': $comment, 'compress': boolean($compress) }),
    utils:redirect($dba:SUB, { 'name': $name, 'info': 'Backup was created.' })
  } catch * {
    utils:redirect($dba:SUB, { 'name': $name, 'error': $err:description })
  }
};

(:~
 : Creates backups.
 : @param  $names  names of databases
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/backups-create')
  %rest:query-param('name', '{$names}')
function dba:backups-create(
  $names  as xs:string*
) as empty-sequence() {
  try {
    $names ! db:create-backup(.),
    utils:redirect($dba:CAT, { 'info': utils:info($names, 'database', 'backed up') })
  } catch * {
    utils:redirect($dba:CAT, { 'error': $err:description })
  }
};
