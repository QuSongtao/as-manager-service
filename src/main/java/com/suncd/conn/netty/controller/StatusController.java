package com.suncd.conn.netty.controller;

import com.suncd.conn.netty.system.constants.Constant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sk")
public class StatusController {

    @Value("${netty.local.addr}")
    private String localAddr;
    @Value("${netty.local.port}")
    private String localPort;
    @Value("${netty.remote.addr}")
    private String remoteAddr;
    @Value("${netty.remote.port}")
    private String remotePort;

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public Map getStatus() {
        Map<String, String> map = new HashMap<>();
        map.put("clientStatus", Constant.CLIENT_STATUS == 0 ? "DOWN" : "RUNNING");
        map.put("clientAddr", remoteAddr + "(" + remotePort + ")");
        map.put("serverStatus", Constant.SERVER_STATUS == 0 ? "DOWN" : "RUNNING");
        map.put("serverAddr", localAddr + "(" + localPort + ")");
        return map;
    }
}
