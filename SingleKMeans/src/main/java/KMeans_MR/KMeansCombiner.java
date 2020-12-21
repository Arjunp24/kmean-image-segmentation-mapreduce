package KMeans_MR;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class KMeansCombiner extends Reducer<IntWritable, DataRow, IntWritable, DataRow> {

    @Override
    public void reduce(IntWritable centroid, Iterable<DataRow> dataRows, Context context) throws IOException,
            InterruptedException {
        DataRow rowSum = DataRow.createNewDataRow(dataRows.iterator().next());
        while (dataRows.iterator().hasNext()) {
            rowSum.sumDataRow(dataRows.iterator().next());
        }

        context.write(centroid, rowSum);
    }

}
