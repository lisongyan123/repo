@Override
public List<ProductImpl> findByIds(final Collection<Long> ids) {
    if (ids == null || ids.isEmpty()) {
        return Collections.emptyList();
    }

    // 转为 Set 去重，避免重复 ID 导致异常（某些 DB 对 IN 有重复敏感）
    Set<Long> uniqueIds = new LinkedHashSet<>(ids);

    // 使用 HQL 查询，Hibernate 会自动 JOIN @SecondaryTable
    String hql = "FROM ProductImpl p WHERE p.prodNum IN (:ids)";
    List<ProductImpl> results = getSessionFactory().getCurrentSession()
            .createQuery(hql, ProductImpl.class)
            .setParameter("ids", uniqueIds)
            .getResultList();

    // 反代理处理
    List<ProductImpl> deproxied = results.stream()
            .map(HibernateDeproxyHelper::deproxy)
            .collect(Collectors.toList());

    // 可选：记录未找到的 ID（与 findById 行为一致）
    if (log.isDebugEnabled()) {
        Set<Long> foundIds = deproxied.stream()
                .map(ProductImpl::getProdNum)
                .collect(Collectors.toSet());
        Set<Long> missingIds = new LinkedHashSet<>(uniqueIds);
        missingIds.removeAll(foundIds);
        for (Long missingId : missingIds) {
            log.debug("Product not found for ID: {}", missingId);
        }
    }

    return deproxied;
}
