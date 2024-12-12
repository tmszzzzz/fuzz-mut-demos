package edu.nju.isefuzz.fuzzer;


import com.sun.jna.*;

public class EnergySchedulingComponent {

    // Determines how many mutations to apply based on the seed's "favor" status
    public int getMutationPower(Seed currentSeed, int previousCoverage) {
        int baseEnergy = 3; // 基础能量值，最低变异次数
        int energy = baseEnergy;

        // 计算覆盖率增量比例
        int currentCoverage = currentSeed.getCoverageRate();
        double coverageIncrementRatio = previousCoverage == 0 ? 1.0 :
                (double) (currentCoverage - previousCoverage) / previousCoverage;
        coverageIncrementRatio = currentCoverage == 0 ? 0 : coverageIncrementRatio;
        // 基于覆盖率增量调整能量
        if (coverageIncrementRatio > 0) {
            energy += (int) (coverageIncrementRatio * 10); // 增量比例每提升 10%，增加 1 次变异
        }

        // 如果种子触发了崩溃，增加额外能量
        if (currentSeed.isCrash()) {
            energy += 5; // 崩溃种子额外增加 5 次变异
        }

        // 如果种子被标记为 favored，增加额外能量
        if (currentSeed.isFavored()) {
            energy += 3; // favored 种子额外增加 3 次变异
        }

        // 限制能量范围，避免过高或过低
        energy = Math.max(1, Math.min(energy, 20)); // 限制变异次数在 1 到 20 次之间

        return energy;
    }

}
