@Override
public List<ProductImpl> findbyIds(final Collection<Long> ids) {
    // 1. 检查输入参数是否为空
    if (ids == null || ids.isEmpty()) {
        return Collections.emptyList();
    }

    // 2. 使用 Hibernate Session 批量查询
    // 通过 HQL 语句进行批量查询，避免 N+1 问题
    String hql = "FROM ProductImpl p WHERE p.prodNum IN (:ids)";
    List<ProductImpl> instances = this.getSessionFactory()
            .getCurrentSession()
            .createQuery(hql, ProductImpl.class)
            .setParameter("ids", ids)
            .getResultList();

    // 3. 将查询结果转换为 Map 以方便后续查找（键为 prodNum）
    Map<Long, ProductImpl> instanceMap = instances.stream()
            .collect(Collectors.toMap(ProductImpl::getProdNum, instance -> instance));

    // 4. 构建结果列表，保持输入 id 的顺序
    List<ProductImpl> result = new ArrayList<>(ids.size());
    for (Long id : ids) {
        ProductImpl instance = instanceMap.get(id);
        if (instance == null) {
            // 5. 记录未找到实体的日志
            if (log.isDebugEnabled()) {
                log.debug("Product not found for ID: {}", id);
            }
            result.add(null); // 对应位置填充 null
        } else {
            // 6. 进行反代理处理，返回真实的实体对象
            result.add(HibernateDeproxyHelper.deproxy(instance));
        }
    }

    return result;
}
