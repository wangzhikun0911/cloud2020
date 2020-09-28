

# 第二季

## JUC多线程及并发包

### 谈谈对volatile的理解

#### **1.volatile**

 volatile是Java虚拟机提供的轻量级的同步机制。

> 特性: 
>
> 1.1 保证可见性
>
> **1.2 不保证原子性**
>
> 1.3 禁止指令重排

#### 2.谈谈JMM

![1596901741166](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1596901741166.png)

**JMM**(Java内存模型 **Java Memory Model**，简称JMM)本身是一种**抽象的概念并不真实存在**，它描述的是一组规则或规范，通过规范定制了程序中各个变量(包括实例字段，静态字段和构成数组对象的元素)的访问方式。

JMM关于同步规定:
1.线程解锁前，必须把共享变量的值刷新回主内存
2.线程加锁前，必须读取主内存的最新值到自己的工作内存
3.加锁解锁是同一把锁

由于JVM运行程序的实体是线程，而每个线程创建时JVM都会为其创建一个工作内存(有些地方成为栈空间)，工作内存是每个线程的私有数据区域，而**Java内存模型中规定所有变量都存储在主内存**，主内存是共享内存区域，所有线程都可访问，**但线程对变量的操作(读取赋值等)必须在工作内存中进行**，**首先要将变量从主内存拷贝到自己的工作空间，然后对变量进行操作，操作完成再将变量写回主内存**，不能直接操作主内存中的变量，各个线程中的工作内存储存着主内存中的变量副本拷贝，因此不同的线程无法访问对方的工作内存，此线程间的通讯(传值) 必须通过主内存来完成，其简要访问过程如下图:

![1596902144933](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1596902144933.png)



##### 2.1 可见性

通过前面对JMM的介绍，我们知道各个线程对主内存中共享变量的操作都是各个线程各自拷贝到自己的工作内存操作后再写回主内存中的。

这就可能存在一个线程AAA修改了共享变量X的值还未写回主内存中时 ，另外一个线程BBB又对内存中的一个共享变量X进行操作，但此时A线程工作内存中的共享变量X对线程B来说并不可见，这种工作内存与主内存同步延迟现象就造成了可见性问题。

```java
/**
   验证volatile的可见性
   1.1 假如int number = 0;number变量之前根本 没有添加volatile关键字修改，没有可见性
   1.2 添加了volatile，可以解决可见性问题。
*/
class MyData {//资源类
    //volatile可以保证可见性，及时通知其他线程，主物理内存的值已经被修改。
    volatile int number = 0; 
	
    public void add60() {
        number = 60;
    }
}

public class TestVolatile {
    public static void main(String[] args) {
        //线程操作资源类
        MyData myData = new MyData();
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "\t come in");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            myData.add60();
            System.out.println(Thread.currentThread().getName() + "\t update number value " + myData.number);
        }， "AA").start();
		
        while (myData.number == 0) {
            //加了volatile之后如果AA线程修改了number，会写回到主内存，主线程也会收到值得变化了。
            //如果number=0就一直等待
        }
        System.out.println(Thread.currentThread().getName() + "\t get number value " + myData.number);
    
}

```

##### 2.2 原子性

```java
/**
 *  验证volatile不保证原子性
 *  2.1 原子性指的是什么意思？
 *  	不可分割，完整性，也即某个线程正在做某个具体业务时，中间不可以被加塞或者被分割。
 *  	需要整体完整性，要么同时成功，要是同时失败。
 *  2.2 valatile不保证原子性的案件演示
 *  
 *  2.3 why
 *  
 *  2.4 解决方法
 *   2.4.1 加synchronized
 *   2.4.2 使用AtomicInteger
 */
class MyData {//资源类
    volatile int number = 0;
    
    public void addPlusPlus(){ //①加synchronized
        number++;
    }
    
    AtomicInteger atomicInteger= new AtomicInteger();
    //②使用AtomicInteger
    public void addMyAtomic(){
        atomicInteger.getAndIncrement();
    }
}

public class TestVolatile {
    public static void main(String[] args) {

        MyData myData = new MyData();
        for (int i = 1; i <= 20 ; i++) {
            new Thread(()->{
                for (int j = 1; j <= 1000 ; j++) {
                    myData.addPlusPlus();
                    myData.addMyAtomic();
                }
             }，String.valueOf(i)).start();
        }

        while(Thread.activeCount() > 2){
            Thread.yield(); //调用yield()的线程释放当前CPU的执行权
        }

        System.out.println(Thread.currentThread().getName()+"\t int type result value " + myData.number);
        System.out.println(Thread.currentThread().getName()+"\t AtomicInteger type get result value " + myData.atomicInteger);
    }
}
```

![1596948396154](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1596948396154.png)

number++在多线程下是非线程安全的

![1596948337599](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1596948337599.png)

##### **2.3 有序性**

计算机在执行程序时，为了提高性能，编译器和处理器常常会做**指令重排**，一把分为以下3中

![1596951257486](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1596951257486.png)

> 单线程环境里面确保程序最终执行结果和代码顺序执行的结果一致.
>
> 处理器在进行重新排序是必须要考虑指令之间的**数据依赖性**
>
> 多线程环境中线程交替执行，由于编译器优化重排的存在，两个线程使用的变量能否保持一致性，是无法确定的，结果无法预测

- **重排1**

```java
public void mySort(){
    int x=11;//语句1
    int y=12;//语句2
    x=x+5;//语句3
    y=x*x;//语句4
}
1234
2134
1324
//问题:请问语句4 可以重排后变成第一条码?
//存在数据的依赖性 没办法排到第一个

```

- **重排2**

```java
int a ，b ，x，y=0;
//线程1	线程2
x=a;	y=b;
b=1;	a=2;
	
x=0 y=0	

// 如果编译器对这段代码进行执行重排优化后，可能出现下列情况:
线程1	线程2
b=1;	a=2;
x=a;	y=b;
	
x=2 y=1	
```

这也就说明在多线程环境下，由于编译器优化重排的存在，两个线程使用的变量能否保持一致是无法确定的.

**案例**

```java
public class ReSortSeqDemo {
    int a = 0;
    boolean flag = false;

    public void method1(){
        a = 1;             //语句1
        flag = true;       //语句2
    }
    public void method2(){
        if(flag){
            a = a + 5;     //语句3        
            System.out.println("**value**:"+a); //正常是6 ，
            //但是在多线程下发生指令重排有可能是5
        }
    }
}
```

 

- 禁止指令重排小总结(了解)

> volatile实现了**禁止指令重排优化**，从而避免多线程环境下程序出现乱序执行的现象
>
> 先了解下一个概念，内存屏障(Memory Barries)又称为内存栅栏，是一个CPU指令，他的作用有两个:
>
> 一是保证特定操作的执行顺序，
>
> 二是保证某些变量的内存可见性（利用改特性实现volatile的内存可见性）。
>
> 由于编译器和处理器都能执行指令重排优化，如果在指令间插入一条Memory Barries则会告诉编译器和CPU，不管什么指令都不能和这条Memory Barries指令重排序，也就是说**通过插入内存屏障禁止在内存屏障前后的指令执行重排优化。**内存屏障另外一个作用是强制刷新出各种CPU的缓存数据，因此CPU上的线程都能够读取到这些数据的最新版本。

![1596961890407](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1596961890407.png)



- **线程安全获得保证：**

> 工作内存与主内存同步延迟现象导致的可见性问题：
>
> 可以使用synchronized或者volatile关键字解决，它们都可以使一个线程修改的变量立即对其它线程可见。
>
> 
>
> 对于指令重排导致的可见性问题和有序性问题：
>
> 可以利用volatile关键字解决，因为volatile的另一个作用就是禁止重排序优化。

#### **3.哪些地方用到过volatile**

3.1 单例模式DCL代码;

```java
public class SingletonDemo {
    private static volatile SingletonDemo instance = null;
    private SingletonDemo (){
        System.out.println(Thread.currentThread().getName()+"\t SingletonDemo() 构造方法");
    }
    public static SingletonDemo getInstance(){
        if(instance == null){
            synchronized (SingletonDemo.class){
                if(instance == null){
                    instance = new SingletonDemo();
                }
            }
        }
        return instance;
    }

    public static void main(String[] args) {
        for (int i = 1; i <= 10 ; i++) {
            new Thread(()->{
                SingletonDemo.getInstance();
             }，String.valueOf(i)).start();
        }
    }
}
```

3.2 代理模式volatile分析

DCL: Double Check Lock 双端检锁机制

> DCL(双端检锁) 机制不一定线程安全，原因是有指令重排的存在，加入volatile可以禁止指令重排，
> 原因在于某一个线程在执行到第一次检测，读取到的instance不为null时，instance的引用对象可能没有完成初始化.
> instance=new SingletonDem(); 可以分为以下步骤(伪代码)
>
> memory=allocate();//1.分配对象内存空间
> instance(memory);//2.初始化对象
> instance=memory;//3.设置instance的指向刚分配的内存地址，此时instance!=null 
>
> 步骤2和步骤3不存在数据依赖关系.而且无论重排前还是重排后程序执行的结果在单线程中并没有改变，因此这种重排优化是允许的.
> memory=allocate();//1.分配对象内存空间
> instance=memory;//3.设置instance的指向刚分配的内存地址，此时instance!=null 但对象还没有初始化完.
> instance(memory);//2.初始化对象
> 但是指令重排只会保证串行语义的执行一致性(单线程) 并不会关心多线程间的语义一致性
> 所以当一条线程访问instance不为null时，由于instance实例未必完成初始化，也就造成了线程安全问题.
>
>  
>



### **CAS**你知道吗

#### 1.什么是CAS？ 

CAS =====> compareAndSet

 比较并交换

```java
public class CASDemo {
    public static void main(String[] args) {
       AtomicInteger atomicInteger =  new AtomicInteger(5);//主物理内存的值

       //第一个参数是期望值，第二个参数为更新值
        System.out.println(atomicInteger.compareAndSet(5， 2019) + "\t update value " + atomicInteger.get());
        System.out.println(atomicInteger.compareAndSet(5， 1024) + "\t update value " + atomicInteger.get());
    }
}
//如果线程的期望值与主物理内存的真实值是一样的，就修改为更新值，返回true，获取的值是最新值；
//如果不一样，则更新失败，返回false，获取的还是之前的值
```

![1596978910073](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1596978910073.png)

#### **2.CAS底层原理**

###### **2.1.getAndIncrement()源代码**

atomicInteger.getAndIncrement()方法的源代码:

```java
/**
 * Atomically increments by one the current value.
 *
 * @return the previous value
 */
// private static final Unsafe unsafe = Unsafe.getUnsafe();
//参数:this代表当前对象;valueOffset代表当前对象内存地址偏移量
public final int getAndIncrement() { 
    return unsafe.getAndAddInt(this， valueOffset， 1);
}
//印出来一个问题:UnSafe类是什么?
//UnSafe是jdK自身携带的一个类，在lib/rt.jar/sun/misc包下
```

##### **2.2.对UnSafe类的理解**

```java
/**
	Java类无法操作内存
	Java可以调用c++，native
	c++可以操作内存
	Java的后门，可以通过这个类UnSafe类操作内存
*/
public class AtomicInteger extends Number implements java.io.Serializable {
    private static final long serialVersionUID = 6214790243416807050L;

    // setup to use Unsafe.compareAndSwapInt for updates
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long valueOffset;

    static {
        try {
            valueOffset = unsafe.objectFieldOffset
                (AtomicInteger.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }

    private volatile int value;
}
```

> 1.UnSafe：
>
> 是CAS的核心类，由于Java 方法无法直接访问底层(内存) ，需要通过本地(native)方法来访问，UnSafe相当于一个后门，基于该类**（UnSafe）可以直接操作特额定的内存数据.**UnSafe类在于sun.misc包中，其内部方法操作可以向C的指针一样直接操作内存，**因为Java中CAS操作的助兴依赖于UnSafe类的方法。**
>
> 2.变量valueoffset，便是该变量在内存中的偏移地址，因为UnSafe就是根据内存偏移地址获取数据的
>
> ```java
> public final int getAndIncrement() { 
>     return unsafe.getAndAddInt(this， valueOffset， 1);
> }
> private volatile int value;
> ```
>
> 3.变量value被volatile修饰，保证了多线程之间的可见性.



##### 2.3.CAS是什么

CAS的全称为Compare-And-Swap ，它是一条**CPU并发原语.**
它的功能是判断内存某个位置的值是否为预期值，如果是则更新为新的值，这个过程是原子的.

CAS并发原语体现在Java语言中就是sun.misc.UnSafe类中的各个方法。调用**UnSafe类中的CAS方法**，JVM会帮我们实现CAS汇编指令。这是一种完全依赖于硬件功能，通过它实现了原子操作，再次强调，由于CAS是一种系统原语，原语属于操作系统用于范畴，是由若干条指令组成，用于完成某个功能的一个过程，并且**原语的执行必须是连续的，在执行过程中不允许中断，也即是说CAS是一条原子指令，不会造成所谓的数据不一致的问题.**

```java
//某个线程对象调用了addMyAtomic()方法
public void addMyAtomic(){
    atomicInteger.getAndIncrement();
}
---------------------------------------------------------

public final int getAndIncrement() { 
    return unsafe.getAndAddInt(this， valueOffset， 1);
}
---------------------------------------------------------
public native int getIntVolatile(Object var1， long var2);  //根据当前对象与内存地址获取真实值
---------------------------------------------------------
public final int getAndAddInt(Object var1， long var2， int var4) {
    int var5;
    do {
        var5 = this.getIntVolatile(var1， var2);
    } while(!this.compareAndSwapInt(var1， var2， var5， var5 + var4));

    return var5;
}
```

**3.1 unsafe.getAndIncrement**

> var1 AtomicInteger对象本身.
> var2 该对象值的引用地址
> var4 需要变动的数值
> var5 是用过var1 var2找出内存中绅士的值
> 用该对象当前的值与var5比较
> 如果相同，更新var5的值并且返回true
> 如果不同，继续取值然后比较，直到更新完成

![1596985508397](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1596985508397.png)

>  假设线程A和线程B两个线程同时执行getAndAddInt操作(分别在不同的CPU上):
>
> 1.AtomicInteger里面的value原始值为3，即主内存中AtomicInteger的value为3，根据JMM模型，线程A和线程B各自持有一份值为3的value的副本分别到各自的工作内存.
>
> 2.线程A通过getIntVolatile(var1，var2) 拿到value值3，这是线程A被挂起.
>
> 3.线程B也通过getIntVolatile(var1，var2) 拿到value值3，此时刚好线程B没有被挂起并执行compareAndSwapInt方法比较内存中的值也是3 成功修改内存的值为4 线程B打完收工 一切OK.
>
>  4.这是线程A恢复，执行compareAndSwapInt方法比较，发现自己手里的数值和内存中的数字4不一致，说明该值已经被其他线程抢先一步修改了，那A线程修改失败，只能重新来一遍了.
>
>  5.线程A重新获取value值，因为变量value是volatile修饰，所以其他线程对他的修改，线程A总是能够看到，线程A继续执行compareAndSwapInt方法进行比较替换，直到成功.

**3.2 底层汇编**

![1596985562289](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1596985562289.png)

**3.3 简单版小总结**

![1596985586499](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1596985586499.png)

> 总结:
>
>   java 的 CAS 利用的的是 UnSafe这个类提供的 CAS操作 
>
>  UnSafe的CAS 依赖的是 jvm 针对不同的操作系统实现的 Atomic::cmpxchg 
>
>  Atomic::cmpxchg 的实现使用了汇编的 cas 操作，并使用 cpu 硬件提供的 lock信号保证其原子性 

#### 3.CAS缺点

3.1循环时间长开销很大（如果每次使用compareAndSwapInt方法返回来是false，就会每次调用do while循环，在高并发下CPU开销很大）

![1596986197562](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1596986197562.png)

3.2 只能保证一个共享变量的原子性

![1596986255105](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1596986255105.png)

3.3 引出来ABA问题？？？

CAS ==> Unsafe ==> CAS底层思想 ==> ABA ==> 原子引入更新 ==> 如何规避ABA问题

### 原子类AtomicInteger

#### 1.ABA产生的问题

![1596990810057](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1596990810057.png)

解决ABA问题：理解原子引用+ 新增一种机制，那就是修改版本号(类似时间戳)

**使用AtomicStamperReferece解决**

#### 2.原子引用

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
class User {
    private String name;
    private int age;
}

public class AtomicReferenceDemo {
    public static void main(String[] args) {
        User z3 = new User("z3"， 22);
        User li4 = new User("li4"， 25);
        AtomicReference<User> atomicReference = new AtomicReference<>();
        atomicReference.set(z3);//相当于主内存值是z3
        System.out.println(atomicReference.compareAndSet(z3， li4) + "\t " + atomicReference.get());
        System.out.println(atomicReference.compareAndSet(z3， li4) + "\t " + atomicReference.get());
    }
}
//执行结果
//true	 User(name=li4， age=25)
//false	 User(name=li4， age=25)
```

#### 3.时间戳原子引用

```java
public class AtomicStampedReferenceDemo {
    static AtomicReference<Integer> atomicReference = new AtomicReference<>(100);
    //第一个参数是主内存的值  第二个是版本号
    static AtomicStampedReference stampedReference = new AtomicStampedReference(100， 1);
	
    public static void main(String[] args) {
        System.out.println("===以下是ABA问题的产生===");

        new Thread(() -> {
            atomicReference.compareAndSet(100， 101);
            atomicReference.compareAndSet(101， 100);
        }， "t1").start();


        new Thread(() -> {
            //先暂停1秒，保证出现ABA现象
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(atomicReference.compareAndSet(100， 2019) + "\t" + atomicReference.get());
        }， "t2").start();

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("===以下是ABA问题的解决===");
        
        new Thread(() -> {
            int stamp = stampedReference.getStamp();//获取当前的版本号
            System.out.println(Thread.currentThread().getName() + "\t 第1次版本号" + stamp + "\t值是" + stampedReference.getReference()); //第一次的引用值即放在主内存中的值
            //暂停1秒钟t3线程
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            stampedReference.compareAndSet(100， 101， stampedReference.getStamp()， stampedReference.getStamp() + 1);
            System.out.println(Thread.currentThread().getName() + "\t 第2次版本号" + stampedReference.getStamp() + "\t值是" + stampedReference.getReference());
            stampedReference.compareAndSet(101， 100， stampedReference.getStamp()， stampedReference.getStamp() + 1);
            System.out.println(Thread.currentThread().getName() + "\t 第3次版本号" + stampedReference.getStamp() + "\t值是" + stampedReference.getReference());
        }， "t3").start();

        new Thread(() -> {
            int stamp = stampedReference.getStamp();
            System.out.println(Thread.currentThread().getName() + "\t 第1次版本号" + stamp + "\t值是" + stampedReference.getReference());
            //保证线程3完成1次ABA
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean result = stampedReference.compareAndSet(100， 2019， stamp， stamp + 1);
            System.out.println(Thread.currentThread().getName() + "\t 修改成功否" + result + "\t最新版本号" + stampedReference.getStamp());
            System.out.println("最新的值\t" + stampedReference.getReference());
        }， "t4").start();
    }
```

### 线程不安全集合类

#### ArrayList

**在高并发多线程下会报java.util.ConcurrentModificationException并发修改异常**

```java
//案例
public class ArrayListNotSafe {

    public static void main(String[] args) {
//        List list = new ArrayList<>(); //ArrayList线程不安全
//        List list = new Vector();//底层是使用synchronized修饰，处理并发能力低
//        List list = Collections.synchronizedList(new ArrayList<>());
        List list = new CopyOnWriteArrayList<>();//CopyOnWrite写时复制容器
        for (int i = 1; i <= 30; i++) {
            new Thread(() -> {
                list.add(UUID.randomUUID().toString().substring(0， 8));
                System.out.println(list);
            }， String.valueOf(i)).start();
        }
    }
}
/**
 * 1.故障现象
 * java.util.ConcurrentModificationException 修改并发异常
 * 2.导致原因
 *  并发争抢修改导致；一个人正在写入，另外一个人过来抢夺，导致数据不一致。并发修改异常。
 * 3.解决方法
 *  3.1：new Vector()
 *  3.2：Collections.synchronizedList(new ArrayList<>())
 *  3.3：new CopyOnWriteArrayList<>();
 */

```

> 写时复制:
>
> copyOnWrite 容器即写时复制的容器 往容器添加元素的时候，不直接往当前容器object[]添加，而是先将当前容器object[]进行copy 复制出一个新的object[] newElements 然后向新容器object[] newElements 里面添加元素 添加元素后， 再将原容器的引用指向新的容器 setArray(newElements);这样的好处是可以对copyOnWrite容器进行并发的读，而不需要加锁 因为当前容器不会添加任何容器.所以copyOnwrite容器也是一种读写分离的思想，读和写不同的容器.

```java
//copyOnWriteArrayList底层调用的add源码
public boolean add(E e) {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        Object[] elements = getArray();
        int len = elements.length;
        Object[] newElements = Arrays.copyOf(elements， len + 1);
        newElements[len] = e;
        setArray(newElements);
        return true;
    } finally {
        lock.unlock();
    }
   }
```

#### HashSet

```java
/**
  解决方法:
  1.Collections.synchronizedSet(new HashSet<>())
  2.new CopyOnWriteArraySet<>();
*/
public class HashSetNotSafe {
    public static void main(String[] args) {
//        Set<String> set = new HashSet<>();//线程不安全
//        Set<String> set =  Collections.synchronizedSet(new HashSet<>());
        Set<String> set = new CopyOnWriteArraySet<>();
        for (int i = 1; i <= 30; i++) {
            new Thread(() -> {
                set.add(UUID.randomUUID().toString().substring(0， 8));
                System.out.println(set);
            }， String.valueOf(i)).start();
        }
    }
}
```

#### HashMap

```java
/**
  解决方法:
  1.Collections.synchronizedMap(new HashMap<String， String>())
  2.new ConcurrentHashMap<>()
*/
public class HashMapNotSafe {
    public static void main(String[] args) {
//        Map<String， String> map = new HashMap<>();//线程不安全
//        Map<String， String> map = Collections.synchronizedMap(new HashMap<String， String>());
        Map<String， String> map = new ConcurrentHashMap<>();
        for (int i = 1; i <= 30; i++) {
            new Thread(() -> {
                map.put(Thread.currentThread().getName()， UUID.randomUUID().toString().substring(0， 8));
                System.out.println(map);
            }， String.valueOf(i)).start();
        }
    }
```

### 锁的理解

#### 公平锁与非公平锁

公平锁：是指多个线程按照申请锁的顺序来获取锁，类似排队打饭 ，先来后到

非公平锁：是指在多线程获取锁的顺序并不是按照申请锁的顺序，有可能后申请的线程比先申请的线程优先获取到锁，在高并发的情况下，有可能造成优先级反转或者饥饿现象



公平锁与非公平锁的区别：

![1597157256609](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597157256609.png)

题外话:

并发包ReentrantLock的创建可以指定构造函数的boolean类型来得到公平锁或者非公平锁， 默认是非公平锁

非公平锁的优点在于吞吐量比公平锁大。

对于synchronized而言也是一种非公平锁。

```java
Lock lock = new ReentrantLock(false);//默认false 非公平锁 如果设置为true则为公平锁
public ReentrantLock(boolean fair) {
    sync = fair ? new FairSync() : new NonfairSync();
}
```

#### 可重入锁(又名递归锁)

指的是同一个线程外层函数获得锁之后，内层递归函数仍然能获取该锁的代码，在同一个线程在外层方法获取锁的时候，再进入内层方法会自动获取锁。

也就是说，**线程可以进入任何一个它已经拥有的锁所同步着的代码块。**

**ReentrantLock/synchronized就是一个典型的可重入锁。**

**可重入锁的最大作用就是避免死锁。**

```java
//使用synchronized加锁
class Phone {
    public synchronized void sendEMS() throws Exception {
        System.out.println(Thread.currentThread().getName() + " synchronized sendEMS()");
        sendEmail();
    }

    public synchronized void sendEmail() throws Exception {
        System.out.println(Thread.currentThread().getName() + " synchronized sendEmail()");
    }

}

public class ReentrantLockDemo {
    public static void main(String[] args) {
        Phone phone = new Phone();
        new Thread(() -> {
            try {
                phone.sendEMS();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }， "t1").start();
        new Thread(() -> {
            try {
                phone.sendEMS();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }， "t2").start();
    }
}
```

```java
//使用Lock加锁
class Phone implements Runnable {
    private Lock lock = new ReentrantLock();

    @Override
    public void run() {
        get();
    }

    public void get() {
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + "\tget()");
            set();
        } finally {
            lock.unlock();
        }
    }

    public void set() {
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + "\tset()");
        } finally {
            lock.unlock();
        }
    }
}

public class ReentrantLockDemo {
    public static void main(String[] args) {
        Phone phone = new Phone();
        Thread t3 = new Thread(phone， "t3");
        Thread t4 = new Thread(phone， "t4");
        t3.start();
        t4.start();
    }
}

```

#### 自旋锁

自旋锁(spinlock)：是指尝试获取锁的线程不会立即堵塞，而是**采用循环的方式去尝试获取锁，**这样的好处是减少线程上下文切换的消耗，缺点是循环会消耗CPU

```java
//源码
public final int getAndAddInt(Object var1， long var2， int var4) {
    int var5;
    do {
        var5 = this.getIntVolatile(var1， var2);
    } while(!this.compareAndSwapInt(var1， var2， var5， var5 + var4));

    return var5;
}
```

手动写个自旋锁

```java
public class SpinLockDemo {
    //原子引用线程
    AtomicReference<Thread> atomicReference = new AtomicReference<>();

    public void myLock() {
        Thread thread = Thread.currentThread();
        System.out.println(Thread.currentThread().getName() + " come in O(∩_∩)O哈哈~");
        while (!atomicReference.compareAndSet(null， thread)) {
            System.out.println(Thread.currentThread().getName() + "一直等待...");
        }
    }

    public void myUnLock() {
        Thread thread = Thread.currentThread();
        atomicReference.compareAndSet(thread， null);
        System.out.println(Thread.currentThread().getName() + " myUnLock()");
    }


    public static void main(String[] args) {
        SpinLockDemo spinLockDemo = new SpinLockDemo();
        new Thread(() -> {
            spinLockDemo.myLock();
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            spinLockDemo.myUnLock();
        }， "AA").start();

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            spinLockDemo.myLock();
            spinLockDemo.myUnLock();
        }， "BB").start();
    }
}
```



#### 读写锁

**独占锁（写锁）/共享锁（读锁）/互斥锁**

独占锁：指该锁一次只能被一个线程所持有。对ReentrantLock和Synchronized而言都是独占锁。

共享锁：指该锁可被多个线程所持有。

对ReentrantReadWriteLock而言，其读锁是共享锁，其写锁是独占锁。

读锁的共享锁可保证并发读是非常高效的，读写，写读，写写的过程是互斥的。

```java
/**多个线程同时操作 一个资源类没有任何问题 所以为了满足并发量
 * 读取共享资源应该可以同时进行
 * 但是
 * 如果有一个线程想去写共享资源来  就不应该有其他线程可以对资源进行读或写
 * 小总结:
 * 读 读能共存
 * 读 写不能共存
 * 写 写不能共存
 * 写操作 原子+独占 整个过程必须是一个完成的统一整体 中间不允许被分割 被打断
 */
class Mycache {//资源类
    //volatile为了保证可见性
    private volatile Map<String， Object> map = new HashMap<>();
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	
    //写操作
    public void put(String key， Object value) {
        readWriteLock.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + "\t开始写入：" + key);
            map.put(key， value);
            System.out.println(Thread.currentThread().getName() + "\t写入完成");
        } finally {
            readWriteLock.writeLock().unlock();
        }

    }

    //读操作
    public void get(String key) {
        try {
            System.out.println(Thread.currentThread().getName() + "\t开始读取");
            Object result = map.get(key);
            System.out.println(Thread.currentThread().getName() + "\t读取完成：" + result);
        } finally {
            readWriteLock.readLock().unlock();
        }

    }

}

public class ReentrantReadWriteLockDemo {
    public static void main(String[] args) {
        Mycache mycache = new Mycache();
        //模拟分别有5个线程进行写操作和读操作
        for (int i = 1; i <= 5; i++) {
            final int tempInt = i;
            new Thread(() -> {
                mycache.put(tempInt + ""， tempInt + "");
            }， String.valueOf(i)).start();
        }

        for (int i = 1; i <= 5; i++) {
            final int tempInt = i;
            new Thread(() -> {
                mycache.get(tempInt + "");
            }， String.valueOf(i)).start();
        }

    }
}

```

### JUC的辅助类

#### CountDownLatch

AQS

CountDownLatch：减少计数

CountDownLatch：让一些线程堵塞直到另外一些线程完成后才唤醒

CountDownLatch主要有两个方法：countDown()和await()

当一个或多个线程调用await()方法时，调用线程会被堵塞。其他线程调用countDown()方法计数减1(调用countDown()方法时线程不会堵塞)，当计数器的值变为0，因调用await()被堵塞的线程会被唤醒，继续执行。

```java
//离开公司，最后锁门
public class CountDownLatchDemo {
    public static void main(String[] args) {
        CountDownLatch countDownLatch = new CountDownLatch(6);
        for (int i = 1; i <= 6; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + "离开公司");
                countDownLatch.countDown();
            }， String.valueOf(i)).start();
        }
        try {
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + "\t锁门");
    }
}

```

```java
//秦灭六国统一华夏
public class CountDownLatchDemo {
    public static void main(String[] args) {
        CountDownLatch countDownLatch = new CountDownLatch(6);
        for (int i = 1; i <= 6; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + "\t国被灭");
                countDownLatch.countDown();
            }， CountryEnum.foreach(i).getRetMessage()).start();
        }
        try {
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + "\t秦帝国统一华夏");
    }
}
//枚举类，也可以作为一个数据库使用
public enum CountryEnum {
    ONE(1， "齐")， TWO(2， "楚")， THREE(3， "燕")， FOUR(4， "赵")， FIVE(5， "魏")， SIX(6， "韩");
    
    @Getter
    private Integer retCode;
    @Getter
    private String retMessage;

    CountryEnum(Integer retCode， String retMessage) {
        this.retCode = retCode;
        this.retMessage = retMessage;
    }

    public static CountryEnum foreach(Integer index) {
        CountryEnum[] countryEnums  = CountryEnum.values();
        for (CountryEnum element : countryEnums ) {
            if (element.getRetCode() == index) {
                return element;
            }
        }
        return null;
    }
}
```

#### **CycliBarrier**

CycliBarrier：循环栅栏

CycliBarries的字面意思是可循环(Cyclic)使用的屏障(cyclicBarrier)。它要做的事情是：让一组线程到达一个屏障(也叫做同步点)时被堵塞，直到最后一个线程到达屏障时，屏障才会开门，所有被屏障拦截的线程才会继续干活，线程进入屏障通过CycliBarrier的await()方法。

```java
//集齐7颗龙珠召唤神龙
public class CyclicBarrierDeo {
    public static void main(String[] args) {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(7， () -> {
            System.out.println(Thread.currentThread().getName() + "\t召唤神龙");
        });
        for (int i = 1; i <= 7; i++) {
            final int tempInt = i;
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + "\t收到到第：" + tempInt + "颗龙珠");
                try {
                    cyclicBarrier.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }， String.valueOf(i)).start();
        }
    }
}
```



#### Semaphore

多个线程抢多个资源

Semaphore：信号灯或者信号量

信号量的主要用于两个目的：一个是用于多个共享资源的互斥作用，另一个用于并发线程数的控制

信号量的两个方法：

acquire()：当一个线程调用了acquire操作时，它要么通过成功获取信号量(信号量减1)，要么一直等下去，直到有线程释放信号量或超时。

release()：实际上会将信号量加1，然后唤醒等待的线程。

```java
//抢车位
public class SemaphoreDemo {
    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(3);

        //模拟6辆车争夺3个停车位
        for (int i = 1; i <= 6; i++) {
            new Thread(() -> {
                try {
                    semaphore.acquire();//抢到资源
                    System.out.println(Thread.currentThread().getName() + "\t号车停车了");
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName() + "\t号开走了");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release();//释放资源
                }
            }， String.valueOf(i)).start();
        }
    }
}
```

### 堵塞队列

#### BlockingQueue

MQ底层原理用的就是这个

阻塞队列：顾名思义，首先它是一个队列，而一个阻塞队列在数据结构中所起的作用大致如图所示:![1597230045669](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597230045669.png)

队列+堵塞队列

当堵塞队列是空时，从队列中获取元素的操作将会被堵塞。

当堵塞队列是满时，往队列中添加元素的操作将会被堵塞。

> 为什么用？有什么好处？
>
> 在多线程领域，所谓阻塞，在某些情况下会挂起线程(即线程阻塞)，一旦条件满足，被挂起的线程就会被自动唤醒。
>
> 为什么需要使用BlockingQueue
>
> 好处是我们不需要关心什么时候需要阻塞线程，什么时候需要唤醒线程，因为BlockingQueue都一手给你包办好了
>
> 在concurrent包 发布以前，在多线程环境下，我们每个程序员都必须自己去控制这些细节，尤其还要兼顾效率和线程安全，而这会给我们的程序带来不小的复杂度。

#### 架构介绍

![1597230922178](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597230922178.png)

#### 种类分析

> **ArrayBlockingQueue：由数组结构组成的有界堵塞队列。**
>
> **LinkedBlockingDeque：由链表结构组成的有界(但大小默认Integer.Max_Value)堵塞队列**
>
> PriorityBlockingQueue：支持优先级排序的无界堵塞队列。
>
> DelayQueue：使用优先级队列实验的延迟无界堵塞队列。
>
> **SynchronousQueue：不存储元素的堵塞队列，也即是单个元素的队列。**
>
> LinkedTransferQueue：由链表结构组成的无界堵塞队列。
>
> LinkedBlockingDeque：由链表结构组成的双向堵塞队列。

#### BlockingQueue的核心方法

![1597231930957](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597231930957.png)

| 抛出异常 | 当阻塞队列满时，再往队列里面add插入元素会抛IllegalStateException: Queue full<br/>当阻塞队列空时，再往队列Remove元素时候回抛出NoSuchElementException |
| :------- | :----------------------------------------------------------- |
| 特殊值   | 插入方法，成功返回true 失败返回false<br/>移除方法，成功返回元素，队列里面没有就返回null |
| 一直阻塞 | 一直阻塞	当阻塞队列满时，生产者继续往队列里面put元素，队列会一直阻塞直到put数据or响应中断退出<br/>当阻塞队列空时，消费者试图从队列take元素，队列会一直阻塞消费者线程直到队列可用. |
| 超时退出 | 当阻塞队列满时，队列会阻塞生产者线程一定时间，超过后限时后生产者线程就会退出 |



##### 抛出异常类型

```java
public class BlockingQueueDemo {
    public static void main(String[] args) {
//        List<String> list = new ArrayList<>();
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue(3);

        System.out.println(blockingQueue.add("a"));//true
        System.out.println(blockingQueue.add("b"));//true
        System.out.println(blockingQueue.add("c"));//true
        //队列已满，如果再次添加元素就会报异常java.lang.IllegalStateException: Queue full
//        System.out.println(blockingQueue.add("d"));


        System.out.println(blockingQueue.remove());//a
        System.out.println(blockingQueue.remove());//b
        System.out.println(blockingQueue.remove());//c
        //队列已空，如果再次移除或者查看元素将会报异常java.util.NoSuchElementException
//        System.out.println(blockingQueue.remove());
        
        //查看顶端元素(探测器)，根据FIFO先进先出的原则
//        System.out.println(blockingQueue.element());//a

    }
}
```

##### 特殊值类型

```java
public class BlockingQueueDemo {
    public static void main(String[] args) {
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(3);
        System.out.println(blockingQueue.offer("a"));
        System.out.println(blockingQueue.offer("b"));
        System.out.println(blockingQueue.offer("c"));
        System.out.println(blockingQueue.offer("d"));//false

        System.out.println(blockingQueue.peek());

        System.out.println(blockingQueue.poll());
        System.out.println(blockingQueue.poll());
        System.out.println(blockingQueue.poll());
        System.out.println(blockingQueue.poll());//null

    }
}
```

##### 堵塞类型

```java
public class BlockingQueueDemo {
    public static void main(String[] args) throws Exception{
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(3);
        blockingQueue.put("a");
        blockingQueue.put("b");
        blockingQueue.put("c");
//        blockingQueue.put("d");//一直堵塞中

        blockingQueue.take();
        blockingQueue.take();
        blockingQueue.take();
        blockingQueue.take();//一直堵塞

    }
}

```

##### 超时类型

```java
public class BlockingQueueDemo {
    public static void main(String[] args) throws Exception{
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(3);
        blockingQueue.offer("a"，2， TimeUnit.SECONDS);
        blockingQueue.offer("b"，2， TimeUnit.SECONDS);
        blockingQueue.offer("c"，2， TimeUnit.SECONDS);
//        blockingQueue.offer("d"，2， TimeUnit.SECONDS);//会堵塞2秒


        blockingQueue.poll(2，TimeUnit.SECONDS);
        blockingQueue.poll(2，TimeUnit.SECONDS);
        blockingQueue.poll(2，TimeUnit.SECONDS);
        blockingQueue.poll(2，TimeUnit.SECONDS);//会堵塞2秒
    }
}
```

#### 同步队列

SynchronousQueue

> SynchronousQueue没有容量
>
> 与其他BlcokingQueue不同，SynchronousQueue是一个不存储元素的BlcokingQueue
>
> 每个put操作必须要等待一个take操作，否则不能继续添加元素，反之亦然.
>
> 生成一个消费一个。

```java
public class SynchronousQueueDemo {
    public static void main(String[] args) {
        BlockingQueue<String> blockingQueue = new SynchronousQueue<>();

        new Thread(() -> {
            try {
                System.out.println(Thread.currentThread().getName() + "\t put1");
                blockingQueue.put("1");

                System.out.println(Thread.currentThread().getName() + "\t put2");
                blockingQueue.put("2");

                System.out.println(Thread.currentThread().getName() + "\t put3");
                blockingQueue.put("3");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "AAA").start();


        new Thread(() -> {

            try {
                try {
                    TimeUnit.SECONDS.sleep(5);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + "\t" + blockingQueue.take());
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + "\t" + blockingQueue.take());
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + "\t" + blockingQueue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }, "BBB").start();
    }
}

```

#### 线程通信

##### 生产者消费者传统版

传统版：一个初始值为0的变量 两个线程交替操作 一个加1 一个减1来5轮

```java
class ShareData {
    private int number = 0;
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public void increment() throws Exception {
        lock.lock();
        try {
            //1.判断 ,为了虚假唤醒使用while
            while (number != 0) {
                //等待 不生产
                condition.await();
            }
            //2.干活
            number++;
            System.out.println(Thread.currentThread().getName() + "\t" + number);
            //3.通知唤醒
            condition.signalAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void decrement() throws Exception {
        lock.lock();
        try {
            //1.判断
            while (number == 0) {
                //等待 不生产
                condition.await();
            }
            //2.干活
            number--;
            System.out.println(Thread.currentThread().getName() + "\t" + number);
            //3.通知唤醒
            condition.signalAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}

public class ProdConsumerTraditionDemo {
    public static void main(String[] args) {
        //线程操作资源类
        ShareData shareData = new ShareData();

        new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                try {
                    shareData.increment();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "AA").start();
        new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                try {
                    shareData.decrement();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "BB").start();
        
    }
}
```

##### 生产者消费者堵塞队列版

堵塞队列版：一个初始值为0的变量 两个线程交替操作 一个加1 一个减1来5轮

```java
class MyResource {//资源类
    private volatile boolean flag = true;//默认为true 生产+消费
    private AtomicInteger atomicInteger = new AtomicInteger();
    private BlockingQueue<String> blockingQueue = null;

    public MyResource(BlockingQueue<String> blockingQueue) {
        this.blockingQueue = blockingQueue;
        System.out.println(blockingQueue.getClass().getName());
    }

    public void produce() throws Exception {
        String data = null;
        while (flag) {
            data = atomicInteger.incrementAndGet() + "";
            boolean result = blockingQueue.offer(data, 2, TimeUnit.SECONDS);
            if (result) {
                System.out.println(Thread.currentThread().getName() + "生产蛋糕\t" + data + "成功");
            } else {
                System.out.println(Thread.currentThread().getName() + "生产蛋糕\t" + data + "失败");
            }

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println(Thread.currentThread().getName() + "\t 叫停生产");
    }

    public void consumer() throws Exception {
        String result = null;
        while (flag) {
            result = blockingQueue.poll(2, TimeUnit.SECONDS);
            if (result == null || result.equalsIgnoreCase("")) {
                flag = false;
                System.out.println(Thread.currentThread().getName() + "\t" + "超过2m没有取到 消费退出");
                return;
            }
            System.out.println(Thread.currentThread().getName() + "消费蛋糕\t" + result + "成功");

        }
    }

    public void stop() {
        this.flag = false;
    }
}

public class ProConsumerBlockQueue {
    public static void main(String[] args) {
        MyResource myResource = new MyResource(new ArrayBlockingQueue<String>(10));

        new Thread(() -> {
            try {
                myResource.produce();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "produce").start();

        new Thread(() -> {
            try {
                myResource.consumer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "consumer").start();

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        myResource.stop();
    }
}
```



#### synchronized与Lock的区别

汇编语言

<img src="C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597247486374.png" alt="1597247486374" style="zoom: 100%;" />

例子：

```java
/**
 * 题目：多线程之间按照顺序调用，实现A->B->三个线程启动，
 * 要求如下：
 * AA打印5	次，BB打印10次，CC打印15次
 * 紧接着
 * AA打印5次，BB打印10次，CC打印15次
 * ....
 * 来10轮
 */
class MyCondition {

    private Integer letter = 1; //AA 1 BB 2 CC 3
    private Lock lock = new ReentrantLock();
    private Condition condition1 = lock.newCondition();
    private Condition condition2 = lock.newCondition();
    private Condition condition3 = lock.newCondition();

    public void print5() {
        lock.lock();
        try {
            //判断
            while (letter != 1) {
                condition1.await();
            }
            //干活
            letter = 2;
            for (int i = 1; i <= 5; i++) {
                System.out.println(Thread.currentThread().getName() + "\t" + i);
            }
            //通知
            condition2.signalAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void print10() {
        lock.lock();
        try {
            //判断
            while (letter != 2) {
                condition2.await();
            }
            //干活
            letter = 3;
            for (int i = 1; i <= 10; i++) {
                System.out.println(Thread.currentThread().getName() + "\t" + i);

            }
            //通知
            condition3.signalAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void print15() {
        lock.lock();
        try {
            //判断
            while (letter != 3) {
                condition3.await();
            }
            //干活
            letter = 1;
            for (int i = 1; i <= 15; i++) {
                System.out.println(Thread.currentThread().getName() + "\t" + i);
            }
            //通知
            condition1.signalAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

}

public class ConditionDemo {
    public static void main(String[] args) {
        //线程操作资源类
        MyCondition myCondition = new MyCondition();
        new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                myCondition.print5();
            }
        }, "AA").start();

        new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                myCondition.print10();
            }
        }, "BB").start();

        new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                myCondition.print15();
            }
        }, "CC").start();
    }
}

```

### Callable接口

futureTask.get()：获取值时要放到主线程最后，否则会堵塞主线程。只计算一次，get方法放到最后。

futureTask：未来的任务，用它就干一件事，异步调用。

![1597334280801](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597334280801.png)

```java
//源码 
public FutureTask(Callable<V> callable) {
     if (callable == null)
         throw new NullPointerException();
     this.callable = callable;
     this.state = NEW;       // ensure visibility of callable
 }
```

```java
class MyThread implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        System.out.println(Thread.currentThread().getName() + "\t come in Callable");
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 1024;
    }
}

public class CallableDemo {
    public static void main(String[] args) throws Exception {
        MyThread thread = new MyThread();
        FutureTask<Integer> futureTask = new FutureTask<>(thread);
        new Thread(futureTask, "AA").start();

        System.out.println(Thread.currentThread().getName() + "*********");
        int result1 = 100;
        //要求获得Callable线程的计算结果，如果没有计算完成就要去强求，会导致阻塞，直到计算完成。
        int result2 = futureTask.get();
        System.out.println("value = " + (result1 + result2));
    }
}

```

### 线程池

为什么使用线程池，优势？

> 线程池做的工作主要是控制运行的线程的数量，处理过程中将任务加入队列，然后在线程创建后启动这些任务，如果先生超过了最大数量，超出的数量的线程排队等候，等其他线程执行完毕，再从队列中取出任务来执行。
>
> 他的主要特点为：线程复用；控制最大并发数；管理线程
>
> 第一:降低资源消耗.通过重复利用自己创建的线程降低线程创建和销毁造成的消耗。
> 第二: 提高响应速度.当任务到达时，任务可以不需要等到线程和粗昂就爱你就能立即执行。
> 第三: 提高线程的可管理性.线程是稀缺资源，如果无限的创阿金，不仅会消耗资源，还会较低系统的稳定性，使用线程池可以进行统一分配，调优和监控。

#### 架构实现

Java中的线程池是通过Executor框架实现的,该框架中用到了Executor，Execurors，ExecutorService，ThreadPoolExecutor这几个类

![1597468565261](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597468565261.png)

#### **编码实现**

##### newFixedThreadPool

```java
Executors.newFixedThreadPool(int);//执行一个长期的任务，性能好很多
//底层源码
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads, nThreads,
                                  0L, TimeUnit.MILLISECONDS,
                                  new LinkedBlockingQueue<Runnable>());
}
主要特点如下:
1.创建一个定长线程池,可控制线程的最大并发数,超出的线程会在队列中等待.
2.newFixedThreadPool创建的线程池corePoolSize和MaxmumPoolSize是相等的,它使用是LinkedBlockingQueue
    
```

##### newSingleThreadExecutor

```java
Executors.newSingleThreadExecutor();//一个任务一个任务执行的场景
//底层源码
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService
        (new ThreadPoolExecutor(1, 1,
                                0L, TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>()));
}
主要特点如下:
1.创建一个单线程化的线程池,它只会用唯一的工作线程来执行任务,保证所有任务都按照指定顺序执行.
2.newSingleThreadExecutor将corePoolSize和MaxmumPoolSize都设置为1,它使用的的LinkedBlockingQueue
```

##### newCachedThreadPool

```java
Executors.newCachedThreadPool()；//适用：执行很多短期异步的小程序或者负载较轻的服务器
//底层源码    
 public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                  60L, TimeUnit.SECONDS,
                                  new SynchronousQueue<Runnable>());
}
主要特点如下:
1.创建一个可缓存线程池,如果线程池长度超过处理需要,可灵活回收空闲线程,若无可回收,则创建新线程.
2.newCachedThreadPool将corePoolSize设置为0，MaxmumPoolSize设置为Integer.MAX_VALUE,它使用的是SynchronousQueue,也就是说来了任务就创建线程运行,如果线程空闲超过60秒,就销毁线程
```

##### newWorkStealingPool

```
newWorkStealingPool(int) ;//java8新增，使用目前机器上可以的处理器作为他的并行级别。
```

##### newScheduledThreadPool

##### Demo实现

```java
public class ThreadPoolExecutorDemo {
    public static void main(String[] args) {
//    ExecutorService threadPool = Executors.newFixedThreadPool(5);//一个池子5个处理线程
//    ExecutorService threadPool = Executors.newSingleThreadExecutor();//一个池子1个处理线程
        ExecutorService threadPool = Executors.newCachedThreadPool();//一个池子N个处理线程

        try {
            for (int i = 1; i <= 10; i++) {
                threadPool.execute(() -> {
                    System.out.println(Thread.currentThread().getName() + "\t处理业务");
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }
}
                                                                                                                                                                              
```

#### ThreadPoolExecutor

```java
//底层源码 
public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          ThreadFactory threadFactory,
                          RejectedExecutionHandler handler) {
    if (corePoolSize < 0 ||
        maximumPoolSize <= 0 ||
        maximumPoolSize < corePoolSize ||
        keepAliveTime < 0)
        throw new IllegalArgumentException();
    if (workQueue == null || threadFactory == null || handler == null)
        throw new NullPointerException();
    this.acc = System.getSecurityManager() == null ?
        null :
    AccessController.getContext();
    this.corePoolSize = corePoolSize;
    this.maximumPoolSize = maximumPoolSize;
    this.workQueue = workQueue;
    this.keepAliveTime = unit.toNanos(keepAliveTime);
    this.threadFactory = threadFactory;
    this.handler = handler;
}
```

![1597677131504](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597677131504.png)

##### 7大参数

> 1.corePoolSize：线程池中的常驻核心线程数
>
> ​	在创建了线程池后,当有请求任务来之后,就会安排池中的线程去执行请求任务,近视理解为今日当值线程，
> ​	当线程池中的线程数目达到corePoolSize后,就会把到达的任务放入到缓存队列当中。
>
> 2.maximumPoolSize：线程池能够容纳同事执行的最大线程数，此值必须大于等于1
>
> 3.keepAliveTime：多余的空闲线程的存活时间。
>
> 当前线程池数量超过corePoolSize时，当空闲时间达到keepAliveTime值时，多余空闲线程会被销毁直到只剩下corePoolSize线程为止。
>
> 4.unit：keepAliveTime的单位
>
> 5.workQueue：任务队列，被提交但尚未被执行的任务。
>
> 6.threadFactory：表示生成线程池中工作线程的线程工厂，用于创建线程一般用默认的即可。
>
> 7.handler：拒绝策略，表示当队列满了并且工作线程大于等于线程池的最大线程数(maximumPoolSize)如何来拒绝。

##### 底层原理

![1597678244784](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597678244784.png)

![1597678253006](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597678253006.png)

##### 拒绝策略

###### 是什么？

> 等待队列也已经排满了，再也塞不下新的任务了，同时，程池的max也到达了,无法接续为新任务服务，
>
> 线程池的max也到达了,无法接续为新任务服务。

###### JDK内置的拒绝策略

> 1.AbortPolicy(默认)：直接抛出RejectedExecutionException异常阻止系统正常运行。

> 2.CallerRunsPolicy："调用者运行"一种调节机制，该策略既不会抛弃任务，不会抛出异常，而是将任务回退给调用者，从而降低新任务的流量。

> 3.DiscardOldestPolicy：抛弃队列中等待最久的任务，然后把当前任务加入队列中尝试再次提交。
>
> 4.DiscardPolicy：直接丢弃任务，不予任何处理也不抛出异常，如果允许任务丢失，这是最好的拒绝策略。
>
> 以上内置策略均实现了RejectExecutionHandler接口。	

###### 自定义线程池

![1597682602675](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597682602675.png)

【强制】线程资源必须通过线程池提供，不允许在应用中自行显式创建线程。 说明：使用线程池的好处是减少在创建和销毁线程上所消耗的时间以及系统资源的开销，解决资源不足的问题。如果不使用线程池，有可能造成系统创建大量同类线程而导致消耗完内存或者“过度切换”的问题。

![1597679371524](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597679371524.png)

###### 合理配置线程池

CPU密集型：

System.out.println(Runtime.getRuntime().availableProcessors());查看CPU核数

![1597682092040](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597682092040.png)

IO密集型：

1.由于IO密集型任务线程并不是一直在执行任务，则应配置尽可能多的线程，如CPU核数*2

2.![1597682171615](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597682171615.png)

#### 死锁编码

##### 是什么？

![1597762528539](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597762528539.png)

```java
class HoldLockThread implements Runnable {

    private String lockA;
    private String lockB;

    public HoldLockThread(String lockA, String lockB) {
        this.lockA = lockA;
        this.lockB = lockB;
    }

    @Override
    public void run() {
        synchronized (lockA) {
            System.out.println(Thread.currentThread().getName() + "\t 持有" + lockA + "\t尝试获取" + lockB);
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (lockB) {
                System.out.println(Thread.currentThread().getName() + "\t 持有" + lockB + "\t尝试获取" + lockA);
            }
        }

    }
}

public class DeadLockDemo {
    public static void main(String[] args) {
        new Thread(new HoldLockThread("lockA", "lockB"), "ThreadAAA").start();
        new Thread(new HoldLockThread("lockB", "lockA"), "ThreadBBB").start();
    }
}

```

##### 产生死锁原因

> 1.系统资源不足
>
> 2.进程运行推进的顺序不合适
>
> 3.资源分配不当

##### 解决

1.jps命令定位进程编号

![1597762931450](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597762931450.png)

2.jstack找到死锁查看

![1597762971756](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597762971756.png)



## JVM

### JVM体系结构概览

![1597764486274](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597764486274.png)

灰色线程私有，不存在GC

黄色线程所共享，存在GC

### 类装载器ClassLoader

负责加载class文件，class文件在文件开头**有特定的文件标示**，将class文件字节码内容加载到内存中，并将这些内容转换成方法区中的运行时数据结构并且ClassLoader只负责class文件的加载，至于它是否可以运行，则由Execution Engine决定。

![1597767170139](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597767170139.png)

### 类加载器分类![1597767236113](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597767236113.png)

> sun.misc.Launcher它是一个java虚拟机的入口应用

### 双亲委派机制

![1597766645620](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597766645620.png)

### 沙箱安全机制

沙箱安全机制是由基于双亲委派机制上采取的一种JVM的自我保护机制,假设你要写一个java.lang.String 的类,由于双亲委派机制的原理,此请求会先交给Bootstrap试图进行加载,但是Bootstrap在加载类时首先通过包和类名查找rt.jar中有没有该类,有则优先加载rt.jar包中的类,因此就保证了java的运行机制不会被破坏. 

### 执行引擎

Execution Engine 执行引擎：负责解释命令，提交操作系统执行。

### 本地方法栈

native在本地方法栈主要调底层的C语言函数库。

登记native方法，在Execution Engine 执行时加载本地方法库。

### 本地方法接口

本地接口的作用是融合不同的编程语言为 Java 所用，它的初衷是融合 C/C++程序，Java 诞生的时候是 C/C++横行的时候，要想立足，必须有调用 C/C++程序，于是就在内存中专门开辟了一块区域处理标记为native的代码，它的具体做法是 Native Method Stack中登记native方法，在Execution Engine 执行时加载native libraies。

### PC寄存器(程序计数器)

每个线程都有一个程序计数器，是线程私有的,就是一个指针，指向方法区中的方法字节码（**用来存储指向下一条指令的地址,也即将要执行的指令代码**），由执行引擎读取下一条指令，是一个非常小的内
存空间，几乎可以忽略不记。(存在CPU上)

### 方法区

方法区是被所有线程共享，所有字段和方法字节码，以及一些特殊方法如构造函数，接口代码也在此定义。简单说，所有定义的方法的信息都保存在该区域，此区属于共享区间。
**静态变量+常量+类信息(构造方法/接口定义)+运行时常量池存在方法区中**
But
**实例变量存在堆内存中,和方法区无关**

### 栈(stack)

**栈管运行，堆管存储。**

> 1.队列（FIF0）：先进先出
>
> 2.栈（FILO）：先进后出，后进先出

栈也叫栈内存，主管Java程序的运行，是在线程创建时创建，它的生命期是跟随线程的生命期，线程结束栈内存也就释放， 对于栈来说不存在垃圾回收问题，只要线程一结束该栈就Over，生命周期和线程一致，是线程私有的。**8种基本类型的变量+对象的引用变量+实例方法**都是在函数的栈内存中分配。

**栈帧中主要保存3 类数据：**

> 1.本地变量（Local Variables）:输入参数和输出参数以及方法内的变量；
> 2.栈操作（Operand Stack）:记录出栈、入栈的操作；
> 3.栈帧数据（Frame Data）:包括类文件、方法等等。

**栈运行原理**:

栈中的数据都是以栈帧（Stack Frame）的格式存在，栈帧是一个内存区块，是一个数据集，是一个有关方法(Method)和运行期数据的数据集，当一个方法A被调用时就产生了一个栈帧 F1，并被压入到栈中，A方法又调用了 B方法，于是产生栈帧 F2 也被压入栈，B方法又调用了 C方法，于是产生栈帧 F3 也被压入栈，
……
执行完毕后，先弹出F3栈帧，再弹出F2栈帧，再弹出F1栈帧……
遵循“先进后出”/“后进先出”原则。

![1597934760586](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597934760586.png)

**如果一直压栈操作,就会造成栈内存溢出Exception in thread "main" java.lang.StackOverflowError**



**栈 + 堆 + 方法区的交互关系**

> HotSpot是使用指针的方式来访问对象：
> Java堆中会存放访问类元数据的地址，
> reference存储的就直接是对象的地址

![1598023433086](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598023433086.png)

### 堆(heap)

> 一个JVM实例只存在一个堆内存，堆内存的大小是可以调节的。类加载器读取了类文件后，需要把类、方法、常变量放到堆内存中，保存所有引用类型的真实信息，以方便执行器执行，堆内存分为三部分：
>
> Young Generation Space 新生区   Young/New
>
> Tenure generation space 养老区  Old/ Tenure
>
> Permanent Space 永久区  Perm  物理上位置存放在方法区

Heap堆( Java7 之前)
堆内存 逻辑上 分为三部分：新生区+养老区+永久区 (java8元空间代替永久区)

物理上分为新生区和养老区,

![1597940771628](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1597940771628.png)
**新生区:**

新生区又分为两部分： 伊甸区（Eden space）和幸存者区（Survivor pace） ，所有的类都是在伊甸
区被new出来的。幸存区有两个： 0区（Survivor 0 space）和1区（Survivor 1 space）。

当伊甸园的空间用完时，程序又需要创建对象，JVM的垃圾回收器将对伊甸园区进行垃圾回收(Minor GC)，将伊甸园区中的不再被其他对象所引用的对象进行销毁。然后将伊甸园中的剩余对象移动到幸存 0区。若幸存 0区也满了，再对该区进行垃圾回收，然后移动到 1 区。那如果1 区也满了呢？GC15次之后将存活的对象，再移动到养老区。若养老区也满了，那么这个时候将产生MajorGC（FullGC），进行养老区的内存清理。若养老区执行了Full GC之后发现依然无法进行对的保存，就会产生OOM异常“OutOfMemoryError”。

**如果出现java.lang.OutOfMemoryError: Java heap space异常，说明Java虚拟机的堆内存不够。**

**原因有二：**
**（1）Java虚拟机的堆内存设置不够，可以通过参数-Xms、-Xmx来调整。**
**（2）代码中创建了大量大对象，并且长时间不能被垃圾收集器收集（存在被引用）。**

**Java7**

![1598023120057](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598023120057.png)

新生区各比例：伊甸园区：幸存0区：幸存1区=8：1：1

新生区：养老区=1/3:2/3

**Java8**

![1598024355928](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598024355928.png)

**永久区：**

永久存储区是一个常驻内存区域，用于存放JDK自身所携带的Class,Interface 的元数据，也就是说它存储的是运行环境必须的类信息，被装载进此区域的数据是不会被垃圾回收器回收掉的，关闭 JVM 才会释放此区域所占用的内存。

如果出现java.lang.OutOfMemoryError: PermGen space，说明是Java虚拟机对永久代Perm内存设置不够。一般出现这种情况，都是程序启动需要加载大量的第三方jar包。例如：在一个Tomcat下部署了太多的应用。或者大量动态反射生成的类不断被加载，最终导致Perm区被占满。
Jdk1.6及之前： 有永久代, 常量池1.6在方法区
Jdk1.7： 有永久代，但已经逐步“去永久代”，常量池1.7在堆
Jdk1.8及之后： 无永久代，常量池1.8在元空间

**内存调优**

![1598024388574](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598024388574.png)

```java
long maxMemory = Runtime.getRuntime().maxMemory() ;//返回Java虚拟机试图使用的最大内存量
long totalMemory = Runtime.getRuntime().totalMemory() ;// 返回Java虚拟机中的内存总量。

System.out.println("xmx MAX_MEMORY = " + maxMemory + " （字节）、 " +
(maxMemory / (double)1024 / 1024) + "MB");//1895825408 （字节）、 1808.0MB
System.out.println("xms TOTAL_MEMORY = " + totalMemory + " （字节）、 " +
(totalMemory / (double)1024 / 1024) + "MB");//128974848 （字节）、 123.0MB
//生成环境-Xms与-Xmx一定一样大，避免应用程序争抢内存，理论值的峰值忽高忽低
```

VM参数： -Xms1024m -Xmx1024m -XX:+PrintGCDetails

![1598026288355](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598026288355.png)

Exception in thread "main" java.lang.OutOfMemoryError: Java heap space

堆内存溢出是养老区的FullGC清理不了导致的，新生区不会导致堆内存溢出。

### GC收集日志信息

-XX:+PrintGCDetails：输出详细GC收集日志信息

GC:

![1598027297675](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598027297675.png)

FullGC:

![1598027346229](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598027346229.png)

## GC

### 分代收集算法

> 1.次数上频繁收集Young区
>
> 2.次数上较少收集Old区
>
> 3.基本不动Perm区(java8是元空间)

### 四大算法

#### 引用计数法

![1598164508123](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598164508123.png)

#### 复制算法

**年轻代中使用的是Minor GC,这种GC算法采用的是复制算法(Copying)**

![1598164965398](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598164965398.png)

当对象在 Eden ( 包括一个 Survivor 区域，这里假设是 from 区域 ) 出生后，在经过一次 Minor GC 后，如果对象还存活，并且能够被另外一块 Survivor 区域所容纳( 上面已经假设为 from 区域，这里应为 to 区域，即 to 区域有足够的内存空间来存储 Eden 和 from 区域中存活的对象 )，则使用复制算法将这些仍然还存活的对象复制到另外一块 Survivor 区域 ( 即 to 区域 ) 中，然后清理所使用过的 Eden 以及 Survivor 区域 ( 即 from 区域 )，并且将这些对象的年龄设置为1，以后对象在 Survivor 区每熬过一次 Minor GC，就将对象的年龄 + 1，当对象的年龄达到某个值时 ( 默认是 15 岁，通过-XX:MaxTenuringThreshold 来设定参数)，这些对象就会成为老年代。

-XX:MaxTenuringThreshold — 设置对象在新生代中存活的次数

在GC开始的时候，对象只会存在于Eden区和名为“From”的Survivor区，Survivor区“To”是空的。紧接着进行GC，Eden区中所有存活的对象都会被复制到“To”，而在“From”区中，仍存活的对象会根据他们的年龄值来决定去向。年龄达到一定值(年龄阈值，可以通过-XX:MaxTenuringThreshold来设置)的对象会被移动到年老代中，没有达到阈值的对象会被复制到“To”区域。经过这次GC后，Eden区和From区已经被清空。这个时候，“From”和“To”会交换他们的角色，也就是新的“To”就是上次GC前的“From”，新的“From”就是上次GC前的“To”。不管怎样，都会保证名为To的Survivor区域是空的。Minor GC会一直重复这样的过程，直到“To”区被填满，“To”区被填满之后，会将所有对象移动到年老代中。

![1598164998567](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598164998567.png)

> 劣势:
>
> 复制算法它的缺点也是相当明显的。 
> 　　1、它浪费了一半的内存，这太要命了。 
> 　　2、如果对象的存活率很高，我们可以极端一点，假设是100%存活，那么我们需要将所有对象都复制一遍，并将所有引用地址重置一遍。复制这一工作所花费的时间，在对象存活率达到一定程度时，将会变的不可忽视。 所以从以上描述不难看出，复制算法要想使用，最起码对象的存活率要非常低才行，而且最重要的是，我们必须要克服50%内存的浪费。

#### 标记清除

**老年代一般是由标记清除或者标记清除与标记整理的混合实现**

![1598166647447](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598166647447.png)

优点:不需要额外的空间

缺点:两次扫描耗时严重，会产生内存碎片

> 劣势
>
> 1、首先，它的缺点就是效率比较低（递归与全堆对象遍历），而且在进行GC的时候，需要停止应用程序，这会导致用户体验非常差劲
> 2、其次，主要的缺点则是这种方式清理出来的空闲内存是不连续的，这点不难理解，我们的死亡对象都是随即的出现在内存的各个角落的，现在把它们清除之后，内存的布局自然会乱七八糟。而为了应付这一点，JVM就不得不维持一个内存的空闲列表，这又是一种开销。而且在分配数组对象的时候，寻找连续的内存空间会不太好找。 

#### 标记压缩

**老年代一般是由标记清除或者标记清除与标记整理的混合实现**

![1598166945112](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598166945112.png)

在整理压缩阶段，不再对标记的对像做回收，而是通过所有存活对像都向一端移动，然后直接清除边界以外的内存。可以看到，标记的存活对象将会被整理，按照内存地址依次排列，而未被标记的内存会被清理掉。如此一来，当我们需要给新对象分配内存时，JVM只需要持有一个内存的起始地址即可，这比维护一个空闲列表显然少了许多开销。 

　　标记/整理算法不仅可以弥补标记/清除算法当中，内存区域分散的缺点，也消除了复制算法当中，内存减半的高额代价

![1598167055130](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598167055130.png)

>  
>
> 内存效率：复制算法>标记清除算法>标记整理算法（此处的效率只是简单的对比时间复杂度，实际情况不一定如此）。 
> 内存整齐度：复制算法=标记整理算法>标记清除算法。 
> 内存利用率：标记整理算法=标记清除算法>复制算法。 
>
> 可以看出，效率上来说，复制算法是当之无愧的老大，但是却浪费了太多内存，而为了尽量兼顾上面所提到的三个指标，标记/整理算法相对来说更平滑一些，但效率上依然不尽如人意，它比复制算法多了一个标记的阶段，又比标记/清除多了一个整理内存的过程
>
>
> 没有最好的算法，只有最合适的算法。==========>分代收集算法。
>
> 年轻代(Young Gen)  
>
> 年轻代特点是区域相对老年代较小，对像存活率低。
>
> 这种情况复制算法的回收整理，速度是最快的。复制算法的效率只和当前存活对像大小有关，因而很适用于年轻代的回收。而复制算法内存利用率不高的问题，通过hotspot中的两个survivor的设计得到缓解。
>
> 老年代(Tenure Gen)
>
> 老年代的特点是区域较大，对像存活率高。
>
> 这种情况，存在大量存活率高的对像，复制算法明显变得不合适。一般是由标记清除或者是标记清除与标记整理的混合实现。
>
> Mark阶段的开销与存活对像的数量成正比，这点上说来，对于老年代，标记清除或者标记整理有一些不符，但可以通过多核/线程利用，对并发、并行的形式提标记效率。
>
> Sweep阶段的开销与所管理区域的大小形正相关，但Sweep“就地处决”的特点，回收的过程没有对像的移动。使其相对其它有对像移动步骤的回收算法，仍然是效率最好的。但是需要解决内存碎片问题。
>
> Compact阶段的开销与存活对像的数据成开比，如上一条所描述，对于大量对像的移动是很大开销的，做为老年代的第一选择并不合适。
>
> 基于上面的考虑，老年代一般是由标记清除或者是标记清除与标记整理的混合实现。以hotspot中的CMS回收器为例，CMS是基于Mark-Sweep实现的，对于对像的回收效率很高，而对于碎片问题，CMS采用基于Mark-Compact算法的Serial Old回收器做为补偿措施：当内存回收不佳（碎片导致的Concurrent Mode Failure时），将采用Serial Old执行Full GC以达到对老年代内存的整理。
>
> 
>



## JVM+GC解析

### GC Roots

[1.JVM垃圾回收的时候如何确定垃圾？是否知道什么是GC Roots?]()

GC Roots: 常说的GC(Garbage Collector) roots，特指的是垃圾收集器（Garbage Collector）的对象，GC会收集那些不是GC roots且没有被GC roots引用的对象。 

> - **Class** - 由系统类加载器(system class loader)加载的对象，这些类是不能够被回收的，他们可以以静态字段的方式保存持有其它对象。我们需要注意的一点就是，通过用户自定义的类加载器加载的类，除非相应的java.lang.Class实例以其它的某种（或多种）方式成为roots，否则它们并不是roots，.
> - **Thread** - 活着的线程
> - **Stack Local** - Java方法的local变量或参数
> - **JNI Local** - JNI方法的local变量或参数
> - **JNI Global** - 全局JNI引用
> - **Monitor Used** - 用于同步的监控对象
> - **Held by JVM** - 用于JVM特殊目的由GC保留的对象，但实际上这个与JVM的实现是有关的。可能已知的一些类型是：系统类加载器、一些JVM知道的重要的异常类、一些用于处理异常的预分配对象以及一些自定义的类加载器等。**然而，JVM并没有为这些对象提供其它的信息，因此需要去确定哪些是属于"JVM持有"的了。**

> 什么是垃圾？
>
> 简单的说就是内存中已经不再被使用的空间就说垃圾。

要进行垃圾回收，如何判断一个对象是否可以被回收？

1.引用计数法

Java中,引用和对象是有关联的。如果要操作对象则必须用引用进行
因此,很显然一个简单的办法是通过引用计数来判断一个对象是否可以回收。简单说,给对象中添加一个引用计数器,
每当有一个地方引用它,计数器值加1
每当有一个引用失效时,计数器值减1。
任何时刻计数器值为零的对象就是不可能再被使用的,那么这个对象就是可回收对象
那为什么主流的Java虚拟机里面都没有选用这种算法呢?其中最主要的原因是它很难解决对象之间相互循环引用的问题

![1598170441313](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598170441313.png)

2.枚举根节点做可达性分析(根搜索路径)

为了解决引用计数法的循环引用问题，Java使用了可达性分析的方法。

![1598170558544](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598170558544.png)

**所谓"GC Roots"或者Tracing GC的"根集合"就是一组必须活跃的引用。**

**基本思路就是通过一系列名为"GC Roots"的对象作为起始点，**从这个被称为GC  Roots的对象可是向下搜索，如果一个对象到GC Roots没有任何引用链相连时，则说明此对象不可用。也即给定一个集合的引用作为根出发，通过引用关系遍历对象图，能被遍历到的(可到达的)对象就被判断为存活，没有被遍历的就自然判断为死亡。

![1598171068484](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598171068484.png)

**Java可以作为GC Roots的对象**

> 1.虚拟机栈(栈帧中的局部变量区，也叫做局部变量表)中引用的对象
>
> 2.方法区中的类静态属性引用的对象
>
> 3.方法区中常量引用的对象
>
> 4.本地方法栈中JNI(Native方法)引用的对象

### JVM参数类型

[2.你说你做过JVM调优和参数配置，请问如何盘点查看MM系统默认值]()

#### **标配参数**

![1598172094959](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598172094959.png)

#### x参数(了解)

> -Xint：解释执行
>
> -Xcomp：第一次使用就编译成本地代码
>
> -Xmixed：混合模式(先编译后执行)

![1598172349179](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598172349179.png)

#### XX参数

如何查看一个正在运行中的java程序，它的某个jvm参数是否开启，具体值是多少？

cls:清屏

jps -l：查看正在运行的进程(其实就是获取进程编号)

jinfo -flag 配置项(进程参数)  java进程编号：查看当前运行程序的配置(jvm参数是否开启，初始值，具体值)

jinfo -flags java进程编号：查看当前进程编号下面的所有配置

##### Boolean类型

公式：-XX:+或者-某个属性值

+表示开启 -表示关闭

例如：-XX:+PrintGCDetails  -XX:-PrintGCDetails

##### KV设值类型

公式：-XX:属性key=属性值value

例如：-XX:MetaspaceSize=128m  -XX:MaxTenuringThreshole=15

##### 题外话(坑题)

两个经典参数：-Xms  -Xmx

-Xms等价于-XX:InitialHeapSize

-Xmx等价于-XX:MaxHeapSize

#### 查看JVM默认值

**java -XX:+PrintFlagsInital  查看初始默认值**

![1598178135401](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598178135401.png)

**java -XX:+PrintFlagsFinal  主要查看修改更新**

= 与:=m 

![1598178215031](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598178215031.png)

运行Java命令的同时打印出参数

![1598178544506](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598178544506.png)

**java -XX:+PrintCommandLineFlags**  也是查看初始值

![1598178640813](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598178640813.png)

### JVM常用基本配置参数

[3.你平时工作用过的M常用基本配置参数有哪些？]()

#### -Xms

初始堆内存大小，默认为物理内存的1/64

等价与-XX:InitialHeapSize

#### -Xmx

最大堆内存，默认为物理内存的1/4

等价与-XX:MaxHeapSize

#### -Xss

设置单个线程栈的大小，一般默认为512~1024K

等价于-XX:ThreadStackSize

![1598191907763](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598191907763.png)

#### -Xmn

设置年轻代大小

#### -XX:MetaspaceSize

设置元空间大小

元空间的本质和永久代类似，都是对JVM规范中方法区的实现。不过元空间与永久代之间最大的区别在于：**元空间并在虚拟机中，而是使用本地物理内存。**因为，默认情况下，元空间的大小仅受本地内存限制。



典型设置案例

-Xms128m -Xmx4096m -Xss1024k -XX:MetaspaceSize=512m -XX:+PrintCommandLineFlags -XX:+PrintGCDetails -XX:+UseSerialGC

![1598193029742](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598193029742.png)

-XX:+UseSerialGC 串行垃圾回收器

-XX:+UseParallelGC 并行垃圾回收器(java8默认)



#### -XX:+PrintGCDetails

输出详细GC收集日志信息

##### GC

![1598366518692](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598366518692.png)



##### FullGC

![1598367836970](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598367836970.png)

#### -XX:SurvivorRatio

设置新生代中eden和s0/s1空间的比例

默认

XX:SurvivorRatio=8 ,Eden:s0:s1=8:1:1

假如

XX:SurvivorRatio=4 ,Eden:s0:s1=4:1:1

SurvivorRatio值就是设置eden区的比例占多少，s0/s1相同



#### -XX:NewRatio

 /ˈreɪʃiəʊ/  配置年轻代与老年代在堆结构的占比

默认 1:2

-XX:NewRatio=2 新生代占1，老年代占2，年轻代占整个堆的1/3;

假如

-XX:NewRatio=4 新生代占1，老年代占4，年轻代占整个堆的1/5;

:NewRatio值就是设置老年代的占比，剩下的1给新生代



#### -XX:MaxTenuringThreshold

设置垃圾最大年龄

![1598369679493](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598369679493.png)



### 引用

[4.强引用、软引用、弱引用、虚引用分别是什么？]()

#### 整体架构

![1598370752227](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598370752227.png)



#### 强引用

Reference默认支持模式

```java
Object obj1 = new Object();//这样定义就是强引用
Object obj2 = obj1;//obj2引用赋值
System.out.println(obj1);//java.lang.Object@6e0be858
System.out.println(obj2);//java.lang.Object@6e0be858

obj1 = null;//置空
System.gc();
System.out.println(obj1);//null
System.out.println(obj2);//java.lang.Object@6e0be858
```

![1598372297307](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598372297307.png)

#### 软引用

SoftReference

软引用是一种相对于强引用弱化了一些的引用，需要java.lang.ref.SoftReference类实现，可以让对象豁免一些垃圾收集。

对应只有软引用的对象来说：

​	**当系统内存充足时它不会被回收；**

​	**当系统内存不足是它会被回收。**

软引用通常用在内存敏感的程序中，比如高速缓存就有用到软引用，**内存够用的时候就保留，不够就回收！**

```java
Object o1 = new Object();
SoftReference<Object> softReference = new SoftReference<Object>(o1);

System.out.println(o1);//java.lang.Object@6e0be858
System.out.println(softReference.get());//java.lang.Object@6e0be858

o1 = null;
System.gc();
try {
	byte[] bytes = new byte[30 * 1024 * 1024];
}catch (Throwable e){
	e.printStackTrace();
}finally {
    System.out.println(o1);//null
    System.out.println(softReference.get());//null
}
```



#### 弱引用

WeakReference

弱引用需要用java.lang.ref.WeakReference类来实现，它比软引用的生存期更短，对于只有软引用的对象来说，只要垃圾回收机制一运行，不管JVM内存空间是否足够，都会回收改对象占用的内存。

```java
Object o1 = new Object();
WeakReference<Object> weakReference = new WeakReference<Object>(o1);

System.out.println(o1);//java.lang.Object@6e0be858
System.out.println(weakReference.get());//java.lang.Object@6e0be858

o1 = null;
System.gc();

System.out.println(o1);//null
System.out.println(weakReference.get());//null
```

**软引用和弱引用的适用场景：**

![](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598373345089.png)

**知道弱引用的话，谈谈WeakHashMap?**

WeakHashMap，此种Map的特点是，当除了自身有对key的引用外，此key没有其他引用那么此map会自动丢弃此值，所以比较适合做缓存。

简单来说，`WeakHashMap`实现了Map接口，基于`hash-table`实现，在这种Map中，key的类型是`WeakReference`。如果对应的key被回收，则这个key指向的对象会被从Map容器中移除。 

```java
private static void myHashMap() {
    Map<Integer, String> map = new HashMap<>();
    Integer key = new Integer(1);
    String value = "HashMap";
    map.put(key, value);
    System.out.println(map);//{1=HashMap}
	
    //使用迭代器进行遍历map集合
    Iterator i = map.entrySet().iterator();
    while (i.hasNext()) {
        Map.Entry entry = (Map.Entry) i.next();
        System.out.println(entry.getKey() + "\t" + entry.getValue());
    }
    
    key = null;
    System.gc();
    System.out.println(map);//{1=HashMap}
}

 private static void myWeekHashMap() {
    WeakHashMap<Integer, String> map = new WeakHashMap<>();
    Integer key = new Integer(1);
    String value = "HashMap";
    map.put(key, value);
    System.out.println(map);//{1=HashMap}

    key = null;
    System.gc();
    System.out.println(map);//空
}
```



#### 虚引用

PhantomReference

虚引用的主要作用是跟踪对象被垃圾回收的状态，目的是在这个对象被收集器回收的时候收到一个系统通知或者后续添加进一步的处理。

被回收前需要被引用队列（ReferenceQueue）保存下。

![1598453817591](C:\Users\Administrator\Desktop\1598453817591.png)

```java
 Object o1 = new Object();
 ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();
 WeakReference<Object> weakReference = new WeakReference<Object>(o1, referenceQueue);
 System.out.println(o1);//java.lang.Object@6e0be858
 System.out.println(weakReference.get());//java.lang.Object@6e0be858
 System.out.println(referenceQueue.poll());//null

System.out.println("====================");
o1 = null;
System.gc();

System.out.println(o1);//null
System.out.println(weakReference.get());//null
System.out.println(referenceQueue.poll());//java.lang.ref.WeakReference@61bbe9ba

===============================================================================
Object o1 = new Object();
ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();
PhantomReference<Object> phantomReference = new PhantomReference<Object>(o1, referenceQueue);
System.out.println(o1);//java.lang.Object@6e0be858
System.out.println(phantomReference.get());//null
System.out.println(referenceQueue.poll());//null

System.out.println("====================");
o1 = null;
System.gc();

System.out.println(o1);//null
System.out.println(phantomReference.get());//null
System.out.println(referenceQueue.poll());//java.lang.ref.PhantomReference@61bbe9ba
```

**GCRoots和四大引用的总结**

![1598455585810](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598455585810.png)

### 对OOM的认识

![1598456864505](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598456864505.png)

#### **java.lang.StackOverflowError**

**栈内存溢出**

```java
 public static void main(String[] args) {
     stackOverflowError();
    }
 //Exception in thread "main" java.lang.StackOverflowError
  private static void stackOverflowError() {
    stackOverflowError();
  }

```

------



#### java.lang.OutOfMemoryError:Java heap space

**堆内存溢出**

```java
//Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
byte[] bytes = new byte[80 * 1024 * 1024];//80mb -Xms10m -Xmx10m
```

------



#### java.lang.OutOfMemoryError:GC overhead limit exceeded

```java
//-Xms10m -Xmx10m -XX:MaxDirectMemorySize=5m -XX:+PrintGCDetails 
//Exception in thread "main" java.lang.OutOfMemoryError: GC overhead limit exceeded
int i = 0;
 List<String> list = new ArrayList<>();
 try {
 while (true) {
	 list.add(String.valueOf(++i).intern());
 }
 } catch (Throwable e) {
     System.out.println("*********" + i);
     e.printStackTrace();
	 throw e;
 }
```

![1598458653530](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598458653530.png)

------



#### java.lang.OutOfMemoryError:Direct buffer memory

![1598459136809](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598459136809.png)

```java
//-Xms10m -Xmx10m -XX:MaxDirectMemorySize=5m -XX:+PrintGCDetails
//Exception in thread "main" java.lang.OutOfMemoryError: Direct buffer memory
System.out.println(VM.maxDirectMemory() / (double) 1024 / 1024 + "Mb");
 try {
 	TimeUnit.SECONDS.sleep(3);
 } catch (InterruptedException e) {
 	e.printStackTrace();
 }
 ByteBuffer buffer = ByteBuffer.allocateDirect(6 * 1024 * 1024);
```

------



#### java.lang.OutOfMemoryError:unable to new create native thread

![1598542895208](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598542895208.png)

```java
public class UnableToNewNativeThread {
    public static void main(String[] args) {
        for (int i = 1; ; i++) {
            System.out.println("*********** i = " + i);
            new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "" + i).start();
        }
    }
}
```

![1598543046269](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598543046269.png)

------



#### java.lang.OutOfMemoryError:Metaspace

使用java -XX:+PrintFlagsInitial命令查看本机的初始化参数，-XX:MetaspaceSize为21810376B(约为20M)

![](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598544238325.png)

```java
public class MetaspaceDemo {

    static class OOMTest {}

    public static void main(final String[] args) {
        int i = 0;
        try {
            while (true) {
                i++;
                Enhancer enhancer = new Enhancer();
                enhancer.setSuperclass(OOMTest.class);
                enhancer.setUseCache(false);
                enhancer.setCallback(new MethodInterceptor() {
                    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                        return methodProxy.invoke(o, args);
                    }
                });
                enhancer.create();
            }
        } catch (Throwable throwable) {
            System.out.println("**********多少次后发生异常：" + i);
            throwable.printStackTrace();
        }
    }
}

```

------

### 垃圾回收器

[G垃圾回收算法和垃圾收集器的关系？分别是什么请你谈谈？]()

GC算法(引用计数/复制/清除/整理)是内存回收的方法论，垃圾收集器就是算法落地实现。

![1598545159364](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598545159364.png)

#### 垃圾回收的方式有哪些

> java10之前的
>
> 串行垃圾回收器(Serial)：它为单线程环境设计并且只使用一个线程进行垃圾回收，会暂时所有的用户程序，所以不适合服务器环境。
>
> 并行垃圾回收器(Parallel)：java8默认的，多个垃圾回收线程并行工作，此时用户线程是暂停的，适用于科技计算/大数据处理等弱交互场景。
>
> 并发垃圾回收器(CMS)：用户线程和垃圾收集线程同时指向(不一定并行，可能交替执行)，不需要暂时用户程序，互联网公司多用它，适用于对相应时间有要求的场景。
>
> G1垃圾回收器：G1垃圾回收器将堆内存分割成不同的区域然后并发的对其进行垃圾回收
>
> java10之后新加了一个ZGC

![1598545611716](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598545611716.png)

------

#### 查看默认的垃圾回收器

java -XX:+PrintCommandLineFlags -version 查看默认垃圾回收器

![](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598546736013.png)

#### 垃圾收集器有哪些

java的gc回收的类型只要有几种：

UseSerialGC，UsePraNewGC，**UseParallelGC**，，

UseSerialOldGC，**UseParallelOldGC**，UseConcMarkSweepGC，

UseG1GC

![1598630982707](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598630982707.png)

![1598623337725](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598623337725.png)

![1598622648562](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598622648562.png)

![1598622737900](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598622737900.png)

------

Server/Client模式分别是什么意思？

![1598623520833](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598623520833.png)

##### 新生代

###### 串行GC(Serial)/(Serial Coping)

![1598628597341](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598628597341.png)

串行收集器是最古老，最稳定以及效率最高的收集器，只使用一个线程去回收但在进行垃圾回收过程中可生会产生较长的停顿("Stop-The-World"状态)。虽然在收集垃圾回收过程中需要暂停所有其他的工作线程，但是它简单高效，对应限定单个CPU环境来说，没有线程交互的开销可以获得最高的单线程垃圾收集效率，因为Serial垃圾收集器依然是java虚拟机运行在Client模式下默认的新生代垃圾收集器。

对应JVM的参数是：-XX:+UseSerialGC

开启后会使用：Serial（Young区用）+Serial Old（Old区用）的收集器组合。

表示：新生代、老年代都会使用串行垃圾回收器，新生代使用复制算法，老年代使用标记-整理算法。

![1598629008766](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598629008766.png)

------

###### 并行GC(ParNew)

![1598631137406](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598631137406.png)

![1598631172704](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598631172704.png)

------

###### 并行回收GC(Parallel)/(Parallel Scavenge)

![1598631332566](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598631332566.png)![1598631367579](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598631367579.png)

![1598631337365](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598631337365.png)

------

##### 老年代

###### 串行回收GC(Serial Old)/(Serial CMS)

![1598631594923](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598631594923.png)

###### 并行GC(Parallel Old)/(Parallel CMS)

![1598631562457](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598631562457.png)

###### 并发标记清除GC(CMS)

![1598631626505](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598631626505.png)

![1598690293077](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598690293077.png)

4步过程

> 1**.初始标记**
>
> 2**.并发标记**和用户线程一起
>
>  进行GC Roots跟踪过程，和用户线程一起工作，不需要暂停工作线程。主要标记过程，标记全部对象。
>
> 3.**重新标记**
>
> 为了修正在并发标记期间，因用户程序继续运行而导致标记产生变动的那一部分对象的标记记录，仍然需要    暂停所有的工作线程。
>
> 由于并发标记时，用户线程依然运行，因此在正式清理前，再做修正。
>
> 4.**并发清除**和用户线程一起
>
> 清除GC Roots不可达对象，和用户线程一起工作，不需要暂停工作线程。基于标记结果，直接清理对象。
>
> 由于耗时最长的并发标记和并发清除过程中，垃圾收集器可以和用户线程一起并发工作，所以总体上来看CMS收集器的内存回收和用户线程是一起并发执行。

![1598691031259](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598691031259.png)

**优点**：并发收集低停顿

**缺点**：

1.并发执行，对CPU资源压力大

![1598691179831](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598691179831.png)

2.采用的标记清除算法会导致大量碎片

![1598691228646](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598691228646.png)

#### 如何选择垃圾收集器

 ![1598691275196](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598691275196.png)

![1598691279645](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598691279645.png)

#### G1垃圾收集器

##### 以前收集器特点

> 1.年轻代和老年代是各自独立且连续的内存块
>
> 2.年轻代收集使用eden+s0+s1进行复制算法
>
> 3.老年代收集必须扫描整个老年代区域
>
> 4.都是以尽可能少而快的执行GC为设计原则

##### G1是什么

G1(Garbage-First)收集器，是一款面向服务端应用的收集器。

![1598692950656](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598692950656.png)

![1598692985921](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598692985921.png)

##### G1收集器特点

> 1.G1能充分利用多CPU，多核环境硬件优势，尽量缩短STW。
>
> 2.G1整体上采用标记-整理算法，局部通过复制算法，不会产生内存碎片。
>
> 3.宏观上看G1之中不再区分年轻代和老年代。把内存划分成多个独立的子区域(Region)，可以近似理解为一个围棋的棋盘。
>
> 4.G1收集器里面将整个的内存区都混合在一起了，但其本身依然在小范围内要进行年轻代和老年代的区分，保留了新生代和老年代，但它们不再是物理隔离的，而是一部分Region的集合而不需要Region是连续的，也就是说依然会采用不同的GC方式来处理不同的区域。
>
> 5.G1虽然也是分代收集器，但整个内存分区不存在在物理上的年轻代和老年代的区别，也不需要完全独立的survivor(to space)堆做复制准备。G1只有逻辑上的分代概念，或者说每个分区都有可能随G1的运行在不同代之间前后切换。

##### 底层原理

Region区域垃圾收集器

最大好处是化整为零，避免全内存扫描，只需要按照区域来进行扫描即可。

![1598695743918](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598695743918.png)

![1598695796609](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598695796609.png)

G1算法将堆划分为若干个区域(Region)，它仍然属于分代收集器。

这些Region的一部分包含新生代，新生代的垃圾收集依然采用暂停所有应用线程的方式，将存活对象拷贝到老年代或者Survior空间。

这些Region的一部分包含老年代，G1收集器通过将对象从一个区域复制到另外一个区域，完成了清理工作。这就意味着，在正常的处理过程中，G1完成了堆的压缩(至少是部分堆的压缩)，这样也就不会有CMS内存碎片问题的存在了。

![1598696455429](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598696455429.png)

##### 回收步骤

![1598696081770](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598696081770.png)

![1598696085334](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598696085334.png)

##### 4步过程

![1598696137207](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598696137207.png)

##### 常用配置参数

![1598696192740](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598696192740.png)

##### G1和CMS相比的优势

> 1.G1不会产生内存碎片
>
> 2.是可以精确控制停顿。该收集器是整个堆(新生代，老年代)划分成多个固定大小的区域，每次根据允许停顿的时间去收集垃圾最多的区域。

##### 生产部署和调参优化

![1598697025853](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598697025853.png)

------

## Linux命令

[生产环境服务器变慢，诊断思路和性能评估谈谈？]()

### 整机

#### **top：查看整机性能**

![](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598712465631.png)

> 1.CPU
>
> 2.内存
>
> 3.id(idle) 空闲率，越多越好
>
> 4.load average:负载均衡率  
>
> 三个值：1分钟 5分钟 15分钟的平均负量，三个值相加除以3*100如果高于60%，说明系统负担重

#### **uptime：查看整体性能(负载均衡)**

![1598712494261](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598712494261.png)

------



### CPU

#### **vmstat：查看CPU(包含不限于)**

![1598712191281](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598712191281.png)

#### **vmstat -n 2 3**

第一个参数2是采样的时间间隔单位是秒，第二个参数3是采样的次数

-procs：

​	r：运行和等待CPU时间片的进程数，原则上1核的CPU的运行队列不要超过2，整个系统的运行队列不能超过总	     核数的2倍，否则代表系统压力过大。

​    b：等待资源的进程数，比如正在等待磁盘I/O，网络I/O等。

-cpu：

​	us：用户进程消耗CPU时间百分比，us值高，用户进程消耗CPU时间多，如果长期大于50%，优化程序。	

​	sy：内核进程消耗CPU时间百分比。

​	us+sy参考值为80%，如果大于80%，说明可能存在CPU不足。

​    id：处于空闲的CPU百分比。

​	wa：系统等待IO的CPU时间百分比。

​	st：来自于一个虚拟机偷取的CPU时间百分比。

#### mpstat -P ALL 2

查看所有CPU信息

![1598714245376](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598714245376.png)

#### pidstat -u 1  -p 进程编号

每个进程使用CPU的用量分解信息

![1598714308936](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598714308936.png)

------

### 内存

#### free -m

应用程序可用内存

![1598714343307](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598714343307.png)

应用程序可用内存/系统物理内存>70%内存充足

应用程序可用内存/系统物理内存<20%内存不足，需要增加内存

20%<应用程序可用内存/系统物理内存<70%内存基本够用

#### pidstat -p 进程号 -r 采样间隔秒数

![1598714524755](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598714524755.png)

------

### 硬盘

#### df -h

查看磁盘剩余空闲数

![1598714643598](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598714643598.png)

------

### 磁盘IO

#### iostat -xdk 2 3

磁盘IO性能评估

![1598714767708](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598714767708.png)

![1598714782887](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598714782887.png)

------

### 网络IO

#### ifstat l

![1598716048319](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598716048319.png)

默认本地没有，需要下载

![1598714959477](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598714959477.png)

------

### CPU过高

[假如生成环境出现CPU占用过高，请谈谈你的分析思路和定位？]()

结合Linux和JDK命令一块分析

> 1.先用top命令找出CPU占比最高的
>
> ![1598773660514](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598773660514.png)
>
> 2.用ps -ef或者jps进一步定位，得知是一个怎么样的一个后台程序
>
> ![1598773691073](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598773691073.png)
>
> 3.定位到具体线程或代码：ps -mp 进程编号 -o THREAD,tid,time
>
> ​	参数解释：
>
> ​	-m：显示所有线程
>
> ​	-p ：pid进程使用的CPU的时间
>
> ​	-o：该参数后是用户自定义格式
>
> ![1598773780176](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598773780176.png)
>
> 4.将需要的线程ID转换为16进制格式(英文小写格式) printf "%x\n" 有问题的线程id![1598773808508](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598773808508.png)
>
> 5.jstack 进程ID |grep tid(16进制线程ID小写英文) -A60
>
> ![1598773843427](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598773843427.png)

------

对于JDK自带的JVM监控和性能 分析工具用过哪些？一般你是怎么用的？

性能监控工具

> jps：虚拟机进程状态工具
>
> jinfo：Java配置信息工具
>
> jmap：内存映像工具
>
> jstack：统计信息监控工具

## GitHub

### 常用词定义

> watch:会持续收到该项目的动态
>
> fork:复制某个项目到自己的github仓库中
>
> star:可以理解为点赞
>
> clone:将项目下载到本地
>
> follow:关注你感兴趣的作者，会收到他们的动态

###  in关键词限制搜索范围

seckill in:name,readme

![](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598775693273.png)

### star或fork数量关键词去查找

springboot stars:>= 5000

springboot forks:>=500

springboot forks:100..200 stars:80..100

![](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598776554845.png)

### awesome加强搜索

/ˈɔːsəm/ 

公式：awesome 关键字

awesome系列：一般用来收集学习，工具，书籍类。

搜索youxiu的redis相关的项目包括框架，教程等。awesome redis

### 高亮显示每一行代码

![1598777202821](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598777202821.png)

### 项目内搜索英文t

 https://docs.github.com/en/github/getting-started-with-github/keyboard-shortcuts 

![1598777436495](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598777436495.png)

### 搜索某个地区的大佬

![1598777615942](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598777615942.png)

------

# 第一季

## 自增变量

```java
public static void main(String[] args) {
    int i = 1;
    i = i++;
    int j = i++;
    int k = i + ++i * i++;
    System.out.println("i=" + i);//4
    System.out.println("j=" + j);//1
    System.out.println("k=" + k);//11
   }
```

## 单例设计模式

![1598790453690](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598790453690.png)

![1598790504385](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1598790504385.png)