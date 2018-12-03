import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author laowang
 * @date 2018/12/3 9:27 AM
 * @Description:
 */
public class Client {


    /**
     * 获取代表服务端接口的动态代理对象
     *
     * @param serviceInterface 请求接口名
     * @param addr             带请求服务端ip：端口
     * @param <T>
     * @return
     */
    public <T> T getRemoteProxyObj(final Class serviceInterface, final InetSocketAddress addr) {

        try {
            /**
             * 两个返回值，
             *  1，动态代理类对象
             *  2，代理类方法的返回值
             */
            return (T) Proxy.newProxyInstance(
                    serviceInterface.getClassLoader(),
                    new Class<?>[]{serviceInterface},
                    new InvocationHandler() {

                        /**
                         *
                         * @param proxy     需要代理的对象
                         * @param method    对应代理对象的方法
                         * @param args      对应方法的参数列表
                         * @return
                         * @throws Throwable
                         */
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            ObjectOutputStream outputStream = null;
                            ObjectInputStream inputStream = null;
                            try {
                                Socket socket = new Socket();
                                System.out.println("客户端准备发送请求。。。");
                                socket.connect(addr);

                                // 发送，对象流（序列流）
                                outputStream = new ObjectOutputStream(socket.getOutputStream());
                                // 接口名、方法名、参数类型、方法参数、
                                outputStream.writeUTF(serviceInterface.getName());
                                outputStream.writeUTF(method.getName());
                                outputStream.writeObject(method.getParameterTypes());//参数类型
                                outputStream.writeObject(args);//方法具体参数

                                System.out.println("等待服务端处理");

                                inputStream = new ObjectInputStream(socket.getInputStream());
                                Object result = inputStream.readObject();
                                System.out.println("客户端接收到的返回值 = 【" + result.toString() + "]");
                                return result;
                            } catch (IOException e) {
                                e.printStackTrace();
                                return null;
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                                return null;
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
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void main(String[] args) throws Exception {
        Client client = new Client();
        // todo RPC客户端和RPC服务器端需要一个相同的接口类，RPC客户端通过一个代理类来调用RPC服务器端的函数
         HelloServicee servicee = client.getRemoteProxyObj(Class.forName("HelloServicee"), new InetSocketAddress("localhost", 9999));
        servicee.sayHi("lao wang");
    }
}
