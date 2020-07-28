/**
 * @author wangzhikun
 * @date 2020/7/25 0025 10:13
 * @description
 */
public class TestLazy {
    public static void main(String[] args) {
        Lazy l1 = Lazy.getInstance();
        Lazy l2 = Lazy.getInstance();
        System.out.println(l1 == l2);
    }
}
//懒汉式
class Lazy{

    private static Lazy instance;

    private Lazy(){
        System.out.println("无参构造");
    }

    public static Lazy getInstance(){
        if(instance == null){
            instance = new Lazy();
        }
        return instance;
    }
}

