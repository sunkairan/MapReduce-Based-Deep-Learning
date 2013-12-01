/*
 * Project Name: Deep learning based on Hadoop (MapReduce)
 * 
 * Class Description: 
 * 	This is the mapper of forward propagation part in deep learning MapReduce program.
 *  The task of this MapReduce is to compute the input of next layer using the input 
 *  from lower layer and the weights just computed. 
 *  
 *  It is executed between every two layers. So the total execution time for this MapReduce
 *  program is the number of layers - 1.
 * 
 * Class Structure: 
 * 	It contains four parts: 
 * 		configure() reads all the configurations and distributed cache from outside. 
 * 		initialize() parse the input strings into parameters, and initialize parameters for algorithm.
 * 		prop2nextLayer() compute the forward propagation algorithm.
 * 		map() implements the mapper. It outputs the original key and updated value pair.
 * 
 * Author: Kairan Sun
 * Last Update Date: Nov. 30, 2013 
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.io.*;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;


public class PropMapper extends MapReduceBase implements Mapper<LongWritable, Text, LongWritable, Text> 
{
	/*
	 * This is the mapper of forward propagation MapReduce program
	 */
    
    // These are the variables and parameters used in algorithm
    private double epsilonw;
    private double epsilonvb;
    private double espilonhb;
    private double weightcost;
    private double initialmomentum;
    private double finalmomentum;
    private int numhid;
    private int numdims;
    private int numbatches;
    private int maxepoch;
    
    private Matrix hidbiases;
    private Matrix visbiases;
    private Matrix poshidprobs;
    private Matrix neghidprobs;
    private Matrix posprods;
    private Matrix negprods;
    private Matrix vishidinc;
    private Matrix hdibiasinc;
    private Matrix visbiasinc;
    private Matrix poshidstates;
    private Matrix data;
    private Matrix vishid;
    private String weightline;
    private String inputData;
    private String inputNumdims;
    private String inputNumhid;
    
    // this is the intermediate output
    private int [] newinput;
    private Text word = new Text();
    
    public void configure(JobConf conf) {
    	/*
    	 * It reads all the configurations and distributed cache from outside. 
    	 */
    	
    	// Read number of nodes in input layer and output layer from configuration 
    	inputNumdims = conf.get("numdims");
    	inputNumhid  = conf.get("numhid");
    	
    	// Read the weights from distributed cache
        Path[] pathwaysFiles = new Path[0];
        try {
               pathwaysFiles = DistributedCache.getLocalCacheFiles(conf);
               for (Path path: pathwaysFiles) {
            	   /*
            	    * this loop reads all the distributed cache files
            	    * In fact, the driver program ensures that there is only one distributed cache file 
            	    */
                   BufferedReader fis = new BufferedReader(new FileReader(path.toString()));
                   weightline = fis.readLine();
              }
         } catch (Exception e) {
                 e.printStackTrace();
         }
    }
    
    
    
    private void  initialize(){
    	/*
    	 * It parses the input strings into parameters, and initialize parameters for algorithm.
    	 */
    	
    	
    	// These are the adjustable parameters for deep learning algorithm.
    	// For details, please refer to Dr. Hinton's paper: 
    	// Reducing the dimensionality of data with neural networks. Science, Vol. 313. no. 5786, pp. 504 - 507, 28 July 2006.
        epsilonw = 0.1; 
        epsilonvb = 0.1;
        espilonhb = 0.1;
        weightcost = 0.000;//0.0002;
        initialmomentum = 0.5;
        finalmomentum = 0.9;

        // Parse the number of nodes in input layer and output layer
        numhid = Integer.parseInt(inputNumhid);
        numdims = Integer.parseInt(inputNumdims);
        
        // Parse the weights
        String [] tokens = inputData.split("\t");
        String [] DataString;
        if (tokens.length == 1)
        // This case happens when first time read the data
        {
            DataString = tokens[0].trim().split("\\s+");
        }
        else
        // Else, the input line is output by previous layer
        {
            DataString = tokens[1].trim().split("\\s+");
        }
        
        double [] DataVector = new double[numdims];
        double [] VishidMatrix = new double[numdims * numhid];
        int count = 0;
        String line;
        String [] tempst;
    	line = weightline;
    	tempst = line.trim().split(" ");
        count = tempst.length;
        
        if (numdims != DataString.length || numdims * numhid != count)
        {
        	/*
        	 * Check if the input data match the expectation  
        	 */
            throw new IllegalArgumentException("Input data and value do not match!");
        }

        for(int i = 0; i < numdims; i++)
        {
            DataVector[i] = (double)(Integer.parseInt(DataString[i]))/255.0;
        }
        for(int i = 0; i < count; i++)
        {
        	VishidMatrix[i] = Double.parseDouble(tempst[i]);
        }

        
        // initialize the variables 
        // Most of them are matrix.
        this.data = new Matrix(DataVector,1);
        this.vishid = new Matrix(VishidMatrix, numdims); 
        
        hidbiases = new Matrix(1,numhid);
        visbiases = new Matrix(1,numdims);
        poshidprobs = new Matrix(1,numhid);
        neghidprobs = new Matrix(1,numhid);
        posprods = new Matrix(numdims,numhid);
        negprods = new Matrix(numdims,numhid);
        vishidinc = new Matrix(numdims,numhid);
        hdibiasinc = new Matrix(1,numhid);
        visbiasinc = new Matrix(1,numhid);
        poshidstates = new Matrix(1,numhid);
        newinput = new int [numhid];
    }
    
    private void prop2nextLayer()
    {
    	/*
    	 * It computes the forward propagation algorithm.
    	 */
        poshidprobs = data.times(vishid);
        //(1 * numdims) * (numdims * numhid)
        poshidprobs.plusEquals(hidbiases);
        //data*vishid + hidbiases
        double [] [] product_tmp2 = poshidprobs.getArray();
        
        for( int i2 = 0; i2 < numhid; i2++)
        {
        	/*
        	 * compute the updated input, and write them to newinput
        	 */
		    product_tmp2[0][i2] = 1/(1 + Math.exp(-product_tmp2[0][i2]));
		    newinput[i2] = (int) (product_tmp2[0][i2] * 255.0);
        }
            
    }
    
    private void getposphase()
    {
    	/*
    	 * It does the positive phase of unsupervised RBM training algorithm
    	 * 
    	 * For details, please refer to Dr. Hinton's paper: 
    	 * Reducing the dimensionality of data with neural networks. Science, Vol. 313. no. 5786, pp. 504 - 507, 28 July 2006.
    	 */
    	    	
        //Start calculate the positive phase
        //calculate the cured value of h0
        poshidprobs = data.times(vishid);
        //(1 * numdims) * (numdims * numhid)
        poshidprobs.plusEquals(hidbiases);
        //data*vishid + hidbiases
        double [] [] product_tmp2 = poshidprobs.getArray();
        int i2 = 0;
        while( i2 < numhid)
        {
                product_tmp2[0][i2] = 1/(1 + Math.exp(-product_tmp2[0][i2]));
                i2++;
        }
        posprods = data.transpose().times(poshidprobs);
        //(numdims * 1) * (1 * numhid)
        
        //end of the positive phase calculation, find the binary presentation of h0
        int i3 =0;
        double [] [] tmp1 = poshidprobs.getArray();
        double [] [] tmp2 = new double [1][numhid];
        Random randomgenerator = new Random();
        while (i3 < numhid)
        {
        	/*
        	 * a sampling according to possiblity given by poshidprobs
        	 */
                if (tmp1[0][i3] > randomgenerator.nextDouble())
                        tmp2[0][i3] = 1;
                else tmp2[0][i3] = 0;
                i3++;
        }
        
        // poshidstates is a binary sampling according to possiblity given by poshidprobs
        poshidstates = new Matrix(tmp2);
    }
    
    private void getnegphase()
    {
    	/*
    	 * It does the negative phase of unsupervised RBM training algorithm
    	 * 
    	 * For details, please refer to Dr. Hinton's paper: 
    	 * Reducing the dimensionality of data with neural networks. Science, Vol. 313. no. 5786, pp. 504 - 507, 28 July 2006.
    	 */
    	
        //start calculate the negative phase
        //calculate the curved value of v1,h1
        //find the vector of v1
        Matrix negdata = poshidstates.times(vishid.transpose());
        //(1 * numhid) * (numhid * numdims) = (1 * numdims)
        negdata.plusEquals(visbiases);
        //poshidstates*vishid' + visbiases
        double [] [] tmp1 = negdata.getArray();
        int i1 = 0;
        while( i1 < numdims)
        {
                tmp1[0][i1] = 1/(1 + Math.exp(-tmp1[0][i1]));
                i1++;
        }
        
        //find the vector of h1
        neghidprobs = negdata.times(vishid);
        //(1 * numdims) * (numdims * numhid) = (1 * numhid)
        neghidprobs.plusEquals(hidbiases);
        double [] [] tmp2 = neghidprobs.getArray();
        int i2 = 0;
        while( i2 < numhid)
        {
            tmp2[0][i2] = 1/(1 + Math.exp(-tmp2[0][i2]));
            i2++;
        }
        negprods = negdata.transpose().times(neghidprobs);
        //(numdims * 1) *(1 * numhid) = (numdims * numhid)
    }
    
    //update the weights and biases
    // This serves as a reducer
    private void update()
    {
    	/*
    	 * It computes the update of weights using previous results and parameters
    	 * 
    	 * For details, please refer to Dr. Hinton's paper: 
    	 * Reducing the dimensionality of data with neural networks. Science, Vol. 313. no. 5786, pp. 504 - 507, 28 July 2006.
    	 */
		double momentum;
		// if (epoch > 5)
		//        momentum = finalmomentum;
		// else
		//        momentum = initialmomentum;
		// vishidinc = momentum*vishidinc + epsilonw*( (posprods-negprods)/numcases - weightcost*vishid);
		// vishidinc.timesEquals(momentum);
        Matrix temp1 = posprods.minus(negprods);
        Matrix temp2 = vishid.times(weightcost);
        temp1.minusEquals(temp2);
        temp1.timesEquals(epsilonw);
        
        // the final weights updates are written in vishidinc
        vishidinc.plusEquals(temp1);
    
    }
    
    public void map(LongWritable key, Text value, OutputCollector<LongWritable, Text> output, Reporter reporter) throws IOException 
    {
    	/*
    	 * It implements the mapper. It outputs the original key and updated value pair.
    	 */
    	inputData = value.toString();
    	
    	initialize();
    	prop2nextLayer();
    	
    	// put all the newinput together into updated
    	String updated = "";
    	
    	for(int i = 0; i < numhid; i++)
    	{
    		updated += String.valueOf(newinput[i]) + " ";
    	}
    	
    	word.set(updated);
    	
    	// output the key, value pair
    	output.collect(key, word);
        
      }
    

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

 } 
