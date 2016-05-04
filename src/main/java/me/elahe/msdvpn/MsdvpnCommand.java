package me.elahe.msdvpn;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;

@Command(scope = "msdvpn", name = "dropRule",
	description = "Blocks the path from one host to another")
public class MsdvpnCommand extends AbstractShellCommand {
    @Override
    protected void execute() {
        SDVPN service = get(SDVPN.class);
        service.dropRule();
    }
}
