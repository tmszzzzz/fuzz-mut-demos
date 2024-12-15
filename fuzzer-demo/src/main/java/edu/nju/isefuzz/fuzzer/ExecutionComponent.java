package edu.nju.isefuzz.fuzzer;

import java.io.*;
import java.util.concurrent.*;

public class ExecutionComponent {

    private static final int TIMEOUT_SECONDS = 10; // 超时时间（秒）

    /**
     * 执行二进制模糊目标，并返回执行结果。
     *
     * @param binaryPath 二进制文件路径
     * @param input      模糊测试生成的输入
     * @param shmId      共享内存 ID，用于覆盖率反馈
     * @param shmManager 共享内存管理器，用于获取覆盖率信息
     * @return ExecutionResult 包含目标输出、退出码和覆盖率
     * @throws IOException          如果进程启动错误
     * @throws InterruptedException 如果执行被中断
     */
    public ExecutionResult execute(String binaryPath, String input, int shmId, SharedMemoryManager shmManager) throws IOException, InterruptedException {
        // 创建进程构建器
        ProcessBuilder pb = new ProcessBuilder(binaryPath,input);
        pb.redirectErrorStream(true); // 将标准错误重定向到标准输出
        pb.environment().put("__AFL_SHM_ID", String.valueOf(shmId)); // 设置共享内存环境变量

        // 启动进程
        Process process = pb.start();
        // 捕获输出流
        BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        // 使用线程池管理器处理超时问题
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> outputFuture = executor.submit(() -> {
            StringBuilder outputBuilder = new StringBuilder();
            String line;
            while ((line = outputReader.readLine()) != null) {
                outputBuilder.append(line).append('\n');
            }
            return outputBuilder.toString();
        });

        String output;
        int exitCode;
        try {
            // 等待进程执行完成或超时
            process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // 获取进程输出
            output = outputFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            exitCode = process.exitValue(); // 获取退出码
        } catch (TimeoutException e) {
            process.destroyForcibly(); // 强制终止超时的进程
            executor.shutdownNow();
            throw new InterruptedException("Execution timed out after " + TIMEOUT_SECONDS + " seconds.");
        } catch (Exception e) {
            throw new IOException("Error during process execution", e);
        } finally {
            executor.shutdown();
        }

        // 获取覆盖率信息（如果存在共享内存管理器）
        int coverageRate = shmManager != null ? shmManager.getCoverageRate() : 0;

        // 返回执行结果
        return new ExecutionResult(output, exitCode, coverageRate);
    }
}