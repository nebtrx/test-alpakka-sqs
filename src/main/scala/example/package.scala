import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.sqs.{AmazonSQSAsync, AmazonSQSAsyncClientBuilder}

package object example {
  def mkSQSAsyncClient(sqsEndpoint: String, region: String, credentialsProvider: AWSStaticCredentialsProvider): AmazonSQSAsync = {
    AmazonSQSAsyncClientBuilder
      .standard()
      .withCredentials(credentialsProvider)
      .withEndpointConfiguration(new EndpointConfiguration(sqsEndpoint, region))
      .build()
  }
}
