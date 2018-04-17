package example

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.sqs.scaladsl.{SqsAckSink, SqsSink, SqsSource}
import akka.stream.alpakka.sqs.{FifoQueue, MessageAction, SqsSourceSettings}
import akka.stream.scaladsl.Source
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.sqs.model.{Message, SendMessageRequest}
import com.amazonaws.services.sqs.{AmazonSQSAsync, AmazonSQSAsyncClientBuilder}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.StdIn

object Producer extends App {
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  var counter = 1

  val accessKey = System.getenv("AWS_ACCESS_KEY")
  val secretKey = System.getenv("AWS_SECRET_KEY")
  val sqsEndPoint = System.getenv("AWS_SQS_ENDPOINT")
  val queueUrl = System.getenv("AWS_QUEUE_URL")
  val region = System.getenv("AWS_REGION")
  val credentials = new BasicAWSCredentials(accessKey, secretKey)
  val credentialsProvider = new AWSStaticCredentialsProvider(credentials)

  implicit val client: AmazonSQSAsync = mkSQSAsyncClient(sqsEndPoint, region, credentialsProvider)
  val sqsSourceSettings = SqsSourceSettings.Defaults

//  val consumer = mkConsumer(queueUrl, sqsSourceSettings)

  val initialDelay = 0 seconds
  val interval = 5 seconds

  val continuousProducer = mkContinuousProducer(initialDelay, interval, queueUrl)

  // halts after 30 secs
//  Await.ready(continuousProducer, 120.second)

  StdIn.readLine

  // Helpers
  private def mkSQSAsyncClient(sqsEndpoint: String, region: String, credentialsProvider: AWSStaticCredentialsProvider) = {
    AmazonSQSAsyncClientBuilder
      .standard()
      .withCredentials(credentialsProvider)
      .withEndpointConfiguration(new EndpointConfiguration(sqsEndpoint, region))
      .build()
  }

//  private def mkConsumer(queueUrl: String, sqsSourceSettings: SqsSourceSettings) = {
//    SqsSource(queueUrl, sqsSourceSettings)
//      .map { m: Message =>
//        println(s"::::: MESSAGE RECEIVED")
//        println(s"$m")
//        println(s"::::: _______________")
//        (m, MessageAction.Delete)
//      }
//      .runWith(SqsAckSink(queueUrl))
//      .onComplete(e => println(s":::: RESULT $e"))
//  }

  private def mkContinuousProducer(initialDelay: FiniteDuration, interval: FiniteDuration, queueUrl: String) = {
    val testMessage = "TEST MESSAGE - OMAR"

//    Source.tick(initialDelay, interval, pushMsg(testMessage))
//      .runWith(SqsSink.messageSink(queueUrl))

    while (true) {
      Source.single(pushMsg(testMessage)).runWith(SqsSink.messageSink(queueUrl))
      Thread.sleep(2000)
    }
  }

  // SendMessageRequest groups messages into batches if it can
  private def pushMsg(msg: String): SendMessageRequest = {
    val now = System.currentTimeMillis()
    val body = s"$counter $msg at $now"
    val f = new SendMessageRequest()
      .withMessageBody(body)
      .withMessageGroupId("blaa")
      .withMessageDeduplicationId(UUID.randomUUID.toString)
    println(s"::::: MESSAGE PRODUCED")
    println(s"$body")
    println(s"::::: _______________")
    counter = counter + 1
    f
  }
}


object Consumer extends App {
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  val accessKey = System.getenv("AWS_ACCESS_KEY")
  val secretKey = System.getenv("AWS_SECRET_KEY")
  val sqsEndPoint = System.getenv("AWS_SQS_ENDPOINT")
  val queueUrl = System.getenv("AWS_QUEUE_URL")
  val region = System.getenv("AWS_REGION")
  val credentials = new BasicAWSCredentials(accessKey, secretKey)
  val credentialsProvider = new AWSStaticCredentialsProvider(credentials)

  implicit val client: AmazonSQSAsync = mkSQSAsyncClient(sqsEndPoint, region, credentialsProvider)
  val sqsSourceSettings = SqsSourceSettings.Defaults

  val consumer = mkConsumer(queueUrl, sqsSourceSettings)

//  val initialDelay = 0 seconds
//  val interval = 1 second
//
//  val continuousProducer = mkContinuousProducer(initialDelay, interval, queueUrl)
//
//  // halts after 30 secs
//  Await.ready(continuousProducer, 30.second)

  StdIn.readLine

  // Helpers
  private def mkSQSAsyncClient(sqsEndpoint: String, region: String, credentialsProvider: AWSStaticCredentialsProvider) = {
    AmazonSQSAsyncClientBuilder
      .standard()
      .withCredentials(credentialsProvider)
      .withEndpointConfiguration(new EndpointConfiguration(sqsEndpoint, region))
      .build()
  }

  private def mkConsumer(queueUrl: String, sqsSourceSettings: SqsSourceSettings) = {
    SqsSource(queueUrl, sqsSourceSettings)
      .map { m: Message =>
        println(s"::::: MESSAGE RECEIVED")
        println(s"$m")
        println(s"::::: _______________")
        (m, MessageAction.Delete)
      }
      .runWith(SqsAckSink(queueUrl))
      .onComplete(e => println(s":::: RESULT $e"))
  }

//  private def mkContinuousProducer(initialDelay: FiniteDuration, interval: FiniteDuration, queueUrl: String) = {
//    val testMessage = "TEST MESSAGE - OMAR"
//
//    Source.tick(initialDelay, interval, pushMsg(testMessage))
//      .runWith(SqsSink.messageSink(queueUrl))
//  }
//
//  // SendMessageRequest groups messages into batches if it can
//  private def pushMsg(msg: String): SendMessageRequest = {
//    val now = System.currentTimeMillis()
//    new SendMessageRequest().withMessageBody(s"$msg at $now")
//  }
}
