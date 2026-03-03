import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductImplTest {

    // 模拟的 ProductImpl 实体类（简化版）
    static class ProductImpl {
        private Long prodNum;
        private String name;

        public ProductImpl(Long prodNum, String name) {
            this.prodNum = prodNum;
            this.name = name;
        }

        public Long getProdNum() {
            return prodNum;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "ProductImpl{prodNum=" + prodNum + ", name='" + name + "'}";
        }
    }

    // 模拟的反代理工具类（你的真实代码中可能有）
    static class HibernateDeproxyHelper {
        public static ProductImpl deproxy(ProductImpl proxy) {
            // 实际中可能返回解代理后的实体，这里简单返回原对象模拟
            return proxy;
        }
    }

    // 被测试的类
    static class ProductRepository {
        @Mock
        private SessionFactory sessionFactory;

        private Session session;
        private Query<ProductImpl> query;

        public ProductRepository() {
            this.session = mock(Session.class);
            this.query = mock(Query.class);
        }

        public SessionFactory getSessionFactory() {
            return sessionFactory;
        }

        public List<ProductImpl> findbyIds(final Collection<Long> ids) {
            if (ids == null || ids.isEmpty()) {
                return Collections.emptyList();
            }

            String hql = "FROM ProductImpl p WHERE p.prodNum IN (:ids)";
            List<ProductImpl> instances = this.getSessionFactory()
                    .getCurrentSession()
                    .createQuery(hql, ProductImpl.class)
                    .setParameter("ids", ids)
                    .getResultList();

            Map<Long, ProductImpl> instanceMap = instances.stream()
                    .collect(Collectors.toMap(ProductImpl::getProdNum, instance -> instance));

            List<ProductImpl> result = new ArrayList<>(ids.size());
            for (Long id : ids) {
                ProductImpl instance = instanceMap.get(id);
                if (instance == null) {
                    result.add(null);
                } else {
                    result.add(HibernateDeproxyHelper.deproxy(instance));
                }
            }

            return result;
        }
    }

    // ========== 测试入口 ==========
    public static void main(String[] args) {
        System.out.println("=== 测试 findbyIds 方法 ===\n");

        // 创建模拟对象
        ProductRepository repo = new ProductRepository();
        SessionFactory sessionFactory = mock(SessionFactory.class);
        Session session = mock(Session.class);
        Query<ProductImpl> query = mock(Query.class);

        // 设置模拟行为
        when(sessionFactory.getCurrentSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(ProductImpl.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenAnswer(invocation -> {
            Collection<Long> ids = invocation.getArgument(1);
            List<ProductImpl> mockData = new ArrayList<>();
            if (ids.contains(1L)) mockData.add(new ProductImpl(1L, "Product A"));
            if (ids.contains(3L)) mockData.add(new ProductImpl(3L, "Product C"));
            if (ids.contains(5L)) mockData.add(new ProductImpl(5L, "Product E"));
            return mockData;
        });

        // 注入模拟的 SessionFactory
        repo.sessionFactory = sessionFactory;

        // === 测试用例 1: 正常情况，部分存在 ===
        System.out.println("✅ 测试 1: 输入 [1, 2, 3, 4, 5]，期望返回 [Product1, null, Product3, null, Product5]");
        List<Long> ids1 = Arrays.asList(1L, 2L, 3L, 4L, 5L);
        List<ProductImpl> result1 = repo.findbyIds(ids1);
        System.out.println("结果: " + result1);
        assertEquals(5, result1.size());
        assertNotNull(result1.get(0)); // 1L 存在
        assertNull(result1.get(1));   // 2L 不存在
        assertNotNull(result1.get(2)); // 3L 存在
        assertNull(result1.get(3));   // 4L 不存在
        assertNotNull(result1.get(4)); // 5L 存在
        assertEquals("Product A", result1.get(0).getName());
        assertEquals("Product C", result1.get(2).getName());
        assertEquals("Product E", result1.get(4).getName());
        System.out.println("✅ 测试 1 通过！\n");

        // === 测试用例 2: 空集合 ===
        System.out.println("✅ 测试 2: 输入空集合，期望返回空列表");
        List<ProductImpl> result2 = repo.findbyIds(Collections.emptyList());
        assertTrue(result2.isEmpty());
        System.out.println("结果: " + result2);
        System.out.println("✅ 测试 2 通过！\n");

        // === 测试用例 3: null 输入 ===
        System.out.println("✅ 测试 3: 输入 null，期望返回空列表");
        List<ProductImpl> result3 = repo.findbyIds(null);
        assertTrue(result3.isEmpty());
        System.out.println("结果: " + result3);
        System.out.println("✅ 测试 3 通过！\n");

        // === 测试用例 4: 全部不存在 ===
        System.out.println("✅ 测试 4: 输入 [99, 100]，全部不存在，期望返回 [null, null]");
        List<ProductImpl> result4 = repo.findbyIds(Arrays.asList(99L, 100L));
        assertEquals(2, result4.size());
        assertNull(result4.get(0));
        assertNull(result4.get(1));
        System.out.println("结果: " + result4);
        System.out.println("✅ 测试 4 通过！\n");

        // === 测试用例 5: 顺序保持 ===
        System.out.println("✅ 测试 5: 输入 [5, 1, 3]，顺序必须保持");
        List<ProductImpl> result5 = repo.findbyIds(Arrays.asList(5L, 1L, 3L));
        assertEquals("Product E", result5.get(0).getName());
        assertEquals("Product A", result5.get(1).getName());
        assertEquals("Product C", result5.get(2).getName());
        System.out.println("结果: " + result5);
        System.out.println("✅ 测试 5 通过！\n");

        System.out.println("\n🎉 所有测试通过！");
    }
}
