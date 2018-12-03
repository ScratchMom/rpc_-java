import java.awt.event.HierarchyBoundsAdapter;

/**
 * @author laowang
 * @date 2018/12/3 9:13 AM
 * @Description:
 */
public class HelloServiceeImpl implements HelloServicee {
    public String sayHi(String name) {
        return  "Hi " + name;
    }
}
