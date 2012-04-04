package test.scala.akka.setak
import scala.collection.mutable.HashSet
import scala.collection.mutable.ArrayBuffer

class ScheduleFileReader(traceFile: scala.io.BufferedSource) {

  def getSchedulesLines(result: String): HashSet[HashSet[String]] = {
    var lines = traceFile.getLines()
    var resultLines = new ArrayBuffer[String]()
    var posLines = new HashSet[String]
    var schedules = new HashSet[HashSet[String]]
    var scheduleStarted = false
    var relatedLines = false
    for (line ← lines) {
      if (line.contains("RESULT")) {
        if (line.contains(result)) {
          relatedLines = true
        } else
          relatedLines = false
      }
      if (relatedLines) {

        if (line.startsWith("(")) {
          posLines.add(line)
          scheduleStarted = true
        }
        if (line.startsWith("Schedule") && scheduleStarted) {
          schedules.add(posLines)
          //return schedules
          scheduleStarted = false
          posLines = new HashSet[String]
        }
      }

      schedules.add(posLines)
    }
    return schedules
  }

}