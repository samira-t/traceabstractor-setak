package akka.setak
import scala.collection.mutable.HashSet
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap

class ScheduleFileReader(traceFile: scala.io.BufferedSource) {

  def getSchedulesLines(result: String): HashMap[Int, HashSet[String]] = {
    var lines = traceFile.getLines()
    var resultLines = new ArrayBuffer[String]()
    var posLines = new HashSet[String]
    var schedules = new HashMap[Int, HashSet[String]]
    var scheduleStarted = false
    var relatedLines = false
    var lineNumber = 0
    for (line ← lines) {

      lineNumber += 1
      if (line.contains("RESULT")) {
        if (line.contains(result)) {
          relatedLines = true
        } else
          relatedLines = false
      }
      if (relatedLines) {

        if (line.startsWith("(")) {
          posLines.add(line)
          //println(posLines.size + "::" + line)
          scheduleStarted = true
        }
        if (line.startsWith("Schedule") && scheduleStarted) {
          schedules.put(lineNumber, posLines)

          //return schedules
          scheduleStarted = false
          posLines = new HashSet[String]
        }
      }

    }
    schedules.put(lineNumber, posLines)
    return schedules
  }

}