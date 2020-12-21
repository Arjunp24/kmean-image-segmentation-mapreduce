package KMeans_MR;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KMeansParallelMapper extends Mapper<LongWritable, Text, Text, DataRow> {

  private Map<Integer, List<DataRow>> centroids;
  private final DataRow row = new DataRow();
  private int finalCentroid;
  List<Integer> k;

  public void setup(Context context) throws IOException {
    k = new ArrayList<>();
    URI[] uris = context.getCacheFiles();
    for (URI url : uris) {
      String[] split = url.toString().split("/");
      BufferedReader br = new BufferedReader(new FileReader(split[split.length - 1]));
      String line = br.readLine();

      while (line != null) {
        k.add(Integer.parseInt(line));
        line = br.readLine();
      }
    }
    this.centroids = new HashMap<>();
    for (int x : k) {
      if (!context.getConfiguration().getBoolean("k" + x, true))
        this.centroids.put(x, new ArrayList<>());
    }
    for (int x : k) {
      if (!context.getConfiguration().getBoolean("k" + x, true)) {
        for (int j = 0; j < x; j++) {

          String[] centroid = context.getConfiguration().getStrings("centroid." + x + "." + j);
          this.centroids.get(x).add(new DataRow(centroid));
        }
      }
    }

  }

  @Override
  protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

    String[] dataString = value.toString().split(",");
    row.setFeatures(dataString);
    for (int i : centroids.keySet()) {
      finalCentroid = row.findClosestCentroidList(centroids.get(i));
      row.setClosestCentroid(finalCentroid);
      context.write(new Text(i+"."+finalCentroid), row);
    }
    // }
  }
}
