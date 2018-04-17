package example

import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import akka.stream.alpakka.sqs.SqsSourceSettings
import akka.stream.alpakka.sqs.scaladsl.SqsSink
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, ThrottleMode}
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.SendMessageRequest

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.StdIn

object Producer extends App {
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

  val interval = 2 seconds
  val messageGroupId = "Group"
  val baseMsg = "TEST MESSAGE - OMAR"

  val continuousProducer = mkContinuousProducer(interval, queueUrl)

  StdIn.readLine

  private def mkContinuousProducer(interval: FiniteDuration, queueUrl: String): Future[Done] = {
    Source(1 to 30)
      .map(c => {
        val msg = s"$c - $baseMsg"
        pushMsg(msg)
      })
      .throttle(1, interval, 1, ThrottleMode.shaping)
      .runWith(SqsSink.messageSink(queueUrl))
  }

  // SendMessageRequest groups messages into batches if it can
  private def pushMsg(msg: String): SendMessageRequest = {
    val deduplicatedId = UUID.randomUUID.toString
    val now = System.currentTimeMillis()
    val msgRequest = new SendMessageRequest()
      .withMessageBody(msg)
      .withMessageGroupId(messageGroupId)
      .withMessageDeduplicationId(deduplicatedId)
    println(s"::::: MESSAGE PRODUCED :::::")
    println(s"$msg")
    println(s"::::: ::::: ::::: ::::: ::::: ")
    msgRequest
  }
}
