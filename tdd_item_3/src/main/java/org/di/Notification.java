package org.di;

public class Notification {
    private MessageSender messageSender;

    public Notification(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public void sendMessage(String cellPhone, String message){
        messageSender.send(cellPhone, message);
    }

    public static void main(String []args){
        MessageSender sender = new SmsSender();
        Notification notification = new Notification(sender);
        notification.sendMessage("1812340967", "短信验证码：11wq23");
    }
}
