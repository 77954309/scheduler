package com.lm.scheduler.collection

/**
 * @Classname TestLoopArray
 * @Description TODO
 * @Date 2020/9/22 19:12
 * @Created by limeng
 */
object TestLoopArray {
  def main(args: Array[String]): Unit = {
    val loop = new BlockingLoopArray[Int](10)
    loop.put(1)
    loop.put(2)
    loop.put(3)
    loop.put(4)
    loop.put(5)



  }
}
