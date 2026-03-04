// 在 ProductDao 中修改 findById 方法
public ProductImpl findById(Long id) {
    // 使用 HQL 显式 JOIN FETCH 关联表，避免懒加载触发 N+1
    String hql = "SELECT p FROM ProductImpl p " +
                 "LEFT JOIN FETCH p.languageSupportProducts langs " + // 假设关联属性名
                 "WHERE p.prodNum = :id";
    try {
        ProductImpl instance = (ProductImpl) getSession().createQuery(hql)
            .setParameter("id", id)
            .uniqueResult();
        
        // 手动处理代理（如果需要）
        return HibernateDeproxyHelper.deproxy(instance);
    } catch (Exception e) {
        log.error("查询产品失败", e);
        return null;
    }
}