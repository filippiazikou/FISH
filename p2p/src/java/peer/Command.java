package peer;
/**
 * Command abstraction
 * @author filippia zikou, georgios paralykidis
 */
public class Command {

    private CommandName commandName;

    static enum CommandName {

        search,mysharedfiles, mydownloadfiles, exit, help;
    };

    public Command() {
    }

    /**
     * Get command name from Command object
     * @return 
     */
    public CommandName getCommandName() {
        return commandName;
    }

    public Command(CommandName commandName, float amount) {
        this.commandName = commandName;
    }
}
