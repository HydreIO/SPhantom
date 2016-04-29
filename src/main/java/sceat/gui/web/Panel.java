package sceat.gui.web;

import java.util.Map;
import java.util.Set;

import sceat.domain.adapter.api.PhantomApi;
import sceat.domain.adapter.api.PhantomApi.ServerApi;
import sceat.domain.adapter.api.PhantomApi.VpsApi;
import sceat.domain.shell.Input;

public class Panel {

	public static void main(String[] args) {
		Map<String, VpsApi> allVps = PhantomApi.getAllVps();// ex
		Set<ServerApi> allServers = allVps.get("").getAllServers();
		allServers.forEach(srv -> srv.getStatus());

		// pour le SytemIn
		Input.register(System.out::print);
	}

}
