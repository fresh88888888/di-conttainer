package org.isp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleHttpServer {
    private String host;
    private String port;
    private final Map<String, List<Viewer>> viewers = new HashMap<>();

    public SimpleHttpServer(String host, String port) {
        this.host = host;
        this.port = port;
    }

    public void addViewers(String urlDirectory, Viewer viewer){
        if (!viewers.containsKey(urlDirectory)){
            viewers.put(urlDirectory, new ArrayList<Viewer>());
        }
        viewers.get(urlDirectory).add(viewer);
    }

    public void run(){
        //...
    }
}
