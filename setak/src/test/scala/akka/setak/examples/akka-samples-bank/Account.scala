package com.weiglewilczek.demo.akka
package banking

import akka.transactor.Transactor
import akka.stm.Ref

/**
 * API for Account actor: Messages, exceptions etc.
 */
object Account {

  /** Base message type. */
  sealed trait AccountMessage

  /** Message to ask for the balance. */
  case object GetBalance extends AccountMessage

  /** Message to answer with the balance. */
  case class Balance(vlaue: Int) extends AccountMessage

  /** Message to disposit the given amount. */
  case class Deposit(amount: Int) extends AccountMessage

  /** Message to withdraw the given amount. */
  case class Withdraw(amount: Int) extends AccountMessage

  /** Aborts a withdrawl. */
  class OverdrawException extends RuntimeException
}

/**
 * Actor for a bank account. Receives the following messages:
 * <ul>
 * <li>GetBalance: Replies with Balance</li>
 * <li>Deposit: Increases the balance by the given amount; no reply</li>
 * <li>Withdraw: Decreases the balance by the given amount; no reply</li>
 * </ul>
 */
class Account extends Transactor {
  import Account._

  //log ifDebug "Account created."

  def atomically = {
    case GetBalance       ⇒ self reply Balance(balanceValue)
    case Deposit(amount)  ⇒ deposit(amount)
    case Withdraw(amount) ⇒ withdraw(amount)
  }

  private val balance = Ref(0)

  private def balanceValue = balance getOrElse 0

  private def deposit(amount: Int) {
    balance swap balanceValue + amount
    //log ifInfo "New balance after Deposit(%s) = %s".format(amount, balanceValue)
  }

  private def withdraw(amount: Int) {
    val newValue = balanceValue - amount
    if (newValue < -10) {
      //log ifWarning "Account overdrawn! Aborting by throwing an OverdrawException."
      throw new OverdrawException
    } else {
      balance swap newValue
      //log ifInfo "New balance after Deposit(%s) = %s".format(amount, newValue)
    }
  }
}
