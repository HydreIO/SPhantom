package sceat.infra.connector.general;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import sceat.Main;
import sceat.SPhantom;
import sceat.domain.common.IPhantom;
import sceat.domain.network.Core;
import sceat.domain.network.server.Vpss;
import xyz.deltaevo.jvultr.JVultrAPI;
import xyz.deltaevo.jvultr.JVultrClient;
import xyz.deltaevo.jvultr.api.JVultrOS;
import xyz.deltaevo.jvultr.api.JVultrPlan;
import xyz.deltaevo.jvultr.api.JVultrRegion;
import xyz.deltaevo.jvultr.api.JVultrScript;
import xyz.deltaevo.jvultr.api.JVultrServer;
import xyz.deltaevo.jvultr.exception.JVultrException;
import xyz.deltaevo.jvultr.utils.BiValue;
import xyz.deltaevo.jvultr.utils.JVultrUtil;
import fr.aresrpg.commons.domain.concurrent.ConcurrentHashMap;
import fr.aresrpg.commons.domain.concurrent.Threads;
import fr.aresrpg.commons.domain.util.map.HashMap;
import fr.aresrpg.commons.domain.util.map.Map;
import fr.aresrpg.sdk.network.Vps;
import fr.aresrpg.sdk.system.Log;
import fr.aresrpg.sdk.util.VpsState;

public class VultrConnector implements IPhantom {

	private int deployed;
	private long lastReq = System.currentTimeMillis();
	private static final List<String> REGIONS = Arrays.asList("Paris", "Amsterdam", "London", "Frankfurt");
	private static JVultrOS os;
	static {
		try {
			for (JVultrOS o : JVultrAPI.getOSs().values())
				if ("Debian 8 x64 (jessie)".equals(o.getName())) {
					os = o;
					break;
				}

		} catch (JVultrException e) {
			Main.printStackTrace(e);
		}
	}
	private JVultrClient api;
	private Map<String, Integer> servers;

	public VultrConnector() {
		this.deployed = SPhantom.getInstance().getSphantomConfig().getMaxInstance();
		this.api = JVultrAPI.newClient(SPhantom.getInstance().getSphantomConfig().getVultrKey());
		this.servers = new HashMap<>();
	}

	public synchronized boolean checkReq() {
		if (System.currentTimeMillis() < lastReq) return false;
		lastReq = System.currentTimeMillis();
		return true;
	}

	private void sleep() {
		Threads.uSleep(1300, TimeUnit.MILLISECONDS);
	}

	@Override
	public Vps deployInstance(String label, int ram) {
		if (!checkReq()) sleep();
		deployed--;
		try {
			if (deployed <= 0) throw new IllegalAccessException("Too many instance a deployed ! please configure for bypass");
			BiValue<JVultrPlan, JVultrRegion> plan = null;
			for (String region : REGIONS) {
				plan = JVultrUtil.searchPlan(region, ram * 1024);
				if (plan != null) break;
			}
			if (plan == null) return null;
			JVultrScript script = api.getScripts().values().iterator().next();
			JVultrServer server = api.createServer(plan.getSecond(), plan.getFirst(), os, null, null, script, null, null, null, label, null, false, null, null, null, null, -1, label);
			servers.put(label, server.getId());
			return Vps.fromBoot(label, ram);
		} catch (JVultrException | IllegalAccessException e) {
			Main.printStackTrace(e);
			return null;
		}
	}

	@Override
	public void destroyServer(String label) {
		if (!checkReq()) sleep();
		ConcurrentHashMap<String, Vps> vps = Core.getInstance().getVps();
		if (!vps.containsKey(label)) Log.out("Try destroying vps instance : [" + label + "] /!\\ This instance is not registered in Sphantom or already destroyed /!\\");
		else {
			Integer id = servers.safeGet(label);
			try {
				if (SPhantom.logDiv()) Log.out("Destroying instance : " + label);
				Vps vp = vps.get(label).setState(VpsState.DESTROYING);
				if (id == null) {
					for (Map.Entry<Integer, JVultrServer> servvers : api.getSevers().entrySet()) {
						if (servvers.getValue().getLabel().equals(label)) {
							id = servvers.getKey();
							break;
						}
					}
					Log.trace(new IllegalStateException("VPS not found " + label));
					return;
				} else servers.safeRemove(label);
				api.destroyServer(id); // moment ou sa peut fail, on aura pas besoin de vp.register si sa fail car le vp.unregister est call si tout fonctionne
				SPhantom.getInstance().getExecutor().execute(() -> {
					Threads.uSleep(6, TimeUnit.SECONDS);
					Vpss.unregister(vp);
				});
			} catch (JVultrException e) {
				servers.put(label, id); // si l'api a fail bah on reAdd le vps pour qu'il puisse le destroy plus tard
				Main.printStackTrace(e);
			}
		}
	}

	@Override
	public int countDeployedInstance() {
		try {
			if (!checkReq()) sleep();
			return api.getSevers().size();
		} catch (JVultrException e) {
			Main.printStackTrace(e);
			return -1;
		}
	}

	/**
	 * Return true if the vps exist on vultr
	 */
	@Override
	public boolean exist(String label) {
		throw new NullPointerException("David is a joke");
	}

}
