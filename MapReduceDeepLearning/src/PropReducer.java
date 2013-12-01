/*
 * Project Name: Deep learning based on Hadoop (MapReduce)
 * 
 * Class Description: 
 * 	This is the reducer of forward propagation part in deep learning MapReduce program.
 *  The task of this MapReduce is to compute the input of next layer using the input 
 *  from lower layer and the weights just computed. 
 *  
 *  Each reducer collects the <testCaseID, testCaseContent> pair, and output it directly.
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


public class PropReducer extends MapReduceBase implements Reducer<LongWritable, Text, LongWritable, Text> 
{
	/*
	 * It implements the reducer. It outputs the <key, value> pair directly.
	 */
	public void reduce(LongWritable key, Iterator<Text> values, OutputCollector<LongWritable, Text> output, Reporter reporter) throws IOException 
	{
       while (values.hasNext()) 
       {
    	   /*
    	    * Directly output the <key, value> pair
    	    */
    	   output.collect(key, values.next());
       }
	}
}
