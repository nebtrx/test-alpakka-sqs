package example

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.sqs.scaladsl.{SqsAckSink, SqsSource}
import akka.stream.alpakka.sqs.{MessageAction, SqsSourceSettings}
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.Message

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn

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

  StdIn.readLine

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
}
