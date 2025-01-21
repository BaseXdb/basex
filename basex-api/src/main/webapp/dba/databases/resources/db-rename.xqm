(:~
 : Rename resource.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace html = 'dba/html' at '../../lib/html.xqm';
import module namespace utils = 'dba/utils' at '../../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';
(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Rename resource.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $target    target path
 : @param  $do        perform update
 : @return form or redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/db-rename')
  %rest:form-param('name',     '{$name}')
  %rest:form-param('resource', '{$resource}')
  %rest:form-param('target',   '{$target}')
  %rest:form-param('do',        '{$do}')
  %output:method('html')
  %output:html-version('5')
function dba:db-rename(
  $name      as xs:string,
  $resource  as xs:string?,
  $target    as xs:string?,
  $do        as xs:string?
) {
  html:update($do, { 'header': ($dba:CAT, $name) }, fn() {
    <tr>
      <td>
        <form method='post' autocomplete='off'>
          <input type='hidden' name='do' value='do'/>
          <input type='hidden' name='name' value='{ $name }'/>
          <input type='hidden' name='resource' value='{ $resource }'/>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            html:link($name, $dba:SUB, { 'name': $name }), ' » ',
            html:link($resource, $dba:SUB, { 'name': $name, 'resource': $resource }), ' » ',
            html:button('db-rename', 'Rename')
          }</h2>
          <table>
            <tr>
              <td>New path:</td>
              <td>
                <input type='text' name='target' value='{ $target otherwise $resource }' autofocus=''/>
                <div class='small'/>
              </td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
  }, fn() {
    if (db:exists($name, $target)) {
      error((), 'Resource already exists.')
    } else {
      db:rename($name, $resource, $target),
      utils:redirect($dba:SUB, { 'name': $name, 'resource': $target, 'info': 'Resource was renamed.' })
    }
  })
};
