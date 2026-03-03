import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 简化测试类：演示 findbyIds 方法并用 main 测试
 */
public class ProductDaoTest {

    // 产品实体类（简化）
    public static class ProductImpl {
        private Long prodNum;
        private String name;

        public ProductImpl() {}

        public ProductImpl(Long prodNum, String name) {
            this.prodNum = prodNum;
            this.name = name;
        }

        public Long getProdNum() { return prodNum; }
        public void setProdNum(Long prodNum) { this.prodNum = prodNum; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        @Override
        public String toString() {
            return "ProductImpl{" +
                    "prodNum=" + prodNum +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    // 静态方法：反代理（简化为直接返回）
    public static <T> T deproxy(T entity) {
        return entity;
    }

    // 模拟 DAO 方法
    public static List<ProductImpl> findbyIds(SessionFactory sessionFactory, Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        Session session = sessionFactory.getCurrentSession();
        String hql = "FROM ProductImpl p WHERE p.prodNum IN (:ids)";
        Query<ProductImpl> query = session.createQuery(hql, ProductImpl.class);
        query.setParameter("ids", ids);
        List<ProductImpl> instances = query.getResultList();

        Map<Long, ProductImpl> instanceMap = instances.stream()
                .collect(Collectors.toMap(ProductImpl::getProdNum, p -> p));

        List<ProductImpl> result = new ArrayList<>(ids.size());
        for (Long id : ids) {
            ProductImpl instance = instanceMap.get(id);
            if (instance == null) {
                System.out.println("未找到 ID: " + id);
                result.add(null);
            } else {
                result.add(deproxy(instance));
            }
        }
        return result;
    }

    // 主函数：测试入口
    public static void main(String[] args) {
        // 创建 SessionFactory（需 hibernate.cfg.xml 在 classpath 中）
        Configuration cfg = new Configuration().configure();
        try (SessionFactory sf = cfg.buildSessionFactory()) {

            // 开启事务并插入测试数据
            Session session = sf.getCurrentSession();
            session.beginTransaction();

            // 清空旧数据避免重复
            session.createQuery("DELETE FROM ProductImpl").executeUpdate();

            // 插入测试数据
            session.save(new ProductImpl(1L, "苹果手机"));
            session.save(new ProductImpl(2L, "三星手机"));
            session.save(new ProductImpl(4L, "谷歌手机"));
            // 注意：不插入 ID=3 的数据

            session.getTransaction().commit();

            // 测试 findbyIds
            List<Long> idsToFind = Arrays.asList(1L, 2L, 3L, 4L);
            System.out.println("正在查询 IDs: " + idsToFind);

            List<ProductImpl> results = findbyIds(sf, idsToFind);

            // 输出结果（保持顺序）
            for (int i = 0; i < idsToFind.size(); i++) {
                Long id = idsToFind.get(i);
                ProductImpl p = results.get(i);
                System.out.println("ID=" + id + " -> " + (p != null ? p : "null"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
