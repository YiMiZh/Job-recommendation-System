package external;

import com.monkeylearn.MonkeyLearn;
import com.monkeylearn.MonkeyLearnResponse;
import com.monkeylearn.MonkeyLearnException;
import com.monkeylearn.Tuple;
import com.monkeylearn.ExtraParam;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class MonkeyLearnClient {
	// Use the API key from your account, similar to MySQLDBConnection
	private static final String API_KEY = "1728085becb059178f82e621cda41b37fd81709f";
	
	public static void main( String[] args ) throws MonkeyLearnException {
        // Use the keyword extractor
        String[] textList = {"Elon Musk has shared a photo of the spacesuit designed by SpaceX. This is the second image shared of the new design and the first to feature the spacesuit’s full-body look."};
        List<List<String>> words = extractKeywords(textList);
		for (List<String> ws : words) {
			for (String w : ws) {
				System.out.println(w);
			}
			System.out.println();
		}
    }
	
	// Function to extract Keywords from given texts.
	public static List<List<String>> extractKeywords(String[] text) {
		if (text == null || text.length == 0) {
			return new ArrayList<>();
		}
		
		MonkeyLearn ml = new MonkeyLearn(API_KEY);
		
		ExtraParam[] extraParams = {new ExtraParam("max_keywords", "3")};
		
		MonkeyLearnResponse response;
		try {
			response = ml.extractors.extract("ex_YCya9nrn", text, extraParams);
			JSONArray resultArray = response.arrayResult;
			return getKeywords(resultArray);
		} catch (MonkeyLearnException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	// Helper to extract information we want from the response of MonkeyLearn
	private static List<List<String>> getKeywords(JSONArray mlResultArray) {
		List<List<String>> topKeywords = new ArrayList<>();
		// Iterate the result array and convert it to our format.
		for (int i = 0; i < mlResultArray.size(); ++i) {
			List<String> keywords = new ArrayList<>();
			JSONArray keywordsArray = (JSONArray) mlResultArray.get(i);
			for (int j = 0; j < keywordsArray.size(); ++j) { 
				JSONObject keywordObject = (JSONObject) keywordsArray.get(j);
				// We just need the keyword, excluding other fields.
				String keyword = (String) keywordObject.get("keyword");
				keywords.add(keyword);
			}
			topKeywords.add(keywords);
		}
		return topKeywords;
	}
}


