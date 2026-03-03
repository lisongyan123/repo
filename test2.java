import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ProductServiceImplTest {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImplTest.class);

    // 模拟的 SessionFactory（使用 Mockito 伪实现）
    private final SessionFactory sessionFactory = new MockSessionFactory();

    public static void main(String[] args) {
        ProductServiceImplTest test = new ProductServiceImplTest();
        test.runTest();
    }

    public void runTest() {
        // 测试用的 ID 列表（包含存在的和不存在的）
        List<Long> ids = Arrays.asList(1L, 2L, 3L, 5L, 7L);

        System.out.println("=== 正在调用 findbyIds(...) ===");

        // 执行目标方法
        List<ProductImpl> result = findbyIds(ids);

        // 输出结果
        System.out.println("\n=== 查询结果 ===");
        for (int i = 0; i < result.size(); i++) {
            Long id = ids.get(i);
            ProductImpl product = result.get(i);
            if (product == null) {
                System.out.printf("ID: %d -> 未找到（返回 null）\n", id);
            } else {
                System.out.printf("ID: %d -> Name: %s\n", id, product.getName());
            }
        }

        System.out.println("\n=== 测试完成 ===");
    }

    // 目标方法（直接复制你的代码）
    public List<ProductImpl> findbyIds(final Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        String hql = "FROM ProductImpl p WHERE p.prodNum IN (:ids)";
        List<ProductImpl> instances = sessionFactory.getCurrentSession()
                .createQuery(hql, ProductImpl.class)
                .setParameter("ids", ids)
                .getResultList();

        Map<Long, ProductImpl> instanceMap = instances.stream()
                .collect(Collectors.toMap(ProductImpl::getProdNum, instance -> instance));

        List<ProductImpl> result = new ArrayList<>(ids.size());
        for (Long id : ids) {
            ProductImpl instance = instanceMap.get(id);
            if (instance == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Product not found for ID: {}", id);
                }
                result.add(null);
            } else {
                result.add(deproxy(instance));
            }
        }

        return result;
    }

    // 模拟 deproxy，这里简单返回原对象
    public static <T> T deproxy(T entity) {
        return entity;
    }

    // ==================== 模拟的 SessionFactory ====================
    private static class MockSessionFactory implements SessionFactory {
        private final Session session = new MockSession();

        @Override
        public Session getCurrentSession() {
            return session;
        }

        // 其他方法忽略，只实现需要的
        @Override
        public void close() {}

        @Override
        public boolean isClosed() { return false; }

        // 可以根据需要添加更多方法...
    }

    // ==================== 模拟的 Session ====================
    private static class MockSession implements Session {
        private final Transaction transaction = new MockTransaction();

        @Override
        public Query<?> createQuery(String s, Class<?> aClass) {
            return new MockQuery<>(s, aClass);
        }

        @Override
        public Transaction getTransaction() {
            return transaction;
        }

        @Override
        public void close() {}

        @Override
        public boolean isOpen() { return true; }

        // 其他方法忽略...
    }

    // ==================== 模拟的 Query ====================
    private static class MockQuery<T> implements Query<T> {
        private final String hql;
        private final Class<T> resultType;
        private Object parameterValue;

        public MockQuery(String hql, Class<T> resultType) {
            this.hql = hql;
            this.resultType = resultType;
        }

        @Override
        public Query<T> setParameter(String name, Object value) {
            this.parameterValue = value;
            return this;
        }

        @Override
        public List<T> getResultList() {
            // 根据参数值模拟查询结果
            List<Long> ids = (List<Long>) parameterValue;
            List<ProductImpl> results = new ArrayList<>();

            // 模拟数据库中只有 1, 2, 3 存在
            Set<Long> existingIds = Set.of(1L, 2L, 3L);

            for (Long id : ids) {
                if (existingIds.contains(id)) {
                    ProductImpl p = new ProductImpl();
                    p.setProdNum(id);
                    p.setName("Product-" + id);
                    results.add(p);
                }
            }

            return (List<T>) results;
        }

        // 其他方法忽略...
        @Override
        public Query<T> setParameter(int position, Object value) { return this; }
        @Override
        public Query<T> setMaxResults(int maxResult) { return this; }
        @Override
        public T getSingleResult() { return null; }
        @Override
        public long executeUpdate() { return 0; }
        @Override
        public Query<T> setFirstResult(int firstResult) { return this; }
        @Override
        public Query<T> setFlushMode(FlushModeType flushMode) { return this; }
        @Override
        public Query<T> setHint(String hintName, Object value) { return this; }
        @Override
        public void setLockMode(LockModeType lockMode) {}
        @Override
        public void setLockMode(String alias, LockModeType lockMode) {}
        @Override
        public Query<T> setParameter(String name, Object value, Class<?> type) { return this; }
        @Override
        public Query<T> setParameter(int position, Object value, Class<?> type) { return this; }
        @Override
        public Query<T> setParameter(String name, Object value, Type type) { return this; }
        @Override
        public Query<T> setParameter(int position, Object value, Type type) { return this; }
        @Override
        public Query<T> setParameterList(String name, Collection<?> values) { return this; }
        @Override
        public Query<T> setParameterList(String name, Object[] values) { return this; }
        @Override
        public Query<T> setParameterList(int position, Collection<?> values) { return this; }
        @Override
        public Query<T> setParameterList(int position, Object[] values) { return this; }
        @Override
        public Query<T> setComment(String comment) { return this; }
        @Override
        public Query<T> setCacheable(boolean cacheable) { return this; }
        @Override
        public Query<T> setCacheRegion(String cacheRegion) { return this; }
        @Override
        public Query<T> setFetchSize(int fetchSize) { return this; }
        @Override
        public Query<T> setTimeout(int timeout) { return this; }
        @Override
        public Query<T> setReadOnly(boolean readOnly) { return this; }
        @Override
        public Query<T> setFlushMode(FlushModeType flushMode, boolean override) { return this; }
        @Override
        public Query<T> setParameter(String name, Object value, Type type, Class<?> clazz) { return this; }
        @Override
        public Query<T> setParameter(int position, Object value, Type type, Class<?> clazz) { return this; }
        @Override
        public Query<T> setParameterList(String name, Collection<?> values, Type type) { return this; }
        @Override
        public Query<T> setParameterList(String name, Object[] values, Type type) { return this; }
        @Override
        public Query<T> setParameterList(int position, Collection<?> values, Type type) { return this; }
        @Override
        public Query<T> setParameterList(int position, Object[] values, Type type) { return this; }
    }

    // ==================== 模拟的 Transaction ====================
    private static class MockTransaction implements Transaction {
        @Override
        public void begin() {}
        @Override
        public void commit() {}
        @Override
        public void rollback() {}
        @Override
        public void setRollbackOnly() {}
        @Override
        public boolean getRollbackOnly() { return false; }
        @Override
        public void setTimeout(int seconds) {}
        @Override
        public boolean wasCommitted() { return false; }
        @Override
        public boolean wasRolledBack() { return false; }
    }

    // ==================== 测试实体类 ====================
    public static class ProductImpl {
        private Long prodNum;
        private String name;
        private Double price;

        public Long getProdNum() { return prodNum; }
        public void setProdNum(Long prodNum) { this.prodNum = prodNum; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }

        @Override
        public String toString() {
            return "ProductImpl{" +
                    "prodNum=" + prodNum +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
