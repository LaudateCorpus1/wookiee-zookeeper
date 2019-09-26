/*
 * Copyright 2015 Webtrends (http://www.webtrends.com)
 *
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webtrends.harness.component.zookeeper.discoverable

import akka.actor.Actor
import akka.util.Timeout
import org.apache.curator.x.discovery.UriSpec

import scala.concurrent.Future

/**
 * @author Michael Cuthbert on 7/9/15.
 */
trait Discoverable {
  this: Actor =>

  import this.context.system
  private lazy val service = DiscoverableService()
  val port = context.system.settings.config.getInt("akka.remote.netty.tcp.port")
  val address = context.system.settings.config.getString("akka.remote.netty.tcp.hostname")

  def queryForNames(basePath:String)(implicit timeout:Timeout) = service.queryForNames(basePath)

  def queryForInstances(basePath:String, name:String, id:Option[String]=None)
                       (implicit timeout:Timeout) = service.queryForInstances(basePath, name, id)

  def makeDiscoverable(basePath:String, id:String, name:String)(implicit timeout:Timeout): Future[Boolean] = {
    makeDiscoverable(basePath, id, name, Some(address), port, new UriSpec(s"akka.tcp://server@$address:$port/$name"))
  }

  def makeDiscoverable(
                        basePath:String,
                        id:String,
                        name:String,
                        address: Option[String],
                        port: Int,
                        uriSpec: UriSpec)(implicit timeout:Timeout): Future[Boolean] = {
    service.makeDiscoverable(basePath, id, name, address, port, uriSpec)
  }

  def getInstances(basePath:String, name:String)(implicit timeout:Timeout) = service.getAllInstances(basePath, name)

  def getInstance(basePath:String, name:String)(implicit timeout:Timeout) = service.getInstance(basePath, name)
  def updateWeight(weight: Int, basePath:String, name:String, id: String, forceSet: Boolean = false)(implicit timeout:Timeout) =
    service.updateWeight(weight, basePath, name, id, forceSet)
}
