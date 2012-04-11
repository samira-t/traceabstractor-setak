package test.scala.akka.setak
import scala.collection.mutable.HashSet
import akka.setak.core.TestEnvelopSequence
import akka.setak.core.TestEnvelop
import akka.setak.SetakTest
import scala.collection.mutable.HashMap

class ScheduleGenerator(mapper: EnvelopMapper) {

  def getSchedule(lines: HashSet[String])(implicit test: SetakTest) = {
    var pos = new HashSet[TestEnvelopSequence]()
    var definedEnvelops = new HashMap[String, TestEnvelop]

    var curLine = ""
    for (line ← lines) {
      curLine = line.replace("(_)", "_");
      var sequence: TestEnvelopSequence = null
      var recStr = ""
      while (curLine.indexOf("(") >= 0) {
        val event = curLine.substring(curLine.indexOf("(") + 1, curLine.indexOf(")"))
        //println("event " + event)
        if (!event.contains("!")) {
          var envelop: TestEnvelop = null
          if (!definedEnvelops.contains(event)) {
            //println("event " + event)
            val eventParts = event.split(",")
            recStr = eventParts(1)
            val sender = mapper.getActorRef(eventParts(0))
            val receiver = mapper.getActorRef(eventParts(1))
            if (sender != null && receiver != null) {
              if (eventParts(2).contains("_")) {
                envelop = test.testEnvelopPattern(sender, receiver, mapper.getPFContent(eventParts(2)))
              } else {
                envelop = test.testEnvelop(sender, receiver, mapper.getContent(eventParts(2)))
              }
              definedEnvelops.put(event, envelop)
            }
          } else {
            envelop = definedEnvelops.get(event).get

          }
          //if (recStr.equals("2")) println(curLine)
          if (envelop != null) {
            if (sequence == null) sequence = envelop
            else {
              if (curLine.startsWith("->")) {
                sequence ->= envelop
                //if (recStr.equals("2")) println(sequence)
              } else if (curLine.startsWith(",(")) {
                sequence.tails.add(envelop)
                //if (recStr.equals("2")) println(sequence)
              } else println("error")
            }
          }
        }
        curLine = curLine.substring(curLine.indexOf(")") + 1)

      }
      //if (recStr.equals("2")) println(sequence)
      pos.add(sequence)

    }
    pos
  }

}