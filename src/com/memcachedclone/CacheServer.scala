package com.memcachedclone

import java.io._
import java.net.{Socket,SocketException}

case class CacheServer(socket: Socket) extends Thread("ServerThread") {
  val cache = new Cache()

  override def run(): Unit = {
    try {
      val out = new DataOutputStream(socket.getOutputStream)
      val in = new BufferedReader(new InputStreamReader(socket.getInputStream))

      while (socket.isConnected) {

        // Read in the command
        var line = in.readLine()
        if (line == null)
          line = ""
        var tokens = Seq[String]()
        var currToken = ""
        line.foreach(ch => {
          if (ch == ' ') {
            if (currToken != "")
              tokens ++= Seq[String](currToken)
            currToken = ""
          } else {
            currToken += ch
          }
        })
        if (currToken != "")
          tokens ++= Seq[String](currToken)

        // Process the command
        if (tokens.nonEmpty) {
          val rest = tokens.slice(1, tokens.length)

          tokens.head match {
            case "get" =>
              // Rest of the tokens are keys
              rest.foreach(key => {
                val data = cache.get(key)
                data match {
                  case Some(bytes) =>
                    out.writeBytes(s"VALUE $key 0 ${bytes.data.length}\r\n")
                    out.write(bytes.data.toArray)
                    out.writeBytes("\r\n")
                  case None =>
                    // Do nothing for miss
                }
              })
              out.writeBytes("END\r\n")
              out.flush()
            case "set" =>
              // Validate command
              if (5 < rest.length || rest.length > 6) {
                out.writeBytes("CLIENT_ERROR bad command line format\r\n")
              } else {
                val key = rest.head
                //val flags = rest(1)
                //val exptime = rest(2)
                val size = rest(3).toInt

                // Read up until the number of bytes
                var buffer = Seq[Byte]()
                var read = 0
                while (read < size) {
                  val data = in.readLine.getBytes.toSeq
                  read += data.length
                  buffer ++= data
                }
                // Set the data
                cache.set(key, buffer)

                // Reply
                if (!rest.contains("noreply")) {
                  out.writeBytes("STORED\r\n")
                  out.flush()
                }
              }
            case "delete" =>
              if (rest.isEmpty) {
                out.writeBytes("CLIENT_ERROR bad command line format\r\n")
                out.flush()
              } else {
                val key = rest.head
                cache.delete(key)
                if (!rest.contains("noreply")) {
                  out.writeBytes("DELETED\r\n")
                  out.flush()
                }
              }
            case default =>
              out.writeBytes("ERROR\r\n")
              out.flush()
          }
        } else {
          out.writeBytes("ERROR\r\n")
          out.flush()
        }

      }
      out.close()
      in.close()
      socket.close()
    }
    catch {
      case e: EOFException =>
        ()
      case e: SocketException =>
        () // avoid stack trace when stopping a client with Ctrl-C
      case e: IOException =>
        e.printStackTrace()
    }
  }

}