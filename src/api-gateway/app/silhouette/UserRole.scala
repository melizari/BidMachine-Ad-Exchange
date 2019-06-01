package silhouette

import enumeratum.values.{StringEnum, StringEnumEntry}

sealed abstract class UserRole(val value: String) extends StringEnumEntry

object UserRole extends StringEnum[UserRole] {

  case object Inactive extends UserRole("inactive")
  case object Admin extends UserRole("admin")
  case object Buyer extends UserRole("buyer")
  case object API extends UserRole("api")
  case object Seller extends UserRole("seller")

  def values = findValues
}
