package edu.nju.isefuzz.fuzzer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EvaluationComponent {
    private int allSeeds = 0;
    private int favoredSeeds = 0;
    private int crashSeeds = 0;
    private int max_Coverage_rate = 0;
    // 新增成员变量
    public final int intervalSeconds; // 数据记录的时间间隔
    private int testCaseCount = 0; // 当前时间段内测试用例的数量
    private int minCoverage = Integer.MAX_VALUE; // 最小覆盖率
    private int maxCoverage = 0; // 最大覆盖率
    private double averageCoverage = 0.0; // 平均覆盖率

    // 记录时间点与覆盖率数据的存储列表
    private List<Integer> timePoints = new ArrayList<>();
    private List<Integer> minCoverages = new ArrayList<>();
    private List<Integer> maxCoverages = new ArrayList<>();
    private List<Integer> avgCoverages = new ArrayList<>();

    public EvaluationComponent(int intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }
    public void addAllSeeds() {allSeeds++;}
    public void addFavored() {favoredSeeds++;}
    public void addCrashed() {crashSeeds++;}
    public void addCoverage(int cov) {
        max_Coverage_rate = Math.max(max_Coverage_rate, cov);
    }

    // Evaluate the fuzzing progress
    public void evaluate() {
        System.out.printf("[EVALUATION] Seeds Num: %d\n", allSeeds);
        System.out.printf("[EVALUATION] Favored Seeds: %d\n", favoredSeeds);
        System.out.printf("[EVALUATION] Crashed Seeds: %d\n", crashSeeds);
        System.out.printf("[EVALUATION] Max Code Coverage: %d\n", max_Coverage_rate);
    }

    /**
     * 将覆盖率数据（包括最小值、最大值和平均值）存储到 CSV 文件中
     * @param outputFile     CSV 文件路径
     * @throws IOException   写入文件时可能出现异常
     */
    public void saveCoverageWithBoundsToCSV(
            String outputFile) throws IOException {

        // 检查数据合法性
        if (timePoints.size() != minCoverages.size() ||
                timePoints.size() != maxCoverages.size() ||
                timePoints.size() != avgCoverages.size()) {
            throw new IllegalArgumentException("All input lists must have the same length.");
        }

        try (FileWriter writer = new FileWriter(outputFile)) {
            // 写入 CSV 文件的表头
            writer.append("Time (Second),Coverage,Max Coverage,Average Coverage\n");

            // 写入每一行数据
            for (int i = 0; i < timePoints.size(); i++) {
                writer.append(String.format("%d,%d,%d,%d\n",
                        timePoints.get(i),    // 时间点
                        minCoverages.get(i),  // 最小覆盖率
                        maxCoverages.get(i),  // 最大覆盖率
                        avgCoverages.get(i)   // 平均覆盖率
                ));
            }

            System.out.println("Coverage data with bounds successfully saved to: " + outputFile);
        }
    }

    public void updateCoverageData(int coverage) {
        testCaseCount++; // 增加当前时间段内的测试用例数量

        // 更新最小覆盖率
        minCoverage = Math.min(minCoverage, coverage);

        // 更新最大覆盖率
        maxCoverage = Math.max(maxCoverage, coverage);

        // 更新平均覆盖率
        averageCoverage = ((averageCoverage * (testCaseCount - 1)) + coverage) / testCaseCount;
    }

    public void recordAndReset(int currentTimeInSeconds) {
        // 存储当前时间段的数据
        timePoints.add(currentTimeInSeconds);
        minCoverages.add(minCoverage == Integer.MAX_VALUE ? 0 : minCoverage);
        maxCoverages.add(maxCoverage);
        avgCoverages.add((int) averageCoverage);

        // 重置变量，为下一个时间段准备
        testCaseCount = 0;
        minCoverage = Integer.MAX_VALUE;
        maxCoverage = 0;
        averageCoverage = 0.0;

        System.out.printf("[EVALUATION] Time: %d seconds, Min Coverage: %d, Max Coverage: %d, Avg Coverage: %.2f\n",
                currentTimeInSeconds, minCoverage, maxCoverage, averageCoverage);
    }
}
