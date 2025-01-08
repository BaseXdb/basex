(:~
 : Replace resource.
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
 : Replace resource.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $file      file input
 : @param  $do        perform update
 : @return form or redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/db-replace')
  %rest:form-param('name',     '{$name}')
  %rest:form-param('resource', '{$resource}')
  %rest:form-param('file',     '{$file}')
  %rest:form-param('do',       '{$do}')
  %output:method('html')
  %output:html-version('5')
function dba:db-replace(
  $name      as xs:string,
  $resource  as xs:string?,
  $file      as item()?,
  $do        as xs:string?
) {
  html:update($do, { 'header': ($dba:CAT, $name) }, fn() {
    <tr>
      <td>
        <form method='post' enctype='multipart/form-data' autocomplete='off'>
          <input type='hidden' name='do' value='do'/>
          <input type='hidden' name='name' value='{ $name }'/>
          <input type='hidden' name='resource' value='{ $resource }'/>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            html:link($name, $dba:SUB, { 'name': $name }), ' » ',
            html:link($resource, $dba:SUB, { 'name': $name, 'resource': $resource }), ' » ',
            html:button('db-replace', 'Replace')
          }</h2>
          <table>
            <tr>
              <td>
                <input type='file' name='file' autofocus=''/>
                <div class='small'/>
              </td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
  }, fn() {
    let $key := $file[. instance of map(*)] ! map:keys(.)
    return if (not($key)) {
      error((), 'No input specified.')
    } else {
      let $input := if (db:type($name, $resource) = 'xml') {
        fetch:binary-doc($file($key))
      } else {
        $file($key)
      }
      return db:put($name, $input, $resource),
      utils:redirect($dba:SUB, {
        'name': $name, 'resource': $resource, 'info': 'Resource was replaced.'
      })
    }
  })
};
