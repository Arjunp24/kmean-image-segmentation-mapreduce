package KMeans_MR;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class KMeansMapper extends Mapper<LongWritable, Text, IntWritable, DataRow> {

    private DataRow[] centroids;
    private final DataRow row = new DataRow();
    private final IntWritable finalCentroid = new IntWritable();

    public void setup(Context context) {
        int k = Integer.parseInt(context.getConfiguration().get("k"));
        this.centroids = new DataRow[k];
        for(int i = 0;i < k; i++) {
            String[] centroid = context.getConfiguration().getStrings("centroid." + i);
            this.centroids[i] = new DataRow(centroid);
        }

    }
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

        String[] dataString = value.toString().split(",");
        row.setFeatures(dataString);

        int closestCentroid = row.findClosestCentroid(centroids);
        finalCentroid.set(closestCentroid);
        context.write(finalCentroid, row);

    }
}
