package nearsoft.academy.bigdata.recommendation;

import com.google.common.base.Charsets;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;


import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MovieRecommender {


    private BiMap<Integer, String> mapProducts = HashBiMap.create();
    private BiMap<Integer, String> mapUsers = HashBiMap.create();
    int totalReviews = 0;

    private File fileInput;
    private FileWriter fileWriter;

    public MovieRecommender(String path) throws IOException {
        this.fileInput = new File(path);
        this.fileWriter = new FileWriter("/home/ernesto/Desktop/movies.csv");
        readInputFile();
    }

    public int getTotalReviews(){
        return totalReviews;
    }

    public int getTotalProducts(){
        return this.mapProducts.size();
    }

    public int getTotalUsers(){
        return this.mapUsers.size();
    }



    private void readInputFile() throws IOException {


        String PRODUCT_ID ="product/productId: ";
        String USER_ID = "review/userId: ";
        String SCORE = "review/score: ";

        String readedLine;
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(fileInput.getPath()), Charsets.ISO_8859_1);


        int mapKey = 1;
        String[] data = new String[3];
        while((readedLine = bufferedReader.readLine()) != null){
            if(readedLine.contains(PRODUCT_ID)){
                String splitedLine = readedLine.split(PRODUCT_ID)[1];
                data[1] = splitedLine;
                if(!this.mapProducts.containsValue(data[1])){
                    this.mapProducts.put(mapKey, data[1]);
                    mapKey++;
                }
            }else if(readedLine.contains(USER_ID)){
                String splitedLine = readedLine.split(USER_ID)[1];
                data[0] = splitedLine;
                if(!this.mapUsers.containsValue(data[0])){
                    this.mapUsers.put(mapKey, data[0]);
                    mapKey++;
                }
            }else if(readedLine.contains(SCORE)){
                String splitedLine = readedLine.split(SCORE)[1];
                data[2] = splitedLine;
                this.totalReviews++;
            }
            if(data[0] != null && data[1] != null && data[2] != null){
                this.createCSV(data);
                data[0] = null;
                data[1] = null;
                data[2] = null;
            }
        }
        System.out.println("csv created");
        System.out.println(this.mapProducts.size());
        System.out.println(this.mapUsers.size());
        this.closeCSV();
    }

    public BiMap<Integer, String> getMap(BiMap<Integer, String> biMap, String data){
        int id = 1;
        if(!biMap.containsKey(data)){
            biMap.put(id,data);
            id++;
        }
        return biMap;
    }



    private void createCSV(String[] data){
        try {
            int userId = mapUsers.inverse().get(data[0]);
            int productId = mapProducts.inverse().get(data[1]);
            this.fileWriter.append(data[0]);
            this.fileWriter.append(",");
            this.fileWriter.append(data[1]);
            this.fileWriter.append(",");
            this.fileWriter.append(data[2]);
            this.fileWriter.append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeCSV(){
        try{
            this.fileWriter.flush();
            this.fileWriter.close();
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public List<String> getRecommendationsForUser(String userId){
        List<String> listRecommendations = new ArrayList<>();
        try {

            DataModel model = new FileDataModel(new File("/home/ernesto/Desktop/movies.csv"));
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
            List recommendations = recommender.recommend(this.mapUsers.inverse().get(userId), 3);
            for (Object recommendation : recommendations) {
                if(this.mapProducts.containsValue(recommendation)){
                    listRecommendations.add(this.mapProducts.get(this.mapProducts.get(recommendation)));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TasteException e) {
            e.printStackTrace();
        }
        return listRecommendations;
    }


}
