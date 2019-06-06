# ![bidmachine.io](https://appodeal-uploads.s3.eu-central-1.amazonaws.com/server/production/wl_domain/logo_bottom/8/logo-2-1.png)
​

All components of the solution launch in Docker containers or on container orchestrators. We have tested the solution work in DCOS and k8s.  

​

The solution includes the following components:

1. scala-applications: api-gateway, auction-gateway, druid-service, settings-service, tracking-gateway.

  auction-gateway - Handles incoming ad requests. Performs callout and auctions. Serves ads.

  tracker-gateway - Handles tracking events callbacks, such as impressions and clicks.

  api-gateway - Provides settings HTTP API for Dashboard app.

  settings-service - Stores all settings.

  druid-service - Provides analytics HTTP API on top of Druid Query API for Dashboard app. 


2. redis (as cache)


3. postgresql (as database for configurational and service data)


4. analytics stack: kafka, druid cluster, and other dependiencies (ZooKeeper, mysql/mariadb/pxc/postgresql/cassandra for metadata storage, S3/HDFS/shared-storage for Deep Storage). In order to import data from kafka to druid, one needs to implement tranquility (configuration example see in directory tranquility/conf) or use druid extension "druid-kafka-indexing-service" (see http://druid.io/docs/latest/development/extensions-core/kafka-ingestion.html).

​

## Dependencies for build scala-applications:


* JDK8, by default OpenJDK is used, third-party distributives of OpenJDK (Amazon Corretto или Azul Zulu) can be used as well. Oracle JDK and Oracle JRE are supposed to be working, bu this configuration was not tested. GraalVM is also applicable, but in case of multiprocessors systems emerges an issue with NUMA.

* scala 2.12.x (currently 2.12.8 is used)


* sbt. All dependencies and their versions are described in build.sbt 


* GeoIP-functionality work in auction-gateway requires additional base MaxMind GeoIP2 or GeoLite2. ​

​

​

Scala-applications can be built on the host where all dependencies (git, jdk8, scala, sbt, etc) are installed, or on CI/CD-solution’s side or in build container (Docker).

​

​

## Stages of manual build on host for all scala-applications:


1. aquire source code from version control system:​

```shell

git clone git@github.com:bidmachine/BidMachine-Ad-Exchange.git

```


2. move to directory:


```shell

cd BidMachine-Ad-Exchange

```


3. Manual edit of configuration files, Dockerfiles and scripts if necessary.


4. Run compilation and tests of scala-application via sbt:


```shell

sbt "project ${APP_NAME}" test docker:stage

```


5. build docker image with the application and push the image to registry:


Note. To build the image with auction-gateway in Dockerfile, the following command is required:


```dockerfile

COPY GeoIP2-City.mmdb /opt/maxmind/GeoIP2-City.mmdb

```


or


```dockerfile

COPY GeoLite2-City.mmdb /opt/maxmind/GeoLite2-City.mmdb

```

​

Dockerfile also can be patched on the CI/CD-solution’s side. For example Jenkinfile for Jenkins looks as follows:​


``` groovy

pipeline {

agent {

    label 'master'

   }

	stages {

		stage ('Git fetch and checkout') {

			steps {

				checkout([$class: 'GitSCM', branches: [[name: '${BRANCH}']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'CleanBeforeCheckout'], [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: true, recursiveSubmodules: true, reference: '', trackingSubmodules: true]], submoduleCfg: [], userRemoteConfigs: [[url: 'git@github.com:bidmachine/BidMachine-Ad-Exchange.git']]])

			}

		}

		stage ('Sources compile and test via sbt') {

			steps {

				sh '''sbt -Dsbt.log.noformat=true "project ${APP_NAME}" clean test docker:stage'''

			}

		}

		stage ('APP_NAME specific steps') {

			steps {

				script {

					if (env.APP_NAME == 'auction-gateway') { 

				sh """ cp /mnt/geoip-data/GeoIP2-City.mmdb ./src/${APP_NAME}/target/docker/stage/GeoIP2-City.mmdb && echo "USER root" >> ./src/${APP_NAME}/target/docker/stage/Dockerfile && echo "RUN mkdir -p /opt/maxmind && chmod -R 777 /opt/maxmind" >> ./src/${APP_NAME}/target/docker/stage/Dockerfile && echo "COPY GeoIP2-City.mmdb /opt/maxmind/GeoIP2-City.mmdb" >> ./src/${APP_NAME}/target/docker/stage/Dockerfile &&  echo "RUN chmod +r /opt/maxmind/GeoIP2-City.mmdb" >> ./src/${APP_NAME}/target/docker/stage/Dockerfile && echo "USER daemon" >> ./src/${APP_NAME}/target/docker/stage/Dockerfile """

					} else {

						echo 'skip step'

					}

				}

			}

		}		

		stage ('Build image') {

			steps {

				sh """docker build -t ${REGISTRY_URL}/${IMAGE_GROUP_NAME}/${APP_NAME}:${TAG} ./src/${APP_NAME}/target/docker/stage/ """

			}

		}

		stage ('Push image to registry') {

			steps {

				withDockerRegistry(credentialsId: '${CI_REGISTRY}', url: 'https://${REGISTRY_URL}') {

					sh """ docker push ${REGISTRY_URL}/${IMAGE_GROUP_NAME}/${APP_NAME}:${TAG} """

				}

			}

		}

	}

}

```

​

Where the variable `APP_NAME` takes on values: api-gateway, auction-gateway, druid-service, settings-service, tracking-gateway

​

Example of basic Dockerfile for multi-stage building:

​

```dockerfile

FROM openjdk:8-jdk AS build-env

# base

RUN apt-get update && apt-get install -y apt-utils && apt-get upgrade -yq \

&& apt-get install -y openjdk-8-jre-headless git curl wget apt-transport-https ca-certificates gnupg2 software-properties-common

# docker

ARG dockergroup=docker

ARG dockergid=999

RUN curl -fsSL https://download.docker.com/linux/$(. /etc/os-release; echo "$ID")/gpg | apt-key add - \

&& add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/$(. /etc/os-release; echo "$ID") $(lsb_release -cs) edge" \

&& apt-get update && apt-get install -y docker-ce-cli 

# scala and sbt

ARG SCALA_VERSION=2.12.8

RUN cd /tmp && wget http://scala-lang.org/files/archive/scala-${SCALA_VERSION}.deb \

&& cd /tmp && dpkg -i ./scala-${SCALA_VERSION}.deb && rm -rf /tmp/scala-${SCALA_VERSION}.deb \

&& echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list \

&& apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823 \

&& apt-get update && apt-get install sbt -y 

```

​

Example of basic Dockerfile for Jenkins’ image:

​

```dockerfile

FROM jenkins/jenkins:latest

USER root

ARG dockergroup=docker

ARG dockergid=999

ARG JENKINS_SLAVE_AGENT_PORT=57000

# base

RUN apt-get update && apt-get install -y apt-utils && apt-get upgrade -yq \

&& apt-get install -y openjdk-8-jre-headless git curl wget apt-transport-https ca-certificates gnupg2 software-properties-common

# docker

ARG DOCKER_COMPOSE_VERSION=1.24.0

RUN groupadd -g ${dockergid} ${dockergroup} && gpasswd -a jenkins $dockergroup \

&& curl -fsSL https://download.docker.com/linux/$(. /etc/os-release; echo "$ID")/gpg | apt-key add - \

&& add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/$(. /etc/os-release; echo "$ID") $(lsb_release -cs) edge" \

&& apt-get update && apt-get install -y docker-ce-cli && gpasswd -a jenkins docker \

&& curl -L "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose \

&& chmod +x /usr/local/bin/docker-compose 

# scala and sbt

ARG SCALA_VERSION=2.12.8

RUN cd /tmp && wget http://scala-lang.org/files/archive/scala-${SCALA_VERSION}.deb \

&& cd /tmp && dpkg -i ./scala-${SCALA_VERSION}.deb && rm -rf /tmp/scala-${SCALA_VERSION}.deb \

&& gpasswd -a jenkins scala \

&& echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list \

&& apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823 \

&& apt-get update && apt-get install sbt -y \

&& gpasswd -a jenkins sbt 

# for deploy to DCOS

ARG DCOS_CLI_VERSION=1.11

RUN cd /tmp/ && curl https://downloads.dcos.io/binaries/cli/linux/x86-64/dcos-${DCOS_CLI_VERSION}/dcos -o dcos \

&& mv /tmp/dcos /usr/local/bin/ \

&& chmod +x /usr/local/bin/dcos 

# for deploy to k8s via kubectl

RUN curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | apt-key add - \

&& echo "deb http://apt.kubernetes.io/ kubernetes-xenial main" | tee -a /etc/apt/sources.list.d/kubernetes.list \

&& apt-get update && apt-get install -y kubectl 

# pip3

RUN echo "deb http://ftp.de.debian.org/debian testing main" | tee /etc/apt/sources.list.d/test.list \

&& apt-get update && apt-get -t testing install -y python3-pip 

# for deploy to k8s via ansible 

RUN pip3 install --upgrade ansible openshift

# aws

RUN pip3 install awscli ecscli

# for deploy to k8s via helm and/or werf

RUN mkdir -p /tmp/helm && cd /tmp/helm \ 

&& wget https://storage.googleapis.com/kubernetes-helm/helm-v2.14.0-linux-amd64.tar.gz \ 

&& tar -xvzf helm-v2.14.0-linux-amd64.tar.gz && cp /tmp/helm/linux-amd64/helm /usr/local/bin/helm \ 

&& chmod +x /usr/local/bin/helm && rm -rf /tmp/helm

RUN curl -L https://dl.bintray.com/flant/werf/v1.0.2-alpha.9/werf-linux-amd64-v1.0.2-alpha.9 -o /usr/local/bin/werf && chmod +x /usr/local/bin/werf

USER jenkins

```

​

## Deploy.


Depending on environment and platform, deploy can be performed in various ways:


1. via docker-compose (APIv3) in Docker SWARM cluster or on standalone Docker-hosts;


2. via kubectl to k8s


3. via ansible (with k8s module) to k8s


4. via helm or werf to k8s


5. via dcos-cli to DCOS


6. via plugins or extentions of CI/CD-solution to any orchestrator (k8s, DCOS, nomad)



​

Example of deploy of one application (auction-gateway) with Jenkins and ansible.
