package io.github.abhishekghoshh.basics;

import io.github.abhishekghoshh.runner.Run;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;

@Run
public class ProxyImplementationTest {
    record User(Long id, String name) {
    }

    static List<User> users = List.of(
            new User(1L, "Abhishek"),
            new User(2L, "Ravi"),
            new User(3L, "Neha")
    );

    static interface LocalRepository {

        List<User> getAll();

        Optional<User> getById(Long id);
    }

    static class RepositoryFactory {
        public static LocalRepository create() {
            return (LocalRepository) Proxy.newProxyInstance(
                    LocalRepository.class.getClassLoader(),
                    new Class[]{LocalRepository.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();
                        if (methodName.startsWith("getAll")) {
                            return users;
                        } else if (methodName.startsWith("getById")) {
                            Long id = (Long) args[0];
                            return users.stream()
                                    .filter(user -> user.id().equals(id))
                                    .findFirst();
                        }
                        throw new UnsupportedOperationException("Method not implemented: " + methodName);
                    }
            );
        }
    }

    @Run
    static void proxyImplementationTest1() {
        LocalRepository repository = RepositoryFactory.create();
        System.out.println("All Users: " + repository.getAll());
        System.out.println("User with ID 2: " + repository.getById(1L));
    }

}
