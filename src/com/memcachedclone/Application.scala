package com.memcachedclone

import java.io._
import java.net.ServerSocket


/**
  * Created by kaley on 28/10/16.
  */
object Application {
  def main(args: Array[String]): Unit = {


      try {
        val listener = new ServerSocket(9999);
        while (true)
          new CacheServer(listener.accept()).start();
        listener.close()
      }
      catch {
        case e: IOException =>
          System.err.println("Could not listen on port: 9999.");
          System.exit(-1)



    }
  }

}
