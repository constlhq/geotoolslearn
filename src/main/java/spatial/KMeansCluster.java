package spatial;
import org.apache.commons.math3.ml.clustering.*;
import org.apache.commons.math3.ml.distance.CanberraDistance;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class KMeansCluster {

  public static class AttributeWaper implements Clusterable {

    public int id;
    private double[] vector;
    public int categoryId;


    public AttributeWaper(int id,double[] vector) {
      this.vector = vector;
      this.id = id;
    }

    public void setCategoryId(int categoryId) {
      this.categoryId = categoryId;
    }

    @Override
    public double[] getPoint(){
      return this.vector;
    }
  }

  public static Map<Integer,Integer> cluster(List<Integer> ids , List<double[]> vectors,int kClass){
    List<AttributeWaper> clusterInput = new LinkedList<>();
    int[] cur = {0};
    vectors.stream().forEach(pt->{
      clusterInput.add(new AttributeWaper(ids.get(cur[0]),pt));
      cur[0]++;
    });

    KMeansPlusPlusClusterer<AttributeWaper> kMeansPlusPlusClusterer = new KMeansPlusPlusClusterer<>(kClass, 1000,new CanberraDistance());
    MultiKMeansPlusPlusClusterer<AttributeWaper> multiKMeansPlusPlusClusterer = new MultiKMeansPlusPlusClusterer<>(kMeansPlusPlusClusterer,20);

    //    DBSCANClusterer<AttributeWaper> dbscanClusterer = new DBSCANClusterer<>(0.1, 1000, new EuclideanDistance());

    List<CentroidCluster<AttributeWaper>> clusterResults = multiKMeansPlusPlusClusterer.cluster(clusterInput);

    Map<Integer,Integer> id_catId_map = new HashMap<>();

    for (int i=0; i<clusterResults.size(); i++) {
      for (AttributeWaper attributeWaper : clusterResults.get(i).getPoints()){
        id_catId_map.put(attributeWaper.id,i);
      }
    }
    return id_catId_map;
  }

}
