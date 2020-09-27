package com.lm.scheduler.queue

/**
 * @Classname JobInfo
 * @Description TODO
 * @Date 2020/9/25 17:20
 * @Created by limeng
 */
class JobInfo(id: String, output: String, state: String, progress: Float, metric: String){
  def getId:String = id
  def getOutput:String = output
  def getState:String = state
  def getProgress:Float = progress
  def getMetric:String = metric
}
