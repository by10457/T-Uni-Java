package t.uni.system.service;

import t.uni.domain.system.vo.monitor.ServerMonitorVo;

public interface MonitorService {

    /**
     * 获取当前管理服务的资源监控信息。
     *
     * @return 服务监控信息
     */
    ServerMonitorVo getServerMonitor();
}
