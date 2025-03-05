package app.runner;

@Run
public class Sample {

    @Run(timer = true, printMethodName = true, print = true, showError = true)
    static String test() throws Exception {
        System.out.println("Hello my name is abhishek");
        throw new Exception("throwing");
    }

    @Run(timer = true, printMethodName = true)
    static void test2() {
        System.out.println("Hello my name is abhishek2");
    }
}
