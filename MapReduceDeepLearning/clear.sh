#! /bin/bash
# clear the output in hadoop of deep learning
/usr/local/hadoop/bin/hadoop dfs -mv /input/0/ /0/
/usr/local/hadoop/bin/hadoop dfs -rmr /output/
/usr/local/hadoop/bin/hadoop dfs -rmr /input/
/usr/local/hadoop/bin/hadoop dfs -mkdir /input/
/usr/local/hadoop/bin/hadoop dfs -mv /0/ /input/0/
