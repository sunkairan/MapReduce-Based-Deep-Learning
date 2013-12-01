#! /bin/bash
# run the single-node deep learning hadoop
/usr/local/hadoop/bin/hadoop jar ~/workspace/MapReduceDeepLearning/mrdl.jar DeepLearningDriver "hdfs://localhost:9000/input" "hdfs://localhost:9000/output" 2 3 784 10 3 2
