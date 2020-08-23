package fr.ps.eng.kafka.app4s.common

import com.typesafe.config.{Config, ConfigFactory}
import fr.ps.eng.kafka.app4s.DemoTestProvider
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HelperFunctionsSpec extends AnyFlatSpec
  with Matchers
  with GivenWhenThen
  with DemoTestProvider
  with HelperSerdes
  with HelperFunctions {

  val configFile: String = s"""
    |config1 = true
    |config2 = 42
    |config3 = 24 hours
    |config4 = {
    | config41 = "bla"
    | config42 = "di"
    | config43 = "bla"
    |}
    |config5 = ["foo", "bar", "mut", "mut"]
    |config6 = {
    | config7 {
    |   config71 = localhost
    |   config71 = $${?FAKE_VARIABLE}
    |   config72 = 80
    | }
    |}
    |""".stripMargin


  val config: Config = ConfigFactory.parseString(configFile).resolve()

  "configMapperOps#toMap" should "convert a config into a map" in {
    Given("a typesafe config object")
    When("when the typesafe configuration is converted to map")
    val result = config.toMap

    Then("the configuration can b")
    result.get("config1") should contain(true)
    result.get("config2") should contain(42)
    result.get("config3") should contain("24 hours")
    result.get("config4") shouldBe empty
    result.get("config4.config41") should contain("bla")
    result.get("config4.config42") should contain("di")
    result.get("config4.config43") should contain("bla")
    result.get("config5") should contain
    theSameElementsInOrderAs(Array("foo", "bar", "mut", "mut"))
    result.get("config6") shouldBe empty
    result.get("config6.config7") shouldBe empty
    result.get("config6.config7") shouldBe empty
    result.get("config6.config7.config71") should contain("localhost")
    result.get("config6.config7.config72") should contain(80)
  }


  "configMapperOps#toProperties" should "convert a config into a property" in {
    Given("a typesafe config object")
    When("when the typesafe configuration is converted to property")
    val result = config.toProperties

    Then("the configuration can ")
    result.get("config1") shouldBe true
    result.get("config2") shouldBe 42
    result.get("config3") shouldBe "24 hours"
    result.get("config4") should be (null)
    result.get("config4.config41") shouldBe "bla"
    result.get("config4.config42") shouldBe "di"
    result.get("config4.config43") shouldBe "bla"
    result.get("config5") should contain
    theSameElementsInOrderAs(Array("foo", "bar", "mut", "mut"))
    result.get("config6") should be (null)
    result.get("config6.config7") should be (null)
    result.get("config6.config7") should be (null)
    result.get("config6.config7.config71") shouldBe "localhost"
    result.get("config6.config7.config72") shouldBe 80
  }
}
