package edu.nju.isefuzz.fuzzer;


import com.sun.jna.*;

public class EnergySchedulingComponent {

    private static final int BITMAP_SIZE = 65536;

    // Determines how many mutations to apply based on the seed's "favor" status
    public int getMutationPower(Seed seed) {
        int basePower = 5;
        return seed.isFavored() ? basePower * 10 : basePower;
    }

    // Adjusts mutation power dynamically based on execution feedback (e.g., performance metrics)
    public int adjustMutationPower(Seed seed, long executionTime) {
        int mutationPower = getMutationPower(seed);

        // Example of adjusting based on execution time: more time-consuming mutations may get higher priority
        if (executionTime > 1000) {
            mutationPower += 5; // Increase mutation strength for seeds with longer execution time
        }

        return mutationPower;
    }


    public interface LibC extends Library {
        LibC INSTANCE = Native.load("c", LibC.class);
        Pointer shmat(int shmid, Pointer shmaddr, int shmflg);
        int shmdt(Pointer shmaddr);
    }

    public byte[] readCoverageBitmap(int shmId) {
        // 连接共享内存
        Pointer shmPtr = LibC.INSTANCE.shmat(shmId, null, 0);
        if (shmPtr == null) {
            throw new IllegalStateException("Failed to attach to shared memory");
        }

        // 读取位图数据
        byte[] bitmap = shmPtr.getByteArray(0, BITMAP_SIZE);

        // 分离共享内存
        LibC.INSTANCE.shmdt(shmPtr);

        return bitmap;
    }
}
