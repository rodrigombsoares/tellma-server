package com.tellma.websocket;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;


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
	public void handleMessage(@PathParam("roomId") int roomId, String message, Session userSession) throws IOException{
		// Get all users inside the room
		Set<Session> roomUsers = rooms.get(roomId);
		Iterator<Session> iterator = roomUsers.iterator();
		// Convert message to JSON and insert datetime value
		JsonReader jsonReader = Json.createReader(new StringReader(message));
	    JsonObject response = jsonReader.readObject();
	    Date today = new Date();
	    response = jsonObjectToBuilder(response).add("datetime", today.toString()).build();
	    // Send response to all participants
		while (iterator.hasNext()) iterator.next().getBasicRemote().sendText(response.toString());
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
	
	private JsonObjectBuilder jsonObjectToBuilder(JsonObject jo) {
	    JsonObjectBuilder job = Json.createObjectBuilder();

	    for (Entry<String, JsonValue> entry : jo.entrySet()) {
	        job.add(entry.getKey(), entry.getValue());
	    }

	    return job;
	}
}
