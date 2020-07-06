package finder

import cats.effect.ExitCode
import cats.implicits._
import monix.eval.{Task, TaskApp}

object Main extends TaskApp {
  import impl._


  def run(args: List[String]): Task[ExitCode] =
    Program.runAndBenchmark[Task].as(ExitCode.Success)
}
