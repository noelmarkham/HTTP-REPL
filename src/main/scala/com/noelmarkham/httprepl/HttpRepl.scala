package com.noelmarkham.httprepl

import java.io.InputStream
import org.http4s.HttpService
import org.http4s.dsl._
import org.http4s.server.blaze.BlazeBuilder

import java.io.PipedInputStream
import java.io.PipedOutputStream
import scala.annotation.tailrec
import scala.sys.process._

object HttpRepl extends App {

  /*
   * The pipes:
   *
   * requestPipeOut -> requestPipeIn -> process -> responsePipeOut -> responsePipeIn
   *
   * That is, process reads from an input stream and writes to an output stream
   * So we need to connect the process's input stream for the process to something we can write to
   * And connect the process's output stream to something we can read from
   */
  val requestPipeIn = new PipedInputStream
  val requestPipeOut = new PipedOutputStream(requestPipeIn)

  val responsePipeOut = new PipedOutputStream
  val responsePipeIn = new PipedInputStream(responsePipeOut)

  val sbt = Process("sbt", List("console-quick")) #< requestPipeIn #> responsePipeOut
  sbt.run

  def consume(inputStream: InputStream): String = {

    @tailrec
    def take(inputStream: InputStream, acc: List[Byte]): List[Byte] = acc match {
      case _ :: '>' :: 'a' :: 'l' :: 'a' :: 'c' :: 's' :: _ => acc
      case _ => take(inputStream, inputStream.read().asInstanceOf[Byte] :: acc)
    }

    new String(take(inputStream, Nil).drop(7).reverse.toArray)
  }

  val route = HttpService {
    case req @ POST -> Root / "repl" =>
      req.decode[String] { body =>

        requestPipeOut.write(body.getBytes, 0, body.length)
        requestPipeOut.write('\n')

        val responseFromSbt = consume(responsePipeIn).drop(body.length()).trim

        Ok(responseFromSbt + "\n")
      }
  }

  println(s"Ready: [${consume(responsePipeIn)}]")

  BlazeBuilder.bindHttp(8080)
    .mountService(route, "/")
    .run
    .awaitShutdown()
}
