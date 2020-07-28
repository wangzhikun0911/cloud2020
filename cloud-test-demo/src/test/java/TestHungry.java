/**
 * @author wangzhikun
 * @date 2020/7/25 0025 10:18
 * @description
 */
public class TestHungry {
    public static void main(String[] args) {
        Hungry h1 = Hungry.getInstance();
        Hungry h2 = Hungry.getInstance();
        System.out.println(h1 == h2);
    }
}
class Hungry{
    private static Hungry instance = new Hungry();

    private Hungry(){

    }

    public static Hungry getInstance(){
        return instance;
    }
}
