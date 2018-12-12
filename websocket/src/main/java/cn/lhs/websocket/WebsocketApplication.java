package cn.lhs.websocket;

import cn.lhs.websocket.netty.WebSocketRun;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebsocketApplication {
	public static void main(String[] args) {
		SpringApplication.run(WebsocketApplication.class, args);
		new WebSocketRun ().start ();
	}
}
