import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author laowang
 * @date 2018/12/3 9:15 AM
 * @Description:
 */
public class ServerImpl implements Server {
    private static Map<String, Class> serverRegister = new HashMap<String, Class>();
    private static boolean isRunning = false;
    private int port;
    private static ThreadPoolExecutor threadPoolExecutor
            = new ThreadPoolExecutor(2,5,3, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),new ThreadPoolExecutor.DiscardOldestPolicy());

    public ServerImpl() {
    }

    public ServerImpl(int port) {
        this.port = port;
    }

    public void start() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("处理器个数 ： " + Runtime.getRuntime().availableProcessors());
        isRunning = true;
        while (true) {

            System.out.println("服务端等待客户端连接。。。。");
            try {
                // 客户端每次请求一次连接，则服务端从连接池中获取一个线程去处理
                Socket socket = serverSocket.accept();
                threadPoolExecutor.execute(new ServiceTask(socket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        isRunning = false;
        threadPoolExecutor.shutdown();

    }

    class ServiceTask implements Runnable{

        private Socket socket;
        public ServiceTask(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            ObjectOutputStream outputStream = null;
            ObjectInputStream inputStream = null;
            try {

                inputStream = new ObjectInputStream(socket.getInputStream());
                String interfaceName = inputStream.readUTF();               //接口名
                String methodName = inputStream.readUTF();                  //方法名
                Class[] paramsType = (Class[]) inputStream.readObject();   //参数类型
                Object[] params = (Object[]) inputStream.readObject();       //参数列表
                System.out.println("服务端输出 ---> 接口名:【" + interfaceName + "】," +
                        "方法名 :[" + methodName + "],");

                // 到regist中找到与之对应的接口
                Class serverClass = serverRegister.get(interfaceName);
                Method method = serverClass.getMethod(methodName, paramsType);
                Object result = method.invoke(serverClass.newInstance(), params);
                System.out.println("服务度 method.invoke返回结果 ： 【" + result + "】");
                System.out.println("向客户端将方法执行完毕的返回值传递给客户端");


                outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.writeObject(result);


            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void register(Class service, Class serviceImpl) {
        serverRegister.put(service.getName(), serviceImpl);
    }



    public static void main(String[] args) throws Exception {
        ServerImpl server = new ServerImpl(9999);
        server.register(HelloServicee.class, HelloServiceeImpl.class);
        server.start();
    }
}
