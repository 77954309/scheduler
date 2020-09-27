package com.lm.scheduler.queue

/**
 * @Classname LoopArrayQueue
 * @Description TODO
 * @Date 2020/9/25 17:40
 * @Created by limeng
 */
class LoopArrayQueue(var group: Group) extends ConsumeQueue{

  private val eventQueue:Array[Any] = new Array[Any](group.getMaximumCapacity)

  private val maxCapacity:Int = group.getMaximumCapacity

  private val writeLock = new Array[Byte](0)

  private val readLock = new Array[Byte](0)

  private var flag = 0

  private var tail = 0

  private var takeIndex = 0

  protected[this] var realSize = 0

  private def filledSize:Int = if(tail >= flag) tail - flag else tail + maxCapacity - flag

  def toIndexedSeq:IndexedSeq[SchedulerEvent] = if(filledSize == 0) IndexedSeq.empty[SchedulerEvent] else eventQueue synchronized {(min to max).map(x =>get(x).get).filter(x => x != None)}

  def min:Int = realSize

  def max:Int = {
    var _size = filledSize
    if(_size == 0) {
      _size = 1
    }
    realSize + _size - 1
  }

  override def remove(event: SchedulerEvent): Unit = {
    get(event).foreach(x=>x.cancel())
  }

  override def getWaitingEvents: Array[SchedulerEvent] = {
    eventQueue synchronized {
      toIndexedSeq.filter(x => x.getState.equals(SchedulerEventState.Inited)).toArray
    }
  }

  override def size: Int = filledSize

  override def isEmpty: Boolean = size == 0

  override def isFull: Boolean = filledSize == maxCapacity - 1 && takeIndex == realSize

  override def clearAll(): Unit = {
    flag = 0
    tail = 0
    realSize = 0
    (0 until maxCapacity).foreach(eventQueue(_) = null)
  }

  override def get(event: SchedulerEvent): Option[SchedulerEvent] = {
    eventQueue synchronized {
      val eventSeq = toIndexedSeq.filter(f=>f.getId.equals(event.getId)).seq
      if(eventSeq.size > 0) Some(eventSeq(0)) else None
    }
  }

  override def get(index: Int): Option[SchedulerEvent] = {
    var event:SchedulerEvent = null
    eventQueue synchronized {
      val _max =max
      if(index < realSize) throw new IllegalArgumentException("The index " + index + " has already been deleted, now index must be better than " + realSize)
      else if(index > _max) throw new IllegalArgumentException("The index " + index + " must be less than " + _max)
      val _index = (flag + (index - realSize)) % maxCapacity
      event = eventQueue(_index).asInstanceOf[SchedulerEvent]
    }
    Option(event)
  }

  override def getGroup: Group = group

  override def setGroup(group: Group): Unit = {
    this.group=group
  }

  /**
   * 添加一个，如果队列满了，将会一直阻塞，直到队列可用
   * @param event
   * @return
   */
  override def put(event: SchedulerEvent): Int = {
    var index = -1
    writeLock synchronized {
      while (isFull) writeLock.wait(1000)
      index = add(event)
    }
    readLock synchronized( readLock.notify())
    index
  }

  def waitingSize: Int = if(takeIndex <= realSize) size else {
    val length = size - takeIndex + realSize
    if(length < 0) 0 else length
  }

  def add(event:SchedulerEvent):Int={
    eventQueue synchronized {
      val index = (tail + 1) % maxCapacity
      if(index == flag){
        flag = (flag + 1) % maxCapacity
        realSize += 1
      }
      eventQueue(tail) = event
      tail = index
    }
    max
  }

  /**
   * 添加一个，如果队列满了，返回None
   * @param event
   * @return
   */
  override def offer(event: SchedulerEvent): Option[Int] = {
    var index = -1
    writeLock synchronized {
      if(isFull) return None
      else {
        index = add(event)
      }
    }

    readLock synchronized {readLock.notify()}
    Some(index)
  }

  /**
   * 获取某个group最新SchedulerEvent，如果不存在，就一直阻塞
   * @return 该方法会移动指针
   */
  override def take(): SchedulerEvent = {
    val t =  readLock synchronized {
      while (waitingSize == 0 || takeIndex > max){
        readLock.wait(1000)
      }
      if(takeIndex < min) takeIndex = min
      val t = get(takeIndex)
      takeIndex += 1
      t
    }

    writeLock synchronized { writeLock.notify() }
    t.get
  }

  /**
   * 获取某个group 最新的SchedulerEvent ,如果不存在，就阻塞到最大等待时间
   * @param mills
   * @return
   */
  override def take(mills: Long): Option[SchedulerEvent] = {
    val t =readLock synchronized {
      if(waitingSize == 0 || takeIndex > max) readLock.wait(mills)
      if(waitingSize == 0 || takeIndex > max) return None
      if(takeIndex < min) takeIndex = min
      val t = get(takeIndex)
      takeIndex += 1
      t
    }
    writeLock synchronized { writeLock.notify() }
    t
  }

  /**
   * 获取某个group最新的SchedulerEvent,并移动指针到下一个，如果没能，直接返回None
   *
   * @return
   */
  override def poll(): Option[SchedulerEvent] = {
    val event =readLock synchronized {
      val _min = min
      val _max = max
      if(takeIndex < _min) takeIndex = _min
      else if(takeIndex > _max) {
        println(s"none, notice...max: ${_max}, takeIndex: $takeIndex, realSize: $realSize.")
        return None
      }
      val t = get(takeIndex)
      if(t == null) {
        println("null, notice...")
      }
      takeIndex += 1
      t
    }
    writeLock synchronized { writeLock.notify() }
    event
  }

  /**
   * 只获取某个group最新的SchedulerEvent，并不移动指针。如果没有，直接返回None
   * @return
   */
  override def peek(): Option[SchedulerEvent] = {
    if(waitingSize == 0 || takeIndex > max) None
    else if(takeIndex < min) get(min)
    else get(takeIndex)
  }

  /**
   * 获取某个group满足条件的最新的SchedulerEvent，并不移动指针。如果没有，直接返回None
   * @param op
   * @return
   */
  override def peek(op: SchedulerEvent => Boolean): Option[SchedulerEvent] = {
    if(waitingSize == 0 || takeIndex > max) None
    else if(takeIndex < min) {
      val event = get(min)
      if(op(event.get)) event else None
    }
    else {
      val event = get(takeIndex)
      if(op(event.get)) event else None
    }
  }

}
