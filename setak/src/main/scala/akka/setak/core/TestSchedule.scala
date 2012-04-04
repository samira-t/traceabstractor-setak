/**
 * Copyright (C) 2011 Samira Tasharofi
 */
package akka.setak.core

import scala.collection.mutable.HashSet
import scala.collection.mutable.ListBuffer

/**
 * The schedule is a set of partial orders between the test messages.
 * This class is synchronized to make it thread-safe.
 *
 * @author <a href="http://www.cs.illinois.edu/homes/tasharo1">Samira Tasharofi</a>
 *
 */
class TestSchedule {

  private var partialOrders = new HashSet[TestEnvelopSequence]

  def this(partialOrderSet: Set[TestEnvelopSequence]) {
    this()
    for (partialOrder ← partialOrderSet)
      partialOrders.+=(partialOrder)
  }

  def addPartialOrder(partialOrder: TestEnvelopSequence) = synchronized {
    partialOrders.+=(partialOrder)
    println("****" + partialOrders.size)
  }

  /**
   * @return The least index of the message among all the sequences.
   * There are multiple cases for the returned index:
   * 1) the index is zero: the message can be delivered
   * 2) the index is greater than zero: the message should be delivered later
   * 3) the index is -1: the message can be delivered without any constraints
   * (the message is not matched with any messages in the partial orders)
   */

  def isInSchedule(envelop: RealEnvelop): Boolean = synchronized {
    for (partialOrder ← partialOrders) {
      if (partialOrder.getMatchingTestEnvelop(envelop) != null)
        return true
    }
    return false

  }
  //  def leastMatchingIndexOf(envelop: RealEnvelop): Int = synchronized {
  //    var leastIndex = -1
  //    for (partialOrder ← partialOrders) {
  //      val currIndex = partialOrder.matchingIndexOf(envelop)
  //      if (currIndex == 0) return currIndex
  //      else if (currIndex > 0 && leastIndex == -1)
  //        leastIndex = currIndex
  //      else if (currIndex > 0 && leastIndex > -1)
  //        leastIndex = math.min(currIndex, leastIndex)
  //    }
  //    return leastIndex
  //
  //  }

  def isOnlyInHeadOfSchedules(testEnvelop: TestEnvelop): Boolean = synchronized {
    for (partialOrder ← partialOrders) {
      val index = partialOrder.indexOf(testEnvelop)
      if (index > 0) return false
    }
    return true

  }

  def matchingHead(envelop: RealEnvelop): TestEnvelop = synchronized {
    var leastIndex = -1
    for (partialOrder ← partialOrders) {
      val currIndex = partialOrder.matchingIndexOf(envelop)
      if (currIndex == 0) return partialOrder.getMatchingTestEnvelop(envelop)
      else if (currIndex > 0 && leastIndex == -1)
        leastIndex = currIndex
      else if (currIndex > 0 && leastIndex > -1)
        leastIndex = math.min(currIndex, leastIndex)
    }
    return null

  }

  /**
   * It is called by the dispatcher to remove the message that is in the head
   * of partial orders in the schedule (move forward the pointer for the current
   * schedule). It returns once it removes an envelop from the head of one of the
   * partial orders. Therefore, it does not remove the envelop from the head of all
   * partial orders if there are multiple matches.
   */
  //  def removeFromHead(realEnvelop: RealEnvelop): Boolean = synchronized {
  //    var removed: TestEnvelop = null
  //    for (partialOrder ← partialOrders) {
  //      removed = partialOrder.removeFromHead(realEnvelop)
  //      if (removed != null) {
  //        if (partialOrder.isEmpty)
  //          partialOrders.-=(partialOrder)
  //      }
  //    }
  //    return false
  //
  //  }
  def removeFromHeads(testEnvelop: TestEnvelop): Boolean = synchronized {
    var removed = false
    var emptyPOs = new HashSet[TestEnvelopSequence]()
    for (partialOrder ← partialOrders) {
      if (partialOrder.removeFromHead(testEnvelop)) {
        removed = true
        if (partialOrder.isEmpty)
          emptyPOs.add(partialOrder)
      }
    }
    for (emptyPO ← emptyPOs)
      partialOrders.remove(emptyPO)

    return removed

  }

  def isEmpty = synchronized {
    partialOrders.isEmpty
  }

  override def toString(): String = synchronized {
    var outString = "schedule = ["
    for (partialOrder ← partialOrders) {
      outString += partialOrder.toString() + ","
    }
    outString += "]"
    outString
  }

}