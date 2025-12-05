package io.github.shigaichi.jquants;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        //        String unusedValue = "This value is never used";
        //        if (args.length == 0 || args[0] != null) {
        //            new IllegalArgumentException();
        //        }
        TestDto testDto = new TestDto("test");
        System.out.println(testDto.getName());
        System.out.println("Hello World!");
    }
}
