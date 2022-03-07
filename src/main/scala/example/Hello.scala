package example

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import swam.runtime.{Engine, Instance}
import swam.text.Compiler
import java.nio.file.Paths
import fs2.io.file.readAll
import fs2.Stream

object Hello extends IOApp {
  import swam.runtime.formats.DefaultFormatters.intFormatter
  import swam.runtime.formats.DefaultFormatters.longFormatter

  def loadCompiledDebugInstance(modulePath: String, binary: Boolean): IO[Instance[IO]] =
    for {
      engine <- Engine[IO]()
      compiler <- Compiler[IO]
      path = Paths.get(modulePath)
      stream = Stream.resource(Blocker[IO]).flatMap { blocker =>
        readAll[IO](path, blocker, 4096)
      }
      module <-
        if (binary) engine.compileBytes(stream)
        else engine.compile(compiler.stream(path, debug = true))
      instance <- module.instantiate
    } yield instance

  val wasmFiboPath = "src/main/resources/fibo/fibo.wat"
  def runWasmFibo(): IO[Unit] =
    for {
      instance <- loadCompiledDebugInstance(wasmFiboPath, binary = false)
      naive <- instance.exports.typed.function1[Long, Long]("naive")
      clever <- instance.exports.typed.function1[Long, Long]("clever")
      n = 13L
      naiveOne <- naive(n)
      cleverOne <- clever(n)
      _ <- IO(println(s"Naive one: ${naiveOne}"))
      _ <- IO(println(s"Clever one: ${cleverOne}"))
    } yield ()

  val rustHelloPath = "src/main/resources/hello-wasm/pkg/hello_wasm_bg.wasm"
  def runRustHello(): IO[Unit] =
    for {
      instance <- loadCompiledDebugInstance(rustHelloPath, binary = true)
      greet <- instance.exports.typed.function0[Int]("greet")
      number <- greet()
      _ <- IO(println(number))
    } yield ()

  def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- runWasmFibo()
      _ <- runRustHello()
    } yield ExitCode.Success

}
