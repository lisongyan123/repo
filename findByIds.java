@Override
public List<ProductImpl> findByIds(final Collection<Long> ids) {
    // 1. 参数校验：防止传入 null 或空集合导致 SQL 异常或全表扫描
    if (ids == null || ids.isEmpty()) {
        if (log.isDebugEnabled()) {
            log.debug("findByIds received null or empty ids collection. Returning empty list.");
        }
        return Collections.emptyList();
    }

    // 2. 使用 Hibernate Criteria API 构建批量查询
    // 使用 createCriteria 避免 HQL 解析问题，且更安全
    Criteria criteria = this.getSessionFactory()
                              .getCurrentSession()
                              .createCriteria(ProductImpl.class)
                              .add(Restrictions.in("prodNum", ids));

    // 3. 执行查询，获取原始结果列表
    // 注意：Hibernate 可能会返回代理对象（Proxy）或者重复的对象（取决于缓存和抓取策略）
    List<ProductImpl> instances = criteria.list();

    // 4. 日志记录：记录查询到的 ID 和实际数量
    if (log.isDebugEnabled()) {
        log.debug("Product query result count for IDs {}: {}", ids, instances.size());
        // 如果需要更详细的日志，可以打印具体查到了哪些 ID
        // instances.forEach(inst -> log.debug("Found Product: {}", inst.getProdNum()));
    }

    // 5. 反代理处理 & 去重
    // 使用 LinkedHashSet 保持查询结果的顺序（通常与 IN 语句的顺序有关，但数据库不保证，这里保持 List 原有顺序）
    Set<ProductImpl> deproxiedSet = new LinkedHashSet<>();
    for (ProductImpl instance : instances) {
        // 使用你原有的反代理工具类处理每一个对象
        ProductImpl deproxied = HibernateDeproxyHelper.deproxy(instance);
        deproxiedSet.add(deproxied);
    }

    // 6. 转换回 List 返回
    return new ArrayList<>(deproxiedSet);
}
