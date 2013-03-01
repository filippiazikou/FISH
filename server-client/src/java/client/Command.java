package client;

public class Command {

    private String userName;
    private float amount;
    private CommandName commandName;

    static enum CommandName {

        list, share, unshare, get, changefolder, exit, help, currentfolder;
    };

    public Command() {
    }

    public String getUserName() {
        return userName;
    }

    public float getAmount() {
        return amount;
    }

    public CommandName getCommandName() {
        return commandName;
    }

    public Command(CommandName commandName, float amount) {
        this.commandName = commandName;
        this.amount = amount;
    }
}
