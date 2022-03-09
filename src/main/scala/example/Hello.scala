package example

import cats.data.OptionT
import cats.effect.{Blocker, ExitCode, IO, IOApp}
import swam.runtime.{Engine, Instance, Memory, Value}
import swam.text.Compiler
import java.nio.file.Paths
import fs2.io.file.readAll
import fs2.Stream
import swam.runtime.Value.Int32
import swam.{MemType, ValType}
import swam.runtime.formats.ValueWriter
import java.nio.ByteBuffer
import scala.annotation.unused

object Hello extends IOApp {
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
  def runWasmFibo(): IO[Unit] = {
    import swam.runtime.formats.DefaultFormatters.longFormatter

    for {
      instance <- loadCompiledDebugInstance(wasmFiboPath, binary = false)
      naive <- instance.exports.typed.function1[Long, Long]("naive")
      n = 13L
      result <- naive(n)
      _ <- IO(println(s"- WASM returned fibonacci calculation for $n, result: $result"))
    } yield ()
  }

  val rustHelloPath = "src/main/resources/hello-wasm/pkg/hello_wasm_bg.wasm"
  def runRustHello(): IO[Unit] = {
    import swam.runtime.formats.DefaultFormatters.intFormatter
    import swam.runtime.formats.string.utf8

    // This isn't being used in this experiment, but theoretically this would be the way to pass complex parameters
    @unused def stringWriter(address: Int): ValueWriter[IO, String] = new ValueWriter[IO, String] {
      override def write(text: String, m: Option[Memory[IO]]): IO[Value] =
        (for {
          mem <- OptionT.fromOption[IO](m)
          textBuffer = ByteBuffer.allocate(4 + text.length)
          _ = textBuffer.putInt(text.length)
          _ = textBuffer.put(text.toArray.map(_.toByte))
          _ <- OptionT[IO, Unit](mem.writeBytes(address, textBuffer).map(Some(_)))
        } yield Int32(address)).value.map(_.get)

      override val swamType: ValType = ValType.I32
    }

    for {
      instance <- loadCompiledDebugInstance(rustHelloPath, binary = true)
      getBufferAddress <- instance.exports.typed.function0[Int]("get_buffer")
      bufferAddress <- getBufferAddress()
      hexAddress = f"0x${bufferAddress.toHexString.toUpperCase()}"
      _ <- IO(println(s"- Rust returned a buffer at address: $hexAddress"))

      fillBufferByRust <- instance.exports.typed.procedure0("fill_buffer")
      _ <- fillBufferByRust()
      getBufferData <- instance.exports.typed.function0[String]("get_buffer")
      bufferData <- getBufferData()
      _ <- IO(println(s"- Rust filled and returned a buffer with contents: '$bufferData'"))

      scalaText = "Ž! Once again a UTF-8 symbol with a latvian diacritical mark."
      memoryField = instance.exports.list.collect { case (name, _: MemType) => name }.head
      mem <- instance.exports.memory(memoryField)

      scalaTextBuffer = ByteBuffer.allocate(4 + scalaText.length)
      _ = scalaTextBuffer.putInt(scalaText.length)
      _ = scalaTextBuffer.put(scalaText.toArray.map(_.toByte))
      _ <- mem.writeBytes(bufferAddress, scalaTextBuffer)
      _ <- IO(println(s"- Scala filled the buffer $hexAddress in memory '$memoryField' with contents: '$scalaText'"))

      parseAndReverseBufferByRust <- instance.exports.typed.procedure0("parse_and_reverse_buffer")
      _ <- parseAndReverseBufferByRust()
      reversedBufferData <- getBufferData()
      _ <- IO(println(s"- Rust parsed and reversed buffer contents: '$reversedBufferData'"))
    } yield ()
  }

  def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- IO(println("1. Compiling plaintext wasm and running it"))
      _ <- runWasmFibo()
      _ <- IO(println("2. Running Rust compiled to binary WASM"))
      _ <- runRustHello()
    } yield ExitCode.Success

}
