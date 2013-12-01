#! /bin/bash
# upload input files in to file system
/usr/local/hadoop/bin/hadoop fs -get /output/weights/* ~/workspace/weights/
