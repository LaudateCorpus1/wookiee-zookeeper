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
package com.webtrends.harness.component.zookeeper

import com.webtrends.harness.utils.ConfigUtil
import org.apache.curator.framework.imps.CuratorFrameworkState
import org.apache.curator.test.TestingServer
import com.typesafe.config.{Config, ConfigFactory}
import com.webtrends.harness.component.zookeeper.config.ZookeeperSettings
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class CuratorSpec extends WordSpecLike with Matchers with BeforeAndAfterAll {
  val zkServer = new TestingServer()

  "The curator object" should {
    val settings: ZookeeperSettings = ZookeeperSettings(loadConfig)

    "only utilize a single instance of the curator object" in {
      val cur = Curator(settings)
      cur.start(None)

      val cur2 = Curator(settings)
      cur2.start(None)

      val res = cur shouldBe cur2
      cur.stop()
      res
    }

    "allow for lazy startup and shutdown" in {
      val cur = Curator(settings)
      cur.createClient
      cur.client.getState shouldBe CuratorFrameworkState.LATENT

      cur.start(None)
      val res = cur.client.getState shouldBe CuratorFrameworkState.STARTED
      cur.stop()
      res
    }
  }

  override protected def afterAll(): Unit = zkServer.close()

  def loadConfig: Config = {
    val c = ConfigFactory.parseString("""
      wookiee-zookeeper {
        datacenter = "lab"
        pod = "H"
        quorum = "%s"
        session-timeout = 30
        connection-timeout = 30
        retry-sleep = 5
        retry-count = 150
        base-path = "/discovery/clusters"
      } """.format(zkServer.getConnectString)
    ).withFallback(ConfigFactory.load("reference.conf")).resolve
    ConfigUtil.prepareSubConfig(c, "wookiee-zookeeper")
  }
}
