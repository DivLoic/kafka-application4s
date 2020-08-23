package fr.ps.eng.kafka.app4s.common

import java.time.LocalDate

import com.sksamuel.avro4s.RecordFormat
import fr.ps.eng.kafka.app4s.DemoTestProvider
import org.apache.kafka.common.header.Header
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.serialization.{Deserializer, Serializer}
import org.scalacheck.Gen
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters._
import scala.util.{Success, Try}

class HelperSerdesSpec extends AnyFlatSpec
  with Matchers
  with GivenWhenThen
  with DemoTestProvider
  with HelperSerdes {

  val testTopic = "TEST-TOPIC"
  val testHeaders = new RecordHeaders(Array.empty[Header])
  val testSerdeConf = Map("schema.registry.url" -> "mock://notused/")

  "reflectionAvroSerializer4S" should "create a working serializer" in {
    Given("a scala case class")
    case class TestClassFoo(a: String, b: Int, c: Boolean)

    And("its corresponding avro4s formatter")
    implicit val formatter: RecordFormat[TestClassFoo] = RecordFormat[TestClassFoo]

    And("arbitrary instances of the given case class")
    val instances: List[TestClassFoo] = Gen
      .listOfN(sampleSize, Gen.resultOf(TestClassFoo))
      .pureApply(parameter, seed, retries)

    When("the instances are serialized")
    val serializer: Serializer[TestClassFoo] = reflectionAvroSerializer4S[TestClassFoo]
    serializer.configure(testSerdeConf.asJava, false)

    val result = instances.map(serializer.serialize(testTopic, testHeaders, _))

    Then("byte arrays are successfully generated")
    result.foreach(datum => datum should not be empty)
  }

  it should "handle null values" in {
    Given("a scala case class")
    case class TestClassBaz(a: String, b: Double, c: LocalDate)

    And("its corresponding avro4s formatter")
    implicit val formatter: RecordFormat[TestClassBaz] = RecordFormat[TestClassBaz]

    And("arbitrary instances of the given case class")
    val serializer: Serializer[TestClassBaz] = reflectionAvroSerializer4S[TestClassBaz]
    serializer.configure(testSerdeConf.asJava, false)

    When("a null value instance is serialized")
    val nullRecord: TestClassBaz = null
    val result: Array[Byte] = serializer.serialize(testTopic, testHeaders, nullRecord)

    Then("an empty byte array is returned")
    result shouldBe empty
  }

  "reflectionAvroDeserializer4S" should "create a working deserializer" in {
    Given("a scala case class")
    case class TestClassBar(a: String, b: Double, c: LocalDate)

    And("its corresponding avro4s formatter")
    implicit val formatter: RecordFormat[TestClassBar] = RecordFormat[TestClassBar]
    val serializer: Serializer[TestClassBar] = reflectionAvroSerializer4S[TestClassBar]
    serializer.configure(testSerdeConf.asJava, false)

    And("a collection of instances serialized")
    val instances = Gen
      .listOfN(sampleSize, Gen.resultOf(TestClassBar))
      .pureApply(parameter, seed, retries)
      .map(serializer.serialize(testTopic, testHeaders, _))

    When("the collection elements are deserialized")
    val deserializer: Deserializer[TestClassBar] = reflectionAvroDeserializer4S[TestClassBar]
    deserializer.configure(testSerdeConf.asJava, false)

    val result: List[Try[TestClassBar]] = instances
      .map(instance => Try(deserializer.deserialize(testTopic, instance)))

    Then("scala instances are successfully generated")
    result.foreach(datum => datum shouldBe a[Success[_]])
    result.map(_.get.c).foreach(datum => datum.isBefore(LocalDate.MAX))
  }

  it should "handle empty arrays" in {
    Given("a scala case class")
    case class TestClassBaz(a: String, b: Double, c: LocalDate)

    And("its corresponding avro4s formatter")
    implicit val formatter: RecordFormat[TestClassBaz] = RecordFormat[TestClassBaz]

    And("its corresponding deserializer")
    val deserializer: Deserializer[TestClassBaz] = reflectionAvroDeserializer4S[TestClassBaz]
    deserializer.configure(testSerdeConf.asJava, false)

    When("an empty byte array is deserialized")
    val result: TestClassBaz = deserializer.deserialize(testTopic, Array.empty[Byte])

    Then("an null value is returned")
    result should be (null)
  }
}
