package scala.scalanative
package libc

import scalanative.unsafe._

@extern
object unistd {

  def sysconf(name: CInt): CLong = extern

  // Macros

  @name("scalanative_libc_sc_nprocessors_conf")
  def SC_NPROCESSORS_CONF: CInt = extern
}
