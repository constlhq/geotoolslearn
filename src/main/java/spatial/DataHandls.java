package spatial;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.data.*;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDumper;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.grid.Envelopes;
import org.geotools.grid.GridFeatureBuilder;
import org.geotools.grid.Grids;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface DataHandls {

  static void drawGrid() throws Exception{

//    URL url = new File("/home/lhq/Documents/paper/广东省地级市行政区划(1W)/zsj.shp").toURI().toURL();
    URL url = new File("/home/lhq/Code/JavaProjects/jtslearn/data/zsj_town.shp").toURI().toURL();
    FileDataStore dataStore = FileDataStoreFinder.getDataStore(url);
    SimpleFeatureSource ozMapSource = dataStore.getFeatureSource();

    // Set the grid size (1 degree) and create a bounding envelope
    // that is neatly aligned with the grid size
    double sideLen = 0.03;
    ReferencedEnvelope gridBounds = Envelopes.expandToInclude(ozMapSource.getBounds(), sideLen);
    SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
    tb.setName("grid");
    tb.setCRS( DefaultGeographicCRS.WGS84 );
    tb.add(
            GridFeatureBuilder.DEFAULT_GEOMETRY_ATTRIBUTE_NAME,
            Polygon.class,
            gridBounds.getCoordinateReferenceSystem()
    );
    tb.add("id", Integer.class);
    for(int i=13;i<=43;i++){
      if(i!=16){
        tb.add("cat_"+i,Integer.class);
      }
    }

    tb.add("sum", Integer.class);
    tb.add("class", Integer.class);

    SimpleFeatureType TYPE = tb.buildFeatureType();
    GridFeatureBuilder builder = new IntersectionBuilder(TYPE, ozMapSource);
    SimpleFeatureSource grid = Grids.createHexagonalGrid(gridBounds, sideLen, -1, builder);
    dataStore.dispose();

//    System.out.println("save shapefile");

//    File gridFile = new File("/home/lhq/Code/JavaProjects/jtslearn/grid/grid.shp");
//    Map<String, Object> gridMap = new HashMap<>();
//    gridMap.put("url", gridFile.toURI().toURL());
//    DataStore gridDs = DataStoreFinder.getDataStore(gridMap);
//    String tpName = gridDs.getTypeNames()[0];
//    FeatureSource<SimpleFeatureType, SimpleFeature> gridSource = gridDs.getFeatureSource(tpName);


//    Map<String, Object> map = new HashMap<>();
//    map.put("url", shpurl);
//    DataStore ds = DataStoreFinder.getDataStore(map);
//    String typeName = ds.getTypeNames()[0];
//    FeatureSource<SimpleFeatureType, SimpleFeature> source =ds.getFeatureSource(typeName);

//    String geometryPropertyName = source.getSchema().getGeometryDescriptor().getLocalName();

    FeatureIterator<SimpleFeature> features = grid.getFeatures().features();
    URL shpurl = new File("/home/lhq/Code/JavaProjects/jtslearn/data/ali88_done.shp").toURI().toURL();
    FileDataStore ds = FileDataStoreFinder.getDataStore(shpurl);
    SimpleFeatureSource source = ds.getFeatureSource();
//      List<int[]> vectors = new ArrayList<>();
//      List<Integer> ids = new ArrayList<>();
    SimpleFeature feature;
    FilterFactory2 ff;
    Filter filter1,filter2,filter12;
    FeatureCollection withinPts;
    while (features.hasNext()) {
      feature = features.next();
      ff = CommonFactoryFinder.getFilterFactory2();
      ReferencedEnvelope bbox = new ReferencedEnvelope(feature.getBounds());
      filter1 = ff.bbox(ff.property("the_geom"), bbox);
      filter2 = ff.intersects(ff.property("the_geom"),ff.literal(feature.getDefaultGeometry()));
      filter12 = ff.and(filter1, filter2);
      withinPts = source.getFeatures(filter12);
      // 计算网格中的点的构成
      int[] countMap= new int[31];
      Feature[] farr = (Feature[])withinPts.toArray();
      Arrays.stream(farr).forEach(feature1 -> countMap[(int)((long)feature1.getProperty("cat").getValue()-13)]++);
      for(int i=0;i<=30;i++){
        if(i!=3){
          feature.setAttribute("cat_"+(i+13),countMap[i]);
        }
      }
      feature.setAttribute("sum",Arrays.stream(countMap).reduce(0,(a,b)->a+b));

//        ids.add((Integer)feature.getAttribute("id"));
//        vectors.add(countMap);
    }


//    logger.info("start clustering");
//
//      try(OutputStream fout = new BufferedOutputStream(Files.newOutputStream(Paths.get("attributes.txt")))){
//        for(int[] vector : vectors ){
//          fout.write(Arrays.toString(vector).getBytes());
//          fout.write("\n".getBytes());
//        }
//      }catch (IOException e){
//        e.printStackTrace();
//      }


//    Map<Integer,Integer> id_catMap =  KMeansCluster.cluster(ids,vectors,10);
//    try (FeatureIterator<SimpleFeature> features_2 = grid.getFeatures().features()) {
//      while (features_2.hasNext()) {
//        SimpleFeature feature_2 = features_2.next();
//        feature_2.setAttribute("class",id_catMap.get(feature_2.getAttribute("id")));
//      }
//    }

    ShapefileDumper dumper = new ShapefileDumper(new File("/mnt/win/share/毕业论文/数据/shp/grid_3"));
    dumper.setCharset(Charset.forName("UTF-8"));
    int maxSize = 100 * 1024 * 1024;
    dumper.setMaxDbfSize(maxSize);
    Filter notAllZeroFilter = CQL.toFilter("sum > 20");
    //按制造业数量进行筛选
    SimpleFeatureCollection fc = grid.getFeatures(notAllZeroFilter);
    dumper.dump(fc);
  }

  static void pointIntersectionWithPolygon(String pointshp,String polygonshp,String newpgpath) throws Exception{

    URL pointshpurl = new File(pointshp).toURI().toURL();
    URL polygonshpurl = new File(polygonshp).toURI().toURL();
    URL newpolygonshpurl = new File(newpgpath).toURI().toURL();

    ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
    File file = new File(newpgpath);

    Map map = Collections.singletonMap("url", file.toURI().toURL());
    ShapefileDataStore newDataStore = (ShapefileDataStore) factory.createNewDataStore(map);

    SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
    tb.setName("town");
    tb.add("the_geom",MultiPolygon.class);
    tb.add("id", Integer.class);
    tb.add("town", String.class);
    tb.add("city", String.class);

    for(int i=13;i<=43;i++){
      if(i!=16){
        tb.add("cat_"+i,Integer.class);
      }
    }



    SimpleFeatureType TYPE = tb.buildFeatureType();

    newDataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);

    newDataStore.createSchema(TYPE);

    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);

    FileDataStore ptds = FileDataStoreFinder.getDataStore(pointshpurl);
    FileDataStore pgds = FileDataStoreFinder.getDataStore(polygonshpurl);

    SimpleFeatureSource ptsrc = ptds.getFeatureSource();
    SimpleFeatureSource pgsrc = pgds.getFeatureSource();

    SimpleFeature feature;
    FilterFactory2 ff;
    Filter filter1,filter2,filter12;
    FeatureCollection withinPts;

    ListFeatureCollection listFeatureCollection = new ListFeatureCollection(TYPE);
    
    try (FeatureIterator<SimpleFeature> featureIterator =  pgsrc.getFeatures().features()){
      while (featureIterator.hasNext()){
        feature = featureIterator.next();
        ff = CommonFactoryFinder.getFilterFactory2();
        ReferencedEnvelope bbox = new ReferencedEnvelope(feature.getBounds());

        filter1 = ff.bbox(ff.property("the_geom"), bbox);
        filter2 = ff.intersects(ff.property("the_geom"),ff.literal(feature.getDefaultGeometry()));

        filter12 = ff.and(filter1, filter2);

        withinPts = ptsrc.getFeatures(filter12);

        int[] countMap= new int[31];

        Feature[] farr = (Feature[])withinPts.toArray();

        Arrays.stream(farr).forEach(feature1 -> countMap[(int)((long)feature1.getProperty("cat").getValue()-13)]++);
        featureBuilder.add(feature.getDefaultGeometry());
        featureBuilder.add(feature.getAttribute(1));
        featureBuilder.add(feature.getAttribute(2));
        featureBuilder.add(feature.getAttribute(3));

        System.out.println(feature.getAttribute(0).toString().substring(0,100));
        SimpleFeature newFeature = featureBuilder.buildFeature(null);
        for(int i=0;i<=30;i++){
          if(i!=3){
            newFeature.setAttribute("cat_"+(i+13),countMap[i]);
          }
        }

        listFeatureCollection.add(newFeature);
      }
      
    Transaction transaction = new DefaultTransaction("create");
    String typeName = newDataStore.getTypeNames()[0];
    SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
    if (featureSource instanceof SimpleFeatureStore) {
      SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
      featureStore.setTransaction(transaction);
      try {
        featureStore.addFeatures(listFeatureCollection);
        transaction.commit();

      } catch (Exception problem) {
        problem.printStackTrace();
        transaction.rollback();
      } finally {
        transaction.close();
      }
      System.exit(0); // success!
    }
    }
  }
}
