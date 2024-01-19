package com.petproject.model;

public enum ChatCommand {
    HELP("/help"),
    START("/start"),
    REGISTRATION("/registration"),
    CANSEL("/cansel");
    private final String value;

    ChatCommand(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static ChatCommand fromValue(String v) {
        for (ChatCommand command : ChatCommand.values()) {
            if (command.value.equals(v)) return command;
        }
        return null;
    }

}
