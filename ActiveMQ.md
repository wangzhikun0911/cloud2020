# MQ之ActiveMQ

## 入门概述

### 前言

1.在何种场景下使用了消息中间件？

2.为什么要在系统里引入消息中间件？

### MQ产品种类和对比

MQ就是消息中间件。MQ是一种理念，ActiveMQ是MQ的落地产品。不管是哪款消息中间件，都有如下一些技术维度：

![img](file:///C:\Users\ADMINI~1\AppData\Local\Temp\ksohtml2096\wps1.jpg)

### 消息队列的详细比较

|       特性        |   ActiveMQ    | RabbitMQ |     Kafka      |   RocketMQ   |
| :---------------: | :-----------: | :------: | :------------: | :----------: |
| PRODUCER-CUMSUMER |     支持      |   支持   |      支持      |     支持     |
| PUBLISH-SUBSCRIBE |     支持      |   支持   |      支持      |     支持     |
|   REQUEST-REPLY   |     支持      |   支持   |       -        |     支持     |
|     API完备性     |      高       |    高    |       高       | 低(静态配置) |
|    多语言支持     | 支持,Java优先 | 语言无关 | 支持,Java优先  |     支持     |
|    单机吞吐量     |     万级      |   万级   |     十万级     |   单机万级   |
|     消息延迟      |       -       |  微秒级  |     毫秒级     |      -       |
|      可用性       |   高(主从)    | 高(主从) | 非常高(分布式) |      高      |
|     消息丢失      |       -       |    低    | 理论上不会丢失 |      -       |
|     消息重复      |       -       |  可控制  | 理论上会有重复 |      -       |
|   文档的完备性    |      高       |    高    |       高       |      中      |
|   提供快速入门    |      有       |    有    |       有       |      无      |
|   首次部署难度    |       -       |    低    |       中       |      高      |



> 1.Kafka
>
> 编程语言：Scala，大数据领域的主流MQ。
>
> 2.RabbitMQ
>
> 编程语言：erlang，基于erlang语言，不好修改底层，不要查找问题的原因，不建议选用。
>
> 3.RocketMQ
>
> 编程语言：java，适用于大型项目。适用于集群。
>
> 4.ActiveMQ
>
> 编程语言：java，适用于中小型项目

### 从生活case到实际生产案例

![1599970971996](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1599970971996.png)

### MQ的产生背景

系统之间直接调用实际工程落地和存在的问题？

微服务架构后
链式调用是我们在写程序时候的一般流程,为了完成一个整体功能会将其拆分成多个函数(或子模块)，比如模块A调用模块B,模块B调用模块C,模块C调用模块D。但在大型分布式应用中，系统间的RPC交互繁杂，一个功能背后要调用上百个接口并非不可能，从单机架构过渡到分布式微服务架构的通例，
这些架构会有哪些问题？

#### 1.系统之间接口耦合比较严重

每新增一个下游功能，都要对上游的相关接口进行改造；
举个例子：如果系统A要发送数据给系统B和系统C，发送给每个系统的数据可能有差异，因此系统A对要发送给每个系统的数据进行了组装，然后逐一发送；
当代码上线后又新增了一个需求：
把数据也发送给D，新上了一个D系统也要接受A系统的数据，此时就需要修改A系统，让他感知到D系统的存在，同时把数据处理好再给D。在这个过程你会看到，每接入一个下游系统，都要对系统A进行代码改造，开发联调的效率很低。其整体架构如下图

![1599987378278](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1599987378278.png)

#### 2.面对大流量并发时，容易被冲垮

每个接口模块的吞吐能力是有限的，这个上限能力如果是堤坝，当大流量（洪水）来临时，容易被冲垮。
举个例子秒杀业务：
上游系统发起下单购买操作，我就是下单一个操作
下游系统完成秒杀业务逻辑
（读取订单，库存检查，库存冻结，余额检查，余额冻结，订单生产，余额扣减，库存减少，生成流水，余额解冻，库存解冻）

#### 3.等待同步存在性能问题

RPC接口上基本都是同步调用，整体的服务性能遵循“木桶理论”，即整体系统的耗时取决于链路中最慢的那个接口。
比如A调用B/C/D都是50ms，但此时B又调用了B1，花费2000ms，那么直接就拖累了整个服务性能。

![1599987433601](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1599987433601.png)

> 根据上述的几个问题，在设计系统时可以明确要达到的目标：
> 1，要做到系统解耦，当新的模块接进来时，可以做到代码改动最小；**能够解耦**
> 2，设置流量缓冲池，可以让后端系统按照自身吞吐能力进行消费，不被冲垮；**能削峰**
> 3，强弱依赖梳理能将非关键调用链路的操作异步化并提升整体系统的吞吐能力；**能够异步**

### MQ的定义

面向消息的中间件（message-oriented middleware）MOM能够很好的解决以上问题，是指利用高效可靠的消息传递机制与平台无关的数据交流，并基于数据通信来进行分布式系统的集成。
通过提供消息传递和消息排队模型在分布式环境下提供应用解耦，弹性伸缩，冗余存储、流量削峰，异步通信，数据同步等功能。
大致的过程是这样的：
**发送者把消息发送给消息服务器，消息服务器将消息存放在若干队列/主题topic中，在合适的时候，消息服务器回将消息转发给接受者。在这个过程中，发送和接收是异步的，也就是发送无需等待，而且发送者和接受者的生命周期也没有必然的关系；**
尤其在发布pub/订阅sub模式下，也可以完成一对多的通信，即让一个消息有多个接受者。

![img](file:///C:\Users\ADMINI~1\AppData\Local\Temp\ksohtml2096\wps3.jpg)

### MQ的特点

#### 1.采用异步处理模式

消息发送者可以发送一个消息而无须等待响应。消息发送者将消息发送到一条虚拟的通道（主题或者队列）上；
消息接收者则订阅或者监听该爱通道。一条消息可能最终转发给一个或者多个消息接收者，这些消息接收者都无需对消息发送者做出同步回应。整个过程都是异步的。
案例：
也就是说，一个系统跟另一个系统之间进行通信的时候，假如系统A希望发送一个消息给系统B，让他去处理。但是系统A不关注系统B到底怎么处理或者有没有处理好，所以系统A把消息发送给MQ，然后就不管这条消息的“死活了”，接着系统B从MQ里面消费出来处理即可。至于怎么处理，是否处理完毕，什么时候处理，都是系统B的事儿，与系统A无关。

![1599987938552](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1599987938552.png)

#### 2.应用系统之间耦合

发送者和接受者不必了解对方，只需要确认消息。

发送者和接受者不必同时在线。

#### 3.整体架构

![img](file:///C:\Users\ADMINI~1\AppData\Local\Temp\ksohtml2096\wps4.jpg)

#### 4.MQ的缺点

两个系统之间不能同步调用，不能实时回复，不能响应某个调用的回复。

### MQ的主要作用

> 1.解耦：解决了系统之间耦合度调用的问题
>
> 2.削峰：抵御洪峰流量，保护了主业务
>
> 3.异步：调用者无需等待。

### 下载地址

官网地址： http://activemq.apache.org/

### 怎么玩？

最重要的功能：实现高可用，高性能，高伸缩，易用和安全的企业级面向消息服务的系统

异步消息的消费和处理

控制消息的消息顺序

可以和Spring/SpringBoot整合简化代码

配置集群容错的MQ集群

## ActiveMQ安装和控制台

普通启动：./activemq start

普通关闭：./activemq stop

带日志启动：./activemq start > /usr/local/activemq/myrunmq.log

> **查看程序启动是否成功的3种方式**
>
> 1.查看进程
>
> ps -ef|grep activemq |grep -v grep
>
> 2.查看端口是否被占用
>
> netstat -anp|grep 61616 
>
> 3.查看端口是否被占用(需要yum install lsof安装)
>
> lsof -i:61616

**ActiveMQ采用61616端口提供JMS服务**

**ActiveMQ采用8161端口提供管理控制台服务**

访问activemq管理页面地址：http://IP地址:8161/   

账户admin  密码admin

## Java编码实现ActiveMQ通讯

### pom.xml导入依赖

```xml
  <!--  activemq所需要的jar 包-->
  <dependency>
      <groupId>org.apache.activemq</groupId>
      <artifactId>activemq-all</artifactId>
      <version>5.15.9</version>
  </dependency>
  <!--  activemq和spring 整合的基础包 -->
  <dependency>
      <groupId>org.apache.xbean</groupId>
      <artifactId>xbean-spring</artifactId>
      <version>3.16</version>
  </dependency>
```

### JMS编码总体架构

![1600011053403](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1600011053403.png)

### Destination简介

Destination是目的地。下面拿jvm和mq，做个对比。目的地，我们可以理解为是数据存储的地方。

![img](file:///C:\Users\ADMINI~1\AppData\Local\Temp\ksohtml11524\wps2.jpg)

Destination分为两种：队列和主题。下图介绍：

![img](file:///C:\Users\ADMINI~1\AppData\Local\Temp\ksohtml11524\wps3.jpg)

**在点对点的消息传递域中，目的地被称为队列(queue);**

**在发布订阅消息传递域中，目的地被称为主题(topic)**

### JMS开发的基本步骤

![img](file:///C:\Users\ADMINI~1\AppData\Local\Temp\ksohtml11524\wps1.jpg)

### 队列（Queue）

#### 消息生产者的案例

```java
public class JmsProduce {
    //linux上部署的activemq的ip地址 + active的端口号
    public static final String DEFAULT_BROKER_URL = "tcp://47.93.235.107:61616";
    //目的名称
    public static final String QUEUE_NAME = "queue01";
    
    public static void main(String[] args) throws Exception{
        // 1 按照给定的url创建连接工厂，这个构造器采用默认的用户名密码admin。该类的其他构造方法可以指定用户名和密码。
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(DEFAULT_BROKER_URL);

        // 2 通过连接工厂，获得连接connection 并启动访问。
        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();
        // 3 创建会话session 。第一参数是是否开启事务， 第二参数是消息签收的方式
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // 4 创建目的地（两种 ：队列/主题）。Destination是Queue和Topic的父类(父接口)
        Queue queue = session.createQueue(QUEUE_NAME);
        // 5 创建消息的生产者
        MessageProducer messageProducer = session.createProducer(queue);
        // 6 通过messageProducer 生产 3 条 消息发送到消息队列中
        for (int i = 1; i <= 3 ; i++) {
            // 7  创建消息
            TextMessage textMessage = session.createTextMessage("msg" + i);
            // 8  通过messageProducer发送给mq
            messageProducer.send(textMessage);
        }
        // 9 关闭资源
        messageProducer.close();
        session.close();
        connection.close();
        System.out.println("消息发送到MQ完成");
    }
}

```

上面代码运行之后MQ中的结果：

![img](file:///C:\Users\ADMINI~1\AppData\Local\Temp\ksohtml11524\wps5.jpg)

![1600012957418](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1600012957418.png)

#### 阻塞式消费者的案例

```java
public class JmsConsumer {
    //linux上部署的activemq的ip地址 + active的端口号
    public static final String DEFAULT_BROKER_URL = "tcp://47.93.235.107:61616";
    //目的名称
    public static final String QUEUE_NAME = "queue01";

    public static void main(String[] args) throws JMSException {
        // 1 按照给定的url创建连接工厂，这个构造器采用默认的用户名密码admin。该类的其他构造方法可以指定用户名和密码。
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(DEFAULT_BROKER_URL);

        // 2 通过连接工厂，获得连接connection 并启动访问。
        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();
        // 3 创建会话session 。第一参数是是否开启事务， 第二参数是消息签收的方式
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // 4 创建目的地（两种 ：队列/主题）。Destination是Queue和Topic的父类(父接口)
        Queue queue = session.createQueue(QUEUE_NAME);
        //5.创建消息的消费者
        MessageConsumer messageConsumer = session.createConsumer(queue);
        while (true) {
            // 同步堵塞方式reveive() 一直等待接收消息，在能够接收到消息之前将一直阻塞。 是同步阻塞方式 。
            // reveive(Long time) : 等待n毫秒之后还没有收到消息，就是结束阻塞。
            // 因为消息发送者是 TextMessage，所以消息接受者也要是TextMessage
            TextMessage textMessage = (TextMessage) messageConsumer.receive(4000L);
            if (textMessage != null) {
                System.out.println("消费者接收了" + textMessage.getText());
            } else {
                break;
            }
        }
        //关闭连接
        messageConsumer.close();
        session.close();
       connection.close();
    }
}

```

#### 异步监听式消费者案例

```java
public class JmsAsyncListenerConsumer {
    //linux上部署的activemq的ip地址 + active的端口号
    public static final String DEFAULT_BROKER_URL = "tcp://47.93.235.107:61616";
    //目的名称
    public static final String QUEUE_NAME = "queue01";

    public static void main(String[] args) throws Exception {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(DEFAULT_BROKER_URL);
        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(QUEUE_NAME);
        MessageConsumer messageConsumer = session.createConsumer(queue);
        
      /*
     通过监听的方式来消费消息，异步非阻塞的方式(监听onMessage),订阅者或者接收者通过MessageConsumer的setMessageListener(MessageListener messageListener)注册一个消息监听器，当消息到达之后，系统自动调用监听器的MessageListene的onMessage(Message message)方法。
     
     */
        messageConsumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                if(message != null && message instanceof TextMessage){
                    TextMessage textMessage = (TextMessage) message;
                    try {
                        System.out.println("消费者接收消息" + textMessage.getText());
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        //关闭连接
        System.in.read();
        messageConsumer.close();
        session.close();
        connection.close();
    }
}

```

#### 消费者消费情况

> 情况1：只启动消费者1。
>
> 结果：消费者1会消费所有的数据。
>
> 情况2：先生产，然后先启动消费者1，再启动消费者2。
>
> 结果：消费者1消费所有的数据。消费者2不会消费到消息。
>
> 情况3：先启动了消费者1,再启动消费者2。然后生产者生产6条消息，
>
> 结果：消费者1和消费者2平摊了消息。各自消费3条消息。



#### 两种消费方式

> 同步阻塞方式(receive)
> 订阅者或接收者使用MessageConsumer的receive()方法来接收消息，receive方法在能接收到消息之前（或超时之前）将一直阻塞。
>
> 异步非阻塞方式（监听器onMessage()）
> 订阅者或接收者通过MessageConsumer的setMessageListener(MessageListener listener)注册一个消息监听器，
> 当消息到达之后，系统会自动调用监听器MessageListener的onMessage(Message message)方法。



#### 点对点消息传递域的特点

（1）每个消息只能有一个消费者。类似1对1的关系，好比个人快递自己领取自己的。

（2）消息的生产者和消费者之间**没有时间上的相关性**。无论消费者在生产者发送消息的时候是否处于运行状态，消费着都可以提取消息。好比我们的发送短信，发送者发送后不见得接收者会即看收即看。

（3）消息被消费后队列中不会再存储，所以消费者**不会消费到已经被消费掉的消息。**

![1600088634084](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1600088634084.png)

### 主题（Topic）

![1600093788936](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1600093788936.png)

#### 发布主题生产者案例

```java
public class JmsTopicProduce {
    public static final String DEFAULT_BROKER_URL = "tcp://47.93.235.107:61616";
    public static final String TOPIC_NAME = "topic-01";

    public static void main(String[] args) throws Exception {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(DEFAULT_BROKER_URL);
        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(TOPIC_NAME);
        MessageProducer messageProducer = session.createProducer(topic);
        for (int i = 1; i <= 3; i++) {
            TextMessage textMessage = session.createTextMessage("msg" + i);
            messageProducer.send(textMessage);
        }
        messageProducer.close();
        session.close();
        connection.close();
        System.out.println("生产消息到MQ成功");
    }
}
```

#### 订阅主题消费者案例

```java
public class JmsTopicConsumber {
    public static final String DEFAULT_BROKER_URL = "tcp://47.93.235.107:61616";
    public static final String TOPIC_NAME = "topic-01";

    public static void main(String[] args) throws Exception {
        System.out.println("消费者3号");
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(DEFAULT_BROKER_URL);
        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(TOPIC_NAME);
        MessageConsumer messageConsumer = session.createConsumer(topic);

        messageConsumer.setMessageListener((message) -> {
            if (message != null && message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                try {
                    System.out.println("消费者消费" + textMessage.getText());
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
        System.in.read();
        messageConsumer.close();
        session.close();
        connection.close();
    }

}
```

topic有多个消费者时，消费消息的数量 ≈ 在线消费者数量*生产消息的数量

#### 发布/订阅消息传递域的特点

（1）生产者将消息发布到topic中，每个消息可以有多个消费者，属于1：N的关系；
（2）生产者和消费者之间有时间上的相关性。订阅某一个主题的消费者只能消费自它订阅之后发布的消息。
（3）生产者生产时，topic不保存消息它是无状态的不落地，假如无人订阅就去生产，那就是一条废消息，所以，一般先启动消费者再启动生产者。

JMS规范允许客户创建持久订阅，这在一定程度上放松了时间上的相关性要求。持久订阅允许消费者消费它在未处于激活状态时发送的消息。一句话，好比我们的微信公众号订阅。

**先启动订阅再启动生产，不然发送的消息就是费消息。**

### tpoic和queue对比

|  比较项目  |                        Topic模式主题                         |                        Queue模式队列                         |
| :--------: | :----------------------------------------------------------: | :----------------------------------------------------------: |
|  工作模式  | “订阅-发布”模式，如果当前没有订阅者，消息将会被丢弃；如果有多个订阅者，那么这些订阅者都会收到消息。 | “负载均衡”模式，如果当前没有消费者，消息也不会丢弃；如果有多个消费者，那么一条消息也只会发送其中一个消费者，并且要求消息ack信息。 |
|  有无状态  |                            无状态                            | Queue数据默认会在MQ服务器上以文件形式保存，比如ActiveMQ一般保存在$AMQ_HOME\data\kr_store\data下面，也可以配置成DB存储。 |
| 传递完整性 |                 如果没有订阅者，消息会被丢弃                 |                         消息不会丢弃                         |
|  处理效率  | 由于消息要按照订阅者的数量进行复制，所以处理性能会随着订阅者的增加而明显降低，并且还要结合不同消息协议自身的性能差异。 | 由于一条消息只发送给一个消费者，所以就算消费者再多，性能也不会明显降低，当然不同消息协议的具体性能也是有差异的。 |

## JMS规范和落地产品

> JavaEE
>
> JavaEE是一套使用Java进行企业级应用开发的大家一致遵循的13个核心规范工业标准。JavaEE平台提供了一个基于组件的方法来加快设计，开发。装配及部署企业应用程序。
>
> 1，JDBC（Java Databease）数据库连接
> 2，JNDI（Java Naming and Directory Interfaces）Java的命令和目录接口
> 3，EJB（Enterprise JavaBean）
> 4，RMI（Remote Method Invoke）远程方法调用
> 5，Java IDL（Interface Description Language）/CORBA（Common Object Broker Architecture）接口定义语言/共用对象请求代理程序体系结构
> 6，JSP（Java Server Page）
> 7，Servlet
> 8，XML（Extensible Markup Language）可标记白标记语言
> 9，JMS（Java Message Service）Java消息服务
> 10，JTA（Java Transaction API）Java事务API
> 11，JTS（Java Transaction Service）Java事务服务
> 12，JavaMail
> 13，JAF（JavaBean Activation Framework）

### JMS是什么？

JMS：Java Message Service(Java消息服务是JavaEE中的一个技术)

Java消息服务指的是两个应用程序之间进行异步通信的API，它为标准协议和消息服务提供了一组通用接口，包括创建、发送、读取消息等，用于支持Java应用程序开发。在JavaEE中，当两个应用程序使用JMS进行通信时，它们之间不是直接相连的，而是通过一个共同的消息收发服务组件关联起来以达到解耦/异步削峰的效果。

![1600095653675](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1600095653675.png)

### JMS的组成结构和特点

#### JMS Provider

#### JMS Producer

#### JMS Consumer

#### JMS Message

![img](file:///C:\Users\ADMINI~1\AppData\Local\Temp\ksohtml6008\wps1.jpg)

##### 消息头

**JMSDestination**：消息发送目的地，主要指Queue和Topic

**JMSDeliveryMode**：消息持久化模式

​	持久模式和非持久模式。

​	一条持久性的消息：应该被传送“一次仅仅一次”，这就意味着如果JMS提供者出现故障，该消息并不会丢失，它	会在服务器恢复之后再次传递。

​	一条非持久的消息：最多会传递一次，这意味着服务器出现故障，该消息将会永远丢失。

**JMSExpiration**：设置过期时间

​    可以设置消息在一定时间后过期，默认是永不过期

​	消息过期时间，等于Destination的send方法中的timeToLive值加上发送时刻的GMT时间值。

​	如果timeToLive值等于0，则JMSExpiration被设为0，表示该消息永不过期。

​	如果发送后，在消息过期时间之后还没有被发送到目的地，则该消息被清除。

**JMSPriority**：消息的优先级

​	消息优先级，从0-9十个级别，0-4是普通消息5-9是加急消息。

​	JMS不要求MQ严格按照这十个优先级发送消息但必须保证加急消息要先于普通消息到达。默认是4级。

**JMSMessageID**：唯一标识，每个消息的标识由MQ产生

```java
public class JmsProduce_topic {

    public static final String ACTIVEMQ_URL = "tcp://118.24.20.3:61626";
    public static final String TOPIC_NAME = "topic01";

    public static void main(String[] args) throws  Exception{
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(ACTIVEMQ_URL);
        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(TOPIC_NAME);
        MessageProducer messageProducer = session.createProducer(topic);

        for (int i = 1; i < 4 ; i++) {
            TextMessage textMessage = session.createTextMessage("topic_name--" + i);
            // 这里可以指定每个消息的目的地
            textMessage.setJMSDestination(topic);
            /*
            持久模式和非持久模式。
            一条持久性的消息：应该被传送“一次仅仅一次”，这就意味着如果JMS提供者出现故障，该消息并不会丢失，它会在服务器恢复之后再次传递。
            一条非持久的消息：最多会传递一次，这意味着服务器出现故障，该消息将会永远丢失。
             */
            textMessage.setJMSDeliveryMode(0);
            /*
            可以设置消息在一定时间后过期，默认是永不过期。
            消息过期时间，等于Destination的send方法中的timeToLive值加上发送时刻的GMT时间值。
            如果timeToLive值等于0，则JMSExpiration被设为0，表示该消息永不过期。
            如果发送后，在消息过期时间之后还没有被发送到目的地，则该消息被清除。
             */
            textMessage.setJMSExpiration(1000);
            /*  消息优先级，从0-9十个级别，0-4是普通消息5-9是加急消息。
            JMS不要求MQ严格按照这十个优先级发送消息但必须保证加急消息要先于普通消息到达。默认是4级。
             */
            textMessage.setJMSPriority(10);
            // 唯一标识每个消息的标识。MQ会给我们默认生成一个，我们也可以自己指定。
            textMessage.setJMSMessageID("ABCD");
            // 上面有些属性在send方法里也能设置
            messageProducer.send(textMessage);
        }
        messageProducer.close();
        session.close();
        connection.close();
        System.out.println("  **** TOPIC_NAME消息发送到MQ完成 ****");
    }
}
```

##### 消息体

![1600100440704](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1600100440704.png)

```java
//生产者
for (int i = 1; i < 4 ; i++) {
// 发送TextMessage消息体
    TextMessage textMessage = session.createTextMessage("topic_name--" + i);
    messageProducer.send(textMessage);
    // 发送MapMessage  消息体。set方法: 添加，get方式：获取
    MapMessage  mapMessage = session.createMapMessage();
    mapMessage.setString("name", "张三"+i);
    mapMessage.setInt("age", 18+i);
    messageProducer.send(mapMessage);
}
//消费者
 messageConsumer.setMessageListener( (message) -> {
 // 判断消息是哪种类型之后，再强转。
     if (null != message  && message instanceof TextMessage){
         TextMessage textMessage = (TextMessage)message;
         try {
             System.out.println("****消费者text的消息："+textMessage.getText());
         }catch (JMSException e) {
         }
     }
     if (null != message  && message instanceof MapMessage){
         MapMessage mapMessage = (MapMessage)message;
         try {
             System.out.println("****消费者的map消息："+mapMessage.getString("name"));
             System.out.println("****消费者的map消息："+mapMessage.getInt("age"));
         }catch (JMSException e) {
         }
     }

});
```



##### 消息属性

如果需要除消息头字段之外的值，那么可以使用消息属性。

识别/去重/重点标注等操作，非常有用的方法。

他们是以属性名和属性值对的形式制定的。可以将属性是为消息头得扩展，属性指定一些消息头没有包括的附加信息，比如可以在属性里指定消息选择器。消息的属性就像可以分配给一条消息的附加消息头一样。它们允许开发者添加有关消息的不透明附加信息。它们还用于暴露消息选择器在消息过滤时使用的数据。

```java
  	//生产者
	for (int i = 1; i < 4 ; i++) {
      TextMessage textMessage = session.createTextMessage("topic_name--" + i);
      // 调用Message的set*Property()方法，就能设置消息属性。根据value的数据类型的不同，有相应的API。
      textMessage.setStringProperty("From","ZhangSan@qq.com");
      textMessage.setByteProperty("Spec", (byte) 1);
      textMessage.setBooleanProperty("Invalide",true);
      messageProducer.send(textMessage);
   }
	//消费者
 messageConsumer.setMessageListener( (message) -> {
     if (null != message  && message instanceof TextMessage){
         TextMessage textMessage = (TextMessage)message;
         try {
             System.out.println("消息体："+textMessage.getText());
             System.out.println("消息属性："+textMessage.getStringProperty("From"));
             System.out.println("消息属性："+textMessage.getByteProperty("Spec"));
             System.out.println("消息属性："+textMessage.getBooleanProperty("Invalide"));
         }catch (JMSException e) {
         }
     }
 });
```

### JMS的可靠性

#### PERSISTENT：持久化

##### Queue(默认持久化)

非持久化：服务器宕机了，消息不存在。

```java
messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
```

持久化：服务器宕机了，消息依然存在。

```java
messageProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
```

> 持久化消息
> 这是队列的默认传递模式，此模式保证这些消息只被传送一次和成功使用一次。对于这些消息，可靠性是优先考虑的因素。
>
> 可靠性的另一个重要方面是确保持久性消息传送至目标后，消息服务在向消费者传送它们之前不会丢失这些消息。

##### Topic(默认非持久化)

**先启动订阅消费者再启动订阅生产者**

> topic默认就是非持久化的，因为生产者生产消息时，消费者也要在线，这样消费者才能消费到消息。
>
> topic消息持久化，只要消费者向MQ服务器注册过，所有生产者发布成功的消息，该消费者都能收到，不管
>
> 是MQ服务器宕机还是消费者不在线。
>
> 1.一定要先运行一次消费者，等于向MQ注册，类似我订阅了这个主题。
>
> 2.然后再运行生产者发送消息。
>
> 3.之后无论消费者是否在线，都会收到消息。如果不在线的话，下次连接的时候，会把没有收过的消息都接收过来。

**持久化的发布主题生产者**

```java
public class TopicProducePersistent {
    public static final String DEFAULT_BROKER_URL = "tcp://47.93.235.107:61616";
    public static final String TOPIC_PERSISTNET_NAME = "topc_persistent";

    public static void main(String[] args) throws Exception {

        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(DEFAULT_BROKER_URL);
        Connection connection = activeMQConnectionFactory.createConnection();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Topic topic = session.createTopic(TOPIC_PERSISTNET_NAME);
        MessageProducer messageProducer = session.createProducer(topic);

        //设置持久化的Topic
        messageProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
        //设置持久化Topic之后，再启动连接
        connection.start();
        for (int i = 1; i <= 3; i++) {
            TextMessage textMessage = session.createTextMessage("msg" + i);
            messageProducer.send(textMessage);
        }

        messageProducer.close();
        session.close();
        connection.close();
        System.out.println("TOPIC_PERSISTNET_NAME消息发布到MQ完成");
    }
}
```

**持久化的订阅主题消费者**

```java
public class TopiConsumerPersisent {
    public static final String DEFAULT_BROKER_URL = "tcp://47.93.235.107:61616";
    public static final String TOPIC_PERSISTNET_NAME = "topc_persistent";

    public static void main(String[] args) throws Exception {

        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(DEFAULT_BROKER_URL);
        Connection connection = activeMQConnectionFactory.createConnection();

        connection.setClientID("z3");
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(TOPIC_PERSISTNET_NAME);
        //创建一个topic订阅对象
        TopicSubscriber topicSubscriber = session.createDurableSubscriber(topic, "remark...");
        //再开启连接
        connection.start();
        Message message = topicSubscriber.receive();
        while (message != null && message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            System.out.println("收到的持久化 topic" + textMessage.getText());
            message = topicSubscriber.receive();
        }


        session.close();
        connection.close();
    }
}

```

#### Transaction：事务

![](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1600272109119.png)

```java
//生产者提交的事务，如果为false，调用send()时会自动提交进入队列中
Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//生产者提交的事务，如果为true，则必须使用session.commit(),消息才会真正提交到队列中
 Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
 try {
     session.commit();
 } catch (Exception e) {
     e.printStackTrace();
     session.rollback();
 } finally {
     messageProducer.close();
     session.close();
     connection.close();
 }
```

> (1) 生产者开启事务后，执行commit方法，这批消息才真正的被提交。不执行commit方法，这批消息不会提交。执行rollback方法，之前的消息会回滚掉。生产者的事务机制，要高于签收机制，当生产者开启事务，签收机制不再重要。
>
> (2) 消费者开启事务后，执行commit方法，这批消息才算真正的被消费。不执行commit方法，这些消息不会标记已消费，下次还会被消费。执行rollback方法，是不能回滚之前执行过的业务逻辑，但是能够回滚之前的消息，回滚后的消息，下次还会被消费。消费者利用commit和rollback方法，甚至能够违反一个消费者只能消费一次消息的原理。
>
> (3) 问：消费者和生产者需要同时操作事务才行吗？  
>
> 答：消费者和生产者的事务，完全没有关联，各自是各自的事务。

#### Acknowledge：签收

```java
Session.AUTO_ACKNOWLEDGE://自动签收(默认)，该种方式，无需我们程序做任何操作，框架会帮我们自动签收收到的消息。
Session.CLIENT_ACKNOWLEDGE://手动签收,该种方式，需要我们手动调用Message.acknowledge()，来签收消息。如果不签收消息，该消息会被我们反复消费，只到被签收
Session.DUPS_OK_ACKNOWLEDGE://允许重复消息，多线程或多个消费者同时消费到一个消息，因为线程不安全，可能会重复消费。该种方式很少使用到。
Session.SESSION_TRANSACTED：//事务下的签收，开始事务的情况下，可以使用该方式。该种方式很少使用到。
```

```java
//如果使用了Session.CLIENT_ACKNOWLEDGE手动签收，客户端需调用 textMessage.acknowledge();
Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
 while (true) {
     TextMessage textMessage = (TextMessage) messageConsumer.receive(4000L);
     if (textMessage != null) {
         System.out.println("消费者接收了" + textMessage.getText());
         textMessage.acknowledge();
     } else {
         break;
     }
 }
```

**事务和签收的关系**

①　在事务性会话中，当一个事务被成功提交则消息被自动签收。如果事务回滚，则消息会被再次传送。事务优先于签收，开始事务后，签收机制不再起任何作用。

②　非事务性会话中，消息何时被确认取决于创建会话时的应答模式。

③　生产者事务开启，只有commit后才能将全部消息变为已消费。

④　事务偏向生产者，签收偏向消费者。也就是说，生产者使用事务更好点，消费者使用签收机制更好点。

### JMS的点对点总结

点对点模型是基于队列的，生产者发消息到队列，消费者从队列接收消息，队列的存在使得消息的异步传输成为可能。和我们平时给朋友发送短信类似。

如果在Session关闭时有部分消息己被收到但还没有被签收(acknowledged),那当消费者下次连接到相同的队列时，这些消息还会被再次接收

队列可以长久地保存消息直到消费者收到消息。消费者不需要因为担心消息会丢失而时刻和队列保持激活的连接状态，充分体现了异步传输模式的优势

### JMS的发布订阅总结

#### (1) JMS的发布订阅总结

JMS Pub/Sub 模型定义了如何向一个内容节点发布和订阅消息，这些节点被称作topic。

主题可以被认为是消息的传输中介，发布者（publisher）发布消息到主题，订阅者（subscribe）从主题订阅消息。

主题使得消息订阅者和消息发布者保持互相独立不需要解除即可保证消息的传送

#### (2) 非持久订阅

非持久订阅只有当客户端处于激活状态，也就是和MQ保持连接状态才能收发到某个主题的消息。

如果消费者处于离线状态，生产者发送的主题消息将会丢失作废，消费者永远不会收到。

 一句话：先订阅注册才能接受到发布，只给订阅者发布消息。

#### (3) 持久订阅

客户端首先向MQ注册一个自己的身份ID识别号，当这个客户端处于离线时，生产者会为这个ID保存所有发送到主题的消息，当客户再次连接到MQ的时候，会根据消费者的ID得到所有当自己处于离线时发送到主题的消息

当持久订阅状态下，不能恢复或重新派送一个未签收的消息。

持久订阅才能恢复或重新派送一个未签收的消息。

#### (4) 非持久和持久化订阅如何选择

当所有的消息必须被接收，则用持久化订阅。当消息丢失能够被容忍，则用非持久订阅。



## ActiveMQ的Broker

​		Broker相当于一个ActiveMQ服务器实例。说白了，Broker其实就是实现了用代码的形式启动ActiveMQ，将MQ嵌入到Java代码中，以便随时用随时启动，在用的时候再去启动这样能节省了资源，也保证了可用性。这种方式，我们实际开发中很少采用，因为他缺少太多了东西，如：日志，数据存储等等。

​		启动Broker时指定配置文件，可以帮助我们在一台服务器上启动对个Broker。实际工作中一台服务器只启动一个Broker。

```linux
./activemq start xbean:file:/usr/local/activemq/apache-activemq/5.16.0/conf/activemq02.xml 
```

```xml
//添加依赖
<dependency>
     <groupId>com.fasterxml.jackson.core</groupId>
     <artifactId>jackson-databind</artifactId>
     <version>2.10.1</version>
 </dependency>
```

```java
public class BrokerMQ {
    public static void main(String[] args) throws Exception {
        //用ActiveMQ Broker作为独立的消息服务器来构建Java应用。
        //ActiveMQ也支持在vm中通信基于嵌入的broker，能够无缝的集成其他java应用。
        BrokerService brokerService = new BrokerService();
        brokerService.setPopulateJMSXUserID(true);
        brokerService.addConnector("tcp://localhost:61616");
        brokerService.start();
    }
}
```



## Spring整合ActiveMQ

### 1.pom.xml添加依赖

```xml
<dependencies>
   <!-- activemq核心依赖包  -->
    <dependency>
        <groupId>org.apache.activemq</groupId>
        <artifactId>activemq-all</artifactId>
        <version>5.10.0</version>
    </dependency>
    <!--  嵌入式activemq的broker所需要的依赖包   -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.10.1</version>
    </dependency>
    <!-- activemq连接池 -->
    <dependency>
        <groupId>org.apache.activemq</groupId>
        <artifactId>activemq-pool</artifactId>
        <version>5.15.10</version>
    </dependency>
    <!-- spring支持jms的包 -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-jms</artifactId>
        <version>5.2.1.RELEASE</version>
    </dependency>
    <!--spring相关依赖包-->
    <dependency>
        <groupId>org.apache.xbean</groupId>
        <artifactId>xbean-spring</artifactId>
        <version>4.15</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-aop</artifactId>
        <version>5.2.1.RELEASE</version>
    </dependency>
    <!-- Spring核心依赖 -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-core</artifactId>
        <version>4.3.23.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>4.3.23.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-aop</artifactId>
        <version>4.3.23.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-orm</artifactId>
        <version>4.3.23.RELEASE</version>
    </dependency>
</dependencies>
```

### 2.Spring的ActiveMQ配置文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context https://www.springframework.org/schema/context/spring-context.xsd">

    <!--  开启包的自动扫描  -->
    <context:component-scan base-package="com.cuslink.spring"/>
    <!--  配置生产者  -->
    <bean id="connectionFactory" class="org.apache.activemq.pool.PooledConnectionFactory" destroy-method="stop">
        <property name="connectionFactory">
            <!--      正真可以生产Connection的ConnectionFactory,由对应的JMS服务商提供      -->
            <bean class="org.apache.activemq.spring.ActiveMQConnectionFactory">
                <property name="brokerURL" value="tcp://47.93.235.107:61616"/>
            </bean>
        </property>
        <property name="maxConnections" value="100"/>
    </bean>

    <!--  这个是队列目的地,点对点的Queue  -->
    <bean id="destinationQueue" class="org.apache.activemq.command.ActiveMQQueue">
        <!--    通过构造注入Queue名    -->
        <constructor-arg index="0" value="spring-active-queue"/>
    </bean>

    <!--  这个是队列目的地,  发布订阅的主题Topic-->
    <bean id="destinationTopic" class="org.apache.activemq.command.ActiveMQTopic">
        <constructor-arg index="0" value="spring-active-topic"/>
    </bean>

    <!--  Spring提供的JMS工具类,他可以进行消息发送,接收等  -->
    <bean id="jmsTemplate" class="org.springframework.jms.core.JmsTemplate">
        <!--    传入连接工厂    -->
        <property name="connectionFactory" ref="connectionFactory"/>
        <!--    传入目的地    -->
        <property name="defaultDestination" ref="destinationQueue"/>
        <!--    消息自动转换器    -->
        <property name="messageConverter">
            <bean class="org.springframework.jms.support.converter.SimpleMessageConverter"/>
        </property>
    </bean>
</beans>
```

### 3.队列生产者

```java
@Service
public class SpringMQ_Produce {

    @Autowired
    private JmsTemplate jmsTemplate;

    public static void main(String[] args) {

        ClassPathXmlApplicationContext ioc = new ClassPathXmlApplicationContext("applicationContext.xml");
        SpringMQ_Produce springMQ_produce = (SpringMQ_Produce) ioc.getBean("springMQ_Produce");

//        springMQ_produce.jmsTemplate.send(new MessageCreator() {
//            @Override
//            public Message createMessage(Session session) throws JMSException {
//                TextMessage textMessage = session.createTextMessage("SpringActiveMQ的整合case");
//                return textMessage;
//            }
//        });
        springMQ_produce.jmsTemplate.send((session) -> {
            TextMessage textMessage = session.createTextMessage("SpringActiveMQ的整合case");
            return textMessage;
        });
        System.out.println("********* send task over");
    }
}
```

### 4.队列消费者

```java
@Service
public class SpingMQ_Consumer {
    @Autowired
    private JmsTemplate jmsTemplate;

    public static void main(String[] args) {

        ClassPathXmlApplicationContext ioc = new ClassPathXmlApplicationContext("applicationContext.xml");
        SpingMQ_Consumer spingMQ_Consumer = (SpingMQ_Consumer) ioc.getBean("spingMQ_Consumer");
        String resultValue = (String) spingMQ_Consumer.jmsTemplate.receiveAndConvert();
        System.out.println("消费者收到的消息" + resultValue);
    }
}

```

### 5.主题生产者和消费者

主题生产者和消费者的业务代码是一致的，只需要将配置文件默认目的地改为主题即可。

```xml
<!--  这个是队列目的地,  发布订阅的主题Topic-->
    <bean id="destinationTopic" class="org.apache.activemq.command.ActiveMQTopic">
        <constructor-arg index="0" value="spring-active-topic"/>
    </bean>

    <!--  Spring提供的JMS工具类,他可以进行消息发送,接收等  -->
    <bean id="jmsTemplate" class="org.springframework.jms.core.JmsTemplate">
        <!--    传入连接工厂    -->
        <property name="connectionFactory" ref="connectionFactory"/>
        <!--    传入目的地    -->
        <property name="defaultDestination" ref="destinationTopic"/>
        <!--    消息自动转换器    -->
        <property name="messageConverter">
            <bean class="org.springframework.jms.support.converter.SimpleMessageConverter"/>
        </property>
    </bean>
```

### 6.配置消费者的监听器

不需要启动消费者，通过配置监听就可以进行消费。

消费者配置了自动监听，就相当于在Spring里面后台运行，有消息就运行我们实现监听类里面的方法

```xml
<!--  配置Jms消息监听器  -->
    <bean id="defaultMessageListenerContainer" class="org.springframework.jms.listener.DefaultMessageListenerContainer">
        <!--  Jms连接的工厂     -->
        <property name="connectionFactory" ref="connectionFactory"/>
        <!--   设置默认的监听目的地     -->
        <property name="destination" ref="destinationTopic"/>
        <!--  指定自己实现了MessageListener的类     -->
        <property name="messageListener" ref="myMessageListener"/>
    </bean>
```

```java
@Component
public class MyMessageListener implements MessageListener{

    @Override
    public void onMessage(Message message) {
        if(message != null && message instanceof TextMessage){
            TextMessage textMessage = (TextMessage) message;
            try {
                System.out.println("消费者收到的消息"+textMessage.getText());
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}

```

## SpringBoot整合ActiveMQ

pom.xml引入依赖

```xml
 <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-activemq</artifactId>
     <version>2.2.1.RELEASE</version>
 </dependency>
```

yml.xml配置

```yaml
server:
  port: 7777
  
#activemq的配置
spring:
  activemq:
    broker-url: tcp://47.93.235.107:61616
    user: admin
    password: admin
  jms:
    pub-sub-domain: false  #false是Queue true是Topic

myQueueName: springboot-activemq-quey

```

启动类

```java
@SpringBootApplication
@EnableScheduling//是否开始定时任务调度功能
public class ActiveMQMain {
    public static void main(String[] args) {
        SpringApplication.run(ActiveMQMain.class, args);
    }
}
```

配置类

```java
@Configuration
public class MyConfig {

    @Value("${myQueueName}")
    private String queueName;

    @Bean
    public ActiveMQQueue queue() {
        return new ActiveMQQueue(queueName);
    }
}
```

### 队列生产者

```java
@Component
@EnableJms//要开启JMS
public class Queue_Producer {

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Autowired
    private ActiveMQQueue activeMQQueue;
    
    public void sendMsg() {
        jmsMessagingTemplate.convertAndSend(activeMQQueue, UUID.randomUUID().toString().substring(0, 8));
    }
    
    //定时任务，每3秒钟执行一次
    @Scheduled(fixedDelay = 3000)
    public void sendMsgScheduling() {
        jmsMessagingTemplate.convertAndSend(activeMQQueue, UUID.randomUUID().toString().substring(0, 8));
        System.out.println("********** sendMsgScheduling send ok");
    }

}

```

测试

```java
@SpringBootTest(classes = ActiveMQMain.class)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class TestActiveMQ {

    @Autowired
    private Queue_Producer queue_producer;

    @Test
    public void test1() {
        queue_producer.sendMsg();
    }
}
```

### 队列消费者

```java
@Component
public class Queue_Consumer {
    
    //注册一个监听器。destination指定监听的目的地。
    @JmsListener(destination = "${myQueueName}")
    public void receive(TextMessage textMessage) throws Exception {
        System.out.println("消息收到" + textMessage.getText());
    }
}

```

### 主题生产者

```yaml
server:
  port: 7777
spring:
  activemq:
    broker-url: tcp://47.93.235.107:61616
    user: admin
    password: admin
  jms:
    pub-sub-domain: true #主题改为true

myTopic: springboot-activemq-topic

```

```java
@Configuration
@EnableJms
public class MyConfig {

    @Value("${myTopic}")
    private String topicName;

    @Bean
    public Topic topic() {
        return new ActiveMQTopic(topicName);
    }
}
====================================================
@Component
public class Topic_Producer {

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Autowired
    private Topic topic;


    @Scheduled(fixedDelay = 3000)
    public void sendMsg() {
        jmsMessagingTemplate.convertAndSend(topic, "主题:" + UUID.randomUUID().toString().substring(0, 8));
    }
}
```

### 主题消费者

```java
@Component
public class Topic_Consumer {

    //消费者进行监听
    @JmsListener(destination = "${myTopic}")
    public void receive(TextMessage textMessage) throws Exception {
        System.out.println("消息收到：" + textMessage.getText());
    }
}
```

## ActiveMQ的传输协议

面试题：

默认的61616端口如何修改？

你生产上的链接协议如何配置的？使用tcp吗？

### 简介

ActiveMQ支持的client-broker通讯协议有：TCP、NIO、UDP、SSL、Http(s)、VM。其中配置Transport Connector的文件在ActiveMQ安装目录的conf/activemq.xml中的<transportConnectors>标签之内。
见下图实际配置：

![](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1600876550783.png)

在上文给出的配置信息中，
URI描述信息的头部都是采用协议名称：例如
描述amqp协议的监听端口时，采用的URI描述格式为“amqp://······”；
描述Stomp协议的监听端口时，采用URI描述格式为“stomp://······”；
唯独在进行openwire协议描述时，URI头却采用的“tcp://······”。这是因为ActiveMQ中默认的消息协议就是openwire。

### 支持的传输协议

个人说明：除了tcp和nio协议，其他的了解就行。各种协议有各自擅长该协议的中间件，工作中一般不会使用activemq去实现这些协议。如： mqtt是物联网专用协议，采用的中间件一般是mosquito。ws是websocket的协议，是和前端对接常用的，一般在java代码中内嵌一个基站（中间件）。stomp好像是邮箱使用的协议的，各大邮箱公司都有基站（中间件）。

注意：协议不同，我们的代码都会不同。

| 协议    | 描述                                                         |
| ------- | ------------------------------------------------------------ |
| TCP     | 默认的协议，性能相对可以                                     |
| NIO     | 基于TCP协议之上的，进行了扩展和优化，具有更好的扩展性        |
| UDP     | 性能比TCP更好，但是不就有可靠性                              |
| SSL     | 安全链接                                                     |
| HTTP(s) | 基于HTTP或者HTTPS                                            |
| VM      | VM本身不是协议，当客户端和代理在同一个Java虚拟机(VM)中运行时，他们之间需要通信，但不想占用网络通道，而是直接通信，可以使用改方式 |

#### TCP协议(默认)

(1) Transmission Control Protocol(TCP)是默认的Broker配置。TCP的Client监听端口61616

(2) 在网络传输数据前，必须要先序列化数据，消息是通过一个叫wire protocol的来序列化成字节流。

(3) TCP连接的URI形式如：tcp://HostName:port?key=value&key=value，后面的参数是可选的。

(4) TCP传输的的优点：

​	TCP协议传输可靠性高，稳定性强

​	高效率：字节流方式传递，效率很高

​	有效性、可用性：应用广泛，支持任何平台

(5) 关于Transport协议的可选配置参数可以参考官网http://activemq.apache.org/tcp-transport-reference

#### NIO协议

(1) New I/O API Protocol(NIO)

(2) NIO协议和TCP协议类似，但NIO更侧重于底层的访问操作。它允许开发人员对同一资源可有更多的client调用和服务器端有更多的负载。

(3) 适合使用NIO协议的场景：

可能有大量的Client去连接到Broker上，一般情况下，大量的Client去连接Broker是被操作系统的线程所限制的。因此，NIO的实现比TCP需要更少的线程去运行，所以建议使用NIO协议。

可能对于Broker有一个很迟钝的网络传输，NIO比TCP提供更好的性能。

(4) NIO连接的URI形式：nio://hostname:port?key=value&key=value

(5) 关于Transport协议的可选配置参数可以参考官网http://activemq.apache.org/configuring-version-5-transports.html

#### AMQP协议

Advanced Message Queuing Protocol，一个提供统一消息服务的应用层标准高级消息队列协议，是应用层协议的一个开放标准，为面向消息的中间件设计。基于此协议的客户端与消息中间件可传递消息，并不受客户端/中间件不同产品，不同开发语言等条件限制。

#### STOMP协议

STOP，Streaming Text Orientation Message Protocol，是流文本定向消息协议，是一种为MOM(Message Oriented Middleware，面向消息中间件)设计的简单文本协议。

#### SSL协议

安全链接

#### MQTT协议

MQTT(Message Queuing Telemetry Transport，消息队列遥测传输)是IBM开发的一个即时通讯协议，有可能成为物联网的重要组成部分。该协议支持所有平台，几乎可以把所有联网物品和外部连接起来，被用来当作传感器和致动器(比如通过Twitter让房屋联网)的通信协议。

#### WS协议

ws是websocket的协议，是和前端对接常用的，一般在java代码中内嵌一个基站（中间件）

### NIO协议案例

ActiveMQ这些协议传输的底层默认都是使用BIO网络的IO模型。只有当我们指定使用nio才使用NIO的IO模型。

修改配置文件activemq.xml在 <transportConnectors>节点下添加如下内容：

```xml
<transportConnector name="nio" uri="nio://0.0.0.0:61618?trace=true" />
```

### NIO协议案例增加

上面是Openwire协议传输底层使用NIO网络IO模型。 如何让其他协议传输底层也使用NIO网络IO模型呢？

![1600953443531](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1600953443531.png)

在activemq.xml中加入以下配置：

```xml
<transportConnector name="nio" uri="nio://0.0.0.0:61618?trace=true" />

 <transportConnector name="auto+nio" uri="auto+nio://0.0.0.0:61608?maximumConnections=1000&wireFormat.maxFrameSize=104857600&org.apache.activemq.transport.nio.SelectorManager.corePoolSize=20&org.apache.activemq.transport.nio.Se1ectorManager.maximumPoo1Size=50"/>
```

auto	: 针对所有的协议，他会识别我们是什么协议。

nio	：使用NIO网络IO模型

## ActiveMQ的消息存储与持久化

官网： http://activemq.apache.org/persistence 

![img](file:///C:\Users\ADMINI~1\AppData\Local\Temp\ksohtml7036\wps1.jpg)

MQ高可用：事务、可持久、签收，是属于MQ自身特性，自带的。这里的持久化是外力，是外部插件。之前讲的持久化是MQ的外在表现，现在讲的的持久是是底层实现。

### 是什么

为了避免意外宕机以后丢失信息，需要做到重启后可以恢复消息队列，消息系统一半都会采用持久化机制。
ActiveMQ的消息持久化机制有**JDBC，AMQ，KahaDB和LevelDB**，无论使用哪种持久化方式，消息的存储逻辑都是一致的。

就是在发送者将消息发送出去后，消息中心首先将消息存储到本地数据文件、内存数据库或者远程数据库等。再试图将消息发给接收者，成功则将消息从存储中删除，失败则继续尝试尝试发送。

消息中心启动以后，要先检查指定的存储位置是否有未成功发送的消息，如果有，则会先把存储位置中的消息发出去。

### 持久化机制

#### AMQ Message Store

基于文件的存储方式，是以前默认的消息存储，现在不用了。

AMQ是一种文件存储形式，它具有写入速度快和容易恢复的特点。消息存储再一个个文件中文件的默认大小为32M，当一个文件中的消息已经全部被消费，那么这个文件将被标识为可删除，在下一个清除阶段，这个文件被删除。AMQ适用于ActiveMQ5.3之前的版本



#### KahaDB消息存储(默认)

官网：http://activemq.aache.org/kahadb

基于日志文件，从ActiveMQ5.4开始默认的持久化插件。

```xml
    <!--从activemq.xml文件中查看到,kahadb是ActiveMQ的默认持久化消息存储机制-->
	<!--
            Configure message persistence for the broker. The default persistence
            mechanism is the KahaDB store (identified by the kahaDB tag).
            For more information, see:

            http://activemq.apache.org/persistence.html
        -->
        <persistenceAdapter>
            <kahaDB directory="${activemq.data}/kahadb"/>
        </persistenceAdapter>
<!-- KahaDB是目前默认的存储方式，可用于任何场景，提高了性能和恢复能力。
消息存储使用一个事务日志和仅仅用一个索引文件来存储它所有的地址。
KahaDB是一个专门针对消息持久化的解决方案，它对典型的消息使用模型进行了优化。
数据被追加到data logs中。当不再需要log文件中的数据的时候，log文件会被丢弃。-->
```

进入到data/kahadb下看到下图的信息：

![1600961888039](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1600961888039.png)

**KahaDB的存储原理**

KahaDB在消息保存的目录中有**4类文件**和**一个lock**，跟ActiveMQ的其他几种文件存储引擎相比，这就非常简洁了。

1. db-number.log	KahaDB存储消息到预定大小的数据记录文件中，文件名为db-number.log。当数据文件已满时，一个新的文件会随之创建，number数值也会随之递增，它随着消息数量的增多，如每32M一个文件，文件名按照数字进行编号，如db-1.log，db-2.log······。当不再有引用到数据文件中的任何消息时，文件会被删除或者归档。

   ![1600962166277](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1600962166277.png)

   2. db.data	该文件包含了持久化的BTree索引，索引了消息数据记录中的消息，它是消息的索引文件，本质上是B-Tree（B树），使用B-Tree作为索引指向db-number。log里面存储消息。
   3. db.free  当前db.data文件里哪些页面是空闲的，文件具体内容是所有空闲页的ID
   4. db.redo 用来进行消息恢复，如果KahaDB消息存储再强制退出后启动，用于恢复BTree索引。
   5. lock 文件锁，表示当前kahadb独写权限的broker。

#### JDBC消息存储

(1)	MQ+Mysql

![img](file:///C:\Users\ADMINI~1\AppData\Local\Temp\ksohtml5972\wps2.jpg)

(2)	添加mysql数据库的驱动包放到activemq下lib文件夹下![1601102897265](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1601102897265.png)

(3)	jdbcPersistenceAdapter配置

在activemq.xml中修改配置，将默认的kahaDB机制换成jdbcPesisenceAdapter

```xml
<persistenceAdapter>
	<jdbcPersistenceAdapter dataSource="#mysql-ds" createTableOnStartup="true"/>
</persistenceAdapter>
```

![1601103122029](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1601103122029.png)

dataSouce是指定将要引用的持久化数据库的bean名称

createTableOnStartup是否在启动的时候创建数据库表，默认是true，这样每次启动activemq都会去创建表，一般是第一次启动的时候设置为true，然后再改为false。

(4)	数据库连接池的配置

在</broker>标签和<import>标签之间插入数据库连接池配置

```xml
<!-- 默认是的dbcp数据库连接池，如果要换成其他数据库连接池，需要将该连接池jar包，也放到lib目录下。-->
<bean id="mysql-ds" class="org.apache.commons.dbcp2.BasicDataSource" destroymethod="close">
    <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
    <property name="url" value="jdbc:mysql://数据库连接地址/activemq?relaxAutoCommit=true"/>
    <property name="username" value="账号"/>
    <property name="password" value="密码"/>
    <property name="poolPreparedStatements" value="true"/>
  </bean>

```

![1601103437196](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1601103437196.png)

(5)	建库SQL和建表说明

首先在数据库创建一个名为activemq的数据库。

三张表的说明：

ACTIVEMQ_MSGS表

消息表，Queue和Topic都存在里面，结构如下

> ID：自增的数据库主键
> CONTAINER：消息的Destination
> MSGID_PROD：消息发送者的主键
> MSG_SEQ：是发送消息的顺序，MSGID_PROD+MSG_SEQ可以组成JMS的MessageID
> EXPIRATION：消息的过期时间，存储的是从1970-01-01到现在的毫秒数
> MSG：消息本体的Java序列化对象的二进制数据
> PRIORITY：优先级，从0-9，数值越大优先级越高

![1601108693744](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1601108693744.png)

ACTIVEMQ_ACKS表

![1601108785422](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1601108785422.png)

ACTIVEMQ_LOCK表

表ACTIVEMQ_LOCK在集群环境下才有用，只有一个Broker可以获取消息，称为Master Broker，其他的只能作为备份等待Master Broker不可用，才可能成为下一个Master Broker。这个表用于记录哪个Broker是当前的Master Broker

![1601108830795](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1601108830795.png)

如果新建数据库ok，上述配置ok，代码运行Ok，3张表会自动生成。

(6)	代码运行验证

注意：一定要开启持久化，setDeliveryMode(DeliveryMode.PERSISTENT);

(7)	数据库情况

**点对点**

在点对点类型中
当DeliveryMode设置为NON_PERSISTENCE时，消息被保存在内存中
当DeliveryMode设置为PERSISTENCE时，消息保存在broker的相应的文件或者数据库中。

而且点对点类型中消息一旦被Consumer消费，就从数据中删除 

queue模式，非持久化不会将消息持久化到数据库。

queue模式，持久化会将消息持久化数据库。

我们使用queue模式持久化，发布3条消息后，发现ACTIVEMQ_MSGS数据表多了3条数据。

![img](file:///C:\Users\ADMINI~1\AppData\Local\Temp\ksohtml5972\wps3.jpg)

启动消费者，消费了所有的消息后，发现数据表的数据消失了。

![img](file:///C:\Users\ADMINI~1\AppData\Local\Temp\ksohtml5972\wps4.jpg)

**发布/订阅**

我们先启动一下持久化topic的消费者。看到ACTIVEMQ_ACKS数据表多了一条消息。

![img](file:///C:\Users\ADMINI~1\AppData\Local\Temp\ksohtml5972\wps5.jpg)

我们启动持久化生产者发布3个数据，ACTIVEMQ_MSGS数据表新增3条数据，消费者消费所有的数据后，ACTIVEMQ_MSGS数据表的数据并没有消失。持久化topic的消息不管是否被消费，是否有消费者，产生的数据永远都存在，且只存储一条。这个是要注意的，持久化的topic大量数据后可能导致性能下降。这里就像公总号一样，消费者消费完后，消息还会保留。

![img](file:///C:\Users\ADMINI~1\AppData\Local\Temp\ksohtml5972\wps6.jpg)

(8) 	小总结

如果是queue
在没有消费者消费的情况下会将消息保存到activemq_msgs表中，只要有任意一个消费者已经消费过了，消息之后这些消息将会立即被删除。

如果是topic，
一般是先启动消费订阅然后再生产的情况下会将消息保存到activemq_acks

(9)	开发有坑

在配置关系型数据库作为ActiveMQ的持久化存储方案时，有坑

9.1	数据库jar包

记得需要使用到的相关jar包文件放置到ActiveMQ安装路径下的lib目录，mysql-jdbc驱动的jar包和对应的数据库连接池的jar包

9.2	createTableOnStartup属性
默认为true，每次启动activemq都会自动创建表，在第一次启动后，应改为false，避免不必要的损失。

9.3	java.lang.IllegalStateException: LifecycleProcessor not initialized
确认计算机主机名名称没有下划线“_”符号，请更改机器名并且重启后即可解决问题。

#### LevelDB消息存储

官网：http://activemq.apache.org/leveldb-store

这种文件系统是从ActiveMQ5.8之后引进的，它和KahaDB非常相似，也是基于文件的本地数据库存储形式，但是它提供比KahaDB更快的持久性。
但它不使用自定义B-Tree实现来索引独写日志，而是使用基于LevelDB的索引

默认配置如下：

```xml
<persistenceAdapter>
      <levelDB directory="activemq-data"/>
</persistenceAdapter>
```



#### JDBC Message Store with ActiveMQ 

(1)	说明

这种方式克服了JDBC Store的不足，JDBC每次消息过来，都需要去写库读库。ActiveMQ Journal，使用高速缓存写入技术，大大提高了性能。当消费者的速度能够及时跟上生产者消息的生产速度时，journal文件能够大大减少需要写入到DB中的消息。

举个例子：生产者生产了1000条消息，这1000条消息会保存到journal文件，如果消费者的消费速度很快的情况下，在journal文件还没有同步到DB之前，消费者已经消费了90%的以上消息，那么这个时候只需要同步剩余的10%的消息到DB。如果消费者的速度很慢，这个时候journal文件可以使消息以批量方式写到DB。

为了高性能，这种方式使用日志文件存储+数据库存储。先将消息持久到日志文件，等待一段时间再将未消费的消息持久到数据库。该方式要比JDBC性能要高。

(2)	配置

```xml
<!--在activemq.xml中配置-->
<persistenceFactory>        
    <journalPersistenceAdapterFactory 
    journalLogFiles="5" 
    journalLogFileSize="32768" 
    useJournal="true" 
    useQuickJournal="true" 
    dataSource="#mysql-ds" 
    dataDirectory="../activemq-data" /> 
</persistenceFactory>
```

### ActiveM持久化机制小总结

持久化消息主要指的是：
MQ所在服务器宕机了消息不会丢失的机制。

持久化机制演变的过程：
从最初的AMQ Message Store方案到ActiveMQ V4版本推出的High Performance Journal（高性能事务支持）附件，并且同步推出了关于关系型数据库的存储方案。ActiveMQ5.3版本又推出了对KahaDB的支持（5.4版本后被作为默认的持久化方案），后来ActiveMQ 5.8版本开始支持LevelDB，到现在5.9提供了标准的Zookeeper+LevelDB集群化方案。

ActiveMQ消息持久化机制有：
AMQ              基于日志文件
KahaDB          基于日志文件，从ActiveMQ5.4开始默认使用
JDBC              基于第三方数据库
Replicated LevelDB Store 从5.9开始提供了LevelDB和Zookeeper的数据复制方法，用于Master-slave方式的首选数据复制方案。

## ActiveMQ多节点集群

面试题：引入消息队列之后如何保证其高可用性？

### 是什么？

基于Zookeeper和LevelDB搭建ActiveMQ集群,集群仅提供主备方式的高可用集群功能,避免单点故障.

### 三种集群方式对比：

> 1. 基于shareFileSystem共享文件系统(KahaDB)
> 2. 基于JDBC
> 3. 基于可复制的LevelDB

![1601133064813](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1601133064813.png)

------

### ZK+LevelDB主从集群

zookeeper+replicated-leveldb-store的主从集群。

LevelDB，5.6版本之后推出了LevelDB的持久化引擎，它使用了自定义的索引代替常用的BTree索引，其持久化性能高于KahaDB，虽然默认的持久化方式还是KahaDB，但是LevelDB可能会是趋势。
在5.9版本还提供了基于LevelDB和Zookeeper的数据复制方式，作为Master-Slave方式的首选数据复制方案。

本次案例采用zookeeper+replicated-leveldb-store

#### 官方集群原理图



![1601133293276](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1601133293276.png)

使用Zookeeper集群注册所有的ActiveMQ Broker但只有其中一个Broker可以提供服务，它将被视为Master,其他的Broker处于待机状态被视为Slave。
如果Master因故障而不能提供服务，Zookeeper会从Slave中选举出一个Broker充当Master。Slave连接Master并同步他们的存储状态，Slave不接受客户端连接。所有的存储操作都将被复制到连接至Maste的Slaves。
如果Master宕机得到了最新更新的Slave会变成Master。故障节点在恢复后会重新加入到集群中并连接Master进入Slave模式。

所有需要同步的消息操作都将等待存储状态被复制到其他法定节点的操作完成才能完成。
所以，如给你配置了replicas=3，name法定大小是（3/2）+1 = 2。Master将会存储更新然后等待（2-1）=1个Slave存储和更新完成，才汇报success，至于为什么是2-1，阳哥的zookeeper讲解过自行复习。
有一个node要作为观察者存在。当一个新的Master被选中，你需要至少保障一个法定mode在线以能够找到拥有最新状态的node，这个node才可以成为新的Master。

因此，推荐运行至少3个replica nodes以防止一个node失败后服务中断。

#### 部署规划和步骤

1. 环境和版本

2. 关闭防火墙并保证各个服务器能够ping通

3. 要求具体zk集群并可以成功启动

4. 集群部署规划列表

5. 创建3台集群目录

6. 修改管理控制台端口

7. hostname名字映射

8. ActiveMQ集群配置

   配置文件里面的BrokerName要全部一致

   持久化配置(必须)

   ```xml
   <persistenceAdapter>
      <replicatedLevelDB
                  directory="${activemq.data}/leveldb"
                  replicas="3"
                  bind="tcp://0.0.0.0:62621"
                  zkAddress="192.168.10.130:2181,192.168.10.132:2181,192.168.10.133:2181"
                  hostname="192.168.10.130"
                  zkPath="/activemq/leveldb-stores"
                           />
     </persistenceAdapter>
   ```

   9.修改各个节点的消息端口：真实的三台机器不用管

   10.按顺序启动3个ActiveMQ节点，到这步之前是zk集群已经启动成运行

   ​	先启动zk，再启动ActiveMQ

   11.zk集群节点状态说明

#### 集群可用性测试

ActiveMQ的客户端只能访问Master的Broker，其他处于Slave的Broker不能访问，所以客户端连接的Broker应该使用failover协议（失败转移）

当一个ActiveMQ节点挂掉或者一个Zookeeper节点挂点，ActiveMQ服务依然正常运转，如果仅剩一个ActiveMQ节点，由于不能选举Master，所以ActiveMQ不能正常运行。

同样的，
如果zookeeper仅剩一个活动节点，不管ActiveMQ各节点存活，ActiveMQ也不能正常提供服务。
（ActiveMQ集群的高可用依赖于Zookeeper集群的高可用）

## 高级特性和大厂常考重点

## 



