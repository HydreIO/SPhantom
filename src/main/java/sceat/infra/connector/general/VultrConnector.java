package sceat.infra.connector.general;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import sceat.Main;
import sceat.SPhantom;
import sceat.domain.adapter.general.Iphantom;
import sceat.domain.network.Core;
import sceat.domain.network.server.Vps;
import sceat.domain.network.server.Vps.VpsState;
import xyz.deltaevo.jvultr.JVultrAPI;
import xyz.deltaevo.jvultr.JVultrClient;
import xyz.deltaevo.jvultr.api.*;
import xyz.deltaevo.jvultr.exception.JVultrException;
import xyz.deltaevo.jvultr.utils.BiValue;
import xyz.deltaevo.jvultr.utils.JVultrUtil;

public class VultrConnector implements Iphantom {
	private static final List<String> REGIONS = Arrays.asList("Paris" , "Amsterdam" , "London" , "Frankfurt");
	private static JVultrOS os;
	static {
		try {
			for(JVultrOS o : JVultrAPI.getOSs().values())
				if("Debian 8 x64 (jessie)".equals(o.getName())){
					os = o;
					break;
				}

		} catch (JVultrException e) {
			Main.printStackTrace(e);
		}
	}
	private JVultrClient api;
	private Map<String , Integer> servers;

	public VultrConnector() {
		this.api = JVultrAPI.newClient(SPhantom.getInstance().getSphantomConfig().getVultrKey());
		this.servers = new HashMap<>();
	}

	// vultr peut refuser de destroy une instance si elle a été créé ya pas longtemps, faut donc foutre une liste pour add le vps a destroy et retester tout les X temps puis le virer (en laissant le vpsState sur destroying)

	@Override
	public Vps deployInstance(String label, int ram) {
		try{
			BiValue<JVultrPlan , JVultrRegion> plan = null;
			for(String region : REGIONS){
				plan = JVultrUtil.searchPlan(region , ram*1024);
				if(plan != null)
					break;
			}
			if(plan == null)
				return null;
			JVultrScript script = api.getScripts().values().iterator().next();
			JVultrServer server = api.createServer(plan.getSecond() , plan.getFirst() , os , null ,
					null , script , null , null , null , label , null , false , null , null , null ,null);
			servers.put(label , server.getId());
			return Vps.fromBoot(label, ram, InetAddress.getByName(server.getInternalIp()));
		}catch (JVultrException | UnknownHostException e){
			Main.printStackTrace(e);
			return null;
		}
	}

	@Override
	public void destroyServer(String label) {
		ConcurrentHashMap<String, Vps> vps = Core.getInstance().getVps();
		if (!vps.contains(label))
			SPhantom.print("Try destroying vps instance : [" + label + "] /!\\ This instance is not registered in Sphantom or already destroyed /!\\");
		else {
			try {
				if (SPhantom.logDiv())
					SPhantom.print("Destroying instance : " + label);
				Vps vp = vps.get(label).setState(VpsState.Destroying);
				Integer id = servers.get(label);
				if(id == null){
					for(Map.Entry<Integer , JVultrServer> servers : api.getSevers().entrySet()){
						if(servers.getValue().getLabel().equals(label)){
							id = servers.getKey();
							break;
						}
					}
					Main.printStackTrace(new IllegalStateException("VPS not found " + label));
					return;
				}else
					servers.remove(label);
				api.destroyServer(id);
				SPhantom.getInstance().getExecutor().execute(() -> {
					try {
						Thread.sleep(6000); // on attend un peu que le vps soit bien destroy
					} catch (Exception e) {
						Main.printStackTrace(e);
					}
					vp.unregister();
				});
			} catch (JVultrException e) {
				Main.printStackTrace(e);
			}
		}
	}

	@Override
	public int countDeployedInstance() {
		try {
			return api.getSevers().size();
		} catch (JVultrException e) {
			Main.printStackTrace(e);
			return -1;
		}
	}

}
