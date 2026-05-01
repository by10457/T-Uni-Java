package t.uni.domain.system.vo.monitor;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ServerMonitorVo", title = "服务监控信息", description = "服务监控信息")
public class ServerMonitorVo {

    @JsonProperty("java")
    private JavaInfo javaInfo;

    private SystemInfo system;

    private CpuInfo cpu;

    private MemoryInfo memory;

    private List<DiskInfo> disks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JavaInfo {
        private String name;
        private String version;
        private String vendor;
        private String home;
        private String startTime;
        private String runTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemInfo {
        private String osName;
        private String osArch;
        private String osVersion;
        private String hostName;
        private String userDir;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CpuInfo {
        private int processors;
        private double systemCpuLoad;
        private double processCpuLoad;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemoryInfo {
        private long jvmTotal;
        private long jvmMax;
        private long jvmFree;
        private long jvmUsed;
        private double jvmUsage;
        private long systemTotal;
        private long systemFree;
        private long systemUsed;
        private double systemUsage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiskInfo {
        private String name;
        private String type;
        private long total;
        private long free;
        private long usable;
        private long used;
        private double usage;
    }
}
