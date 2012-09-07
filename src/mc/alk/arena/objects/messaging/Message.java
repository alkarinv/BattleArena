package mc.alk.arena.objects.messaging;

import java.util.HashSet;
import java.util.Set;

import mc.alk.arena.objects.messaging.MessageOptions.MessageOption;

public class Message {
	private String msg;
	private MessageOptions messageOptions;
	
	public Message(String msg, MessageOptions messageOptions) {
		this.msg = msg;
		this.messageOptions = messageOptions;		
	}

	public Set<MessageOption> getOptions() {
		return messageOptions != null ? messageOptions.getOptions() : new HashSet<MessageOption>();
	}

	public String getMessage() {
		return msg;
	}

}
