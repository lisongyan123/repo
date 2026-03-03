import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// 假设你的 findByIds 方法在 ProductRepository 类中
class ProductRepository {

    private static final Logger log = LoggerFactory.getLogger(ProductRepository.class);

    private SessionFactory sessionFactory;

    // 构造函数（测试中可简化为注入 mock 或真实 session factory）
    public ProductRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

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
}

// 假设这是你的 ProductImpl 实体（你已存在，这里仅用于编译）
class ProductImpl {
    private Long id;
    private String name;

    public ProductImpl() {}

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
}

// 简化的 HibernateDeproxyHelper（用于测试，实际应由你项目提供）
// 如果你项目中有真实实现，可忽略此部分；这里仅用于编译通过
class HibernateDeproxyHelper {
    public static <T> T deproxy(T proxy) {
        if (proxy == null) {
            return null;
        }
        // 简单模拟：如果是 Hibernate 代理，返回其目标；否则直接返回
        if (Hibernate.isInitialized(proxy) && Hibernate.getClass(proxy) != proxy.getClass()) {
            // 模拟反代理：返回一个真实对象副本（实际应通过 Hibernate.getUnproxiedInstance）
            // 为简化测试，我们假设它能正确返回原始对象
            return proxy; // 实际中应使用 Hibernate.getUnproxiedInstance(proxy)
        }
        return proxy;
    }
}

// ======================= 主测试类 =======================
public class ProductRepositoryTest {

    public static void main(String[] args) {
        // ⚠️ 注意：实际项目中你需要配置 Hibernate SessionFactory
        // 这里为了演示，我们创建一个“假”的 SessionFactory，仅用于编译通过
        // 在真实测试中，你应该使用 Spring Test + @DataJpaTest 或 H2 内存数据库 + 真实 SessionFactory

        // 模拟一个简单的 SessionFactory（实际项目中由 Spring 或 Hibernate 配置提供）
        SessionFactory sessionFactory = new SessionFactory() {
            @Override
            public Session getCurrentSession() {
                // 模拟一个当前 Session（真实环境中由 Hibernate 管理）
                // 这里只是让代码能跑通，实际测试应使用真实环境
                return new Session() {
                    @Override
                    public <T> MultiIdentifierLoadAccess<T> byMultipleIds(Class<T> entityClass) {
                        return new MultiIdentifierLoadAccess<T>() {
                            @Override
                            public List<T> multiLoad(Collection<?> ids) {
                                // 模拟数据库查询结果
                                List<T> results = new ArrayList<>();
                                for (Object id : ids) {
                                    Long productId = (Long) id;
                                    if (productId == 1L) {
                                        results.add((T) new ProductImpl(1L, "Product One"));
                                    } else if (productId == 2L) {
                                        results.add((T) new ProductImpl(2L, "Product Two"));
                                    } else if (productId == 5L) {
                                        results.add((T) new ProductImpl(5L, "Product Five"));
                                    } else {
                                        // 模拟数据库中不存在的 ID，返回 null
                                        results.add(null);
                                    }
                                }
                                return results;
                            }
                        };
                    }

                    // 其他方法留空（仅用于编译）
                    @Override
                    public void close() {}
                    @Override
                    public boolean isOpen() { return true; }
                    @Override
                    public boolean isConnected() { return true; }
                    // ... 其他未实现方法可留空，或抛出 UnsupportedOperationException
                };
            }

            @Override
            public Session openSession() { return null; }
            @Override
            public void close() {}
            @Override
            public boolean isClosed() { return false; }
            @Override
            public Statistics getStatistics() { return null; }
        };

        // 创建仓库实例
        ProductRepository repo = new ProductRepository(sessionFactory);

        // 测试数据：包含存在的和不存在的 ID
        List<Long> testIds = Arrays.asList(1L, 2L, 3L, 5L, 999L); // 3L 和 999L 不存在

        System.out.println("=== 测试 findByIds 方法 ===");
        System.out.println("输入的 ID 列表: " + testIds);

        // 调用方法
        List<ProductImpl> result = repo.findByIds(testIds);

        // 输出结果
        System.out.println("返回的 Product 实例数量: " + result.size());
        System.out.println("返回的 Product 实例:");
        for (ProductImpl p : result) {
            System.out.println("  - " + p);
        }

        // 验证：应该只返回 id=1, 2, 5 的三个对象
        assert result.size() == 3 : "应返回 3 个有效对象";
        assert result.stream().anyMatch(p -> p.getId().equals(1L)) : "缺少 ID=1 的产品";
        assert result.stream().anyMatch(p -> p.getId().equals(2L)) : "缺少 ID=2 的产品";
        assert result.stream().anyMatch(p -> p.getId().equals(5L)) : "缺少 ID=5 的产品";

        System.out.println("\n✅ 测试通过！");
    }
}
