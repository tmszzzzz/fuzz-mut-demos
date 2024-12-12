package edu.nju.isefuzz.fuzzer;


import com.sun.jna.*;

public class EnergySchedulingComponent {

    // Determines how many mutations to apply based on the seed's "favor" status
    public int getMutationPower(Seed seed) {
        double coverageFactor = seed.getCoverageRate() / 65536.0; // 覆盖率比例
        int energy = 3;

        // 基于覆盖率调整能量（覆盖率越高，变异次数越多）
        energy += (int) (coverageFactor * 7); // 覆盖率最多增加 7 次变异

        // 如果是崩溃种子，增加额外变异次数
        if (seed.isCrash()) {
            energy += 3; // 崩溃种子额外增加 3 次变异
        }

        // 如果是 favored 种子，增加额外变异次数
        if (seed.isFavored()) {
            energy += 2; // favored 种子额外增加 2 次变异
        }

        // 限制最大变异次数，防止过高
        energy = Math.max(1, Math.min(energy, 15)); // 限制变异次数在 1 到 15 次之间

        return energy;
    }
}
