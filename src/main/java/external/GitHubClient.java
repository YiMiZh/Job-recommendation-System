package external;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class GitHubClient {
	private static final String URL_TEMPLATE = "https://jobs.github.com/positions.json?description=%s&lat=%s&long=%s";
	// no keyword, by default we search for developer
	private static final String DEFAULT_KEYWORD = "developer";

	public List<Item> search(double lat, double lon, String keyword) {
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		// encode keyword for search
		try {
			keyword = URLEncoder.encode(keyword, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String url = String.format(URL_TEMPLATE, keyword, lat, lon);
//		System.out.println(url);
		CloseableHttpClient httpclient = HttpClients.createDefault();

	    // Create a custom response handler.
		// Returned response is JSONArray, each JSON is job and its information.
	    ResponseHandler<List<Item>> responseHandler = new ResponseHandler<List<Item>>() {
	    	
	    	@Override
	        public List<Item> handleResponse(
	                final HttpResponse response) throws IOException {
	    		// != 200 means it's not a valid request.
	            if (response.getStatusLine().getStatusCode() != 200) {
	            	return new ArrayList<>();
	            }
	            HttpEntity entity = response.getEntity();
	            // Case of empty entity
	            if (entity == null) {
	            	return new ArrayList<>();
	            }
	            String responseBody = EntityUtils.toString(entity);
	            JSONArray array = new JSONArray(responseBody); 
//	            System.out.println(array.length());
	            return getItemList(array);
	        }
	    };
	    
	    //Execute the Get request by passing the response handler object and HttpGet object
	    try {
			return httpclient.execute(new HttpGet(url), responseHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new ArrayList<>();

	}
	
	private List<Item> getItemList(JSONArray array) {
		List<Item> itemList = new ArrayList<>();
		// we need to extract keywords from description in order to provide better
		// recommendation.
		List<String> descriptionList = new ArrayList<>();
		
		for (int i = 0; i < array.length(); i++) {
			// We need to extract keywords from description since GitHub API
			// doesn't return keywords.
			String description = getStringFieldOrEmpty(array.getJSONObject(i), "description");
			// In case some jobs don't have description, we use its title as description.
			if (description.equals("") || description.equals("\n")) {
				descriptionList.add(getStringFieldOrEmpty(array.getJSONObject(i), "title"));
			} else {
				descriptionList.add(description);
			}	
		}
		
		/*                  ----!!!!!Important!!!!!----                  */
		// We need to get keywords from multiple text in one request since
		// MonkeyLearnAPI has limitations on request per minute.
		List<List<String>> keywords = MonkeyLearnClient
				.extractKeywords(descriptionList.toArray(new String[descriptionList.size()]));
		
		for (int i = 0; i < array.length(); ++i) {
			JSONObject object = array.getJSONObject(i);
			ItemBuilder builder = new ItemBuilder();
			
			builder.setItemId(getStringFieldOrEmpty(object, "id"));
			builder.setName(getStringFieldOrEmpty(object, "title"));
			builder.setAddress(getStringFieldOrEmpty(object, "location"));
			builder.setUrl(getStringFieldOrEmpty(object, "url"));
			builder.setImageUrl(getStringFieldOrEmpty(object, "company_logo"));
			builder.setKeywords(new HashSet<String>(keywords.get(i)));
			
			Item item = builder.build();
			itemList.add(item);
		}
		
		return itemList;
	}
	
	private String getStringFieldOrEmpty(JSONObject obj, String field) {
		return obj.isNull(field) ? "" : obj.getString(field);
	}
}

