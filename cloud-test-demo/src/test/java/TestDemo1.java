import org.junit.Test;

/**
 * @author wangzhikun
 * @date 2020/7/24 0024 19:09
 * @description
 */
public class TestDemo1 {


    @Test
    public void test6() {
        int i = 1;
        i = i++;
        int j = i++;
        int k = i + ++i * i++;
        System.out.println("i=" + i);
        System.out.println("j=" + j);
        System.out.println("k=" + k);
    }

    @Test
    public void test5() {
        Integer a = 10;
        Integer b = 10;
        System.out.println(a == b);
        Integer x = 128;
        Integer y = 128;
        System.out.println(x == y);

    }

    @Test
    public void test4() {
    }

    @Test
    public void test3() {
        int[] array = new int[]{4, 2, 5, 3, 1, 7, 9, 0, 8};
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + "\t");
        }
    }

    @Test
    public void test2() {
        int[] array = new int[]{4, 2, 5, 3, 1, 7, 9, 0, 8};
        for (int i = 1; i <= array.length - 1; i++) {
            for (int j = array.length - 1; j >= i; j--) {
                if (array[j] < array[j - 1]) {
                    int temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;
                }

            }
        }
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }
    }

    @Test
    public void test1() {
        int i = 1;
        i *= 0.1;//等价于 i = i * 0.1
        System.out.println(i);//
        i++;
        System.out.println(i);//

    }
}
