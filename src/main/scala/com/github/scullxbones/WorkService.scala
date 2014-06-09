package com.github.scullxbones


trait WorkServiceComponent {

  def workService: WorkService

  trait WorkService {
    def doWork(id: String): Unit
  }

}

trait WorkServiceComponentImpl extends WorkServiceComponent {

  def workService = new WorkServiceImpl

  class WorkServiceImpl extends WorkService {
    def doWork(id: String) =
      println(s"Doing some work on id=$id")
  }

}



