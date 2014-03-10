package mc.alk.arena.objects.messaging;

import mc.alk.arena.objects.messaging.MessageOptions.MessageOption;

import java.util.HashSet;
import java.util.Set;

public class Message {
    final private String msg;
    final private MessageOptions messageOptions;

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
