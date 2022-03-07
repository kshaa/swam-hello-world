package example

import cats.effect.{ExitCode, IO, IOApp}
import swam.runtime.Engine
import swam.text.Compiler
import java.nio.file.Paths

object Hello extends IOApp {
  import swam.runtime.formats.DefaultFormatters.longFormatter

  def run(args: List[String]): IO[ExitCode] =
    for {
      engine <- Engine[IO]()
      compiler <- Compiler[IO]
      cwd = new java.io.File(".").getCanonicalPath
      _ = {println(cwd)}
      module <- engine.compile(compiler.stream(Paths.get("src/main/resources/fibo/fibo.wat"), debug = true))
      instance <- module.instantiate
      naive <- instance.exports.typed.function1[Long, Long]("naive")
      clever <- instance.exports.typed.function1[Long, Long]("clever")
      n = 13L
      naiveOne <- naive(n)
      cleverOne <- clever(n)
      _ <- IO(println(s"Naive one: ${naiveOne}"))
      _ <- IO(println(s"Clever one: ${cleverOne}"))
    } yield ExitCode.Success

}
