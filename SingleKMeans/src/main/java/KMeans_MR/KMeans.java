package KMeans_MR;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Scanner;

public class KMeans extends Configured implements Tool {
  private static final Logger logger = LogManager.getLogger(KMeans.class);

  @Override
  public int run(String[] args) throws Exception {
    Configuration conf = getConf();
    File file = new File(args[3]);
    Scanner sc = new Scanner(file);
    String[] params = sc.nextLine().split(",");
    // getting value of k and maxIterations from params.txt.
    int maxIterations = Integer.parseInt(params[0]);
    int k = Integer.parseInt(params[1]);

    int i = 0;
    float threshold = 0.001f;
    boolean status;
    boolean converged;
    DataRow[] oldCentroids = new DataRow[k];

    // getting initial centroids from ClusterValues.txt.
    file = new File(args[2]);
    sc = new Scanner(file);
    int x =0;
    while(sc.hasNext()){
      String[] arr = sc.nextLine().split(",");
      oldCentroids[x++] = new DataRow(arr);
    }

    // setting initial centroids in conf.
    for(int j = 0; j < k; j++) {
      conf.set("centroid." + j, oldCentroids[j].toString());
    }
    DataRow[] newCentroids;
    conf.setInt("k", k);

    // kmeans algorithm:
    while(true) {
      i++;
      Job job = Job.getInstance(conf, "Kmeans");
      FileInputFormat.addInputPath(job, new Path(args[0]));
      FileOutputFormat.setOutputPath(job, new Path(args[1]));
      setConfiguration(job, k);

      status = job.waitForCompletion(true);
      if (!status) {
        System.out.println("Failed at iteration: "+i);
        System.exit(1);
      }

      // reading new centroids from Reducer output
      newCentroids = readCentroids(conf, k, args[1]);
      // checking for convergence
      converged = checkConvergence(oldCentroids, newCentroids, threshold);

      if(converged || i == (maxIterations -1)) {
        // if converged, concatenating all part-r files into one output file
        writeOutput(conf, newCentroids, args[1]);
        break;
      } else { // if not converged, updating centroids in conf to centroids calculated in the iterations.
        for(int l = 0; l < k; l++) {
          conf.unset("centroid." + l);
          conf.set("centroid." + l, newCentroids[l].toString());
        }
      }
    }
    return 1;
  }

  private void setConfiguration(Job job, int k)  {
    job.setJarByClass(KMeans.class);
    job.setMapperClass(KMeansMapper.class);
    job.setReducerClass(KMeansReducer.class);
    job.setCombinerClass(KMeansCombiner.class);
    job.setNumReduceTasks(k);
    job.setMapOutputKeyClass(IntWritable.class);
    job.setMapOutputValueClass(DataRow.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
  }

  // reads part-r files written by the reducer and creates a DataRow list containing the new centroids.
  private static DataRow[] readCentroids(Configuration conf, int k, String pathString)
          throws IOException, URISyntaxException {
    DataRow[] dataRows = new DataRow[k];
    FileSystem hdfs = FileSystem.get(new java.net.URI(pathString), new Configuration());
    FileStatus[] status = hdfs.listStatus(new Path(pathString));

    for (FileStatus fileStatus : status) {
      if (!fileStatus.getPath().toString().endsWith("_SUCCESS") && !fileStatus.getPath().toString().endsWith(".crc")) {
    	BufferedReader br = new BufferedReader(new InputStreamReader(hdfs.open(fileStatus.getPath()))); // opening part-r file
        String[] keyValueSplit = br.readLine().split("\t"); // splitting by tab to get centroidId and newCentroids
        int centroidId = Integer.parseInt(keyValueSplit[0]); // getting centroidId
        String[] dataRow = keyValueSplit[1].split(","); // getting newCentroids
        dataRows[centroidId] = new DataRow(dataRow); // creating new DataRow objects from newCentroids
        br.close();
      }
    }
    hdfs.delete(new Path(pathString), true);
    return dataRows;
  }

  // this function checks if the difference between old and new centroids is less than a threshold.
  private static boolean checkConvergence(DataRow[] oldCentroids, DataRow[] newCentroids, float threshold) {
    boolean check;
    for(int i = 0; i < oldCentroids.length; i++) {
      check = oldCentroids[i].calculateDistance(newCentroids[i]) <= threshold;
      if(!check) {
        return false;
      }
    }
    return true;
  }

  // creates an output file with all the new centroids after convergence is met.
  private static void writeOutput(Configuration conf, DataRow[] centroids, String output)
          throws IOException, URISyntaxException {
    FileSystem hdfs = FileSystem.get(new java.net.URI(output), new Configuration());
    FSDataOutputStream dos = hdfs.create(new Path(output + "/ClusterValues.txt"), true);
    BufferedWriter br = new BufferedWriter(new OutputStreamWriter(dos));

    for (DataRow centroid : centroids) {
      br.write(centroid.toString());
      br.newLine();
    }
    br.close();
    hdfs.close();
  }

  public static void main(final String[] args) {

    if (args.length != 4) {
      throw new Error("Invalid input arguments. Required format:\n<input-dir> <output-dir> <centroid-dir> <params>");
    }

    try {
      ToolRunner.run(new KMeans(), args);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

}
