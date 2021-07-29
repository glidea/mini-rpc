package top.glidea.interfaces;

import java.util.List;

public interface TestService {

    void sayHello();

    void add(Integer id, String value);

    List<String> list();

    String get(Integer id);
}
