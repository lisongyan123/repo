import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ProductFinderTest {

    private static final Logger log = LoggerFactory.getLogger(ProductFinderTest.class);

    // 模拟 DAO 类，包含要测试的 findByIds 方法
    public static class ProductFinder {

        private SessionFactory sessionFactory;

        public ProductFinder(SessionFactory sessionFactory) {
            this.sessionFactory = sessionFactory;
        }

        public SessionFactory getSessionFactory() {
            return sessionFactory;
        }

        public Session getSessionFactory() {
            return sessionFactory.getCurrentSession();
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
                        log.debug("One product not found in batch load (null returned).");
                    }
                }
            }

            return result;
        }
    }

    // 假设 HibernateDeproxyHelper 已存在，这里简单模拟其实现
    public static class HibernateDeproxyHelper {
        @SuppressWarnings("unchecked")
        public static <T> T deproxy(T entity) {
            if (entity == null) return null;
            // 简单模拟：实际中可能使用 Hibernate.isInitialized() 或反射解代理
            // 这里假设对象已经是真实实例，不做额外处理
            return entity;
        }
    }

    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        // 注意：实际使用中需配置 hibernate.cfg.xml 或编程式配置
        // 这里仅演示逻辑，假设配置已正确加载
        configuration.configure(); // 会加载 hibernate.cfg.xml

        SessionFactory sessionFactory = null;
        try {
            sessionFactory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Failed to create SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }

        ProductFinder finder = new ProductFinder(sessionFactory);

        // 测试用例1: 正常 ID 列表
        List<Long> validIds = Arrays.asList(1L, 2L, 3L);
        System.out.println("=== 测试批量查询 IDs: " + validIds + " ===");
        List<ProductImpl> results1 = finder.findByIds(validIds);
        System.out.println("查询结果数量: " + results1.size());
        for (ProductImpl p : results1) {
            System.out.println("Product: id=" + p.getId() + ", name=" + p.getName());
        }

        // 测试用例2: 包含不存在的 ID
        List<Long> mixedIds = Arrays.asList(1L, 999L, 2L, 9999L);
        System.out.println("\n=== 测试混合 IDs (含不存在): " + mixedIds + " ===");
        List<ProductImpl> results2 = finder.findByIds(mixedIds);
        System.out.println("查询结果数量: " + results2.size());
        for (ProductImpl p : results2) {
            System.out.println("Product: id=" + p.getId() + ", name=" + p.getName());
        }

        // 测试用例3: 空集合
        Collection<Long> emptyIds = Collections.emptyList();
        System.out.println("\n=== 测试空 ID 集合 ===");
        List<ProductImpl> results3 = finder.findByIds(emptyIds);
        System.out.println("空集合查询结果数量: " + results3.size());

        // 测试用例4: null 输入
        System.out.println("\n=== 测试 null ID 集合 ===");
        List<ProductImpl> results4 = finder.findByIds(null);
        System.out.println("null 查询结果数量: " + results4.size());

        // 关闭资源
        sessionFactory.close();
    }
}
