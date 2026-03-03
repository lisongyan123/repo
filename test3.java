import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.metamodel.model.domain.spi.EntityTypeDescriptor;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.resource.transaction.spi.TransactionCoordinator;
import org.mockito.Mockito;

import java.io.Serializable;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class ProductDAOTest {

    // 假设这是你的 DAO 类
    static class ProductDAO {
        private SessionFactory sessionFactory;

        public ProductDAO(SessionFactory sessionFactory) {
            this.sessionFactory = sessionFactory;
        }

        protected SessionFactory getSessionFactory() {
            return sessionFactory;
        }

        public List<ProductImpl> findByIds(Collection<Long> ids) {
            if (ids == null || ids.isEmpty()) {
                System.out.println("Empty or null ids collection provided to findByIds.");
                return Collections.emptyList();
            }

            Session session = this.getSessionFactory().getCurrentSession();

            @SuppressWarnings("unchecked")
            List<ProductImpl> instances = session.byMultipleIds(ProductImpl.class)
                                                 .multiLoad(ids);

            List<ProductImpl> result = new ArrayList<>();
            for (ProductImpl instance : instances) {
                if (instance != null) {
                    ProductImpl deproxied = HibernateDeproxyHelper.deproxy(instance);
                    result.add(deproxied);
                } else {
                    System.out.println("One product not found in batch load (null returned).");
                }
            }

            return result;
        }
    }

    // 简化用的辅助类（模拟反代理）
    static class HibernateDeproxyHelper {
        @SuppressWarnings("unchecked")
        public static <T> T deproxy(T entity) {
            if (entity == null) return null;
            // 实际中可能使用 Hibernate.isInitialized() 或 AOP 判断是否代理
            // 这里简化处理：直接返回原对象
            return entity;
        }
    }

    // --- 主函数测试 ---
    public static void main(String[] args) {
        // 1. 创建 mock SessionFactory 和 Session
        SessionFactory sessionFactory = Mockito.mock(SessionFactory.class);
        Session session = Mockito.mock(Session.class);
        TransactionCoordinator tc = Mockito.mock(TransactionCoordinator.class);

        when(sessionFactory.getCurrentSession()).thenReturn(session);
        when(session.getTransactionCoordinator()).thenReturn(tc);

        // 2. Mock byMultipleIds(...) 行为
        @SuppressWarnings("unchecked")
        org.hibernate.query.MultipleIdentifierLoadAccess<ProductImpl> loadAccess =
                Mockito.mock(org.hibernate.query.MultipleIdentifierLoadAccess.class);

        when(session.byMultipleIds(eq(ProductImpl.class))).thenReturn(loadAccess);

        // --- 测试用例 1: 正常批量查找，部分命中 ---

        List<Long> testIds = Arrays.asList(1L, 2L, 3L, 4L);

        // 模拟数据库只返回 id=1 和 id=3 的对象
        Map<Long, ProductImpl> dbData = new HashMap<>();
        dbData.put(1L, createMockProduct(1L, "Product 1"));
        dbData.put(3L, createMockProduct(3L, "Product 3"));

        List<ProductImpl> expectedLoaded = new ArrayList<>();
        for (Long id : testIds) {
            expectedLoaded.add(dbData.get(id)); // 可能为 null
        }

        when(loadAccess.multiLoad(any(Collection.class))).thenReturn(expectedLoaded);

        // 创建 DAO 并执行测试
        ProductDAO dao = new ProductDAO(sessionFactory);
        List<ProductImpl> results = dao.findByIds(testIds);

        System.out.println("\n✅ Test Case 1 - Partial Load:");
        System.out.println("Requested IDs: " + testIds);
        System.out.println("Found " + results.size() + " products:");
        for (ProductImpl p : results) {
            System.out.println("  -> " + p.getName() + " (ID: " + p.getId() + ")");
        }

        // --- 测试用例 2: 空集合 ---
        System.out.println("\n---");
        List<ProductImpl> emptyResult = dao.findByIds(new HashSet<>());
        System.out.println("Empty input test: returned list size = " + emptyResult.size());

        // --- 测试用例 3: null 输入 ---
        System.out.println("\n---");
        List<ProductImpl> nullResult = dao.findByIds(null);
        System.out.println("Null input test: returned list size = " + nullResult.size());
    }

    // 辅助方法：创建一个模拟的 ProductImpl 对象
    private static ProductImpl createMockProduct(Long id, String name) {
        ProductImpl mock = Mockito.mock(ProductImpl.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        // 其他属性也可以 mock
        return mock;
    }
}
