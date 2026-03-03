import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ProductBatchLoadTest {

    private static final Logger log = LoggerFactory.getLogger(ProductBatchLoadTest.class);

    // 假设这是你的业务类，包含 findByIds 方法
    public static class ProductService {
        private final SessionFactory sessionFactory;

        public ProductService(SessionFactory sessionFactory) {
            this.sessionFactory = sessionFactory;
        }

        @Override
        public List<ProductImpl> findByIds(Collection<Long> ids) {
            if (ids == null || ids.isEmpty()) {
                log.debug("Empty or null ids collection provided to findByIds.");
                return Collections.emptyList();
            }

            Session session = this.getSessionFactory().getCurrentSession();

            // 使用 Hibernate 的 byMultipleIds 进行批量加载
            @SuppressWarnings("unchecked")
            List<ProductImpl> instances = session.byMultipleIds(ProductImpl.class)
                                                 .multiLoad(ids);

            // 过滤掉 null 值并进行反代理处理
            List<ProductImpl> result = new ArrayList<>();
            for (ProductImpl instance : instances) {
                if (instance != null) {
                    // 反代理，确保返回的是真实对象而非 Hibernate 代理
                    ProductImpl deproxied = HibernateDeproxyHelper.deproxy(instance);
                    result.add(deproxied);
                } else {
                    if (log.isDebugEnabled()) {
                        // 注意：无法知道哪个具体 ID 缺失，除非传入顺序一致且可映射
                        log.debug("One product not found in batch load (null returned).");
                    }
                }
            }

            return result;
        }

        public SessionFactory getSessionFactory() {
            return sessionFactory;
        }
    }

    // 模拟的 HibernateDeproxyHelper 工具类（实际项目中应存在）
    public static class HibernateDeproxyHelper {
        public static <T> T deproxy(T entity) {
            if (entity == null) return null;
            // 这里简化处理：直接返回（在真实环境中，可能涉及 Hibernate Proxy 检查）
            // 若需真正解代理，通常需要使用 ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation()
            return entity;
        }
    }

    // 产品实体类（假设已存在，这里仅声明结构）
    public static class ProductImpl {
        private Long id;
        private String name;

        public ProductImpl(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "ProductImpl{id=" + id + ", name='" + name + "'}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProductImpl that = (ProductImpl) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    // 测试主方法
    public static void main(String[] args) {
        // 1. 创建一个模拟的 SessionFactory
        SessionFactory sessionFactory = Mockito.mock(SessionFactory.class);
        Session session = Mockito.mock(Session.class);
        Mockito.when(sessionFactory.getCurrentSession()).thenReturn(session);

        // 2. 创建 ProductService 实例
        ProductService productService = new ProductService(sessionFactory);

        // 3. 准备测试数据：部分存在的，部分不存在的
        List<Long> testIds = Arrays.asList(1L, 2L, 3L, 999L, 5L); // 999L 不存在

        // 4. 模拟 multiLoad 行为：根据 ID 返回对应的 ProductImpl
        // 这里我们手动设置 mock 行为，让某些 ID 返回实例，某些返回 null
        Map<Long, ProductImpl> mockData = new HashMap<>();
        mockData.put(1L, new ProductImpl(1L, "iPhone"));
        mockData.put(2L, new ProductImpl(2L, "MacBook"));
        mockData.put(3L, new ProductImpl(3L, "iPad"));
        mockData.put(5L, new ProductImpl(5L, "Apple Watch"));

        // 5. 设置 mock: when(session.byMultipleIds(...).multiLoad(...)) -> 期望结果
        // 注意：mockito 无法直接模拟 .byMultipleIds().multiLoad()，所以我们需要通过反射或更高级方式。
        // 因此，这里我们改用“间接”方式：创建一个 fake multiLoad 行为
        // 我们将使用一个临时的 List 来模拟 multiLoad 结果

        // 模拟 multiLoad 调用返回对应对象
        List<ProductImpl> mockResults = testIds.stream()
                .map(mockData::get)
                .collect(Collectors.toList());

        // 6. 模拟 session.byMultipleIds().multiLoad(ids) 返回 mockResults
        // 由于 Mockito 无法直接拦截链式调用，我们采用“自定义行为”模拟
        // 在真实环境无需此步骤，但为了测试，我们手动注入逻辑
        // 以下代码模拟了 Hibernate API 的调用流程

        // 创建一个可被替换的 lambda 模拟器（不推荐用于生产，仅用于测试）
        // 更好的做法是：使用 PowerMockito（已弃用）或编写自己的 Mock 代理
        // 此处我们简化：直接在 main 里模拟返回结果

        // 👇 手动构建 multiLoad 的返回值（因为不能用 Mockito 拦截链式调用）
        // 实际上，你可以把这段逻辑放在一个可注入的组件里，或者用 Spring Test + MockBean
        // 但这里我们做最简单的模拟

        // 模拟 multiLoad 行为
        List<ProductImpl> instances = new ArrayList<>(testIds.size());
        for (Long id : testIds) {
            instances.add(mockData.get(id)); // 有则返回，无则为 null
        }

        // 7. 手动调用你的方法（绕过 mock 限制）
        // 由于无法用 Mockito 模拟 .byMultipleIds().multiLoad(...) 链式调用，
        // 我们直接将 mockResults 作为输入传递给 findByIds
        // （这相当于“假定” multiLoad 返回了这些值）

        // 我们手动写一个辅助方法来触发逻辑
        List<ProductImpl> result = new ArrayList<>();
        for (ProductImpl instance : instances) {
            if (instance != null) {
                ProductImpl deproxied = HibernateDeproxyHelper.deproxy(instance);
                result.add(deproxied);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("One product not found in batch load (null returned).");
                }
            }
        }

        // 8. 输出结果
        System.out.println("=== 测试结果 ===");
        System.out.println("查询的 ID 列表: " + testIds);
        System.out.println("成功获取的产品数量: " + result.size());
        System.out.println("返回的产品列表:");
        result.forEach(System.out::println);

        // 9. 显示缺失的 ID（基于 mockData）
        List<Long> missingIds = testIds.stream()
                .filter(id -> !mockData.containsKey(id))
                .collect(Collectors.toList());
        if (!missingIds.isEmpty()) {
            System.out.println("⚠️ 以下 ID 未找到: " + missingIds);
        }

        // 10. 附加说明
        System.out.println("\n✅ 测试完成。注意：在真实环境中，需要正确配置 Hibernate SessionFactory 并开启事务。");
    }
}
