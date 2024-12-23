# FUZZ-DEMO 2024 项目文档

### 1、项目概述

2024秋 南京大学软件学院 软件测试代码大作业 - 覆盖率引导的变异式模糊测试工具

- 托管地址：[tmszzzzz/fuzz-mut-demos: 2024秋 南京大学软件学院 软件测试代码大作业 - 覆盖率引导的变异式模糊测试工具](https://github.com/tmszzzzz/fuzz-mut-demos)

- dockerhub链接：[Image Layer Details - tmsz/fuzz:latest | Docker Hub](https://hub.docker.com/layers/tmsz/fuzz/latest/images/sha256:ffd23d4ab8eed3208fab82db94f088474fe64ac06483a85661ef4c1ba844e5f1?uuid=AC7C718C-AB13-4EAC-A63A-146F515BEEA4)

### 2、项目结构

```
edu.nju.isefuzz.fuzzer
|
|- DemoMutationBlackBoxFuzzer.java	程序主类与运行入口
|- EnergySchedulingComponent.java	能量调度组件，依据输入的种子数据分配能量
|- EvaluationComponent.java			评估组件，每一轮种子变异并输入程序后统计结果
|- ExecutionComponent.java			执行组件，按特定方式执行目标程序
|- ExecutionMonitorComponent.java	执行结果监控组件，监控单个测试样例的运行并反馈结果
|- ExecutionResult.java				执行结果类
|- FuzzUtils.java					工具类
|- MutationComponent.java			变异组件，依照变异算子规则对种子进行变异
|- Seed.java						种子类
|- SeedSchedulingComponent.java		种子排序组件，检出种子队列中最有价值的种子进行变异
|- SharedMemoryManager.java			共享内存管理类，管理目标程序的共享内存段，用于覆盖率位图的传输
|- StreamMutationComponent.java		变异组件的子类：输入流变异，对文件内容进行变异并返回文件路径
|- StringMutationComponent.java		变异组件的子类：字符串变异，对字符串进行变异并返回原字符串
```

### 3、使用方法

- 下载此项目的Docker Hub镜像。

- 将镜像作为容器启动。

- 进入/root/tar目录，当前目录下包含所有相关文件与环境。

- 执行模糊测试：

  - 如果要测试的目标非项目指定的十个目标，则需要自行准备插装程序。具体而言，需要使用AFL++替代C/C++编译器进行编译得到二进制可执行文件。

  - 使用如下格式执行命令：

    ```
    java -cp "fuzzer-demo-1.0.jar:./lib/*" edu.nju.isefuzz.fuzzer.DemoMutationBlackBoxfuzzer <目标程序执行语句> out <输入方式> <初始种子> <运行时长(s)> <输出间隔(s)>
    
    参数解释：
    <目标程序执行语句>: 执行程序需要的执行语句。
    	例如，若通常执行程序program需要的命令是: ./program -abc <输入文件路径 | 输入字符串>，
    	那么此处应当填写: "path/to/program -abc @@"其中@@代表的是测试程序将种子替换到命令中所对应的位置。
    <输入方式>: file | string | stream 分别代表以文件形式输入、字符串输入、输入流输入
    <初始种子>: 与输入方式相关。若为file或stream输入，则填初始种子文件路径；否则直接输入作为初始种子的字符串。
    <运行时长>: 测试执行的时长。
    <输出间隔>: 测试执行时，每经过多长时间就记录一次这段时间内的数据。
    可以参考下文的具体运行命令。
    ```

  - 如果要测试十个指定目标，直接使用如下命令即可：

    ```
    java -cp "fuzzer-demo-1.0.jar:./lib/*" edu.nju.isefuzz.fuzzer.DemoMutationBlackBoxFuzzer "FUZZ_TARGETS/c++filt @@" out string _Z1fv 86400 300
    
    java -cp "fuzzer-demo-1.0.jar:./lib/*" edu.nju.isefuzz.fuzzer.DemoMutationBlackBoxFuzzer "FUZZ_TARGETS/readelf -a @@" out file ./SEEDS/readelf/small_exec.elf 86400 300
    
    java -cp "fuzzer-demo-1.0.jar:./lib/*" edu.nju.isefuzz.fuzzer.DemoMutationBlackBoxFuzzer "FUZZ_TARGETS/nm @@" out file ./SEEDS/nm/small_exec.elf 86400 300
    
    java -cp "fuzzer-demo-1.0.jar:./lib/*" edu.nju.isefuzz.fuzzer.DemoMutationBlackBoxFuzzer "FUZZ_TARGETS/lua @@" out file ./SEEDS/lua/all.lua 86400 300
    
    java -cp "fuzzer-demo-1.0.jar:./lib/*" edu.nju.isefuzz.fuzzer.DemoMutationBlackBoxFuzzer "FUZZ_TARGETS/objdump -d @@" out file ./SEEDS/objdump/small_exec.elf 86400 300
    
    java -cp "fuzzer-demo-1.0.jar:./lib/*" edu.nju.isefuzz.fuzzer.DemoMutationBlackBoxFuzzer "FUZZ_TARGETS/djpeg @@" out file ./SEEDS/djpeg/not_kitty.jpg 86400 300
    
    java -cp "fuzzer-demo-1.0.jar:./lib/*" edu.nju.isefuzz.fuzzer.DemoMutationBlackBoxFuzzer "FUZZ_TARGETS/readpng" out stream ./SEEDS/readpng/not_kitty.png 86400 300
    
    java -cp "fuzzer-demo-1.0.jar:./lib/*" edu.nju.isefuzz.fuzzer.DemoMutationBlackBoxFuzzer "FUZZ_TARGETS/xmllint @@" out file ./SEEDS/xmllint/small_document.xml 86400 300
    
    java -cp "fuzzer-demo-1.0.jar:./lib/*" edu.nju.isefuzz.fuzzer.DemoMutationBlackBoxFuzzer "FUZZ_TARGETS/mjs -f @@" out file ./SEEDS/mjs/small_script.js 86400 300
    
    java -cp "fuzzer-demo-1.0.jar:./lib/*" edu.nju.isefuzz.fuzzer.DemoMutationBlackBoxFuzzer "FUZZ_TARGETS/tcpdump -nr @@" out file ./SEEDS/tcpdump/small_capture.pcap 86400 300
    ```

- 覆盖率曲线图生成

  - 先前的覆盖率分析会生成Coverage.csv文件于当前目录下。

  - 将需要分析的文件重命名后放入./result中。

  - 执行如下命令：

    ```
    source myenv/bin/activate
    python3 ./analization
    ```

  - 在./plots中可以看到分析完成的曲线图。

上述的十个目标的覆盖率数据以及曲线图已经包含于镜像中。