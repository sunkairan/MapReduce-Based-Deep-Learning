/*
 * Project Name: Deep learning based on Hadoop (MapReduce)
 * 
 * Class Description: 
 * 	This is the reducer of restricted Boltzmann machine (RBM) training part in deep learning MapReduce program.
 *  The task of this MapReduce is to train the rbm between two layers in unsupervised way,
 *  and output the updated weight.
 *  
 *  Each reducer collect all the weight updates from one single weight ID. 
 *  It adds the updates from the same weight up and write to the final output.
 * 
 * 
 * Author: Kairan Sun
 * Last Update Date: Nov. 30, 2013 
 */

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;


public class RBMReducer extends MapReduceBase implements Reducer<IntWritable, DoubleWritable, IntWritable, DoubleWritable> 
{
	/*
	 * This is the reducer of restricted Boltzmann machine (RBM) training part in deep learning MapReduce program.
	 * 
	 * Note that the format of intermediate data it taking is <IntWritable, DoubleWritable>, as mapper output.
	 * The format of final output is also <IntWritable, DoubleWritable>, 
	 * because the key is the number of weight (an integer), and the value is the update of weight's value (double)
	 */
	public void reduce(IntWritable key, Iterator<DoubleWritable> values, OutputCollector<IntWritable, DoubleWritable> output, Reporter reporter) throws IOException 
	{
	   double sum = 0;
	   while (values.hasNext()) 
	   {
		   /*
		    * Calculate the sum of all the updates
		    */
		   sum += values.next().get();
	   }
	   // Output the sum
	   output.collect(key, new DoubleWritable(sum));
	}
}
