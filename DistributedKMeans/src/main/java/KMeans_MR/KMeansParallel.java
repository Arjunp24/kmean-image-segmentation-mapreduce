package KMeans_MR;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Scanner;

public class KMeansParallel extends Configured implements Tool {

  @Override
  public int run(String[] args) throws Exception {
    Configuration conf = getConf();
    FileSystem hdfs = FileSystem.get(new java.net.URI(args[5]), new Configuration());
    FileStatus[] fileStat = hdfs.listStatus(new Path(args[5]));

    List<String> params = new ArrayList<>();
    for (FileStatus fileStatus : fileStat) {
      if (fileStatus.getPath().toString().endsWith(args[3].split("/")[args[3].split("/").length-1])) {
        BufferedReader br = new BufferedReader(new InputStreamReader(hdfs.open(fileStatus.getPath())));
        String line = br.readLine();
        while (line != null) {
          params.add(line);
          line = br.readLine();
        }
        br.close();
      }
    }
//    String line = br.readLine();
//    while (line != null) {
//      params.add(line);
//      line = br.readLine();
//    }
    //int maxIterations = Integer.parseInt(args[4].split("/")[args[4].split("/").length-1]);
    int maxIterations = Integer.parseInt(args[4]);
    int k[] = new int[params.size()];
    int i = 0;
    for (String x : params)
      k[i++] = Integer.parseInt(x);
    i = 0;
    float threshold = 0.0001f;
    boolean status;
    boolean converged[] = new boolean[k.length];
    Map<Integer, List<DataRow>> oldCentroids = new HashMap<>();
    for (int x : k) {
      oldCentroids.put(x, new ArrayList<>());
      converged[i] = false;
      conf.setBoolean("k" + x, converged[i++]);
    }
    i = 0;
    hdfs = FileSystem.get(new java.net.URI(args[5]), new Configuration());
    fileStat = hdfs.listStatus(new Path(args[5]));

    List<String> lines = new ArrayList<>();
    for (FileStatus fileStatus : fileStat) {
      if (fileStatus.getPath().toString().endsWith("Values.txt")) {
        BufferedReader br = new BufferedReader(new InputStreamReader(hdfs.open(fileStatus.getPath())));
        String line = br.readLine();
        while (line != null) {
          lines.add(line);
          line = br.readLine();
        }
        br.close();
      }
    }
    // int x = 0;
    for (int x : k) {
      for (int z = 0; z < x; z++) {
        oldCentroids.get(x).add(new DataRow(lines.get(z).split(",")));
        conf.set("centroid." + x + "." + z, oldCentroids.get(x).get(z).toString());
      }
    }


    Map<Integer, List<DataRow>> newCentroids = new HashMap<>();

    boolean con = false;


    while (!con) {
      i++;
      Job job = Job.getInstance(conf, "Kmeans");
      job.addCacheFile(new Path(args[3]).toUri());
      FileInputFormat.addInputPath(job, new Path(args[0]));
      FileOutputFormat.setOutputPath(job, new Path(args[1]));
      setConfiguration(job, k.length);

      status = job.waitForCompletion(true);
      if (!status) {
        System.out.println("Failed at iteration: " + i);
        System.exit(1);
      }

      newCentroids = readCentroids(conf, k, args[1]);
      converged = checkConvergence(oldCentroids, newCentroids, threshold, k);
      con = true;
      for (int j = 0; i < k.length; i++) {
        conf.unset("k" + k[i]);
        conf.setBoolean("k" + k[i], converged[i]);
        con = con && converged[i];
      }

      if (con || i == (maxIterations - 1)) {
        writeOutput(conf, newCentroids, args[1], k);
        break;
      } else {
        for (int j = 0; j < k.length; j++) {
          for (int z = 0; z < k[j]; z++) {
            conf.unset("centroid." + k[j] + "." + j);
            conf.set("centroid." + k[j] + "." + j, newCentroids.get(k[j]).get(j).toString());
          }
        }
      }
    }
    return 1;
  }

  private void setConfiguration(Job job, int k) {
    job.setJarByClass(KMeansParallel.class);
    job.setMapperClass(KMeansParallelMapper.class);
    job.setReducerClass(KMeansParallelReducer.class);
    job.setCombinerClass(KMeansParallelCombiner.class);
    job.setNumReduceTasks(k);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(DataRow.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
  }

  private static Map<Integer, List<DataRow>> readCentroids(Configuration conf, int k[], String pathString)
          throws IOException, URISyntaxException {
    Map<Integer, List<DataRow>> dataRows = new HashMap<>();
    for (int i = 0; i < k.length; i++) {
      dataRows.put(k[i], new ArrayList<>());
    }
    FileSystem hdfs = FileSystem.get(new java.net.URI(pathString), new Configuration());
    FileStatus[] status = hdfs.listStatus(new Path(pathString));

    for (FileStatus fileStatus : status) {
      if (!fileStatus.getPath().toString().endsWith("_SUCCESS") && !fileStatus.getPath().toString().endsWith(".crc")) {
        BufferedReader br = new BufferedReader(new InputStreamReader(hdfs.open(fileStatus.getPath())));
        String line = br.readLine();
        while (line != null) {
          String[] keyValueSplit = line.split("\t");
          String[] keyMapListSplit = keyValueSplit[0].split(",");
          int mapId = Integer.parseInt(keyMapListSplit[0]);
          String[] dataRow = keyValueSplit[1].split(",");
          dataRows.get(mapId).add(new DataRow(dataRow));
          line = br.readLine();

        }
        br.close();
      }
    }
    hdfs.delete(new Path(pathString), true);
    return dataRows;
  }

  private static boolean[] checkConvergence(Map<Integer, List<DataRow>> oldCentroids, Map<Integer, List<DataRow>> newCentroids, float threshold, int k[]) {
    boolean check[] = new boolean[k.length];
    for (int i = 0; i < k.length; i++) {
      check[i] = true;
    }
    
    for (int i = 0; i < k.length; i++) {
      if (newCentroids.containsKey(k[i])) {
        for (int j = 0; j < newCentroids.get(k[i]).size(); j++) {
          boolean c = oldCentroids.get(k[i]).get(j).calculateDistance(newCentroids.get(k[i]).get(j)) <= threshold;
          if (!c) {
            check[i] = false;
          }
        }
      }
    }
    return check;
  }

  private static void writeOutput(Configuration conf, Map<Integer, List<DataRow>> centroids, String output, int k[]) throws
          IOException, URISyntaxException {
    FileSystem hdfs = FileSystem.get(new java.net.URI(output), new Configuration());
    FSDataOutputStream dos = hdfs.create(new Path(output + "/ClusterValues.txt"), true);
    BufferedWriter br = new BufferedWriter(new OutputStreamWriter(dos));

    for (int i = 0; i < k.length; i++) {
      br.write("Centroids for K=" + k[i]);
      br.newLine();
      for (int j = 0; j < centroids.get(k[i]).size(); j++) {
        br.write(centroids.get(k[i]).get(j).toString());
        br.newLine();
      }
      br.newLine();
    }
    br.close();
    hdfs.close();
  }

  public static void main(final String[] args) {

    if (args.length != 6) {
	System.out.println("Args = ");
	for(String temp: args)
		System.out.println(temp);
      throw new Error("Invalid input arguments. Required format:\n<input-dir> <output-dir> <max-iterations> <centroid-dir> <params> <maxitr> <upload>");
    }
	for(String x: args)
		System.out.println(x);

    try {
      ToolRunner.run(new KMeansParallel(), args);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

}
