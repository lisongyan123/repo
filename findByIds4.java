public Map<Long, ProductImpl> findbyIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
        return new HashMap<>();
    }

    Session session = this.getSessionFactory().getCurrentSession();
    Map<Long, ProductImpl> result = new HashMap<>();

    // Step 1: 先从 Session 一级缓存中获取已加载的实体（避免重复查询）
    for (Long id : ids) {
        ProductImpl cached = (ProductImpl) session.get(ProductImpl.class, id);
        if (cached != null) {
            result.put(id, HibernateDeproxyHelper.deproxy(cached));
        }
    }

    // Step 2: 找出尚未缓存的 ID
    List<Long> missingIds = ids.stream()
            .filter(id -> !result.containsKey(id))
            .collect(Collectors.toList());

    if (missingIds.isEmpty()) {
        return result; // 全部命中缓存
    }

    // Step 3: 使用 IN 查询一次性加载未缓存的实体
    String hql = "FROM ProductImpl WHERE prodNum IN :ids";
    Query<ProductImpl> query = session.createQuery(hql, ProductImpl.class);
    query.setParameter("ids", missingIds);

    List<ProductImpl> loadedEntities = query.list();

    // Step 4: 将查询结果加入 Map，并反代理
    for (ProductImpl entity : loadedEntities) {
        result.put(entity.getProdNum(), HibernateDeproxyHelper.deproxy(entity));
    }

    // Step 5: 对于未查询到的 ID（即 missingIds 中未出现在 loadedEntities 中的），记录 debug 日志
    Set<Long> foundIds = loadedEntities.stream()
            .map(ProductImpl::getProdNum)
            .collect(Collectors.toSet());

    for (Long id : missingIds) {
        if (!foundIds.contains(id)) {
            if (log.isDebugEnabled()) {
                log.debug("Product not found for ID: {}", id);
            }
            result.put(id, null); // 明确标记为 null
        }
    }

    return result;
}
