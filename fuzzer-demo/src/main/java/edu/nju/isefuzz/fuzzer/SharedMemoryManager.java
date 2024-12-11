package edu.nju.isefuzz.fuzzer;
import com.sun.jna.*;

public class SharedMemoryManager {

    // 定义 LibC 接口，用于调用系统共享内存函数
    public interface LibC extends Library {
        LibC INSTANCE = Native.load("libc.so.6", LibC.class);

        int shmget(int key, int size, int shmflg);
        Pointer shmat(int shmid, Pointer shmaddr, int shmflg);
        int shmdt(Pointer shmaddr);
        int shmctl(int shmid, int cmd, Pointer buf);
    }
    private static final int KEY = 1145;
    private static final int IPC_CREAT = 01000; // 创建共享内存标志
    private static final int IPC_RMID = 0;     // 删除共享内存标志

    private int shmId;
    private Pointer shmPtr;

    public void createSharedMemory(int size) {
        // 使用随机 key 创建共享内存
        shmId = LibC.INSTANCE.shmget(KEY, size, 0666 | IPC_CREAT);
        if (shmId < 0) {
            throw new RuntimeException("Failed to create shared memory");
        }

        // 映射共享内存
        shmPtr = LibC.INSTANCE.shmat(shmId, null, 0);
        if (shmPtr == null) {
            throw new RuntimeException("Failed to attach shared memory");
        }

        System.out.println("Shared memory created with ID: " + shmId);
    }

    public int getShmId() {
        return shmId;
    }

    public void destroySharedMemory() {
        if (shmId >= 0) {
            LibC.INSTANCE.shmctl(shmId, IPC_RMID, null);
            System.out.println("Shared memory with ID " + shmId + " removed");
        }
    }
}
