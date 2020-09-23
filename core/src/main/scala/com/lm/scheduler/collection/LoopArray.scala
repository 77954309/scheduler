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
    val _max = max
    if(index < realSize) throw new IllegalArgumentException("The index " + index + " has already been deleted, now index must be better than " + realSize)
    else if(index > _max) throw new IllegalArgumentException("The index " + index + " must be less than " + _max)
    val _index = (flag + (index - realSize)) % maxCapacity
    eventQueue(_index).asInstanceOf[T]
  }

  def clear(): Unit ={
    flag = 0
    tail = 0
    realSize = 0
    (0 until maxCapacity).foreach(eventQueue(_) == null)
  }


  def min:Int = realSize

  def max:Int = {
    var _size = filledSize
    if(_size == 0){
      _size = 1
    }
    realSize + _size - 1
  }

  private def filledSize:Int = if(tail >= flag) tail - flag else tail + maxCapacity - flag

  def size:Int = filledSize

  def isFull = filledSize == maxCapacity - 1

  def nonEmpty:Boolean = size > 0

  def toIndexedSeq: IndexedSeq[T] = if(filledSize == 0) IndexedSeq.empty[T] else eventQueue synchronized{(min to max).map(get)}

  def toList = toIndexedSeq.toList
}
class BlockingLoopArray[T](maxCapacity:Int = 32) extends LoopArray[T](maxCapacity){

    private val writeLock = new Array[Byte](0)
    private val readLock = new Array[Byte](0)

    private var takeIndex = 0

    override def add(event: T): T = throw new IllegalAccessException("not supported method!")

  /**
   * 添加一个，如果队列满了，将会一直阻塞，直到队列可用
   * @param event
   * @return
   */
  def put(event:T):Boolean = {
      writeLock synchronized {
        while (isFull) writeLock.wait(1000)
        super.add(event)
      }
      readLock synchronized { readLock.notify() }
      true
   }

  /**
   * 添加一个，如果队列满了，返回false
    * @param event
   * @return
   */
   def offer(event:T):Boolean = if(isFull) false else{
      writeLock synchronized {
        if(isFull) return false
        else super.add(event)
      }
      readLock synchronized{ readLock.notify() }
      true
   }

  /**
   * 获取最新的一个，如果没有，将会一直阻塞，直到有的新的添加进来
   * @return
   */
   def take(): T ={
     readLock synchronized {
       while (waitingSize == 0 || takeIndex > max){
         readLock.wait(1000)
       }
       if(takeIndex < min) takeIndex = min
       val t = get(takeIndex)
       takeIndex += 1
       t
     }
   }

  /**
   * 获取最新的一个，如果没有，将会返回None.
   * 该方法不会移动指针
   * @return
   */
   def peek(): Option[T] = readLock synchronized {
     if(waitingSize == 0 || takeIndex > max) None
     else if(takeIndex < min) Some(get(min))
     else Option(get(takeIndex))
   }

  /**
   * 获取最新的一个，如果没有，将会返回None.
   * 该方法会移动指针
   * @return
   */
  def poll(): Option[T] ={
    if(waitingSize == 0) return None
    val event = readLock synchronized {
      val _min = min
      val _max = max
      if(takeIndex < _min) takeIndex = _min
      else if(takeIndex > _max) return None
      val t = get(takeIndex)
      takeIndex += 1
      Option(t)
    }
    writeLock synchronized {writeLock.notify()}
    event
  }

  override def isFull: Boolean = super.isFull && takeIndex == realSize

  def waitingSize:Int =  if(takeIndex <= realSize) super.size else {
    val length = super.size - takeIndex + realSize
    if(length < 0 ) 0 else length
  }

  override def clear(): Unit = readLock synchronized{
    takeIndex = 0
    super.clear()
  }

  override def toIndexedSeq: IndexedSeq[T] = if(waitingSize == 0) IndexedSeq.empty[T] else readLock synchronized {(takeIndex to max).map(get)}

}
object LoopArray {

  def apply[T](maxCapacity: Int) = new LoopArray[T](maxCapacity)

  def apply[T]() = new LoopArray[T]()
}