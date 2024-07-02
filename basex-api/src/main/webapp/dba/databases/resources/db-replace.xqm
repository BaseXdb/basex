(:~
 : Replace resource.
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
 : Form for putting replacing a resource.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $error     error string
 : @return page
 :)
declare
  %rest:GET
  %rest:POST
  %rest:path('/dba/db-replace')
  %rest:query-param('name',     '{$name}')
  %rest:query-param('resource', '{$resource}')
  %rest:query-param('error',    '{$error}')
  %output:method('html')
  %output:html-version('5')
function dba:db-replace(
  $name      as xs:string,
  $resource  as xs:string,
  $error     as xs:string?
) as element(html) {
  html:wrap({ 'header': ($dba:CAT, $name), 'error': $error },
    <tr>
      <td>
        <form method='post' enctype='multipart/form-data'>
          <input type='hidden' name='name' value='{ $name }'/>
          <input type='hidden' name='resource' value='{ $resource }'/>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            html:link($name, $dba:SUB, { 'name': $name }), ' » ',
            html:link($resource, $dba:SUB, { 'name': $name, 'resource': $resource }), ' » ',
            html:button('db-replace-do', 'Replace')
          }</h2>
          <table>
            <tr>
              <td>
                <input type='file' name='file' autofocus='autofocus'/>
                <div class='small'/>
              </td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
  )
};

(:~
 : Replaces a database resource.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $file      file input
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/db-replace-do')
  %rest:form-param('name',     '{$name}')
  %rest:form-param('resource', '{$resource}')
  %rest:form-param('file',     '{$file}')
function dba:db-replace-do(
  $name      as xs:string,
  $resource  as xs:string,
  $file      as map(*)?
) as empty-sequence() {
  try {
    let $key := map:keys($file)
    return if($key = '') then (
      error((), 'No input specified.')
    ) else (
      let $input := if(db:type($name, $resource) = 'xml') then (
        fetch:binary-doc($file($key))
      ) else (
        $file($key)
      )
      return db:put($name, $input, $resource),
      utils:redirect($dba:SUB, {
        'name': $name, 'resource': $resource, 'info': 'Resource was replaced.'
      })
    )
  } catch * {
    utils:redirect('db-replace', {
      'name': $name, 'resource': $resource, 'error': $err:description
    })
  }
};
