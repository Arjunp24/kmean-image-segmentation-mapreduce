package KMeans_MR;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

public class DataRow implements Writable {

    // array of feature values for every row in the dataset.
    private float[] features;
    // number of features; by default 3 as features here represent RGB values.
    private int featuresDimension;
    // by default 1; some DataRow objects will be a sum of many DataRow objects that belong to the same centroid,
    // in this case, this is number of such DataRow objects.
    private int numberOfRows;
    // the centroidId this row is closest to
    private int closestCentroid;

    // constructors.
    public DataRow(String[] centroid) {
        this.setFeatures(centroid);
    }

    public DataRow() {
        this.featuresDimension = 0;
    }

    public DataRow(float[] features) {
        this.setFeatures(features);
    }

    // getters and setters.
    private void setFeatures(float[] features) {
        this.features = features;
        this.featuresDimension = features.length;
        this.numberOfRows = 1;
    }

    public void setClosestCentroid(int closestCentroid) {
        this.closestCentroid = closestCentroid;
    }

    // creates a copy of a DataRow object.
    public static DataRow createNewDataRow(DataRow row) {
        DataRow newRow = new DataRow(row.features);
        newRow.numberOfRows = row.numberOfRows;
        return newRow;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(this.featuresDimension);
        dataOutput.writeInt(this.numberOfRows);
        for(int i = 0; i < this.featuresDimension; i++) {
            dataOutput.writeFloat(this.features[i]);
        }
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        this.featuresDimension = dataInput.readInt();
        this.numberOfRows = dataInput.readInt();
        this.features = new float[featuresDimension];
        for(int i = 0; i < this.featuresDimension; i++) {
            this.features[i] = dataInput.readFloat();
        }
    }

    // converts list of data points into DataRow object.
    public void setFeatures(String[] dataString) {
        this.features = new float[dataString.length];
        this.featuresDimension = dataString.length;
        this.numberOfRows = 1;
        for (int i = 0; i < dataString.length; i++) {
            this.features[i] = Float.parseFloat(dataString[i]);
        }
    }

    // calculates the Euclidean between the features of 2 DataRow objects.
    public float calculateDistance(DataRow centroid) {
        float distance = 0.0f;

        for (int i = 0; i < this.featuresDimension; i++) {
            distance += Math.pow(Math.abs(this.features[i] - centroid.features[i]), this.featuresDimension);
        }
        distance = (float)Math.round(Math.pow(distance, 1f/this.featuresDimension) * 100000) / 100000.0f;
        return distance;
    }

    // returns the index of the closest centroid to a DataRow object.
    public int findClosestCentroid(DataRow[] centroids) {
        float minimumDistance = Float.POSITIVE_INFINITY;
        int closestCentroid = -1;
        float distance;

        for(int i =0;i < centroids.length;i++) {
            distance = this.calculateDistance(centroids[i]);
            if(distance < minimumDistance) {
                minimumDistance = distance;
                closestCentroid = i;
            }
        }
        return closestCentroid;
    }

    public int findClosestCentroidList(List<DataRow> centroids) {
        float minimumDistance = Float.POSITIVE_INFINITY;
        int closestCentroid = -1;
        float distance;

        for(int i =0;i < centroids.size();i++) {
            distance = this.calculateDistance(centroids.get(i));
            if(distance < minimumDistance) {
                minimumDistance = distance;
                closestCentroid = i;
            }
        }
        return closestCentroid;
    }

    // Adds up the features between 2 DataRow objects to get partial sums and increments the numberOfRows.
    public void sumDataRow(DataRow row) {
        for (int i = 0; i < this.featuresDimension; i++) {
            this.features[i] += row.features[i];
        }
        this.numberOfRows += row.numberOfRows;
    }

    // calculates the average of a DataRow object which has the final sums.
    public void calculateNewCentroid() {
        for (int i = 0; i < this.featuresDimension; i++) {
            this.features[i] = (float)Math.round(this.features[i] / this.numberOfRows * 100000) / 100000.0f;
        }
        this.numberOfRows = 1;
    }

    @Override
    public String toString() {
       StringBuilder sb = new StringBuilder();
       for(float x: this.features){
         sb.append(x+",");
       }
       sb.substring(0,sb.length()-2);
      return sb.toString();
    }
}
