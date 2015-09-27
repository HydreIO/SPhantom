package sceat.domain.shell;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import sceat.SPhantom;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * This class executes the shell script on the remote server
 * 
 * Requires the jSch java library
 * 
 * @author Saket kumar
 * 
 *
 */

public class ShellExecuter {

	public static final ShellExecuter OVH_1 = new ShellExecuter("_", "_", "94.23.218.25", 22);

	private JSch jsch;
	private Session session;

	public ShellExecuter(String user, String pass, String host, int port) {
		try {
			this.jsch = new JSch();
			this.session = getJsch().getSession(user, host, port);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(pass);
		} catch (Exception e) {
			SPhantom.printStackTrace(e);
		}
	}

	public void openSession() {
		if (!getSession().isConnected()) try {
			getSession().connect();
		} catch (JSchException e) {
			SPhantom.printStackTrace(e);
		}
	}

	public void closeSession() {
		if (getSession().isConnected()) getSession().disconnect();
	}

	public void runScript(String cmd) {
		openSession();
		try {
			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
			channelExec.setCommand(cmd);
			channelExec.connect();
			int exit = channelExec.getExitStatus();
			channelExec.disconnect();
			closeSession();
			if (exit < 0) SPhantom.print(cmd + " éxécuté avec succes ! exitstatus inexisant !");
			else if (exit > 0) SPhantom.print(cmd + " éxécuté avec erreurs ! /!\\");
			else SPhantom.print(cmd + " éxécuté avec succes ! exitstatus inexisant !");
		} catch (JSchException e) {
			SPhantom.printStackTrace(e);
		}
	}

	public JSch getJsch() {
		return jsch;
	}

	public Session getSession() {
		return session;
	}

	/**
	 * This method will execute the script file on the server. This takes file name to be executed as an argument The result will be returned in the form of the list
	 * 
	 * @param scriptFileName
	 * @return
	 */
	public List<String> executeFileResult(String scriptFileName) {
		List<String> result = new ArrayList<String>();
		try {
			openSession();

			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");

			InputStream in = channelExec.getInputStream();

			channelExec.setCommand("sh " + scriptFileName);
			channelExec.connect();

			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;

			while ((line = reader.readLine()) != null) {
				result.add(line);
			}

			int exit = channelExec.getExitStatus();
			channelExec.disconnect();
			closeSession();
			if (exit < 0) SPhantom.print(scriptFileName + " éxécuté avec succes ! exitstatus inexisant !");
			else if (exit > 0) SPhantom.print(scriptFileName + " éxécuté avec erreurs ! /!\\");
			else SPhantom.print(scriptFileName + " éxécuté avec succes ! exitstatus inexisant !");

		} catch (Exception e) {
			SPhantom.printStackTrace(e);
		}
		return result;
	}

}
