# test-alpakka-sqs

This is a quick example project showing how to consume [Amazon SQS Service](https://aws.amazon.com/sqs/) 
using [Alpakka](https://github.com/akka/alpakka) [AWS SQS Connector](https://developer.lightbend.com/docs/alpakka/current/sqs.html)


### Usage

1. `$ cp .env.sample .env` and replace the placeholders with the proper values
1. Start the consumer: `$ source .env & sbt "runMain example.Consumer"`
1. Start the producer: `$ source .env & sbt "runMain example.Producer"`
