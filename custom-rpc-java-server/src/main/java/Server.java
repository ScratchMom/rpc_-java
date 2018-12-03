import java.io.IOException;

/**
 * @author laowang
 * @date 2018/12/3 9:14 AM
 * @Description:
 */
public interface Server {

    public void start() ;

    public void stop();

    public void register(Class service,Class serviceImpl)  ;
}
