package sceat.domain.shell;

/*import net.neoremind.sshxcute.core.ConnBean;
 import net.neoremind.sshxcute.core.Result;
 import net.neoremind.sshxcute.core.SSHExec;
 import net.neoremind.sshxcute.exception.TaskExecFailException;
 import net.neoremind.sshxcute.task.impl.ExecShellScript;*/

/**
 * Not actually used
 * 
 * @author MrSceat
 *
 */
public class ShellExecuter {
	/*
	 * private SSHExec ssh;
	 * 
	 * public ShellExecuter(InetAddress adress, int port, String user, String pass) { ssh = SSHExec.getInstance(new ConnBean(adress.getHostAddress(), user, pass)); }
	 * 
	 * private void connect() { getSsh().connect(); }
	 * 
	 * private void disconnect() { getSsh().disconnect(); }
	 * 
	 * public boolean executeScript(String dir, String path, String args) { try { connect(); Result res = getSsh().exec(new ExecShellScript(dir, path, args)); if (res.isSuccess) { Log.out("Return code: " + res.rc); Log.out("sysout: " + res.sysout); } else {
	 * Log.out("Return code: " + res.rc); Log.out("error message: " + res.error_msg); } return true; } catch (TaskExecFailException e) { Log.out("Unable to execute script \"" + path + "\" with args \"" + args + "\""); return false; } finally { disconnect(); } }
	 * 
	 * public SSHExec getSsh() { return ssh; }
	 */
}