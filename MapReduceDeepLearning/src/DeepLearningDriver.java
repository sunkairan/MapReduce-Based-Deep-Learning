/*
 * Project Name: Deep learning based on Hadoop (MapReduce)
 * 
 * Class Description: 
 * 	This is the driver of deep learning MapReduce program.
 * 
 * Class Structure: 
 * 	It contains two MapReduce structures: RBM and forward propagating.     
 * 
 * Author: Kairan Sun
 * Last Update Date: Nov. 29, 2013 
 */

import java.io.*;
import java.text.DecimalFormat;
import java.util.Random;
import java.net.URI; 

import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileStatus; 
import org.apache.hadoop.fs.FileSystem; 
import org.apache.hadoop.fs.FSDataInputStream; 
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;


public class DeepLearningDriver extends Configured implements Tool 
{
	/* 
	 * This class is the driver of MapReduce-based deep learning
	 */
	
	
	public int run(String[] args) throws Exception
	{
		/* 
		 * This is the entrance of this program
		 * Argument format: /input_file/ /output_file/ maxiter layernum nodeNumofLayer1 nodeNumofLayer2 ...
		 */
        int maxEpoch, numLayer, numCase;
        
        // This is the precision of decimals in output weights files
        // Reduce the precision to reduce transmission overload
        DecimalFormat df=new DecimalFormat("#.###");
        
        // This switch controls the way of passing the weights to mappers: distributed cache or configuration
        String useDistributedCache = "1";
        
        // parse the arguments
        String inputFile = args[0];
        String outputFile = args[1];
        maxEpoch = Integer.parseInt(args[2]); 
        numLayer = Integer.parseInt(args[3]);
        int[] numNodeofLayer = new int[numLayer];
        for (int i = 0; i < numLayer; i++)
        {
        	/*
        	 * Read the number of nodes of each layer from arguments
        	 */
            numNodeofLayer[i] = Integer.parseInt(args[i+4]);
        }
        if(args.length <= 4 + numLayer)
        {
        	/*
        	 * This is default number of training items
        	 */
        	numCase = 60000;
        }
        else
        {
        	/*
        	 * Set the number of training items if specified
        	 */
        	numCase = Integer.parseInt(args[4 + numLayer]);
        }
        // this variable is for future use
        int epochInMapper = 1;
        
        for (int layer = 0; layer < numLayer - 1; layer ++)
        {
        	/*
        	 * This loop is for every two adjacent layers
        	 */
        	
        	// get the number of nodes of two adjacent layers
        	// the number of nodes of input layer is numdims
        	// the number of nodes of output layer is numhid
            int numdims = numNodeofLayer[layer];
            int numhid  = numNodeofLayer[layer + 1];
            
            // VishidMatrix contains the  weights between two layers
            // At first, it is initialized as random. 
            double[] VishidMatrix = new double[numdims * numhid];
            Random randomgenerator = new Random();
            
            for(int ri = 0; ri < numdims * numhid; ri++)
            {
                /*
                 * This loop is for initializing weights
                 */
            	VishidMatrix[ri] = 0.1 * randomgenerator.nextGaussian();
            }
            
            
            /*
             * do Max iteration times to update every layer's weights
             * The first MapReduce
             */
            for(int iter = 1; iter <= maxEpoch / epochInMapper; iter++)
            {
            	
        	   /*
				* This loop is for maxEpoch times iteration
				* One MapReduce job is executed in every loop
				*/
            	String WeightFile = outputFile + "//weights//" + "vishid-" + String.valueOf(layer) + "-" + String.valueOf(iter) + ".txt";
            	String currentInput = inputFile+ "//" + String.valueOf(layer) + "//";
            	String currentOutput = outputFile+ "//" + String.valueOf(layer) + "//";
            	
               /*
				* First, initialize a new MapReduce job and prepare the needed data
				* Pass the small data using configurations
				* Pass the large data (weights) using either distributed cache or configuration  
				*/
        	    // Configuration conf = new Configuration();
        	    JobConf conf = new JobConf(getConf());
        	    
        	    // Must manually set the needed classes
        	    conf.setJarByClass(Matrix.class);
        	    conf.setJarByClass(RBMMapper.class);
        	    conf.setJarByClass(RBMReducer.class);
        	    
        	    // Tell the mappers about the node numbers using configurations
        	    conf.set("numdims", String.valueOf(numdims));
				conf.set("numhid", String.valueOf(numhid));
				conf.set("useDistributedCache", useDistributedCache);
				
				if(useDistributedCache == "1")
				{
					/*
					 * This branch is to use distributed cache
					 */
					
					// Make the String of weights
					String WeightString;
					
					// Write the weight into HDFS				
					FileSystem weightFS = FileSystem.get(URI.create(WeightFile) ,conf);
					OutputStream out = weightFS.create(new Path(WeightFile) );
					for(int ri = 0; ri < numdims * numhid; ri++)
					{
						/* 
						 * This loop is to put all the weights into an string split by space
						 * Later, it will output to distributed cache position
						 */
						
						// For saving of transmission overload, 
						// I reduce the output file size by limiting the precision of weight
						// However, the weights in memory is still in double-precision 
						
						WeightString = df.format(VishidMatrix[ri])+" ";
						out.write(WeightString.getBytes());
					}
					
					out.flush();
					out.close();
					
					// Make the single-line file distributed cached
					DistributedCache.addCacheFile(new Path(WeightFile).toUri(), conf);
				}
				else
				{
					/*
					 * This branch is to use configuration to pass weighs 
					 */
				}
				
				/*
				 * Second, run MapReduce
				 */
				
				// Give a job name
				String rbmjobName = "RBM-l"+String.valueOf(layer)+"-i"+String.valueOf(iter);
				conf.setJobName(rbmjobName);

        	    conf.setOutputKeyClass(IntWritable.class);
        	    conf.setOutputValueClass(DoubleWritable.class);

        	    conf.setMapperClass(RBMMapper.class);
        	    conf.setReducerClass(RBMReducer.class);

        	    conf.setInputFormat(TextInputFormat.class);
        	    conf.setOutputFormat(TextOutputFormat.class);
				    
				FileInputFormat.addInputPath(conf, new Path(currentInput));
				FileOutputFormat.setOutputPath(conf, new Path(currentOutput));
				
				JobClient.runJob(conf);
				
				/*
				 * Finally, read the output files and read the weights into memory
				 */
				
				// First, find all the files in output file directory
				FileSystem dirFS = FileSystem.get(URI.create(currentOutput) ,conf); 
		        FileStatus dirStatus = dirFS.getFileStatus(new Path(currentOutput));
		        
		        for(FileStatus fs : dirFS.listStatus(new Path(currentOutput)))
		        {
		        	/*
		        	 * This loop finds all the files in the directory
		        	 * current output file is "fs"
		        	 * current input stream is "in"
		        	 * Then, update every weight according to the output files  
		        	 */
		            // System.out.println(fs.getPath()); 
		        	if( fs.getPath().getName().startsWith("_"))
		        	{
		        		/*
		        		 * Ignore the control/log files
		        		 */
		        		continue;
		        	}
		            FileSystem cfs = FileSystem.get(fs.getPath().toUri(),conf); 
		            FSDataInputStream in = null; 
		            try
		            {
		            	in = cfs.open(fs.getPath() ); 
						String line;
						while((line = in.readLine()) != null)
						{
							/*
							 * This loop is to read every weight update in output files, 
							 * and update the weights in memory
							 */
							String [] tokens = line.split("\t"); 
							VishidMatrix[Integer.parseInt(tokens[0])] += ((Double.parseDouble(tokens[1])) / numCase);
						}
		            	
		            }finally
		            { 
		                IOUtils.closeStream(in); 
		            } 
		            
		            
		        } // end of all output files loop

		        /* 
		         * Delete the temporary output directory after one iteration
		         */
		        
		        dirFS.delete(dirStatus.getPath(), true);
		        
            } // end of maxEpoch loop
            /*
	         * Propagate the input to next level
	         * The second MapReduce
	         */
	        // Matrix vishid = new Matrix(VishidMatrix, numdims);
            
            // First give the file names
            String WeightFile = outputFile + "//weights//" + "vishid-" + String.valueOf(layer) + "-" + String.valueOf(maxEpoch) + ".txt";
            String currentInput = inputFile+ "//" + String.valueOf(layer) + "//";
        	String currentOutput = inputFile+ "//" + String.valueOf(layer + 1) + "//";
            
        	
        	//////////////////////
        	
    	    JobConf conf1 = new JobConf(getConf());
    	    // Must manually set the needed classes
    	    conf1.setJarByClass(Matrix.class);
    	    conf1.setJarByClass(PropMapper.class);
    	    conf1.setJarByClass(PropReducer.class);
    	    
    	    // Tell the mappers about the node numbers using configurations
    	    conf1.set("numdims", String.valueOf(numdims));
			conf1.set("numhid", String.valueOf(numhid));
			conf1.set("useDistributedCache", useDistributedCache);
			
			// Make the String of weights
			String WeightString;
			
			// Write the weight into HDFS				
			FileSystem weightFS = FileSystem.get(URI.create(WeightFile) ,conf1);
			OutputStream out = weightFS.create(new Path(WeightFile) );
			for(int ri = 0; ri < numdims * numhid; ri++)
			{
				/* 
				 * This loop is to put all the weights into an string split by space
				 * Later, it will output to distributed cache position
				 */
				
				// For saving of transmission overload, 
				// I reduce the output file size by limiting the precision of weight
				// However, the weights in memory is still in double-precision 
				
				WeightString = df.format(VishidMatrix[ri])+" ";
				out.write(WeightString.getBytes());
			}
			
			out.flush();
			out.close();
			
			// Make the single-line file distributed cached
			DistributedCache.addCacheFile(new Path(WeightFile).toUri(), conf1);
			
			/*
			 * Second, run MapReduce
			 */
			
			// Give a job name
			String propjobName = "Prop-l"+String.valueOf(layer);
			conf1.setJobName(propjobName);

    	    conf1.setOutputKeyClass(LongWritable.class);
    	    conf1.setOutputValueClass(Text.class);

    	    conf1.setMapperClass(PropMapper.class);
    	    conf1.setReducerClass(PropReducer.class);

    	    conf1.setInputFormat(TextInputFormat.class);
    	    conf1.setOutputFormat(TextOutputFormat.class);
			    
			FileInputFormat.addInputPath(conf1, new Path(currentInput));
			FileOutputFormat.setOutputPath(conf1, new Path(currentOutput));
			
			// run the forward propagate job 
			// generate the input of next layer 
			    
			JobClient.runJob(conf1);
		} // end of two adjacent layers loop
        return 0;
        
	} // end of main function
	
	public static void main(String[] args) throws Exception 
	{
		int res = ToolRunner.run(new Configuration(), new DeepLearningDriver(), args);
	    System.exit(res);
	}

}
