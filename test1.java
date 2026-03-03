import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.*;

public class ProductDaoTest {

    // 模拟 getSessionFactory() 方法（仅用于测试）
    private SessionFactory sessionFactory;

    public ProductDaoTest() {
        // 初始化 Hibernate SessionFactory（请确保你有 hibernate.cfg.xml 或等效配置）
        this.sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    // 你的 findByIds 方法（稍作调整以适配当前类）
    public List<ProductImpl> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            System.out.println("Empty or null ids collection provided to findByIds.");
            return Collections.emptyList();
        }

        Session session = this.getSessionFactory().getCurrentSession();

        session.beginTransaction(); // 开启事务（Hibernate 要求）

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

        session.getTransaction().commit(); // 提交事务
        return result;
    }

    // 主函数：测试 findByIds
    public static void main(String[] args) {
        ProductDaoTest dao = new ProductDaoTest();

        // 假设数据库中有 ID 为 1L, 2L, 3L 的 ProductImpl 记录
        Collection<Long> testIds = Arrays.asList(1L, 2L, 999L); // 999L 故意设为不存在

        List<ProductImpl> products = dao.findByIds(testIds);

        System.out.println("Found " + products.size() + " products:");
        for (ProductImpl p : products) {
            System.out.println(" - ID: " + p.getId() + ", Class: " + p.getClass().getSimpleName());
        }

        // 关闭 SessionFactory（可选，程序结束前）
        dao.getSessionFactory().close();
    }
}
