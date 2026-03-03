import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.*;
import java.util.stream.Collectors;

public class FindByIdsTest {

    // 模拟 ProductImpl 实体
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

    // 模拟 HibernateDeproxyHelper
    static class HibernateDeproxyHelper {
        @SuppressWarnings("unchecked")
        public static <T> T deproxy(T entity) {
            // 简化：直接返回，实际中可能处理代理对象
            return entity;
        }
    }

    // 模拟的 DAO 方法（内联实现）
    public static List<ProductImpl> findbyIds(final Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        // ===== 模拟数据库查询 =====
        // 假设数据库中有这些产品（模拟真实表数据）
        Map<Long, ProductImpl> database = new HashMap<>();
        database.put(1L, new ProductImpl(1L, "Apple"));
        database.put(2L, new ProductImpl(2L, "Banana"));
        database.put(3L, new ProductImpl(3L, "Cherry"));
        database.put(5L, new ProductImpl(5L, "Elderberry"));

        // 模拟 HQL 查询结果：只返回存在的 ID
        List<ProductImpl> instances = ids.stream()
                .filter(database::containsKey)
                .map(database::get)
                .collect(Collectors.toList());
        // =========================

        Map<Long, ProductImpl> instanceMap = instances.stream()
                .collect(Collectors.toMap(ProductImpl::getProdNum, instance -> instance));

        List<ProductImpl> result = new ArrayList<>(ids.size());
        for (Long id : ids) {
            ProductImpl instance = instanceMap.get(id);
            if (instance == null) {
                System.out.println("DEBUG: Product not found for ID: " + id);
                result.add(null);
            } else {
                result.add(HibernateDeproxyHelper.deproxy(instance));
            }
        }

        return result;
    }

    // 主函数：测试
    public static void main(String[] args) {
        // 测试输入：包含存在和不存在的 ID，并注意顺序
        List<Long> inputIds = Arrays.asList(1L, 999L, 3L, 2L, 888L);

        System.out.println("Input IDs: " + inputIds);
        List<ProductImpl> results = findbyIds(inputIds);

        System.out.println("\nResults (in input order):");
        for (int i = 0; i < inputIds.size(); i++) {
            Long id = inputIds.get(i);
            ProductImpl product = results.get(i);
            if (product != null) {
                System.out.println("  ID " + id + " -> " + product);
            } else {
                System.out.println("  ID " + id + " -> NOT FOUND (null)");
            }
        }
    }
}
