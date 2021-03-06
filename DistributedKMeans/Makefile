# Makefile for Hadoop MapReduce WordCount demo project
# Customize these paths for your environment.
# -----------------------------------------------------------
# Settings for Arjun - Task 1
# hadoop.root=/home/arjun/hadoop-3.3.0
# jar.name=mr-demo-1.0.jar
# jar.path=target/${jar.name}
# job.name=KMeans_MR.KMeans
# local.input=input/dataset.txt
# local.output=output
# local.clusters=input/ClusterValues.txt
# local.params=input/params.txt
# local.log=log

# Settings for Urvaksh - Task 2
hadoop.root=/home/ubuntu/hadoop/hadoop-2.9.1
jar.name=mr-demo-1.0.jar
jar.path=target/${jar.name}
job.name=KMeans_MR.KMeansParallel
local.input=input/dataset.txt #placeholders
local.output=output
local.clusters=input/ClusterValues.txt
local.params=input/params4-29.txt
local.maxitr=50
local.upload=input
local.log=log

# Pseudo-Cluster Execution
hdfs.user.name=arjun
hdfs.input=input
hdfs.output=output
# AWS EMR Execution
aws.emr.release=emr-5.17.0
aws.region=us-east-1
aws.bucket.name=urv-proj
aws.subnet.id=subnet-6356553a
aws.input=input/dataset.txt
aws.output=k4-29_large
aws.clusters=input/ClusterValues.txt
aws.params=input/params.txt
aws.maxitr=100
aws.upload=input
aws.log.dir=log
aws.num.nodes=5
aws.instance.type=m4.xlarge


# -----------------------------------------------------------

# Compiles code and builds jar (with dependencies).
jar:
	mvn clean package

# Removes local output directory.
clean-local-output:
	rm -rf ${local.output}*

# Runs standalone
# Make sure Hadoop  is set up (in /etc/hadoop files) for standalone operation (not pseudo-cluster).
# https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/SingleCluster.html#Standalone_Operation
local: jar clean-local-output
	${hadoop.root}/bin/hadoop jar ${jar.path} ${job.name} ${local.input} ${local.output} ${local.clusters} ${local.params} ${local.maxitr} ${local.upload}

# Start HDFS
start-hdfs:
	${hadoop.root}/sbin/start-dfs.sh

# Stop HDFS
stop-hdfs:
	${hadoop.root}/sbin/stop-dfs.sh

# Start YARN
start-yarn: stop-yarn
	${hadoop.root}/sbin/start-yarn.sh

# Stop YARN
stop-yarn:
	${hadoop.root}/sbin/stop-yarn.sh

# Reformats & initializes HDFS.
format-hdfs: stop-hdfs
	rm -rf /tmp/hadoop*
	${hadoop.root}/bin/hdfs namenode -format

# Initializes user & input directories of HDFS.
init-hdfs: start-hdfs
	${hadoop.root}/bin/hdfs dfs -rm -r -f /user
	${hadoop.root}/bin/hdfs dfs -mkdir /user
	${hadoop.root}/bin/hdfs dfs -mkdir /user/${hdfs.user.name}
	${hadoop.root}/bin/hdfs dfs -mkdir /user/${hdfs.user.name}/${hdfs.input}

# Load data to HDFS
upload-input-hdfs: start-hdfs
	${hadoop.root}/bin/hdfs dfs -put ${local.input}/* /user/${hdfs.user.name}/${hdfs.input}

# Removes hdfs output directory.
clean-hdfs-output:
	${hadoop.root}/bin/hdfs dfs -rm -r -f ${hdfs.output}*

# Download output from HDFS to local.
download-output-hdfs: clean-local-output
	mkdir ${local.output}
	${hadoop.root}/bin/hdfs dfs -get ${hdfs.output}/* ${local.output}

# Runs pseudo-clustered (ALL). ONLY RUN THIS ONCE, THEN USE: make pseudoq
# Make sure Hadoop  is set up (in /etc/hadoop files) for pseudo-clustered operation (not standalone).
# https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/SingleCluster.html#Pseudo-Distributed_Operation
pseudo: jar stop-yarn format-hdfs init-hdfs upload-input-hdfs start-yarn clean-local-output
	${hadoop.root}/bin/hadoop jar ${jar.path} ${job.name} ${hdfs.input} ${hdfs.output}
	make download-output-hdfs

# Runs pseudo-clustered (quickie).
pseudoq: jar clean-local-output clean-hdfs-output
	${hadoop.root}/bin/hadoop jar ${jar.path} ${job.name} ${hdfs.input} ${hdfs.output}
	make download-output-hdfs

# Create S3 bucket.
make-bucket:
	aws s3 mb s3://${aws.bucket.name}

# Upload data to S3 input dir.
upload-input-aws: make-bucket
	aws s3 sync ${aws.upload} s3://${aws.bucket.name}/${aws.upload}

# Delete S3 output dir.
delete-output-aws:
	aws s3 rm s3://${aws.bucket.name}/ --recursive --exclude "*" --include "${aws.output}*"

# Upload application to S3 bucket.
upload-app-aws:
	aws s3 cp ${jar.path} s3://${aws.bucket.name}

# Main EMR launch.
# Task 2
aws: jar upload-app-aws delete-output-aws
	aws emr create-cluster \
		--name "KMeans Parallel MR Cluster" \
		--release-label ${aws.emr.release} \
		--instance-groups '[{"InstanceCount":${aws.num.nodes},"InstanceGroupType":"CORE","InstanceType":"${aws.instance.type}"},{"InstanceCount":1,"InstanceGroupType":"MASTER","InstanceType":"${aws.instance.type}"}]' \
	    --applications Name=Hadoop \
	    --steps '[{"Args":["${job.name}","s3://${aws.bucket.name}/${aws.input}","s3://${aws.bucket.name}/${aws.output}","s3://${aws.bucket.name}/${aws.clusters}","s3://${aws.bucket.name}/${aws.params}","${aws.maxitr}", "s3://${aws.bucket.name}/${aws.upload}"],"Type":"CUSTOM_JAR","Jar":"s3://${aws.bucket.name}/${jar.name}","ActionOnFailure":"TERMINATE_CLUSTER","Name":"Custom JAR"}]' \
		--log-uri s3://${aws.bucket.name}/${aws.log.dir} \
		--use-default-roles \
		--enable-debugging \
		--auto-terminate

# Download output from S3.
download-output-aws: clean-local-output
	mkdir ${aws.output}
	aws s3 sync s3://${aws.bucket.name}/${aws.output} ${aws.output}
#	mkdir ${local.log}
#	aws s3 sync s3://${aws.bucket.name}/${aws.log.dir} ${local.log}

# Change to standalone mode.
switch-standalone:
	cp config/standalone/*.xml ${hadoop.root}/etc/hadoop

# Change to pseudo-cluster mode.
switch-pseudo:
	cp config/pseudo/*.xml ${hadoop.root}/etc/hadoop

# Package for release.
distro:
	rm -f MR-Demo.tar.gz
	rm -f MR-Demo.zip
	rm -rf build
	mkdir -p build/deliv/MR-Demo
	cp -r src build/deliv/MR-Demo
	cp -r config build/deliv/MR-Demo
	cp -r input build/deliv/MR-Demo
	cp pom.xml build/deliv/MR-Demo
	cp Makefile build/deliv/MR-Demo
	cp README.txt build/deliv/MR-Demo
	tar -czf MR-Demo.tar.gz -C build/deliv MR-Demo
	cd build/deliv && zip -rq ../../MR-Demo.zip MR-Demo
