(:~
 : Add new pattern.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/users';

import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace options = 'dba/options' at '../modules/options.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'users';
(:~ Sub category :)
declare variable $dba:SUB := 'user';

(:~
 : Form for adding a new pattern.
 : @param  $name     user name
 : @param  $pattern  entered pattern
 : @param  $perm     chosen permission
 : @param  $error    error string
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/add-pattern")
  %rest:query-param("name",    "{$name}")
  %rest:query-param("pattern", "{$pattern}")
  %rest:query-param("perm",    "{$perm}", "write")
  %rest:query-param("error",   "{$error}")
  %output:method("html")
function dba:pattern-add(
  $name     as xs:string,
  $pattern  as xs:string?,
  $perm     as xs:string,
  $error    as xs:string?
) as element(html) {
  html:wrap(map { 'header': ($dba:CAT, $name), 'error': $error },
    <tr>
      <td>
        <form action="pattern-add" method="post" autocomplete="off">
          <h2>{
            html:link('Users', $dba:CAT), ' » ',
            html:link($name, $dba:SUB, map { 'name': $name }), ' » ',
            html:button('create', 'Add Pattern')
          }</h2>
          <input type="hidden" name="name" value="{ $name }"/>
          <table>
            <tr>
              <td>Pattern:</td>
              <td>
                <input type="text" name="pattern" value="{ $pattern }" id="pattern"/>
                { html:focus('pattern') } &#xa0;
                <span class='note'>…support for <a target='_blank'
                  href='http://docs.basex.org/wiki/Commands#Glob_Syntax'>glob syntax</a>.</span>
                <div class='small'/>
              </td>
            </tr>
            <tr>
              <td>Permission:</td>
              <td>
                <select name="perm" size="3">{
                  for $p in $options:PERMISSIONS[position() = 1 to 3]
                  return element option { attribute selected { }[$p = $perm], $p }
                }</select>
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
 : Creates a pattern.
 : @param  $name     user name
 : @param  $perm     permission
 : @param  $pattern  pattern
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path("/dba/pattern-add")
  %rest:query-param("name",    "{$name}")
  %rest:query-param("perm",    "{$perm}")
  %rest:query-param("pattern", "{$pattern}")
function dba:create(
  $name     as xs:string,
  $perm     as xs:string,
  $pattern  as xs:string
) as empty-sequence() {
  try {
    user:grant($name, $perm, $pattern),
    util:redirect($dba:SUB, map { 'name': $name, 'info': 'Pattern was created.' })
  } catch * {
    util:redirect('pattern-add', map {
      'name': $name, 'perm': $perm, 'pattern': $pattern, 'error': $err:description
    })
  }
};
