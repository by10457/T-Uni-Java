package t.uni.system.service.impl;

import org.springframework.stereotype.Service;
import t.uni.domain.system.vo.monitor.ServerMonitorVo;
import t.uni.system.service.MonitorService;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class MonitorServiceImpl implements MonitorService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final Path PROC_MEMINFO = Path.of("/proc/meminfo");

    @Override
    public ServerMonitorVo getServerMonitor() {
        Runtime runtime = Runtime.getRuntime();
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        java.lang.management.OperatingSystemMXBean systemMxBean = ManagementFactory.getOperatingSystemMXBean();

        return ServerMonitorVo.builder()
                .javaInfo(buildJavaInfo(runtimeMxBean))
                .system(buildSystemInfo())
                .cpu(buildCpuInfo(systemMxBean))
                .memory(buildMemoryInfo(runtime, systemMxBean))
                .disks(buildDiskInfo())
                .build();
    }

    private ServerMonitorVo.JavaInfo buildJavaInfo(RuntimeMXBean runtimeMxBean) {
        return ServerMonitorVo.JavaInfo.builder()
                .name(System.getProperty("java.vm.name"))
                .version(System.getProperty("java.version"))
                .vendor(System.getProperty("java.vendor"))
                .home(System.getProperty("java.home"))
                .startTime(DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(runtimeMxBean.getStartTime())))
                .runTime(formatDuration(Duration.ofMillis(runtimeMxBean.getUptime())))
                .build();
    }

    private ServerMonitorVo.SystemInfo buildSystemInfo() {
        return ServerMonitorVo.SystemInfo.builder()
                .osName(System.getProperty("os.name"))
                .osArch(System.getProperty("os.arch"))
                .osVersion(System.getProperty("os.version"))
                .hostName(getHostName())
                .userDir(System.getProperty("user.dir"))
                .build();
    }

    private ServerMonitorVo.CpuInfo buildCpuInfo(java.lang.management.OperatingSystemMXBean systemMxBean) {
        double systemCpuLoad = 0;
        double processCpuLoad = 0;
        if (systemMxBean instanceof com.sun.management.OperatingSystemMXBean sunMxBean) {
            systemCpuLoad = normalizeRate(sunMxBean.getCpuLoad());
            processCpuLoad = normalizeRate(sunMxBean.getProcessCpuLoad());
        }

        return ServerMonitorVo.CpuInfo.builder()
                .processors(systemMxBean.getAvailableProcessors())
                .systemCpuLoad(systemCpuLoad)
                .processCpuLoad(processCpuLoad)
                .build();
    }

    private ServerMonitorVo.MemoryInfo buildMemoryInfo(
            Runtime runtime,
            java.lang.management.OperatingSystemMXBean systemMxBean) {
        long jvmTotal = runtime.totalMemory();
        long jvmFree = runtime.freeMemory();
        long jvmMax = runtime.maxMemory();
        long jvmUsed = jvmTotal - jvmFree;
        long systemTotal = 0;
        long systemFree = 0;

        if (systemMxBean instanceof com.sun.management.OperatingSystemMXBean sunMxBean) {
            systemTotal = sunMxBean.getTotalMemorySize();
            systemFree = sunMxBean.getFreeMemorySize();
            Long linuxAvailableMemory = getLinuxAvailableMemory(systemTotal);
            if (linuxAvailableMemory != null) {
                systemFree = linuxAvailableMemory;
            }
        }

        long systemUsed = Math.max(systemTotal - systemFree, 0);
        return ServerMonitorVo.MemoryInfo.builder()
                .jvmTotal(jvmTotal)
                .jvmMax(jvmMax)
                .jvmFree(jvmFree)
                .jvmUsed(jvmUsed)
                .jvmUsage(percentage(jvmUsed, jvmMax))
                .systemTotal(systemTotal)
                .systemFree(systemFree)
                .systemUsed(systemUsed)
                .systemUsage(percentage(systemUsed, systemTotal))
                .build();
    }

    private List<ServerMonitorVo.DiskInfo> buildDiskInfo() {
        return Arrays.stream(File.listRoots())
                .map(file -> {
                    long total = file.getTotalSpace();
                    long free = file.getFreeSpace();
                    long usable = file.getUsableSpace();
                    long used = Math.max(total - free, 0);
                    return ServerMonitorVo.DiskInfo.builder()
                            .name(file.getAbsolutePath())
                            .type("本地磁盘")
                            .total(total)
                            .free(free)
                            .usable(usable)
                            .used(used)
                            .usage(percentage(used, total))
                            .build();
                })
                .toList();
    }

    private Long getLinuxAvailableMemory(long systemTotal) {
        if (!Files.isReadable(PROC_MEMINFO)) {
            return null;
        }

        try {
            for (String line : Files.readAllLines(PROC_MEMINFO)) {
                if (!line.startsWith("MemAvailable:")) {
                    continue;
                }

                long available = parseMemoryKbLine(line);
                if (available > 0 && (systemTotal <= 0 || available <= systemTotal)) {
                    return available;
                }
            }
        } catch (IOException ignored) {
            return null;
        }

        return null;
    }

    private long parseMemoryKbLine(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length < 2) {
            return 0;
        }

        try {
            return Long.parseLong(parts[1]) * 1024;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ignored) {
            return "unknown";
        }
    }

    private double normalizeRate(double value) {
        if (value < 0) {
            return 0;
        }
        return Math.round(value * 10_000D) / 100D;
    }

    private double percentage(long used, long total) {
        if (total <= 0) {
            return 0;
        }
        return Math.round((double) used * 10_000D / total) / 100D;
    }

    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return "%d天%d小时%d分钟%d秒".formatted(days, hours, minutes, seconds);
    }
}
