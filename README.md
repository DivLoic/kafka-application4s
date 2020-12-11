# Kafka App for Scala

![Master Workflow](https://github.com/DivLoic/kafka-application4s/workflows/Master%20Workflow/badge.svg)

This module is the attached source code from the blog post 
[Getting Started with Scala and Apache Kafka](https://www.confluent.io/blog/kafka-scala-tutorial-for-beginners/).
It discusses how to use the basic Kafka Clients in a Scala application. 
Originally inpired by the first
[scala example](https://github.com/confluentinc/examples/tree/6.0.0-post/clients/cloud/scala),
it goes beyond by showing multiple ways to produce, to consume and to configure the clients.

1. [Try it](#try-it)
2. [Produce](#produce)
3. [Consume](#consume)
3. [Read More](#read-more)

## Try it
```bash
git clone https://github.com/DivLoic/kafka-application4s.git
cd kafka-application4s
sbt compile
```

### Local 

You first need to run Kafka and the 
[Schema Registry](https://docs.confluent.io/platform/current/schema-registry/index.html). 
Any recent installation of Kafka or the Confluent platform can be used. 
Many installation methods can be found on the [CP Download Page](https://www.confluent.io/download).

i.e. Confluent Cli on Mac
```shell script
curl -sL https://cnfl.io/cli | sh -s -- latest -b /usr/local/bin
export CONFLUENT_HOME=...
export PATH=$PATH:$CONFLUENT_HOME
confluent local services start schema-registry
```
 

### Cloud
The module also works with a cluster hosted on Confluent Cloud.
You will find in [consumer.conf](src/main/resources/consumer.conf) 
and [producer.conf](src/main/resources/producer.conf) the commented config related to the cloud. 
After that, you will need either to edit these files or to define the following variables:

```shell script
export BOOTSTRAP_SERVERS="...:9092"
export CLUSTER_API_KEY="..."
export CLUSTER_API_SECRET="..."
export SCHEMA_REGISTRY_URL="https:/..."
export SR_API_KEY="..."
export SR_API_SECRET="..."
```

For more on Confluent Cloud login see the 
[documentation](https://docs.confluent.io/current/cloud/access-management/index.html).

## Produce
Run: 
```shell script
sbt produce "-Djline.terminal=none" --error  
```

[![asciicast](https://asciinema.org/a/weOD3XpAVawpZQrVW0Ic8pN8q.svg)](https://asciinema.org/a/weOD3XpAVawpZQrVW0Ic8pN8q)

## Consume
Run: 
```shell script
sbt consume "-Djline.terminal=none" --error  
```

[![asciicast](https://asciinema.org/a/DjI20wqNnU470hcXkb0uKpe2C.svg)](https://asciinema.org/a/DjI20wqNnU470hcXkb0uKpe2C)

## Read more
- The code is detail in the [blog post](https://www.confluent.io/blog/kafka-scala-tutorial-for-beginners/)
- For a step by step approach including tests checkout this [Kafka Tutorial](https://kafka-tutorials.confluent.io/produce-consume-lang/scala.html)  