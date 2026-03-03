import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

// 假设的实体类
class ProductImpl {
    private Long prodNum;
    private String name;

    public ProductImpl() {}

    public ProductImpl(Long prodNum, String name) {
        this.prodNum = prodNum;
        this.name = name;
    }

    public Long getProdNum() {
        return prodNum;
    }

    public void setProdNum(Long prodNum) {
        this.prodNum = prodNum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ProductImpl{" +
                "prodNum=" + prodNum +
                ", name='" + name + '\'' +
                '}';
    }
}

// 假设的工具类
class HibernateDeproxyHelper {
    public static <T> T deproxy(T entity) {
        // 简单返回实体，实际中可能涉及反代理逻辑
        return entity;
    }
}

// 包含 findbyIds 方法的 DAO 类
class ProductDao {
    private static final Logger log = LoggerFactory.getLogger(ProductDao.class);
    private SessionFactory sessionFactory;

    public ProductDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Override
    public List<ProductImpl> findbyIds(final Collection<Long> ids) {
        // 1. 检查输入参数是否为空
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 使用 Hibernate Session 批量查询
        String hql = "FROM ProductImpl p WHERE p.prodNum IN (:ids)";
        List<ProductImpl> instances = this.getSessionFactory()
                .getCurrentSession()
                .createQuery(hql, ProductImpl.class)
                .setParameter("ids", ids)
                .getResultList();

        // 3. 将查询结果转换为 Map
        Map<Long, ProductImpl> instanceMap = instances.stream()
                .collect(Collectors.toMap(ProductImpl::getProdNum, instance -> instance));

        // 4. 构建结果列表，保持顺序
        List<ProductImpl> result = new ArrayList<>(ids.size());
        for (Long id : ids) {
            ProductImpl instance = instanceMap.get(id);
            if (instance == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Product not found for ID: {}", id);
                }
                result.add(null);
            } else {
                result.add(HibernateDeproxyHelper.deproxy(instance));
            }
        }

        return result;
    }
}

// 测试类
public class ProductDaoTest {
    public static void main(String[] args) {
        // 1. 创建模拟的 SessionFactory（实际中应从 Spring 或 Hibernate 配置获取）
        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();

        // 2. 创建 ProductDao 实例
        ProductDao dao = new ProductDao(sessionFactory);

        // 3. 准备测试数据
        Collection<Long> testIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);

        // 4. 调用方法并获取结果
        List<ProductImpl> results = dao.findbyIds(testIds);

        // 5. 输出结果
        System.out.println("查询结果：");
        for (int i = 0; i < testIds.size(); i++) {
            Long id = testIds.iterator().next();
            ProductImpl product = results.get(i);
            if (product != null) {
                System.out.println("ID: " + id + " -> " + product);
            } else {
                System.out.println("ID: " + id + " -> 未找到");
            }
        }

        // 6. 测试空输入
        System.out.println("\n测试空输入：");
        List<ProductImpl> emptyResult = dao.findbyIds(Collections.emptyList());
        System.out.println("空输入返回大小: " + emptyResult.size());

        // 7. 测试 null 输入
        System.out.println("\n测试 null 输入：");
        List<ProductImpl> nullResult = dao.findbyIds(null);
        System.out.println("null 输入返回大小: " + nullResult.size());
    }
}
