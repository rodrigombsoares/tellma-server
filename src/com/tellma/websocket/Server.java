package com.tellma.websocket;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;


@ServerEndpoint("/{roomId}")
public class Server {
	//store the session from the active users on the room
	static HashMap<Integer, Set<Session>> rooms = new HashMap<Integer, Set<Session>>();
	
	@OnOpen
	public void handleOpen(@PathParam("roomId") int roomId, Session userSession) {
		System.out.println("Client asked to join room "+roomId);
		try {
			// Get list of user on that room (if room exists)
			Set<Session> roomUsers = rooms.get(roomId);
			// Add user session to that list
			roomUsers.add(userSession);
		}
		catch(Exception e) {
			// In case no list is found, create a new one
			Set<Session> roomUsers = Collections.synchronizedSet(new HashSet<Session>());
			// Insert user
			roomUsers.add(userSession);
			// Insert into rooms map
			rooms.put(roomId, roomUsers);
		}
	}
	@OnMessage
	public void handleMessage(@PathParam("roomId") int chatId, String message, Session userSession) throws Throwable{
		
		// Get all users inside the room
		Set<Session> roomUsers = rooms.get(chatId);
		Iterator<Session> iterator = roomUsers.iterator();

		JSONObject jsonMessage = new JSONObject(message.replaceAll("\r?\n", ""));
		// Post message to database
		String response = postMessage(jsonMessage, chatId);
	    // Send response to all participants
		while (iterator.hasNext())
			iterator.next().getBasicRemote().sendText(response);
	}
	@OnClose
	public void handleClose(@PathParam("roomId") int roomId, Session userSession) {
		Set<Session> roomUsers = rooms.get(roomId);
		System.out.println("Client disconected...");
		roomUsers.remove(userSession);
		if(roomUsers.isEmpty()) {
			rooms.remove(roomId);
		}
		else {
			System.out.println(roomUsers.size()+" people on room "+roomId);
			System.out.println(rooms.size()+" rooms open");
		}
	}
	@OnError
	public void handleError(@PathParam("roomId") int roomId, Throwable t) {
		t.printStackTrace();
	}
	
	private String postMessage(JSONObject jsonMessage, int chatId) throws IOException, JSONException {
		CloseableHttpClient client = HttpClients.createDefault();
	    HttpPost httpPost = new HttpPost("http://34.71.71.141/apirest/messages");
	    String text = jsonMessage.getString("text");
	    String userId = jsonMessage.getString("userId");
	    
	    String json = "{\"text\": \""+text+"\",\"chatId\": "+chatId+",\"userId\": "+userId+"}";
	    StringEntity entity = new StringEntity(json);
	    httpPost.setEntity(entity);
	    httpPost.setHeader("Accept", "application/json");
	    httpPost.setHeader("Content-type", "application/json");
	 
	    CloseableHttpResponse response = client.execute(httpPost);
	    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
	    client.close();
	    return responseBody;
	}
}
