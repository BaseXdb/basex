(:~
 : Add new pattern.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace _ = 'dba/users';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $_:CAT := 'users';
(:~ Sub category :)
declare variable $_:SUB := 'user';

(:~
 : Form for adding a new pattern.
 : @param  $name     user name
 : @param  $pattern  entered pattern
 : @param  $perm     chosen permission
 : @param  $error    error string
 :)
declare
  %rest:GET
  %rest:path("/dba/add-pattern")
  %rest:query-param("name",    "{$name}")
  %rest:query-param("pattern", "{$pattern}")
  %rest:query-param("perm",    "{$perm}", "write")
  %rest:query-param("error",   "{$error}")
  %output:method("html")
function _:create(
  $name     as xs:string,
  $pattern  as xs:string?,
  $perm     as xs:string,
  $error    as xs:string?
) as element() {
  cons:check(),
  tmpl:wrap(map { 'top': $_:CAT, 'error': $error },
    <tr>
      <td>
        <form action="add-pattern" method="post" autocomplete="off">
          <h2>
            <a href="{ $_:CAT }">Users</a> »
            { html:link($name, $_:SUB, map { 'name': $name } ) } »
            { html:button('create', 'Add Pattern') }
          </h2>
          <input type="hidden" name="name" value="{ $name }"/>
          <table>
            <tr>
              <td>Pattern:</td>
              <td>
                <input type="text" name="pattern" value="{ $pattern }" id="pattern"/>
                { html:focus('pattern') } &#xa0;
                <span class='note'>…support for <a target='_blank' href='http://docs.basex.org/wiki/Commands#Glob_Syntax'>glob syntax</a>.</span>
                <div class='small'/>
              </td>
            </tr>
            <tr>
              <td>Permission:</td>
              <td>
                <select name="perm" size="3">{
                  for $p in $cons:PERMISSIONS[position() = 1 to 3]
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
 : @param  $name     user
 : @param  $pattern  pattern
 : @param  $perm     permission
 :)
declare
  %updating
  %rest:POST
  %rest:path("/dba/add-pattern")
  %rest:query-param("name",    "{$name}")
  %rest:query-param("perm",    "{$perm}")
  %rest:query-param("pattern", "{$pattern}")
function _:create(
  $name     as xs:string,
  $perm     as xs:string,
  $pattern  as xs:string
) {
  cons:check(),
  try {
    util:update("user:grant($name, $perm, $pattern)", map {
      'name':    $name,
      'perm':    $perm,
      'pattern': $pattern
    }),
    db:output(web:redirect($_:SUB, map {
      'info': 'Created Pattern: ' || $pattern,
      'name': $name
    }))
  } catch * {
    db:output(web:redirect("add-pattern", map {
      'error': $err:description,
      'name': $name,
      'perm': $perm,
      'pattern': $pattern
    }))
  }
};
