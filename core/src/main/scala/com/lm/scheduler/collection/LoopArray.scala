package com.lm.scheduler.collection

/**
 * @Classname LoopArray
 * @Description TODO
 * @Date 2020/9/21 17:13
 * @Created by limeng
 */
class LoopArray[T](maxCapacity: Int) {

  private val eventQueue: Array[Any] = new Array[Any](maxCapacity)

  def this() = this(32)

  protected[this] var realSize = 0
  private var flag = 0
  private var tail = 0

  private def filledSize:Int = if(tail >= flag) tail - flag else tail + maxCapacity - flag

  def size:Int = filledSize

  def add(event:T): T ={
    var t = null.asInstanceOf[T]
    eventQueue synchronized {
      val index = (tail + 1) % maxCapacity
      if(index == flag){
        flag = (flag + 1) % maxCapacity
        realSize += 1
      }

      t = eventQueue(tail).asInstanceOf[T]
      eventQueue(tail) = event
      tail = index
    }

    t
  }

  def get(index:Int):T = eventQueue synchronized {
    val max = max
    if(index < realSize) throw new IllegalArgumentException("The index " + index + " has already been deleted, now index must be better than " + realSize)

  }

  def min:Int = realSize

  def max:Int = {
    val _size = f
  }


}
