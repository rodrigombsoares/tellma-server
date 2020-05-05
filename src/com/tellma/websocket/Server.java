package com.tellma.websocket;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;


@ServerEndpoint("/")
public class Server {
	@OnOpen
	public void handleOpen() {
		System.out.println("Client connected...");
	}
	@OnMessage
	public String handleMessage(String message) {
		System.out.println("Message: "+message);
		String reply = "echo "+message;
		System.out.println("sent: "+reply);
		return reply;
	}
	@OnClose
	public void handleClose() {
		System.out.println("Client disconected...");
	}
	@OnError
	public void handleError(Throwable t) {
		t.printStackTrace();
	}
}
