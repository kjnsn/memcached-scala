package com.memcachedclone

import java.util.concurrent.ConcurrentHashMap

/**
  * Created by kaley on 28/10/16.
  */
class Cache {
  val cacheEntries = new ConcurrentHashMap[String, Seq[Byte]]

  def get(key: String): Option[CacheEntry] = {
    val entry = cacheEntries.get(key)
    if (entry == null)
      return None
    Some(CacheEntry(entry))
  }

  def set(key: String, bs: Seq[Byte]): Unit = {
    cacheEntries.put(key, bs)
  }

  def delete(key: String) = {
    cacheEntries.remove(key)
  }
}

case class CacheEntry(data: Seq[Byte])
