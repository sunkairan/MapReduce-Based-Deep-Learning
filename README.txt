README for source codes -- NerveCloud

The source codes for our project contain two parts: Hadoop-based deep learning (java code) and GUI-based demo software (MATLAB code).

Hadoop-based deep learning is in the folder ¡°MapReduceDeepLearning¡±:
	©À©¤mrdl.jar is the executable java program that can be upload to EC2 and run with MapReduce. 
	©À©¤src\DeepLearningDriver.java is the driver of deep learning MapReduce program. (347 lines)
	©À©¤src\Matrix.java is the class solving all the matrix calculations. (321 lines)
	©À©¤src\RBMMapper.java is the mapper of restricted Boltzmann machine (RBM) training part in deep learning MapReduce program. (393 lines)
	©À©¤src\RBMReducer.java is the reducer of restricted Boltzmann machine (RBM) training part in deep learning MapReduce program. (50 lines)
	©À©¤src\PropMapper.java is the mapper of forward propagation part in deep learning MapReduce program. (372 lines)
	©¸©¤src\PropReducer.java is the reducer of forward propagation part in deep learning MapReduce program. (42 lines)

The format of arguments to execute mrdl.jar is: DeepLearningDriver "input\dir" "output\dir" num_iter num_layer numNode_layer(1) numNode_layer(2) ... numCases

For exmple, if you want to execute the program on EC2, first upload "mrdl.jar" into S3, and upload the input file into \yourname\input\0\. 
If you want the iteration number to be 20, the ANN has 3 layers and each layer has {784 1000 300
} nodes, you can run the EC2 job with following arguments: 


DeepLearningDriver 
s3n://yourname/input/ 
s3n://yourname/output/ 
20 
3 784 1000 300


GUI-based demo software is in the folder ¡°MATLAB Demo¡±
	©À©¤HandWrittenAuto.fig is the GUI design of unsupervised learning demo software. 
	©À©¤HandWrittenAuto.m defines the functions that used in unsupervised learning demo software. (284 lines)
	©À©¤HandWrittenSuper.fig is the GUI design of supervised learning demo software.
	©À©¤HandWrittenSuper.m defines the functions that used in supervised learning demo software. (212 lines)
	©À©¤mnist_classify.mat is the trained weights file for recognizing hand-written digits.
	©¸©¤mnist_encode.mat is the trained weights file for auto-encoding hand-written digits.

Total # of code lines: 2021
