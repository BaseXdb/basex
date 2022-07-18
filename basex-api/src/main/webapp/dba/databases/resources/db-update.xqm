(:~
 : Update resource.
 :
 : @author Christian Grün, BaseX Team 2005-22, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace html = 'dba/html' at '../../lib/html.xqm';
import module namespace util = 'dba/util' at '../../lib/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';
(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Form for updating a resource.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $error     error string
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/db-update')
  %rest:query-param('name',     '{$name}')
  %rest:query-param('resource', '{$resource}')
  %rest:query-param('error',    '{$error}')
  %output:method('html')
function dba:db-update(
  $name      as xs:string,
  $resource  as xs:string,
  $error     as xs:string?
) as element(html) {
  html:wrap(map { 'header': ($dba:CAT, $name), 'error': $error },
    <tr>
      <td>
        <form action='db-update' method='post' enctype='multipart/form-data'>
          <input type='hidden' name='name' value='{ $name }'/>
          <input type='hidden' name='resource' value='{ $resource }'/>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            html:link($name, $dba:SUB, map { 'name': $name }), ' » ',
            html:link($resource, $dba:SUB, map { 'name': $name, 'resource': $resource }), ' » ',
            html:button('db-update', 'Update')
          }</h2>
          <table>
            <tr>
              <td>
                <input type='file' name='file'/>
                { html:focus('file') }
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
 : Updates a database resource.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $file      file input
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/db-update')
  %rest:form-param('name',     '{$name}')
  %rest:form-param('resource', '{$resource}')
  %rest:form-param('file',     '{$file}')
function dba:db-update-post(
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
      return db:update($name, $input, $resource),
      util:redirect($dba:SUB, map {
        'name': $name, 'resource': $resource, 'info': 'Resource was updated.'
      })
    )
  } catch * {
    util:redirect('db-update', map {
      'name': $name, 'resource': $resource, 'error': $err:description
    })
  }
};
